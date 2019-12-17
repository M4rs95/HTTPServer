import com.sun.jndi.ldap.Connection;

import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;
import java.util.StringTokenizer;
import java.io.*;

public class ServerThread implements Runnable {

	public static final String headerServer = "Server: ";
	public static final String headerContentLength = "Content-Length: ";
	public static final String headerContentLang = "Content-Language: de";
	public static final String headerConnection = "Connection: close";
	public static final String headerContentType = "Content-Type: ";


	Socket ClientSocket;
	BufferedReader is;
	OutputStream os;
	PrintWriter out;

	public ServerThread(Socket ConnectionSocket) throws IOException {
		// TODO Implement hand over of Socket
		this.ClientSocket = ConnectionSocket;
		this.is = new BufferedInputStream(new InputStreamReader(ClientSocket.getInputStream());
		this.os = new BufferedOutputStream(ClientSocket.getOutputStream());
		this.out= new PrintWriter(ClientSocket.getOutputStream());
	}

	public void run() {
		// TODO Implement HTTP v0.9, just GET - the available files to the server are given by ServerFiles.files
		try {

			String input = is.readLine();
			StringTokenizer parse = new StringTokenizer(input);
			String method = parse.nextToken().toUpperCase();
			String fileRequested = parse.nextToken().toLowerCase();

			if (!method.equals("GET") && !method.equals("Head")) {
				File file = new File();
				int fileLength = (int) file.length();
				String contentMimeType = "text/html";
				byte[] fileData = readFileData(file,(int) file.length());
				out.println("HTTP/1.1 501 NOT IMPLEMENTED");
				out.println(headerServer+"My Little Pony HTTP Server");
				out.println("Date: " + new Date());
				out.println(headerContentType + contentMimeType);
				out.println(headerContentLength + fileLength);
				out.println();
				out.flush();
				out.write(fileData, 0, (int)fileLength);
			}else {
				if (fileRequested.endsWith("/")){
					fileRequested+=".";
					}
				File file = new File(".", fileRequested);
				int fileLength = (int)file.length();
				String connect = getContentType(fileRequested);

				if (method.equals("GET")){
					byte[] fileData = readFileData(file, file.length());

					out.println("HTTP/1.1 200 OK");
					out.println(headerServer+"My Little Pony HTTP Server");
					out.println("Date: " +  new Date());
					out.println(headerContentType + ClientSocket);
					out.println(headerContentLength+ file.length());
					out.println();
					out.flush();

					os.write(fileData, 0, fileLength);
					os.flush();
				}
			}
		}catch (FileNotFoundException fnfe){
			try{
				fileNotFound(out, os, file);
			}catch(IOException ioe){
				System.err.println("Error with file not found exception: " +ioe.getMessage());
			}
		}
		catch (IOException ioe){
			System.err.println("Server Error" + ioe);
		}finally {
			try{
				is.close();
				out.close();
				os.close();
				ClientSocket.close();
			}catch (Exception e){
				System.err.println("Errror closing stream: " + e.getMessage());
			}
		}

	}
	private byte[] readFileData(File file, int fileLength) throws IOException {
		FileInputStream fileIn = null;
		byte[] fileData = new byte[fileLength];

		try{
			fileIn = new FileInputStream(file);
			fileIn.read(fileData);
		} finally {
			if (fileIn != null){
				fileIn.close();
			}
			return fileData;
		}
	}
	private String getContentType(String fileRequested){
		if (fileRequested.endsWith(".htm") || fileRequested.endsWith(".html")){
			return "text/html";}
			else return "text/plain";
	}

	private void fileNotFound(PrintWriter out, OutputStream os, String fileRequested){
		File file = new File ("index.html", "404 File not Found");
		int fileLength = (int)file.length();
		String content = "text/html";
		byte[] fileData = readFileData(file, fileLength);

		out.println("HTTP/1.1 404 File Not Found");
		out.println(headerServer + "My Little Pony HTTP Server");
		out.println("Date: " + new Date());
		out.println(headerContentType + content);
		out.println(headerContentLength + fileLength);
		out.println();
		out.flush();

		os.write(fileData, 0, fileLength);
		os.flush();


	}
}