package il.ac.technion.cs.sd.app.msg;

/**
 * When the client wants to ask the server if another client is online, he sends
 * this message.
 */
public class OnlineCheckRequestMessage implements IMessage {

	/**
	 * The name of the client who sends the message
	 */
	public final String whoIsChecking;

	/**
	 * The name of the client that the request sender asks about
	 */
	public final String whoIsBeingChecked;

	public OnlineCheckRequestMessage(String whoIsChecking, String whoIsChecked) {
		this.whoIsChecking = whoIsChecking;
		this.whoIsBeingChecked = whoIsChecked;
	}

	@Override
	public void handle(IMessageHandler messageHandler) {
		messageHandler.handle(this);
	}

}
