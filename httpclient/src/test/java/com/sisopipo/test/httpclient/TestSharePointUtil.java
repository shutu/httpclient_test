/**
 * Created By: gelnyang at 163.com
 * Created Date: 2014年9月23日
 */
package com.sisopipo.test.httpclient;

/**
 * @author Geln Yang
 * @version 1.0
 */
public class TestSharePointUtil {

	/**
	 * @param args
	 */
	public final static void main(String[] args) throws Exception {
		String domain = "DOMAIN";
		String user = "xxx";
		String pwd = "xxx";
		String fileUrl = "http://192.168.13.31/Shared%20Documents/readme.txt";
		String saveFilePath = "d:/readme.txt";
		SharePointUtil.ntlmAuthDownload(domain, user, pwd, fileUrl, saveFilePath);
		System.out.println("========================over");
	}

}
