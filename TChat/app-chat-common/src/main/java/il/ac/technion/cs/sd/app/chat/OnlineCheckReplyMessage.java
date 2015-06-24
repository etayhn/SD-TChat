package il.ac.technion.cs.sd.app.msg;

import java.util.Optional;

/**
 * After a server receives an OnlineCheckRequestMessage, he responds with this
 * message.
 */
public class OnlineCheckReplyMessage implements IMessage {

	/**
	 * The name of the person to whom the answer applies.
	 */
	public final String whoIsBeingChecked;

	/**
	 * The answer. A wrapped <code>true</code> if the user is a friend and is
	 * offline; a wrapped <code>false</code> if the user is a friend and is
	 * offline; an empty {@link Optional} if the user isn't a friend of the
	 * client
	 */
	public final Optional<Boolean> answer;

	public OnlineCheckReplyMessage(String whoIsBeingChecked,
			Optional<Boolean> answer) {
		this.whoIsBeingChecked = whoIsBeingChecked;
		this.answer = answer;
	}

	@Override
	public void handle(IMessageHandler messageHandler) {
		messageHandler.handle(this);
	}

}
