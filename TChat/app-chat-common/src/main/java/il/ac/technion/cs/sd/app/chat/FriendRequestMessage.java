package il.ac.technion.cs.sd.app.msg;

/**
 * When a client asks another client for friendship, it sends the server this
 * message. The server "forwards" it the the relevant client.
 */
public class FriendRequestMessage implements IMessage {
	/**
	 * request sender
	 */
	public final String from;

	/**
	 * request recipient
	 */
	public final String to;

	public FriendRequestMessage(String from, String to) {
		this.from = from;
		this.to = to;
	}

	@Override
	public void handle(IMessageHandler messageHandler) {
		messageHandler.handle(this);
	}

}
