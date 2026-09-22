# test/http 의 .http 파일들을 실제로 실행해서 전부 도는지 확인하는 검증용 러너.
# IntelliJ HTTP Client 를 대신하는 게 아니라, 파일에 적힌 요청이 살아 있는지만 본다.
import io, json, os, re, subprocess, sys, time

HERE = os.path.dirname(os.path.abspath(__file__))
env = json.load(io.open(os.path.join(HERE, 'http-client.env.json'), encoding='utf-8'))['local']
V = dict(env)

# client.global.set(...) 을 러너가 흉내내는 부분.
# 요청 URL 에 이 조각이 들어 있으면 응답에서 값을 뽑아 변수에 담는다.
CAPTURE = [
    ('/MarketContext/',    'POST', 'json', 'id',             'marketContextId'),
    ('/Group/',            'POST', 'json', 'id',             'groupId'),
    ('/DemandResponseEvent/', 'POST', 'json', 'id',          'eventId'),
    ('/EiRegisterParty',   'POST', 'xml',  'registrationID', 'registrationId'),
    ('/EiEvent',           'POST', 'xml',  'eventID',        'oadrEventId'),
]

def subst(text):
    out = text
    for k, v in V.items():
        out = out.replace('{{%s}}' % k, str(v))
    out = out.replace('{{$timestamp}}', str(int(time.time())))
    # IntelliJ 의 $isoTimestamp 와 같은 값. OADR 은 시각을 ISO8601 UTC 로 쓴다
    out = out.replace('{{$isoTimestamp}}', time.strftime('%Y-%m-%dT%H:%M:%SZ', time.gmtime()))
    return out

def parse(path):
    lines = io.open(path, encoding='utf-8').read().split('\n')
    blocks, cur = [], None
    for ln in lines:
        if ln.startswith('###'):
            if cur: blocks.append(cur)
            cur = {'title': ln[3:].strip(), 'lines': []}
        elif cur is not None:
            cur['lines'].append(ln)
    if cur: blocks.append(cur)

    reqs = []
    for b in blocks:
        method = url = None
        headers, body, infile = [], [], None
        state = 'head'
        expect = None
        expect_rc = None
        for ln in b['lines']:
            if state == 'head':
                # "# @expect 403" 처럼 적어 두면 2xx 가 아니어도 실패로 안 센다.
                # 일부러 막히는 걸 확인하는 요청과, 지금 알려진 채로 두는 실패에 쓴다.
                m = re.match(r'^#\s*@expect\s+(\d+)', ln)
                if m:
                    expect = m.group(1)
                    continue
                # OADR 은 HTTP 200 안에 responseCode 로 오류를 담는다.
                # 일부러 거절당하는 걸 확인하는 요청은 이걸로 기대값을 적는다.
                m = re.match(r'^#\s*@expect-rc\s+(\d+)', ln)
                if m:
                    expect_rc = m.group(1)
                    continue
                if ln.startswith('#') or ln.strip() == '':
                    continue
                if ln.startswith('@'):
                    k, _, v = ln[1:].partition('=')
                    V[k.strip()] = subst(v.strip())
                    continue
                m = re.match(r'^(GET|POST|PUT|DELETE|PATCH)\s+(\S+)', ln)
                if m:
                    method, url = m.group(1), m.group(2)
                    state = 'headers'
                continue
            if state == 'headers':
                if ln.strip() == '':
                    state = 'body'
                    continue
                if ln.startswith('>'):
                    state = 'done'
                    continue
                headers.append(ln.strip())
                continue
            if state == 'body':
                if ln.startswith('> {%'):
                    state = 'done'; continue
                if ln.startswith('< '):
                    infile = ln[2:].strip(); continue
                body.append(ln)
        if method:
            reqs.append({'title': b['title'], 'method': method, 'url': url,
                         'headers': headers, 'body': '\n'.join(body).strip(), 'infile': infile,
                         'expect': expect, 'expect_rc': expect_rc})
    return reqs

def run(req):
    url = subst(req['url'])
    cmd = ['curl', '-sk', '-o', '/tmp/httprun.out', '-w', '%{http_code}', '-X', req['method'], url]
    for h in req['headers']:
        name, _, val = h.partition(':')
        val = subst(val.strip())
        if name.strip().lower() == 'authorization' and val.lower().startswith('basic '):
            cmd += ['-u', val[6:].replace(' ', ':', 1)]
        else:
            cmd += ['-H', '%s: %s' % (name.strip(), val)]
    if req['infile']:
        p = os.path.join(HERE, req['infile'])
        io.open('/tmp/httprun.in', 'w', encoding='utf-8').write(subst(io.open(p, encoding='utf-8').read()))
        cmd += ['--data-binary', '@/tmp/httprun.in']
    elif req['body']:
        io.open('/tmp/httprun.in', 'w', encoding='utf-8').write(subst(req['body']))
        cmd += ['--data-binary', '@/tmp/httprun.in']
    code = subprocess.run(cmd, capture_output=True, text=True).stdout.strip()
    raw = io.open('/tmp/httprun.out', encoding='utf-8', errors='ignore').read()

    for frag, meth, kind, field, var in CAPTURE:
        if frag in url and meth == req['method']:
            try:
                if kind == 'json':
                    d = json.loads(raw)
                    if field.startswith('0.'):
                        if isinstance(d, list) and d: V[var] = d[0][field[2:]]
                    elif isinstance(d, dict) and field in d:
                        V[var] = d[field]
                else:
                    m = re.search(r'%s>([^<]*)<' % field, raw)
                    if m: V[var] = m.group(1)
            except Exception:
                pass
    return code, raw

def main():
    files = sorted(f for f in os.listdir(HERE) if f.endswith('.http'))
    normal, cleanup = [], []
    for f in files:
        for r in parse(os.path.join(HERE, f)):
            r['file'] = f
            (cleanup if r['title'].startswith('[정리]') else normal).append(r)

    fails = []
    for r in normal + cleanup:
        code, raw = run(r)
        ok = code == r['expect'] if r['expect'] else code.startswith('2')
        extra = '  (기대 %s)' % r['expect'] if r['expect'] else ''
        if not r['expect'] and raw.lstrip().startswith('<'):
            m = re.search(r'responseCode>([^<]*)<', raw)
            root = (re.search(r'<oadr:(\w+)', raw) or [None, '?'])[1]
            rc = m.group(1) if m else None
            want = r['expect_rc'] or '200'
            extra = '  %s rc=%s%s' % (root, rc or '-', '  (기대 %s)' % r['expect_rc'] if r['expect_rc'] else '')
            if rc is not None and rc != want:
                ok = False
        print('%s %-4s %-3s %-46s%s' % ('OK ' if ok else '!! ', r['method'], code, r['title'][:46], extra))
        if not ok:
            fails.append((r['file'], r['title'], code, raw[:200]))
    print()
    print('총 %d개, 실패 %d개' % (len(normal) + len(cleanup), len(fails)))
    for f in fails:
        print('  실패:', f[0], '|', f[1], '|', f[2])
        print('        ', f[3].replace('\n', ' ')[:180])

main()
