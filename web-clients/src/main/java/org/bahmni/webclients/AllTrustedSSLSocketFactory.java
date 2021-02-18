package org.bahmni.webclients;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

public class AllTrustedSSLSocketFactory {

	public SSLConnectionSocketFactory getSSLSocketFactory(){
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkClientTrusted(X509Certificate[] certs, String authType) {
				// Do nothing
			}

			@Override
			public void checkServerTrusted(X509Certificate[] certs, String authType) {
				// Do nothing
			}

		} };

		SSLConnectionSocketFactory socketFactory = null;

		try{
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			socketFactory = new SSLConnectionSocketFactory(sc, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		}catch(Exception ex){
			throw new WebClientsException(ex);
		}

		return socketFactory;
	}
}
