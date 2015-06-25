package il.ac.technion.cs.sd.app.chat;

import java.util.List;

public class ClientsInRoomReply implements IMessage {

	public final String room;
	
	/**
	 * if no such room exists or room is empty, this list should be empty.
	 */
	public final List<String> clientsInRoom;

	public ClientsInRoomReply(String room, List<String> clientsInRoom) {
		this.room = room;
		this.clientsInRoom = clientsInRoom;
	}

	@Override
	public void handle(IMessageHandler messageHandler) {
		messageHandler.handle(this);
	}

}
