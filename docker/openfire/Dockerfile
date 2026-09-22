FROM gizmotronic/openfire:4.4.4

# 빌드 컨텍스트가 리포지토리 루트다(compose 의 context 와 dockerfile 지정).
# 예전에는 빌드 이미지에서 인증서를 꺼냈는데, 그러면 자바 모듈을 전부 빌드해야
# 인프라도 뜰 수 있었다. 인증서는 리포지토리에 그대로 있으니 직접 복사한다.
COPY cert/oadr.com.crt /opt/oadr-vtn20b/cert/oadr.com.crt
COPY cert/xmpp.vtn.oadr.com-rsa.p12 /opt/oadr-vtn20b/cert/xmpp.vtn.oadr.com-rsa.p12

COPY docker/openfire/available-plugins.xml /etc/openfire/available-plugins.xml
COPY docker/openfire/openfire.xml /etc/openfire/openfire.xml

ENV OPENFIRE_PLUGIN="/usr/share/openfire/plugins"
ENV OPENFIRE_CONFIG="/etc/openfire"
ENV OPENFIRE_HOME="/var/lib/openfire"
ENV OPENFIRE_SECURITY="/usr/share/openfire/resources/security"

# Download openadr plugin
ADD https://github.com/avob/openfire-oadr-plugin/releases/download/0.0.2/OpenfireOadrPlugin-openfire-plugin-assembly.jar "$OPENFIRE_PLUGIN/OpenfireOadrPlugin-openfire-plugin-assembly.jar"

# Change Identity Store pass
RUN keytool -importkeystore -srckeystore "$OPENFIRE_SECURITY/keystore" -srcstorepass changeit \
			-destkeystore "$OPENFIRE_SECURITY/keystore" -deststorepass changeme ;

# Import VTN identity
RUN keytool -v -importkeystore -srcstorepass "changeme" -srckeystore /opt/oadr-vtn20b/cert/xmpp.vtn.oadr.com-rsa.p12 -srcstoretype PKCS12 \
			-deststorepass changeme -destkeystore "$OPENFIRE_SECURITY/keystore" -deststoretype JKS;

# Change Truststore pass
RUN keytool -importkeystore -srckeystore "$OPENFIRE_SECURITY/truststore" -srcstorepass changeit \
			-destkeystore "$OPENFIRE_SECURITY/truststore" -deststorepass changeme;

# Import CA certificate
RUN keytool -v -import -alias OadrCA -file /opt/oadr-vtn20b/cert/oadr.com.crt \
			-deststorepass changeme -destkeystore "$OPENFIRE_SECURITY/truststore" -deststoretype JKS -noprompt;

# Change Client Truststore pass
# RUN keytool -importkeystore -srckeystore "$OPENFIRE_SECURITY/client.truststore"  -srcstorepass changeit \
#			-destkeystore "$OPENFIRE_SECURITY/client.truststore" -deststorepass changeme;

# Import CA certificate
# RUN keytool -v -import -alias OadrCA -file /opt/oadr-vtn20b/cert/oadr.com.crt \
#			-deststorepass changeme -destkeystore "$OPENFIRE_SECURITY/client.truststore" -deststoretype JKS -noprompt;



RUN cp /etc/openfire/security/truststore /usr/share/openfire/resources/security/client.truststore

RUN chown -R openfire:openfire "$OPENFIRE_PLUGIN"
RUN chown -R openfire:openfire "$OPENFIRE_CONFIG"
RUN chown -R openfire:openfire "$OPENFIRE_HOME"
RUN chown -R openfire:openfire "$OPENFIRE_SECURITY"
