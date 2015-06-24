package il.ac.technion.cs.sd.app.chat;

import java.util.ArrayList;
import java.util.List;

public class Room {

	private final List<String> clientsInRoom;
	
	public Room() {
		clientsInRoom = new ArrayList<String>();
	}

	public List<String> getClients() {
		return clientsInRoom;
	}
	
}
