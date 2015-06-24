package il.ac.technion.cs.sd.app.msg;

import java.util.List;

/**
 * After the server gets a LoginRequestMessage from a client, he responds with a
 * LoginReplyMessage. Reveiving a LoginReplyMessage means that the server
 * received and accepted the request. The response also contains a list of all
 * the messages for the client that were received when he was not logged in.
 */
public class LoginReplyMessage implements IMessage {

	/**
	 * The list of messages that were not sent to the client when he was not
	 * logged in
	 */
	public List<IMessage> unsentMessages;

	public LoginReplyMessage(List<IMessage> unsentMessages) {
		this.unsentMessages = unsentMessages;
	}

	@Override
	public void handle(IMessageHandler messageHandler) {
		messageHandler.handle(this);
	}

}
