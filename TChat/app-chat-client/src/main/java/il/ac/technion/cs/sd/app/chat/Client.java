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
	public final String username;

	/**
	 * The address of the (only) server with which the client speaks
	 */
	public final String serverAddress;

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
	 * a consumer to be called when an announcement message is received from
	 * server.
	 */
	private Consumer<RoomAnnouncement> announcementConsumer;

	/**
	 * A blocking queue we use to wait for a response from the server when it
	 * needs to send us a response on a <i>myOnlineRooms</i> query
	 */
	private BlockingQueue<MyOnlineRoomsReply> myOnlineRoomsQueue;

	/**
	 * A blocking queue we use to wait for a response from the server when it
	 * needs to send us a response on an <i>allRooms</i> query
	 */
	private BlockingQueue<AllRoomsReply> allRoomsQueue;

	/**
	 * A blocking queue we use to wait for a response from the server when it
	 * needs to send us a response on an <i>clientsInRoom</i> query
	 */
	private BlockingQueue<ClientsInRoomReply> clientsInRoomQueue;

	/**
	 * A blocking queue we use to wait for a response from the server when it
	 * needs to send us a response on an <i>leaveRoom</i> query
	 */
	private BlockingQueue<LeaveRoomReply> leaveRoomQueue;

	/**
	 * A blocking queue we use to wait for a response from the server when it
	 * needs to send us a response on an <i>joinRoom</i> query
	 */
	private BlockingQueue<JoinRoomReply> joinRoomQueue;

	private BlockingQueue<OurChatMessageReply> chatMessaeReplyQueue;

	/**
	 * Creates a new client, starts the connection with the server, and
	 * retrieves all of the unread messages that the client got when he was not
	 * logged in.
	 * 
	 * @param myAddress
	 *            the client's address
	 * @param serverAddress
	 *            the server's address
	 * @param messageConsumer
	 *            The consumer of chat messages (See
	 *            {@link ClientChatApplication#sendMessage(String, String)})
	 * @param announcementConsumer
	 *            The consumer of room announcements (See
	 *            {@link RoomAnnouncement.Announcement})
	 */
	public Client(String myAddress, String serverAddress,
			Consumer<ChatMessage> messageConsumer,
			Consumer<RoomAnnouncement> announcementConsumer) {
		this.username = myAddress;
		this.serverAddress = serverAddress;
		this.messageConsumer = messageConsumer;
		this.announcementConsumer = announcementConsumer;

		this.clientsInRoomQueue = new LinkedBlockingDeque<>();
		this.allRoomsQueue = new LinkedBlockingDeque<>();
		this.myOnlineRoomsQueue = new LinkedBlockingDeque<>();
		this.joinRoomQueue = new LinkedBlockingDeque<>();
		this.leaveRoomQueue = new LinkedBlockingDeque<>();
		this.chatMessaeReplyQueue = new LinkedBlockingDeque<>();

		communicator = new il.ac.technion.cs.sd.lib.clientserver.Client(
				myAddress);
		communicator.start(serverAddress, new Consumer<Object>() {

			@Override
			public void accept(Object o) {
				((IMessage) o).handle(Client.this);
			}

		});

		send(new LoginRequestMessage(myAddress));
	}

	/**
	 * Stops the client, freeing up any resources used. You can assume that
	 * {@link ClientChatApplication#logout()} was called before this method if
	 * the client was logged in.
	 */
	public void stop() {
		if (communicator.isCommunicatorStopped()) {
			return;
		}
		// sending a request
		send(new LogoutRequestMessage(username));
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
		messageConsumer.accept(new ChatMessage(message.who, message.room,
				message.content));
	}

	@Override
	public void handle(OurChatMessageReply message) {
		try {
			chatMessaeReplyQueue.put(message);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void handle(OurRoomAnnouncement message) {
		announcementConsumer.accept(new RoomAnnouncement(message.who,
				message.room, message.type));
	}

	@Override
	public void handle(MyOnlineRoomsReply message) {
		try {
			myOnlineRoomsQueue.put(message);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void handle(JoinRoomReply message) {
		try {
			joinRoomQueue.put(message);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void handle(LeaveRoomReply message) {
		try {
			leaveRoomQueue.put(message);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void handle(AllRoomsReply message) {
		try {
			allRoomsQueue.put(message);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void handle(ClientsInRoomReply message) {
		try {
			clientsInRoomQueue.put(message);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return All the rooms the client joined
	 */
	public List<String> getJoinedRooms() {
		// sending a request
		send(new MyOnlineRoomsRequest(username));

		// waiting for a response
		while (true) {
			try {
				return myOnlineRoomsQueue.take().myRooms;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @return all rooms that have clients currently online, i.e., logged in
	 */
	public List<String> getAllRooms() {
		send(new AllRoomsRequest(username));

		// waiting for a response
		while (true) {
			try {
				return allRoomsQueue.take().allRooms;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
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
		send(new ClientsInRoomRequest(username, room));

		// waiting for a response
		while (true) {
			try {
				List<String> clients = clientsInRoomQueue.take().clientsInRoom;
				if (clients.size() == 0) {
					throw new NoSuchRoomException();
				}
				return clients;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
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
		send(new LeaveRoomRequest(username, room));

		// waiting for a response
		while (true) {
			try {
				if (leaveRoomQueue.take().error == ErrorCode.NotInRoomException)
					throw new NotInRoomException();
				return;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

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
		send(new JoinRoomRequest(username, room));

		// waiting for a response
		while (true) {
			try {
				if (joinRoomQueue.take().error == ErrorCode.AlreadyInRoomException)
					throw new AlreadyInRoomException();
				return;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Logs the user out of chat application. A logged out client cannot perform
	 * any tasks other than logging in. A logged out message will be sent to all
	 * the <i>other</i> clients in rooms with the client.
	 */
	public void logout() {
		stop();
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
		send(new OurChatMessage(username, room, what));

		// waiting for a response
		while (true) {
			try {
				if (chatMessaeReplyQueue.take().error == ErrorCode.NotInRoomException)
					throw new NotInRoomException();
				return;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
