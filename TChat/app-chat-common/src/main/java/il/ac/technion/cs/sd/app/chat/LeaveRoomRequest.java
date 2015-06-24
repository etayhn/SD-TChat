package il.ac.technion.cs.sd.app.chat;

public class LeaveRoomRequest implements IMessage {

	public final String who;
	public final String room;

	public LeaveRoomRequest(String who, String room) {
		this.who = who;
		this.room = room;
	}
	
	@Override
	public void handle(IMessageHandler messageHandler) {
		messageHandler.handle(this);
	}

}
