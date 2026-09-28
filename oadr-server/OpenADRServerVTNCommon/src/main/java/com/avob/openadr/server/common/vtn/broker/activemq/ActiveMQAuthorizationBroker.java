package com.avob.openadr.server.common.vtn.broker.activemq;

import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.Set;

import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerFilter;
import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.command.ConnectionInfo;
import org.apache.activemq.security.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import com.avob.openadr.security.OadrFingerprintSecurity;
import com.avob.openadr.server.common.vtn.security.OadrSecurityRoleService;
import com.google.common.collect.Sets;

public class ActiveMQAuthorizationBroker extends BrokerFilter {

	private static final Logger LOGGER = LoggerFactory.getLogger(ActiveMQAuthorizationBroker.class);

	private OadrSecurityRoleService oadrSecurityRoleService;

	/**
	 * 인증서 지문을 이름으로 갖는 Principal.
	 *
	 * 예전에는 Apache httpclient 4 의 BasicUserPrincipal 을 빌려 썼다. httpclient 를 걷어내면서
	 * 같은 일(이름만 들고 있고 이름으로 같음을 판단)을 하는 record 로 바꿨다
	 */
	private record FingerprintPrincipal(String name) implements Principal {
		@Override
		public String getName() {
			return name;
		}
	}

	public ActiveMQAuthorizationBroker(Broker next) {
		super(next);
	}

	@Override
	public void addConnection(ConnectionContext context, ConnectionInfo info) throws Exception {

		SecurityContext securityContext = context.getSecurityContext();
		if (context.getConnection().getConnector().getBrokerInfo().getBrokerURL().startsWith("tcp://localhost")) {
			super.addConnection(context, info);
			return;
		} else if (!context.getConnection().getConnector().getBrokerInfo().getBrokerURL().startsWith("tcp://localhost")
				&& securityContext == null) {

			Object transportContext = info.getTransportContext();

			if (!(transportContext instanceof X509Certificate[])) {
				throw new SecurityException("Unable to authenticate transport without SSL certificate.");
			}

			if (transportContext != null) {
				X509Certificate[] certs = (X509Certificate[]) info.getTransportContext();
				for (X509Certificate certificate : certs) {
					String fingerprint = OadrFingerprintSecurity.getOadr20bFingerprint(certificate);
					User grantX509Role = oadrSecurityRoleService.grantX509Role(fingerprint);
					for (GrantedAuthority grantedAuthority : grantX509Role.getAuthorities()) {
						if (grantedAuthority.getAuthority().equals("ROLE_VTN")
								|| grantedAuthority.getAuthority().equals("ROLE_APP")) {

							SecurityContext subject = new SecurityContext(fingerprint) {

								@Override
								public Set<Principal> getPrincipals() {
									return Sets.newHashSet(new FingerprintPrincipal(fingerprint));
								}

							};
							context.setSecurityContext(subject);
							super.addConnection(context, info);
							return;
						}
					}
					LOGGER.info(fingerprint + " - " + grantX509Role.getAuthorities());
				}
			}

		}
		throw new SecurityException("Unable to authenticate transport");
	}

	public OadrSecurityRoleService getOadrSecurityRoleService() {
		return oadrSecurityRoleService;
	}

	public void setOadrSecurityRoleService(OadrSecurityRoleService oadrSecurityRoleService) {
		this.oadrSecurityRoleService = oadrSecurityRoleService;
	}

}
