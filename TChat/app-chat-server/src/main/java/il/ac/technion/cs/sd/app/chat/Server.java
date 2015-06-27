package il.ac.technion.cs.sd.app.chat;

import il.ac.technion.cs.sd.app.chat.OurChatMessage;
import il.ac.technion.cs.sd.app.chat.IMessage;
import il.ac.technion.cs.sd.app.chat.IMessageHandler;
import il.ac.technion.cs.sd.app.chat.LoginRequestMessage;
import il.ac.technion.cs.sd.app.chat.LogoutRequestMessage;
import il.ac.technion.cs.sd.app.chat.RoomAnnouncement.Announcement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * This class represents a server in our client-server architecture. The server
 * implements IMessageHandler, and implements a visitor design pattern, to allow
 * it to treat each possible message type differently.
 */
public class Server implements IMessageHandler {
	/**
	 * Stores all of the data about the clients in a clientName->clientData map
	 */
	private Map<String, ClientData> clients;

	private Map<String, Room> allRooms;

	private Map<String, Room> onlineRooms;

	/**
	 * The ServerCommunicator with which the server speaks with the clients
	 */
	private il.ac.technion.cs.sd.lib.clientserver.Server communicator;

	/**
	 * the server's address
	 */
	public final String myAddress;

	/**
	 * Creates a new server, initializes and starts it (and its communicator)
	 */
	public Server(String myAddress) {
		this.myAddress = myAddress;
		clients = new HashMap<>();
		onlineRooms = new ConcurrentHashMap<String, Room>();
		allRooms = new ConcurrentHashMap<String, Room>();

	}

	/**
	 * Stops the server
	 */
	public void stop() {
		communicator.stop();
	}

	public void start() {
		communicator = new il.ac.technion.cs.sd.lib.clientserver.Server(
				myAddress);
		communicator.start(new BiConsumer<Object, String>() {

			@Override
			public void accept(Object o, String from) {
				((IMessage) o).handle(Server.this);
			}

		});

		// load data if possible.
		Optional<Object> data = communicator.readObjectFromFile(myAddress);
		if (data.isPresent()) {
			ServerData serverData = (ServerData) data.get();
			clients = serverData.clients;
			onlineRooms = serverData.onlineRooms;
			allRooms = serverData.allRooms;
		}

	}

	public void sendToRoom(String from, String room, IMessage message) {
		for (String to : allRooms.get(room).getClients()) {
			if (!to.equals(from)) {
				send(to, message);
			}
		}
	}

	/**
	 * Sends a message to a client.
	 * 
	 * @param to
	 *            the client to whom we send the message.
	 * @param message
	 *            the message to send.
	 */
	public void send(String to, IMessage message) {
		if (!clients.containsKey(to)) {
			// there is no such client, so we create one
			clients.put(to, new ClientData());
		}

		// should always succeed
		ClientData clientData = clients.get(to);

		if (clientData.isOnline()) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					communicator.send(to, message, false);

				}
			}).start();
		}
	}

	// ***************************************************************

	@Override
	public void handle(AllRoomsRequest message) {

		List<String> rooms = new ArrayList<>();
		rooms.addAll(onlineRooms.keySet());
		send(message.who, new AllRoomsReply(rooms));
	}

	@Override
	public void handle(MyOnlineRoomsRequest message) {
		List<String> rooms = new ArrayList<>(clients.get(message.who)
				.getRooms().keySet());

		send(message.who, new MyOnlineRoomsReply(rooms));
	}

	@Override
	public void handle(ClientsInRoomRequest message) {

		Room room = onlineRooms.get(message.room);

		List<String> clients;
		if (room == null) {
			clients = new ArrayList<>();
		} else {
			clients = room.getOnlineClients();
		}
		send(message.who, new ClientsInRoomReply(message.room, clients));
	}

	@Override
	public void handle(JoinRoomRequest message) {
		ClientData clientData = clients.get(message.who);

		if (clientData.getRooms().containsKey(message.room)) {
			send(message.who, new JoinRoomReply(
					ErrorCode.AlreadyInRoomException));
			return;
		}

		Room room = allRooms.get(message.room);
		if (room == null) {
			room = new Room(message.room);
			allRooms.put(message.room, room);
		}
		clientData.addRoom(message.room, room);
		room.addClient(message.who, clientData);

		if (room.hasLoggedInUsers()) {
			onlineRooms.put(message.room, room);
		}

		send(message.who, new JoinRoomReply(ErrorCode.Success));
		sendToRoom(message.who, message.room, new OurRoomAnnouncement(
				message.who, message.room, Announcement.JOIN));
	}

	@Override
	public void handle(LeaveRoomRequest message) {
		ClientData clientData = clients.get(message.who);

		if (!clientData.getRooms().containsKey(message.room)) {
			send(message.who, new LeaveRoomReply(ErrorCode.NotInRoomException));
			return;
		}

		Room room = allRooms.get(message.room);

		room.removeClient(message.who);
		if (!room.hasLoggedInUsers()) {
			onlineRooms.remove(message.room);
		}
		clientData.removeRoom(message.room);

		send(message.who, new LeaveRoomReply(ErrorCode.Success));

		// no one to send announcement to.
		if (room.isEmpty()) {
			allRooms.remove(message.room);

			// send announcement.
		} else {
			sendToRoom(message.who, message.room, new OurRoomAnnouncement(
					message.who, message.room, Announcement.LEAVE));
		}
	}

	@Override
	public void handle(LogoutRequestMessage message) {
		ClientData clientData = clients.get(message.who);
		clientData.setOnline(false);

		for (Room room : clientData.getRooms().values()) {
			room.onClientLogout(message.who);
			if (!room.hasLoggedInUsers()) {
				onlineRooms.remove(room.name);
			}
			sendToRoom(message.who, room.name, new OurRoomAnnouncement(
					message.who, room.name, Announcement.DISCONNECT));
		}
	}

	@Override
	public void handle(LoginRequestMessage message) {
		ClientData clientData = clients.get(message.who);
		if (clientData == null) {
			clientData = new ClientData();
			clients.put(message.who, clientData);
		}
		clientData.setOnline(true);

		for (Room room : clientData.getRooms().values()) {
			room.onClientLogin(message.who, clientData);
			if (room.hasLoggedInUsers()) {
				onlineRooms.put(room.name, room);
			}
			sendToRoom(message.who, room.name, new OurRoomAnnouncement(
					message.who, room.name, Announcement.JOIN));
		}
	}

	@Override
	public void handle(OurChatMessage message) {
		// if the client is not in the room
		ClientData clientData = clients.get(message.who);
		if (!clientData.getRooms().containsKey(message.room)) {
			send(message.who, new OurChatMessageReply(
					ErrorCode.NotInRoomException));
			return;
		}

		// otherwise, the client can send the message to the room
		send(message.who, new OurChatMessageReply(ErrorCode.Success));
		sendToRoom(message.who, message.room, message);
	}

	public void saveData() {

		ServerData data = new ServerData(clients, allRooms, onlineRooms);
		communicator.saveObjectToFile(myAddress, data);
	}

	public void removeData() {
		communicator.clearPersistentData();

	}

}
