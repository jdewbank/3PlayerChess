package chess;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	public static void main(String[] args) {
		int portNumber = 12345;
		try {
			ServerSocket sSocket = new ServerSocket(portNumber);
			Socket cSocket = sSocket.accept();
			PrintWriter out = new PrintWriter(cSocket.getOutputStream(), true);

			BufferedReader in = new BufferedReader(new InputStreamReader(
					cSocket.getInputStream()));

			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				out.println(inputLine.substring(1));
			}

		} catch (Exception e) {
			System.err.println(e);
		}
	}
}
