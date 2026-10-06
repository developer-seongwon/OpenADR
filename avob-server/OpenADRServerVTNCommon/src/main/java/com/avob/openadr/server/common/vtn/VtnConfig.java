package com.avob.openadr.server.common.vtn;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import jakarta.annotation.PostConstruct;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;

import com.avob.openadr.security.OadrFingerprintSecurity;
import com.avob.openadr.security.OadrPKISecurity;
import com.avob.openadr.security.exception.OadrSecurityException;
import com.avob.openadr.server.common.vtn.exception.OadrVTNInitializationException;

/**
 * Load vtn configuration from application.properties file
 * 
 * @author bzanni
 *
 */
@Configuration
public class VtnConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(VtnConfig.class);

	public static final String CONTEXT_PATH_CONF = "oadr.server.context_path";
	public static final String PORT_CONF = "oadr.server.port";
	public static final String TRUSTED_CERTIFICATES_CONF = "oadr.security.ven.trustcertificate";

	public static final String PRIVATE_KEY_CONF = "oadr.security.vtn.key";
	public static final String CERTIFICATE_CONF = "oadr.security.vtn.cert";

	public static final String XMPP_PRIVATE_KEY_CONF = "oadr.security.vtn.xmpp.key";
	public static final String XMPP_CERTIFICATE_CONF = "oadr.security.vtn.xmpp.cert";

	public static final String SUPPORT_PUSH_CONF = "oadr.supportPush";
	public static final String SUPPORT_UNSECURED_PHTTP_PUSH_CONF = "oadr.supportUnsecuredHttpPush";
	public static final String PULL_FREQUENCY_SECONDS_CONF = "oadr.pullFrequencySeconds";
	public static final String HOST_CONF = "oadr.server.host";
	public static final String VALIDATE_OADR_PAYLOAD_XSD_CONF = "oadr.validateOadrPayloadAgainstXsd";
	public static final String VALIDATE_OADR_PAYLOAD_XSD_FILEPATH_CONF = "oadr.security.validateOadrPayloadAgainstXsdFilePath";

	public static final String VTN_ID_CONF = "oadr.vtnid";
	public static final String SAVE_VEN_UPDATE_REPORT_CONF = "oadr.saveVenData";
	public static final String REPLAY_PROTECTACCEPTED_DELAY_SECONDS_CONF = "oadr.security.replayProtectAcceptedDelaySecond";
	public static final String CA_KEY_CONF = "oadr.security.ca.key";
	public static final String CA_CERT_CONF = "oadr.security.ca.cert";

	public static final String BROKER_USER_CONF = "oadr.broker.user";
	public static final String BROKER_PASS_CONF = "oadr.broker.password";
	public static final String BROKER_PORT_CONF = "oadr.broker.port";
	public static final String BROKER_HOST_CONF = "oadr.broker.host";

	public static final String BROKER_SSL_HOST_CONF = "oadr.broker.ssl.host";
	public static final String BROKER_SSL_PORT_CONF = "oadr.broker.ssl.port";

	public static final String XMPP_HOST_CONF = "oadr.xmpp.host";
	public static final String XMPP_PORT_CONF = "oadr.xmpp.port";
	public static final String XMPP_DOMAIN_CONF = "oadr.xmpp.domain";

	@Value("${" + CONTEXT_PATH_CONF + ":#{null}}")
	private String contextPath;

	// 설정이 없을 때의 기본값. 이 저장소의 VTN 포트(9970 대)에 맞춘다. 예전 기본값은 8443 이었다
	@Value("${" + PORT_CONF + ":#{9970}}")
	private int port;

	@Value("${" + TRUSTED_CERTIFICATES_CONF + ":#{null}}")
	private String trustCertificatesStr;

	private List<String> trustCertificates = null;

	@Value("${" + PRIVATE_KEY_CONF + ":#{null}}")
	private String key;



	@Value("${" + CERTIFICATE_CONF + ":#{null}}")
	private String cert;

	@Value("${" + XMPP_CERTIFICATE_CONF + ":#{null}}")
	private String xmppCert;

	@Value("${" + XMPP_PRIVATE_KEY_CONF + ":#{null}}")
	private String xmppKey;

	public String getXmppCert() {
		return xmppCert;
	}

	public String getXmppKey() {
		return xmppKey;
	}

	@Value("${" + SUPPORT_PUSH_CONF + ":#{null}}")
	private Boolean supportPush;

	@Value("${" + SUPPORT_UNSECURED_PHTTP_PUSH_CONF + ":#{false}}")
	private Boolean supportUnsecuredHttpPush;

	// VTN 이 등록 응답(oadrRequestedOadrPollFreq)으로 VEN 에 알려 주는 기본 폴링 주기(초). VEN 마다 따로 정할 수도 있다(Ven.pullFrequencySeconds).
	// 설정이 없으면 30초(PT30S). 예전 기본값은 null 이라 응답에 PTnullS 가 실렸다
	@Value("${" + PULL_FREQUENCY_SECONDS_CONF + ":30}")
	private Long pullFrequencySeconds;

	@Value("${" + VALIDATE_OADR_PAYLOAD_XSD_CONF + ":#{false}}")
	private Boolean validateOadrPayloadAgainstXsd;

	@Value("${" + VALIDATE_OADR_PAYLOAD_XSD_FILEPATH_CONF + ":#{null}}")
	private String validateOadrPayloadAgainstXsdFilePath;

	// VTN 이 응답에 싣는 vtnID. 설정이 없으면 이 저장소의 기본 이름을 쓴다(예전에는 null 이라 응답의 vtnID 가 비었다)
	@Value("${" + VTN_ID_CONF + ":AVOB_OPEN_ADR}")
	private String vtnId;

	@Value("${" + REPLAY_PROTECTACCEPTED_DELAY_SECONDS_CONF + ":#{1200}}")
	private Long replayProtectAcceptedDelaySecond;

	@Value("${" + CA_KEY_CONF + ":#{null}}")
	private String caKey;

	@Value("${" + CA_CERT_CONF + ":#{null}}")
	private String caCert;

	@Value("${" + BROKER_HOST_CONF + ":localhost}")
	private String brokerHost;

	@Value("${" + BROKER_PORT_CONF + ":#{null}}")
	private Integer brokerPort;

	@Value("${" + BROKER_USER_CONF + ":#{null}}")
	private String brokerUser;

	@Value("${" + BROKER_PASS_CONF + ":#{null}}")
	private String brokerPass;

	@Value("${" + BROKER_SSL_PORT_CONF + ":#{null}}")
	private Integer brokerSslPort;

	@Value("${" + BROKER_SSL_HOST_CONF + ":localhost}")
	private String brokerSslHost;

	@Value("${" + XMPP_HOST_CONF + ":#{null}}")
	private String xmppHost;

	@Value("${" + XMPP_PORT_CONF + ":#{null}}")
	private Integer xmppPort;

	@Value("${" + XMPP_DOMAIN_CONF + ":#{null}}")
	private String xmppDomain;

	@Autowired
	private ConfigurableEnvironment env;

	private KeyManagerFactory keyManagerFactory;
	private TrustManagerFactory trustManagerFactory;
	private SSLContext sslContext;
	private String oadr20bFingerprint;
	private String oadr20aFingerprint;
	private KeyManagerFactory xmppKeyManagerFactory;
	public KeyManagerFactory getXmppKeyManagerFactory() {
		return xmppKeyManagerFactory;
	}

	public TrustManagerFactory getXmppTrustManagerFactory() {
		return xmppTrustManagerFactory;
	}

	public String getXmppOadr20bFingerprint() {
		return xmppOadr20bFingerprint;
	}

	public String getXmppOadr20aFingerprint() {
		return xmppOadr20aFingerprint;
	}

	private TrustManagerFactory xmppTrustManagerFactory;
	private String xmppOadr20bFingerprint;
	private String xmppOadr20aFingerprint;
	private String brokerUrl;
	private String sslBrokerUrl;

	@PostConstruct
	public void init() {

		// load coma separated trust certificate list
		if (trustCertificatesStr != null) {
			trustCertificates = Arrays.asList(trustCertificatesStr.split(","));
		} else {
			trustCertificates = new ArrayList<>();
		}

		// no VTN key/cert conf might be given when using HTTP only transport
		if (this.getCert() != null && this.getKey() != null) {

			String keystorePassword = UUID.randomUUID().toString();

			try {
				// get VTN fingerprints
				oadr20aFingerprint = OadrFingerprintSecurity.getOadr20aFingerprint(this.getCert());
				oadr20bFingerprint = OadrFingerprintSecurity.getOadr20bFingerprint(this.getCert());
				trustManagerFactory = OadrPKISecurity.createTrustManagerFactory(trustCertificates);
				keyManagerFactory = OadrPKISecurity.createKeyManagerFactory(this.getKey(), this.getCert(),
						keystorePassword);



				// SSL Context Factory
				sslContext = SSLContext.getInstance("TLS");

				// init ssl context
				String seed = UUID.randomUUID().toString();
				getSslContext().init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(),
						new SecureRandom(seed.getBytes()));



				//				sslContext = OadrPKISecurity.createSSLContext(clientPrivateKeyPemFilePath, clientCertificatePemFilePath, trustCertificates, password)
				// init ssl context
			} catch (OadrSecurityException | NoSuchAlgorithmException | KeyManagementException e) {
				throw new OadrVTNInitializationException(e);
			}
		} else {
			LOGGER.warn(
					"VTN " + PRIVATE_KEY_CONF + " or " + CERTIFICATE_CONF + " not given - no HTTP SSL transport supported");
		}

		if (this.getXmppCert() != null && this.getXmppKey() != null) {

			String keystorePassword = UUID.randomUUID().toString();

			try {
				// get VTN fingerprints
				xmppOadr20aFingerprint = OadrFingerprintSecurity.getOadr20aFingerprint(this.getXmppCert());
				xmppOadr20bFingerprint = OadrFingerprintSecurity.getOadr20bFingerprint(this.getXmppCert());
				xmppTrustManagerFactory = OadrPKISecurity.createTrustManagerFactory(trustCertificates);
				xmppKeyManagerFactory = OadrPKISecurity.createKeyManagerFactory(this.getXmppKey(), this.getXmppCert(),
						keystorePassword);
				// XMPP 는 SSLContext 를 따로 만들지 않는다. smack 4.5 가 SSLContext 를 다시 init 해 버려서
				// XmppConnector 가 위 두 팩토리를 직접 넘긴다
			} catch (OadrSecurityException e) {
				throw new OadrVTNInitializationException(e);
			}
		} else {
			LOGGER.warn(
					"VTN " + PRIVATE_KEY_CONF + " or " + CERTIFICATE_CONF + " not given - no XMPP transport supported");
		}

		if (getBrokerPort() == null) {
			brokerPort = findAvailableTcpPort(getBrokerHost());
		}

		if (brokerSslPort == null) {
			brokerSslPort = findAvailableTcpPort(brokerSslHost);
		}
		brokerUrl = "tcp://" + getBrokerHost() + ":" + getBrokerPort();
		sslBrokerUrl = "ssl://" + brokerSslHost + ":" + brokerSslPort;

	}

	/**
	 * 비어 있는 TCP 포트를 하나 잡는다.
	 *
	 * Spring Framework 6 에서 org.springframework.util.SocketUtils 가 제거됐다.
	 * 대체재인 TestSocketUtils 는 spring-test 에 있어 테스트 전용이라
	 * 운영 코드에서 쓰기에 맞지 않으므로 여기서 직접 구현한다.
	 *
	 * 포트를 확인한 뒤 소켓을 닫고 브로커가 다시 여는 구조라
	 * 그 사이에 다른 프로세스가 가져갈 여지는 남아 있다. SocketUtils 도 같았다.
	 *
	 * 확인 소켓은 브로커가 실제로 열 주소(보통 localhost)에 연다. 예전처럼 와일드카드(0.0.0.0)로 열면
	 * macOS 는 다른 프로세스가 127.0.0.1 에만 열어 둔 포트도 비어 있다고 내준다.
	 * 실제로 IntelliJ 내장 서버가 127.0.0.1:64342 를 잡고 있는데 그 포트를 받아서
	 * 브로커가 "Address already in use" 로 못 뜨고 VTNCommon 테스트가 통째로 깨졌다.
	 * 주소를 못 풀면 예전처럼 와일드카드로 확인한다.
	 */
	private static int findAvailableTcpPort(String host) {
		InetAddress address = null;
		try {
			address = InetAddress.getByName(host);
		} catch (UnknownHostException e) {
			LOGGER.warn("Cannot resolve broker host {}, checking free port on all addresses", host);
		}
		try (ServerSocket socket = new ServerSocket(0, 1, address)) {
			socket.setReuseAddress(true);
			return socket.getLocalPort();
		} catch (IOException e) {
			throw new OadrVTNInitializationException(e);
		}
	}

	public boolean hasExternalRabbitMQBroker() {
		List<String> profiles = Arrays.asList(env.getActiveProfiles());
		return profiles.contains("rabbitmq-broker") || profiles.contains("external");
	}

	public boolean hasInMemoryBroker() {
		return !this.hasExternalRabbitMQBroker();
	}

	public String getContextPath() {
		return contextPath;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public List<String> getTrustCertificates() {
		return trustCertificates;
	}

	public String getKey() {
		return key;
	}

	public String getCert() {
		return cert;
	}

	public Boolean getSupportPush() {
		return supportPush;
	}

	public Boolean getSupportUnsecuredHttpPush() {
		return supportUnsecuredHttpPush;
	}

	public Long getPullFrequencySeconds() {
		return pullFrequencySeconds;
	}

	public Boolean getValidateOadrPayloadAgainstXsd() {
		return validateOadrPayloadAgainstXsd;
	}

	public String getVtnId() {
		return vtnId;
	}

	public Long getReplayProtectAcceptedDelaySecond() {
		return replayProtectAcceptedDelaySecond;
	}

	public String getCaKey() {
		return caKey;
	}

	public String getCaCert() {
		return caCert;
	}

	public KeyManagerFactory getKeyManagerFactory() {
		return keyManagerFactory;
	}

	public TrustManagerFactory getTrustManagerFactory() {
		return trustManagerFactory;
	}

	public String getOadr20bFingerprint() {
		return oadr20bFingerprint;
	}

	public String getOadr20aFingerprint() {
		return oadr20aFingerprint;
	}

	public String getBrokerUrl() {
		return brokerUrl;
	}

	public String getSslBrokerUrl() {
		return sslBrokerUrl;
	}

	public String getBrokerUser() {
		return brokerUser;
	}

	public String getBrokerPass() {
		return brokerPass;
	}

	public String getBrokerHost() {
		return brokerHost;
	}

	public Integer getBrokerPort() {
		return brokerPort;
	}

	public String getXmppHost() {
		return xmppHost;
	}

	public Integer getXmppPort() {
		return xmppPort;
	}

	public String getXmppDomain() {
		return xmppDomain;
	}

	public SSLContext getSslContext() {
		return sslContext;
	}

	public String getValidateOadrPayloadAgainstXsdFilePath() {
		return validateOadrPayloadAgainstXsdFilePath;
	}

}
