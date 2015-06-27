package il.ac.technion.cs.sd.app.chat;

public class LeaveRoomReply implements IMessage {

	public final ErrorCode error;
	
	public LeaveRoomReply(ErrorCode error) {
		this.error = error;
	}
	
	@Override
	public void handle(IMessageHandler messageHandler) {
		messageHandler.handle(this);
	}

}
