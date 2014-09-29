package chess;

import java.io.IOException;

public interface Connection {
	public void send(String s);
	public void update() throws IOException;
	
	public abstract int numPlayers();

	public interface Receiver {
		public void receive(String s);
	}
}
