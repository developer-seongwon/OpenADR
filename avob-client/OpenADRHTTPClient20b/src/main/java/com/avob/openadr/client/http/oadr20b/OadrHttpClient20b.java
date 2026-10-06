package com.avob.openadr.client.http.oadr20b;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.UUID;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;


import com.avob.openadr.client.http.OadrHttpClient;
import com.avob.openadr.model.oadr20b.Oadr20bFactory;
import com.avob.openadr.model.oadr20b.Oadr20bJAXBContext;
import com.avob.openadr.model.oadr20b.Oadr20bUrlPath;
import com.avob.openadr.model.oadr20b.exception.Oadr20bException;
import com.avob.openadr.model.oadr20b.exception.Oadr20bHttpLayerException;
import com.avob.openadr.model.oadr20b.exception.Oadr20bMarshalException;
import com.avob.openadr.model.oadr20b.exception.Oadr20bUnmarshalException;
import com.avob.openadr.model.oadr20b.exception.Oadr20bXMLSignatureException;
import com.avob.openadr.model.oadr20b.exception.Oadr20bXMLSignatureValidationException;
import com.avob.openadr.model.oadr20b.oadr.OadrPayload;
import com.avob.openadr.model.oadr20b.xmlsignature.OadrXMLSignatureHandler;
import com.avob.openadr.security.OadrPKISecurity;
import com.avob.openadr.security.exception.OadrSecurityException;

/**
 * Oadr 2.0b profile HTTP client
 * 
 * @author bzanni
 *
 */
public class OadrHttpClient20b {

	private OadrHttpClient client;

	/**
	 * xml signature
	 */
	private Long replayProtectAcceptedDelaySecond;
	private PrivateKey privateKey;
	private X509Certificate clientCertificate;

	private Oadr20bJAXBContext jaxbContext;

	private boolean validateXmlPayload = false;

	public OadrHttpClient20b(OadrHttpClient client) throws JAXBException, OadrSecurityException {
		this(client, null, null, null, null);
	}

	public OadrHttpClient20b(OadrHttpClient client, String privateKeyPath, String clientCertificatePath,
			Long replayProtectAcceptedDelaySecond) throws JAXBException, OadrSecurityException {
		this(client, privateKeyPath, clientCertificatePath, replayProtectAcceptedDelaySecond, null);
	}

	public OadrHttpClient20b(OadrHttpClient client, String privateKeyPath, String clientCertificatePath,
			Long replayProtectAcceptedDelaySecond, Boolean validateXmlPayload)
			throws JAXBException, OadrSecurityException {
		this.jaxbContext = Oadr20bJAXBContext.getInstance("src/test/resources/oadr20b_schema/");
		this.client = client;

		if (privateKeyPath != null && clientCertificatePath != null) {
			this.privateKey = OadrPKISecurity.parsePrivateKey(privateKeyPath);
			this.clientCertificate = OadrPKISecurity.parseCertificate(clientCertificatePath);
		}

		this.replayProtectAcceptedDelaySecond = replayProtectAcceptedDelaySecond;
		if (validateXmlPayload != null) {
			this.validateXmlPayload = validateXmlPayload;
		}
	}

	private boolean isXmlSignatureEnabled() {
		return this.privateKey != null && this.clientCertificate != null
				&& this.replayProtectAcceptedDelaySecond != null;
	}

	/**
	 * Generic oadr 2.0b using default host/credentials
	 * 
	 * @param payload
	 * @param responseKlass
	 * @return
	 * @throws Oadr20bXMLSignatureValidationException
	 * @throws Oadr20bXMLSignatureException
	 * @throws Oadr20aException
	 * @throws URISyntaxException
	 */
	public <T, I extends JAXBElement<?>> T post(I payload, String path, Class<T> responseKlass) throws Oadr20bException,
			Oadr20bHttpLayerException, Oadr20bXMLSignatureException, Oadr20bXMLSignatureValidationException {
		return this.post(null, path, payload, responseKlass);
	}

	/**
	 * Generic oadr 2.0b using given host/credentials
	 * 
	 * @param payload
	 * @param responseKlass
	 * @return
	 * @throws Oadr20bHttpLayerException
	 * @throws Oadr20bXMLSignatureException
	 * @throws Oadr20bXMLSignatureValidationException
	 * @throws Oadr20aException
	 * @throws URISyntaxException
	 */
	// 예전에는 세 번째 인자로 Apache 의 HttpClientContext 를 받았는데 부르는 곳이 전부 null 이었다.
	// 자바 표준 HttpClient 로 바꾸면서 인자에서 뺐다
	public <O, I extends JAXBElement<?>> O post(String host, String path, I payload,
			Class<O> responseKlass) throws Oadr20bException, Oadr20bHttpLayerException, Oadr20bXMLSignatureException,
			Oadr20bXMLSignatureValidationException {
		try {
			String marshal = null;
			if (isXmlSignatureEnabled()) {
				marshal = this.sign(payload.getValue());
			} else {
				marshal = jaxbContext.marshal(payload, validateXmlPayload);
			}

			HttpResponse<String> response = client.post(marshal, host, Oadr20bUrlPath.OADR_BASE_PATH + path);

			// if request did not result in 200 http code throw exception
			// 자바 HttpClient 는 본문을 다 읽어서 돌려주므로 예전 EntityUtils.consumeQuietly 같은 정리가 필요 없다
			int statusCode = response.statusCode();
			if (statusCode != HttpURLConnection.HTTP_OK) {
				throw new Oadr20bHttpLayerException(statusCode,
						String.valueOf(statusCode));
			}

			// if request was a success, validate xml signature if required and then
			// unmarshall response
			if (isXmlSignatureEnabled()) {
				String entity = response.body();
				OadrPayload unmarshal = jaxbContext.unmarshal(entity, OadrPayload.class, validateXmlPayload);
				this.validate(entity, unmarshal);
				if (Object.class.equals(responseKlass)) {
					Object signedObjectFromOadrPayload = Oadr20bFactory.getSignedObjectFromOadrPayload(unmarshal);
					return responseKlass.cast(signedObjectFromOadrPayload);
				} else {
					return Oadr20bFactory.getSignedObjectFromOadrPayload(unmarshal, responseKlass);
				}

			} else {
				String resp = response.body();
				return jaxbContext.unmarshal(resp, responseKlass, validateXmlPayload);
			}

		} catch (IOException | URISyntaxException | Oadr20bUnmarshalException | Oadr20bMarshalException e) {
			throw new Oadr20bException(e);
		}
	}

	private String sign(Object object) throws Oadr20bXMLSignatureException {
		String nonce = UUID.randomUUID().toString();
		Long createdtimestamp = System.currentTimeMillis();
		return OadrXMLSignatureHandler.sign(object, this.privateKey, this.clientCertificate, nonce, createdtimestamp);
	}

	private void validate(String raw, OadrPayload payload) throws Oadr20bXMLSignatureValidationException {
		long nowDate = System.currentTimeMillis();
		OadrXMLSignatureHandler.validate(raw, payload, nowDate, replayProtectAcceptedDelaySecond * 1000L);
	}

}
