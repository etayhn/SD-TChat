package il.ac.technion.cs.sd.app.chat;

public class JoinRoomReply implements IMessage {

	public final ErrorCode error;
	
	public JoinRoomReply(ErrorCode error) {
		this.error = error;
	}
	
	@Override
	public void handle(IMessageHandler messageHandler) {
		messageHandler.handle(this);
	}

}
