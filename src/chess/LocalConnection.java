package chess;

public class LocalConnection implements Connection {

	private LocalConnection otherConnection;
	private Receiver receiver;
	private int numPlayers;

	public LocalConnection(Receiver r, int numPlayers) {
		this.numPlayers = numPlayers;
		receiver = r;
	}

	public LocalConnection(Receiver r, LocalConnection other) {
		otherConnection = other;
		receiver = r;
		otherConnection.otherConnection = this;
	}

	@Override
	public void send(String s) {
		otherConnection.receiver.receive(s);
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
	}

	@Override
	public int numPlayers() {
		return numPlayers;
	}
}
