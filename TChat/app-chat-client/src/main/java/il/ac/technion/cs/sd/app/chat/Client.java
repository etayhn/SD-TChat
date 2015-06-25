package il.ac.technion.cs.sd.app.chat;

import il.ac.technion.cs.sd.app.chat.OurChatMessage;
import il.ac.technion.cs.sd.app.chat.IMessage;
import il.ac.technion.cs.sd.app.chat.IMessageHandler;
import il.ac.technion.cs.sd.app.chat.LogoutRequestMessage;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Consumer;

/**
 * This class represents a client in our client-server architecture. The client
 * implements IMessageHandler, and implements a visitor design pattern, to allow
 * it to treat each possible message type differently.
 */
public class Client implements IMessageHandler {
	/**
	 * The address of the client
	 */
	public final String myAddress;

	/**
	 * The address of the (only) server with which the client speaks
	 */
	public final String serverAddress;

	/**
	 * The rooms in which the client is currently logged in
	 */
	public final Map<String,String> myRooms;

	/**
	 * The ClientCommunicator with which the client and the server speak
	 */
	private il.ac.technion.cs.sd.lib.clientserver.Client communicator;

	/**
	 * A handler that needs to be run when a "regular" (common) instant message
	 * is sent
	 */
	private final Consumer<ChatMessage> messageConsumer;

	/**
	 * a consumer to be called when an announcement message is received from server.
	 */
	private Consumer<RoomAnnouncement> announcementConsumer;

	/**
	 * A blocking queue we use to wait for a response from the server when it
	 * needs to send us a response on a <i>myOnlineRooms</i> query
	 */
	private BlockingQueue<MyOnlineRoomsReply> myOnlineRooms;
	
	/**
	 * A blocking queue we use to wait for a response from the server when it
	 * needs to send us a response on an <i>allRooms</i> query
	 */
	private BlockingQueue<AllRoomsReply> allRooms;

	/**
	 * A blocking queue we use to wait for a response from the server when it
	 * needs to send us a response on an <i>clientsInRoom</i> query
	 */
	private BlockingQueue<ClientsInRoomReply> clientsInRoom;


	/**
	 * Creates a new client, starts the connection with the server, and
	 * retrieves all of the unread messages that the client got when he was not
	 * logged in.
	 * 
	 * @param who
	 *            the client's address
	 * @param serverAddress
	 *            the server's address
	 * @param chatMessageConsumer
	 *            The consumer of chat messages (See
	 *            {@link ClientChatApplication#sendMessage(String, String)})
	 * @param announcementConsumer
	 *            The consumer of room announcements (See
	 *            {@link RoomAnnouncement.Announcement})
	 */
	public Client(String username, String serverAddress,
			Consumer<ChatMessage> messageConsumer,
			Consumer<RoomAnnouncement> announcementConsumer) {
		this.myAddress = username;
		this.serverAddress = serverAddress;
		this.messageConsumer = messageConsumer;
		this.announcementConsumer = announcementConsumer;

		this.clientsInRoom = new LinkedBlockingDeque<>();
		this.allRooms = new LinkedBlockingDeque<>();
		this.myOnlineRooms = new LinkedBlockingDeque<>();
		this.myRooms = new ConcurrentHashMap<String,String>();

		communicator = new il.ac.technion.cs.sd.lib.clientserver.Client(myAddress);
		communicator.start(serverAddress,
				new Consumer<Object>() {

					@Override
					public void accept(Object o) {
						((IMessage) o).handle(Client.this);
					}

				});
	}

	/**
	 * Stops the client.
	 */
	public void stop() {
		if (communicator.isCommunicatorStopped()) {
			return;
		}
		// sending a request
		send(new LogoutRequestMessage(myAddress));
		communicator.stopListenLoop();
	}

	/**
	 * Sends a message to the server.
	 * 
	 * @param message
	 *            the message to send
	 */
	public void send(IMessage message) {
		communicator.send(message);
	}

	@Override
	public void handle(OurChatMessage message) {
		messageConsumer.accept(new ChatMessage(message.from, message.room, message.content));
	}

	@Override
	public void handle(OurRoomAnnouncement message) {
		announcementConsumer.accept(new RoomAnnouncement(message.who, message.room, message.type));
	}

	@Override
	public void handle(MyOnlineRoomsReply message) {
		try {
			myOnlineRooms.put(message);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void handle(AllRoomsReply message) {
		try {
			allRooms.put(message);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void handle(ClientsInRoomReply message) {
		try {
			clientsInRoom.put(message);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public boolean isInRoom(String room) {
		return myRooms.containsKey(room);
	}

	/**
	 * @return All the rooms the client joined
	 */
	public List<String> getJoinedRooms() {
		// sending a request
		send(new MyOnlineRoomsRequest(myAddress));

		// waiting for a response
		while (true) {
			try {
				return myOnlineRooms.take().myRooms;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @return all rooms that have clients currently online, i.e., logged in
	 */
	public List<String> getAllRooms() {
		send(new AllRoomsRequest(myAddress));

		// waiting for a response
		while (true) {
			try {
				return allRooms.take().allRooms;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Gets all the clients that joined the room and are currently logged in. A client
	 * does not have to be in a room to get a list of its clients.
	 * @param room The room to check
	 * @return A list of all the online clients in the room
	 * @throws NoSuchRoomException If the room doesn't exist, or no clients are currently in it (i.e., are logged out)
	 */
	public List<String> getClientsInRoom(String room) throws NoSuchRoomException{
		send(new MyOnlineRoomsRequest(myAddress));

		// waiting for a response
		while (true) {
			try {
				List<String> clients = clientsInRoom.take().clientsInRoom;
				if(clientsInRoom.size() == 0){
					throw new NoSuchRoomException();
				}
				return clients;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void leaveRoom(String room) {

		myRooms.remove(room);
		
	}

	public void joinRoom(String room) {

		myRooms.put(room,room);
	}

}
