package il.ac.technion.cs.sd.app.chat;

import java.util.List;

public class MyOnlineRoomsReply implements IMessage {

	public final List<String> myRooms;

	public MyOnlineRoomsReply(List<String> myRooms) {
		this.myRooms = myRooms;
	}

	@Override
	public void handle(IMessageHandler messageHandler) {
		messageHandler.handle(this);
	}

}
