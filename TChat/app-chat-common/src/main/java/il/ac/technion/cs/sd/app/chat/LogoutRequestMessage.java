package il.ac.technion.cs.sd.app.chat;

/**
 * When a client wants to log out of the server, he sends this message.
 */
public class LogoutRequestMessage implements IMessage {

	/**
	 * The address of the client who wishes to log out.
	 */
	public final String who;

	public LogoutRequestMessage(String myAddress) {
		this.who = myAddress;
	}

	@Override
	public void handle(IMessageHandler messageHandler) {
		messageHandler.handle(this);
	}

}
