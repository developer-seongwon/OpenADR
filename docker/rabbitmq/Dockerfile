FROM rabbitmq:3.8.9-alpine

# 빌드 컨텍스트가 리포지토리 루트다(compose 의 context 와 dockerfile 지정).
# 예전에는 빌드 이미지에서 인증서를 꺼냈는데, 그러면 자바 모듈을 전부 빌드해야
# 인프라도 뜰 수 있었다. 인증서는 리포지토리에 그대로 있으니 직접 복사한다.
COPY cert/vtn.oadr.com-rsa.crt /opt/oadr-vtn20b/cert/vtn.oadr.com-rsa.crt
COPY cert/vtn.oadr.com-rsa.key /opt/oadr-vtn20b/cert/vtn.oadr.com-rsa.key

ENV RABBITMQ_DEFAULT_USER=admin
ENV RABBITMQ_DEFAULT_PASS=admin

RUN rabbitmq-plugins enable --offline rabbitmq_amqp1_0 rabbitmq_auth_backend_http rabbitmq_federation rabbitmq_management rabbitmq_tracing rabbitmq_auth_mechanism_ssl rabbitmq_auth_backend_cache 
