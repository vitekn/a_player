package ru.iptvportal.player;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

import android.os.AsyncTask;
import android.util.Log;


public class SendHttpRequest extends AsyncTask<String,Integer,SendHttpRequest.RawResp>{
	class RawResp{
		public byte[] data;
		public int id;
	}
	private MiddlewareProto _mwp;
	private OnHttpRequestComplete _com;
	final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};
	public SendHttpRequest(MiddlewareProto mwp,OnHttpRequestComplete c){_mwp=mwp;_com=c;}
	
	@Override
	protected SendHttpRequest.RawResp doInBackground(String... params) {
		SendHttpRequest.RawResp rre=new SendHttpRequest.RawResp();
		rre.data=null;
		if (params.length<3)
			return rre;
		String s_url=params[0];
		String data=params[1];
		rre.id=Integer.parseInt(params[2]);
		
		String method="POST";
		if (params.length>3)
			 method=params[3];
		
		//Log.d("HTTP","sendRequest url \n" + s_url + "method="+method);
		boolean https=false;
		HttpURLConnection urlConnection;
		URL url;
		try{
			url = new URL(s_url);
		}
		catch (Exception e)
		{
			return rre;
		}
		
		if (s_url.startsWith("https"))
			https=true;
		
		if (https)
		{
			CertificateFactory cf;
			try {
				cf = CertificateFactory.getInstance("X.509");
			} catch (CertificateException e) {
				//Log.d("HTTP","sendRequest c");
				return rre;
			}
			//Log.d("HTTP","sendRequest -1");
			// From https://www.washington.edu/itconnect/security/ca/load-der.crt
			InputStream caInput;
			try {
				caInput = _mwp.getContext().getAssets().open(_mwp.getSSLSertName());
			} catch (IOException e) {
				//Log.d("HTTP","sendRequest c");
				return rre;
			}//new BufferedInputStream(new FileInputStream("load-der.crt"));
			//Log.d("HTTP","sendRequest 0");
			Certificate ca;
			try {
			    ca = cf.generateCertificate(caInput);
			    System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
			} catch (CertificateException e) {
				//Log.d("HTTP","sendRequest c");
				return rre;
			} finally {
			    try {
					caInput.close();
				} catch (IOException e) {
					//Log.d("HTTP","sendRequest c");
					return rre;
				}
			}
			//Log.d("HTTP","sendRequest 1");
			// Create a KeyStore containing our trusted CAs
			String keyStoreType = KeyStore.getDefaultType();
			KeyStore keyStore;
			try {
				keyStore = KeyStore.getInstance(keyStoreType);
				keyStore.load(null, null);
				keyStore.setCertificateEntry("ca", ca);
				String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
				TrustManagerFactory tmf;
				tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
				tmf.init(keyStore);
				SSLContext context = SSLContext.getInstance("TLS");
				context.init(null, tmf.getTrustManagers(), null);
				HttpsURLConnection surlConnection = (HttpsURLConnection)url.openConnection();
				surlConnection.setHostnameVerifier(DO_NOT_VERIFY);
				surlConnection.setSSLSocketFactory(context.getSocketFactory());
				urlConnection=surlConnection;

			} catch (Exception e) {
				//Log.d("HTTP","sendRequest ks");
				return rre;
			}
		}
		else
			try{
			urlConnection = (HttpURLConnection) url.openConnection();
			}
			catch (Exception e) {
				//Log.d("HTTP","sendRequest ks");
				return rre;
			}
		// Create a TrustManager that trusts the CAs in our KeyStore
	
		try {
			
			
			urlConnection.setRequestMethod(method);
			if (method.equalsIgnoreCase("POST"))
			{
				//Log.d("PROTO","POST Data= "+data);
				urlConnection.setDoOutput(true);
				urlConnection.setDoInput(true);
				urlConnection.connect();
				OutputStream os=urlConnection.getOutputStream();
				os.write(data.getBytes());
				os.flush();
				os.close();
			}
			else
				urlConnection.connect();

			InputStream in = urlConnection.getInputStream();
			
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();

			int nRead;
			byte[] bdata = new byte[1024];

			//while (urlConnection);
			while ((nRead = in.read(bdata)) != -1) {
			  buffer.write(bdata, 0, nRead);
			  // Log.d("PROTO","readed:" + nRead + " CL= " + urlConnection.getContentLength() + new String(bdata));
			  // for (int i=0;i < 1024;++i) bdata[i]='+';
			}

			buffer.flush();
			rre.data=buffer.toByteArray();
			return rre;
		} 
		catch (Exception e) {
			Log.e("HTTP", "sendRequest c "+e.toString());
			return rre;
		}
		
		
	}
	@Override
	public void onPostExecute(RawResp rre)
	{
		_com.onHttpRequestComplete(rre.data,rre.id);
	}

}
