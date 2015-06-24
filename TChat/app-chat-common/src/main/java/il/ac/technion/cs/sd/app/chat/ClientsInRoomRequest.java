package il.ac.technion.cs.sd.app.chat;

public class ClientsInRoomRequest implements IMessage {

	public final String who;
	public final String room;
	
	public ClientsInRoomRequest(String who, String room) {
		this.room = room;
		this.who = who;
	}

	@Override
	public void handle(IMessageHandler messageHandler) {
		messageHandler.handle(this);
	}

}
