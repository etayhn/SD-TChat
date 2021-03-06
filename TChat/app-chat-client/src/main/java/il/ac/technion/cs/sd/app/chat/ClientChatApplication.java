package il.ac.technion.cs.sd.app.chat;

import java.util.List;
import java.util.function.Consumer;

/**
 * The client side of the TChat application. Allows sending and getting messages
 * to and from other clients using a server. <br>
 * You should implement all the methods in this class
 */
public class ClientChatApplication {

	private Client client;
	private final String serverAddress;
	private final String username;

	/**
	 * Creates a new application, tied to a single user
	 * 
	 * @param serverAddress
	 *            The address of the server to connect to for sending and
	 *            receiving messages
	 * @param username
	 *            The username that will be sending and accepting the messages
	 *            using this object
	 */
	public ClientChatApplication(String serverAddress, String username) {
		this.serverAddress = serverAddress;
		this.username = username;
	}

	/**
	 * Logs the user to the server. The user automatically joins all the rooms
	 * he was joined to to before logging out. All the <i>other</i> clients in
	 * the rooms will receive a message upon joining. The server can only reply
	 * to this message using an empty message. Client receive their own messages
	 * and announcements, e.g., a client also receives his own messages.
	 * 
	 * @param chatMessageConsumer
	 *            The consumer of chat messages (See
	 *            {@link ClientChatApplication#sendMessage(String, String)})
	 * @param announcementConsumer
	 *            The consumer of room announcements (See
	 *            {@link RoomAnnouncement.Announcement})
	 */
	public void login(Consumer<ChatMessage> chatMessageConsumer,
			Consumer<RoomAnnouncement> announcementConsumer) {
		client = new Client(username, serverAddress, chatMessageConsumer,
				announcementConsumer);
	}

	/**
	 * Joins the room. If the room does not exist, it will be created. All the
	 * <i>other</i> clients in the room will receive a message.
	 * 
	 * @param room
	 *            The room to join
	 * @throws AlreadyInRoomException
	 *             If the client isn't currently in the room
	 */
	public void joinRoom(String room) throws AlreadyInRoomException {
		client.joinRoom(room);
	}

	/**
	 * Leaves the room. All the <i>other</i> clients in the room will receive a
	 * message.
	 * 
	 * @param room
	 *            The room to leave
	 * @throws NotInRoomException
	 *             If the client isn't currently in the room
	 */
	public void leaveRoom(String room) throws NotInRoomException {
		client.leaveRoom(room);
	}

	/**
	 * Logs the user out of chat application. A logged out client cannot perform
	 * any tasks other than logging in. A logged out message will be sent to all
	 * the <i>other</i> clients in rooms with the client.
	 */
	public void logout() {
		client.logout();
	}

	/**
	 * Broadcasts a message to all <i>other</i> clients in the room.
	 * 
	 * @param room
	 *            The room to broadcast the message to.
	 * @param what
	 *            The message to broadcast.
	 * @throws NotInRoomException
	 *             If the client isn't currently in the room
	 */
	public void sendMessage(String room, String what) throws NotInRoomException {
		client.sendMessage(room, what);
	}

	/**
	 * @return All the rooms the client joined
	 */
	public List<String> getJoinedRooms() {
		return client.getJoinedRooms();
	}

	/**
	 * @return all rooms that have clients currently online, i.e., logged in
	 */
	public List<String> getAllRooms() {
		return client.getAllRooms();
	}

	/**
	 * Gets all the clients that joined the room and are currently logged in. A
	 * client does not have to be in a room to get a list of its clients.
	 * 
	 * @param room
	 *            The room to check
	 * @return A list of all the online clients in the room
	 * @throws NoSuchRoomException
	 *             If the room doesn't exist, or no clients are currently in it
	 *             (i.e., are logged out)
	 */
	public List<String> getClientsInRoom(String room)
			throws NoSuchRoomException {
		return client.getClientsInRoom(room);
	}

	/**
	 * Stops the client, freeing up any resources used. You can assume that
	 * {@link ClientChatApplication#logout()} was called before this method if
	 * the client was logged in.
	 */
	public void stop() {
		client.stop();
	}

}
