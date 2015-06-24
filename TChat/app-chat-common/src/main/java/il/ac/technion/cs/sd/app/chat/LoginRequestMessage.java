package il.ac.technion.cs.sd.app.msg;

/**
 * When a client wants to log into the server, he sends this message to the
 * server.
 */
public class LoginRequestMessage implements IMessage {

	/**
	 * The address of the client that wants to log in
	 */
	public final String myAddress;

	public LoginRequestMessage(String myAddress) {
		this.myAddress = myAddress;
	}

	@Override
	public void handle(IMessageHandler messageHandler) {
		messageHandler.handle(this);
	}

}
