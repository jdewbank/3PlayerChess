package chess;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public class RemoteConnection implements Connection {

	ByteBuffer buf = ByteBuffer.allocate(1024);
	private Receiver receiver;
	private SocketChannel socket;
	private int numPlayers;

	public RemoteConnection(Receiver r, SocketChannel s) throws IOException {
		receiver = r;
		socket = s;
		socket.configureBlocking(false);
	}

	public RemoteConnection(Receiver r, String host, int port, int players)
			throws UnknownHostException, IOException {
		numPlayers = players;
		receiver = r;
		socket = SocketChannel.open();
		socket.connect(new InetSocketAddress(host, port));
		socket.configureBlocking(false);
	}

	@Override
	public int numPlayers() {
		return numPlayers;
	}

	@Override
	public void send(String s) {
		s += "\n";

		// System.out.println("RCon sends: " + s);
		// System.out.println(socket.isConnected() + " " +
		// socket.isConnectionPending());

		ByteBuffer buf = ByteBuffer.allocate(1024);
		for (char ch : s.toCharArray()) {
			buf.putChar(ch);
		}
		buf.flip();
		try {
			socket.write(buf);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void update() throws IOException {
		try {
			socket.read(buf);
		} catch (IOException e) {
			System.err.println("The host has left the building. ");
			System.exit(0);
		}
		buf.flip();
		while (true) {
			String s = "";
			buf.mark();
			try {
				char ch;
				while ((ch = buf.getChar()) != '\n') {
					s += ch;
				}
				receiver.receive(s);
			} catch (BufferUnderflowException ex) {
				buf.reset();
				break;
			}
		}

		buf.compact();
	}
}
