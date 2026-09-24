package com.avob.openadr.client.http.oadr20a;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;

import jakarta.xml.bind.JAXBException;

import com.avob.openadr.client.http.OadrHttpClient;
import com.avob.openadr.model.oadr20a.Oadr20aJAXBContext;
import com.avob.openadr.model.oadr20a.Oadr20aUrlPath;
import com.avob.openadr.model.oadr20a.exception.Oadr20aException;
import com.avob.openadr.model.oadr20a.exception.Oadr20aHttpLayerException;
import com.avob.openadr.model.oadr20a.exception.Oadr20aMarshalException;

/**
 * Oadr 2.0a simple https client
 * 
 * use TLSv1, TLSv1.1, TLSv1.2 with a given PEM key
 * 
 * @author bertrand
 *
 */
public class OadrHttpClient20a {

	private OadrHttpClient client;

	private Oadr20aJAXBContext jaxbContext;

	public OadrHttpClient20a(OadrHttpClient client) throws JAXBException {

		this.jaxbContext = Oadr20aJAXBContext.getInstance();
		this.client = client;
	}

	/**
	 * Generic oadr 2.0a using default host/credentials
	 * 
	 * @param payload
	 * @param responseKlass
	 * @return
	 * @throws Oadr20aException
	 * @throws URISyntaxException
	 */
	public <T> T post(Object payload, String path, Class<T> responseKlass)
			throws Oadr20aException, Oadr20aHttpLayerException {
		return this.post(null, path, payload, responseKlass);
	}

	/**
	 * Generic oadr 2.0a using given host/credentials
	 * 
	 * @param payload
	 * @param responseKlass
	 * @return
	 * @throws Oadr20aException
	 * @throws Oadr20aHttpLayerException
	 * @throws URISyntaxException
	 */
	// 예전에는 세 번째 인자로 Apache 의 HttpClientContext 를 받았는데 부르는 곳이 전부 null 이었다.
	// 자바 표준 HttpClient 로 바꾸면서 인자에서 뺐다
	public <T> T post(String host, String path, Object payload, Class<T> responseKlass)
			throws Oadr20aException, Oadr20aHttpLayerException {
		try {
			String marshal = jaxbContext.marshal(payload);
			HttpResponse<String> response = client.post(marshal, host, Oadr20aUrlPath.OADR_BASE_PATH + path);
			if (response.statusCode() != HttpURLConnection.HTTP_OK) {
				// 자바 HttpClient 에는 reason phrase 가 없다(HTTP/2 에는 아예 없는 개념이다).
				// 2.0b 클라이언트처럼 상태 코드를 문자열로 넣는다
				throw new Oadr20aHttpLayerException(response.statusCode(), String.valueOf(response.statusCode()));
			} else {
				return jaxbContext.unmarshal(response.body(), responseKlass);
			}

		} catch (Oadr20aMarshalException | IOException | URISyntaxException e) {
			throw new Oadr20aException(e);
		}
	}

}
