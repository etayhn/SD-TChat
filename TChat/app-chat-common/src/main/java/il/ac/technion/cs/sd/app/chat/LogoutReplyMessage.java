package il.ac.technion.cs.sd.app.msg;

/**
 * After the server accepts a LogoutRequestMessage, it responds with this
 * message.
 */
public class LogoutReplyMessage implements IMessage {

	@Override
	public void handle(IMessageHandler messageHandler) {
		messageHandler.handle(this);
	}

}
