package il.ac.technion.cs.sd.app.chat;

public class JoinRoomRequest implements IMessage {

	public final String who;
	public final String room;

	public JoinRoomRequest(String who, String room) {
		this.who = who;
		this.room = room;
	}
	
	@Override
	public void handle(IMessageHandler messageHandler) {
		messageHandler.handle(this);
	}

}
