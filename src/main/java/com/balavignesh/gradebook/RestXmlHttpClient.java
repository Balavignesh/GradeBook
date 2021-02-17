/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.balavignesh.gradebook;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 * @author BalaVignesh
 */
public class RestXmlHttpClient {
    public static void main(String[] args) throws IOException {

		String request = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
"<gradebook>\n" +
"    <gradeTitle>vignesh2</gradeTitle>\n" +
"    <gradeId>2</gradeId>\n" +
"    <server>\n" +
"        <name>localhost</name>\n" +
"        <ip>104.48.130.146</ip>\n" +
"        <port>8080</port>\n" +
"        <contextRoot>GradeBook</contextRoot>\n" +
"    </server>\n" +
"</gradebook>";

		URL url = new URL("http://35.224.65.85:8080/GradeBook/resources/gradebook");
                
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		// Set timeout as per needs
		connection.setConnectTimeout(20000);
		connection.setReadTimeout(20000);

		// Set DoOutput to true if you want to use URLConnection for output.
		// Default is false
		connection.setDoOutput(true);

		connection.setUseCaches(true);
		connection.setRequestMethod("POST");

		// Set Headers
		//connection.setRequestProperty("Accept", "application/xml");
		connection.setRequestProperty("Content-Type", "application/xml");

		// Write XML
		OutputStream outputStream = connection.getOutputStream();
		byte[] b = request.getBytes("UTF-8");
		outputStream.write(b);
		outputStream.flush();
		outputStream.close();

		// Read XML
                try{
		InputStream inputStream = connection.getInputStream();
		byte[] res = new byte[2048];
		int i = 0;
		StringBuilder response = new StringBuilder();
		while ((i = inputStream.read(res)) != -1) {
			response.append(new String(res, 0, i));
		}
		inputStream.close();
System.out.println("Response= " + response.toString());
                }catch(Exception ex){
                    ex.printStackTrace();
                }
		

	}
}
