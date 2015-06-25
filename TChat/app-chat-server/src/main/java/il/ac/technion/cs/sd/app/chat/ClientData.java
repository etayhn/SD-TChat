package il.ac.technion.cs.sd.app.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A class that saves a client's data on the server.
 */
public class ClientData {

	/**
	 * the list of friends of the client
	 */
	private Map<String,Room> rooms;

	/**
	 * true iff the client is online, i.e. logged to the server
	 */
	private boolean isOnline;

	public ClientData() {
		rooms = new ConcurrentHashMap<>();
		isOnline = false;
	}

	/**
	 * @return true iff the client is online
	 */
	public boolean isOnline() {
		return isOnline;
	}

	/**
	 * Sets the client's state to be either online or offline
	 * 
	 * @param online
	 *            the new state
	 */
	public void setOnline(boolean online) {
		isOnline = online;
	}

	/**
	 * Adds "room" to the client's list of rooms
	 * 
	 * @param room
	 *            the room to add
	 */
	public void addRoom(String roomName, Room room) {
		rooms.put(roomName, room);
	}

	public Map<String,Room> getRooms() {

		return rooms;
	}

	public void removeRoom(String roomName) {

		rooms.remove(roomName);
		
	}

}
