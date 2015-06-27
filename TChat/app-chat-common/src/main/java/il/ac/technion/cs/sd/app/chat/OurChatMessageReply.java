package il.ac.technion.cs.sd.app.chat;

public class OurChatMessageReply implements IMessage {

	public final ErrorCode error;

	public OurChatMessageReply(ErrorCode error) {
		this.error = error;
	}

	@Override
	public void handle(IMessageHandler messageHandler) {
		messageHandler.handle(this);
	}

}
