package com.sisopipo.test.httpclient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;

/**
 * Created By: gelnyang at 163.com
 * Created Date: Sep 8, 2011 12:08:41 PM
 */

/**
 * @author Geln Yang
 * @version 1.0
 */
@SuppressWarnings("serial")
public class SharePointUtil implements Serializable {

	private static final Log logger = LogFactory.getLog(SharePointUtil.class);

	public final static void main(String[] args) throws Exception {
		String domain = "COMWAVE";
		String user = "xxx";
		String pwd = "xxx";
		String fileUrl = "http://192.168.13.31/Shared%20Documents/FirstBank/Fund%20System/readme.txt";
		String saveFilePath = "d:/readme.txt";
		downloadFile(domain, user, pwd, fileUrl, saveFilePath);
		System.out.println("========================over");
	}

	public static int downloadFile(String domain, String userName, String userPass, String fileUrl, String saveTargetFile) throws Exception {
		return ntlmAuthDownload(domain, userName, userPass, fileUrl, saveTargetFile);
	}

	private final static int ntlmAuthDownload(String domain, String user, String pwd, String fileUrl, String saveTargetFile) throws Exception {
		logger.info("download sharepoint file:" + fileUrl);
		URL url = new URL(fileUrl);
		int port = url.getPort();
		if (port == -1) {
			port = 80;
		}
		String host = url.getHost();
		String protocol = url.getProtocol();
		String baseUrl = protocol + "://" + host + ":" + port;
		String filePath = url.getPath();
		logger.info("base url:" + baseUrl);
		logger.info("file path:" + filePath);
		HttpClientBuilder builder = HttpClientBuilder.create();

		String workstation = InetAddress.getLocalHost().getCanonicalHostName();
		Credentials credential = new NTCredentials(user, pwd, workstation, domain);
		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(AuthScope.ANY, credential);

		builder.setDefaultCredentialsProvider(credentialsProvider);

		// Create AuthCache instance
		AuthCache authCache = new BasicAuthCache();
		// Generate BASIC scheme object and add it to the local auth cache
		BasicScheme basicSchema = new BasicScheme();
		HttpHost httpHost = new HttpHost(host, port, protocol);
		authCache.put(httpHost, basicSchema);

		// Add AuthCache to the execution context
		BasicHttpContext httpContext = new BasicHttpContext();
		httpContext.setAttribute(HttpClientContext.AUTH_CACHE, authCache);

		CloseableHttpClient httpClient = builder.build();
		try {
			logger.info("start download file: " + fileUrl);
			HttpGet httpGet = new HttpGet(fileUrl);
			HttpResponse response = httpClient.execute(httpGet, httpContext);
			StatusLine statusLine = response.getStatusLine();
			logger.info(statusLine.toString());

			if (statusLine.getStatusCode() != 200) {
				logger.error("unexpected status code:" + statusLine.getStatusCode());
				return statusLine.getStatusCode();
			}

			HttpEntity entity = response.getEntity();
			try {
				String tempFile = saveTargetFile + ".temp";
				logger.info("write content to file:" + tempFile);
				FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
				InputStream is = entity.getContent();
				byte[] bytes = new byte[4096];
				while (is.read(bytes) != -1) {
					fileOutputStream.write(bytes);
				}
				fileOutputStream.close();
				checkFileExists(saveTargetFile);
				logger.info("rename " + tempFile + " to " + saveTargetFile);
				FileUtils.moveFile(new File(tempFile), new File(saveTargetFile));
			} finally {
				try {
					EntityUtils.consume(entity);
				} catch (Exception e) {
				}
			}
			return statusLine.getStatusCode();
		} finally {
			httpClient.close();
		}
	}

	private static void checkFileExists(String saveTargetFile) {
		File file = new File(saveTargetFile);
		for (int i = 0; i < 30; i++) {
			if (file.exists()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			} else {
				break;
			}
		}

		/* still exists */
		if (file.exists())
			try {
				logger.warn("delete file:" + saveTargetFile);
				FileUtils.forceDelete(file);
			} catch (IOException e) {
			}
	}

}
