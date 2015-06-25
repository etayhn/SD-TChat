package il.ac.technion.cs.sd.app.chat;

import java.util.List;

public class AllRoomsReply implements IMessage {

	public final List<String> allRooms;

	public AllRoomsReply(List<String> onlineRooms) {
		this.allRooms = onlineRooms;
	}

	@Override
	public void handle(IMessageHandler messageHandler) {
		messageHandler.handle(this);
	}

}
