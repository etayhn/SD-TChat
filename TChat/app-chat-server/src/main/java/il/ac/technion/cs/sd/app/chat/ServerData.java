package il.ac.technion.cs.sd.app.chat;

import il.ac.technion.cs.sd.app.chat.IMessageHandler;
import java.util.Map;

/**
 * This class represents a server in our client-server architecture. The server
 * implements IMessageHandler, and implements a visitor design pattern, to allow
 * it to treat each possible message type differently.
 */
public class ServerData implements IMessageHandler {
	/**
	 * Stores all of the data about the clients in a clientName->clientData map
	 */
	public Map<String, ClientData> clients;
	
	public Map<String, Room> allRooms;
	
	public Map<String, Room> onlineRooms;

	public ServerData(Map<String, ClientData> clients,
			Map<String, Room> allRooms, Map<String, Room> onlineRooms) {
		super();
		this.clients = clients;
		this.allRooms = allRooms;
		this.onlineRooms = onlineRooms;
	}

}
