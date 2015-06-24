package il.ac.technion.cs.sd.app.msg;

/**
 * When a client asks another client for friendship, the latter responds with
 * this message. The message is sent through the server (obviously), which
 * "forwards" it to the request sender.
 */
public class FriendReplyMessage implements IMessage {

	/**
	 * response sender, i.e. request recipient
	 */
	public final String from;

	/**
	 * response receiver, i.e. request sender
	 */
	public final String to;

	/**
	 * the response
	 */
	public final boolean answer;

	public FriendReplyMessage(String from, String to, boolean answer) {
		this.from = from;
		this.to = to;
		this.answer = answer;
	}

	@Override
	public void handle(IMessageHandler messageHandler) {
		messageHandler.handle(this);
	}

}
