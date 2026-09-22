#!/bin/bash
B=https://localhost:8181/testvtn
A=(-sk -u admin:admin)
# VEN 삭제가 VTN20b 쪽 리포트 테이블까지 알아서 치운다.
# 그래서 여기서 DB 를 직접 건드릴 일이 없다.
for p in Ven/httpven Account/user/httptestuser Account/app/httptestapp; do
  curl "${A[@]}" -o /dev/null -w "delete $p -> %{http_code}\n" -X DELETE "$B/$p"
done
for pair in "Group HttpTestGroup" "MarketContext HttpTestMarketContext"; do
  set -- $pair
  id=$(curl "${A[@]}" "$B/$1/$2" | python3 -c 'import sys,json
try:
  print(json.load(sys.stdin).get("id",""))
except Exception:
  print("")')
  if [ -n "$id" ]; then
    curl "${A[@]}" -o /dev/null -w "delete $1/$id -> %{http_code}\n" -X DELETE "$B/$1/$id"
  else
    echo "$1 없음 (정리 불필요)"
  fi
done
