# 3.8.9 -> 4.3. 컨테이너를 새로 만들 때 3.x 데이터가 남아 있으면 4.x 가 뜨지 못한다(feature flag).
# run.sh 가 rabbitmq 컨테이너를 익명 볼륨까지 새로 만든다
FROM rabbitmq:4.3-alpine

# 빌드 컨텍스트가 리포지토리 루트다(compose 의 context 와 dockerfile 지정).
# 예전에는 빌드 이미지에서 인증서를 꺼냈는데, 그러면 자바 모듈을 전부 빌드해야
# 인프라도 뜰 수 있었다. 인증서는 리포지토리에 그대로 있으니 직접 복사한다.
COPY cert/vtn.oadr.com-rsa.crt /opt/oadr-vtn20b/cert/vtn.oadr.com-rsa.crt
COPY cert/vtn.oadr.com-rsa.key /opt/oadr-vtn20b/cert/vtn.oadr.com-rsa.key

ENV RABBITMQ_DEFAULT_USER=admin
ENV RABBITMQ_DEFAULT_PASS=admin

# rabbitmq_amqp1_0 은 뺐다. 4.x 부터 AMQP 1.0 이 코어에 들어갔고 이 플러그인은 빈 껍데기다
# rabbitmq_auth_backend_http, rabbitmq_auth_backend_cache 도 뺐다. 인증은 내부 계정만 쓴다(rabbitmq.config 주석 참고)
RUN rabbitmq-plugins enable --offline rabbitmq_federation rabbitmq_management rabbitmq_tracing rabbitmq_auth_mechanism_ssl
