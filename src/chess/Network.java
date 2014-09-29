package chess;

import java.io.*;
import java.net.Socket;

public class Network {
	public static void main(String[] args) {
		String hostName = "129.116.45.15";
		int portNumber = 12345;

		try {
			Socket echoSocket = new Socket(hostName, portNumber);
			PrintWriter sOut = new PrintWriter(echoSocket.getOutputStream(),
					true);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					echoSocket.getInputStream()));

			BufferedReader stdIn = new BufferedReader(new InputStreamReader(
					System.in));

			String userInput;
			while ((userInput = stdIn.readLine()) != null) {
				sOut.println(userInput);
				System.out.println("echo: " + in.readLine());
			}
			

		} catch (Exception e) {
			System.err.println(e);
		}

	}
}
