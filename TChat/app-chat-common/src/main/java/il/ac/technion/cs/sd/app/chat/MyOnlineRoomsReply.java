package il.ac.technion.cs.sd.app.chat;

import java.util.List;

public class MyOnlineRoomsReply implements IMessage {

	public final List<String> onlineRooms;

	public MyOnlineRoomsReply(List<String> onlineRooms) {
		this.onlineRooms = onlineRooms;
	}

	@Override
	public void handle(IMessageHandler messageHandler) {
		messageHandler.handle(this);
	}

}
