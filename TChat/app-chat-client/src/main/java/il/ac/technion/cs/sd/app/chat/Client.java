package il.ac.technion.cs.sd.app.chat;

import il.ac.technion.cs.sd.app.chat.OurChatMessage;
import il.ac.technion.cs.sd.app.chat.FriendReplyMessage;
import il.ac.technion.cs.sd.app.chat.FriendRequestMessage;
import il.ac.technion.cs.sd.app.chat.IMessage;
import il.ac.technion.cs.sd.app.chat.IMessageHandler;
import il.ac.technion.cs.sd.app.chat.InstantMessage;
import il.ac.technion.cs.sd.app.chat.LoginReplyMessage;
import il.ac.technion.cs.sd.app.chat.LoginRequestMessage;
import il.ac.technion.cs.sd.app.chat.LogoutReplyMessage;
import il.ac.technion.cs.sd.app.chat.LogoutRequestMessage;
import il.ac.technion.cs.sd.app.chat.OnlineCheckReplyMessage;
import il.ac.technion.cs.sd.app.chat.OnlineCheckRequestMessage;
import il.ac.technion.cs.sd.lib.client.communication.ClientCommunicator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

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
	public final List<String> myRooms;

	/**
	 * The ClientCommunicator with which the client and the server speak
	 */
	private ClientCommunicator communicator;

	/**
	 * A handler that needs to be run when a "regular" (common) instant message
	 * is sent
	 */
	private final Consumer<ChatMessage> messageConsumer;

	/**
	 * A function that defines how the client responds to friend requests from
	 * other clients.
	 */
	private final Function<String, Boolean> friendshipRequestHandler;

	/**
	 * a handler that accepts another client's response to a friend request and
	 * processes it.
	 */
	private final BiConsumer<String, Boolean> friendshipReplyConsumer;

	/**
	 * A blocking queue we use to wait for a response from the server when it
	 * needs to send us the unread messages list.
	 */
	private BlockingQueue<List<IMessage>> unreadMessagesQueue;

	/**
	 * A blocking queue we use to wait for a response from the server when it
	 * needs to send us a response on an <i>isOnline</i> query
	 */
	private BlockingQueue<OnlineCheckReplyMessage> isOnlineQueue;

	/**
	 * A blocking queue we use to wait for a response from the server when it
	 * needs to send a confirmation that we can safely log out and close our
	 * communicator.
	 */
	private BlockingQueue<LogoutReplyMessage> logoutQueue;

	private Consumer<RoomAnnouncement> announcementConsumer;

	/**
	 * Creates a new client, starts the connection with the server, and
	 * retrieves all of the unread messages that the client got when he was not
	 * logged in.
	 * 
	 * @param myAddress
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
		this.myAddress = myAddress;
		this.serverAddress = serverAddress;
		this.messageConsumer = messageConsumer;
		this.announcementConsumer = announcementConsumer;

		this.unreadMessagesQueue = new LinkedBlockingDeque<>();
		this.isOnlineQueue = new LinkedBlockingDeque<>();
		this.logoutQueue = new LinkedBlockingDeque<>();

		communicator = new ClientCommunicator(myAddress, serverAddress,
				new Consumer<Object>() {

					@Override
					public void accept(Object o) {
						((IMessage) o).handle(Client.this);
					}

				});

		getUnreadMessages();
	}

	/**
	 * An auxiliary function used to retrieve the messages that were sent to the
	 * client when he was not logged in.
	 */
	private void getUnreadMessages() {
		// sending a request
		send(new LoginRequestMessage(myAddress));

		// waiting for a response with all the unread mail to come
		List<IMessage> unreadMessages;
		while (true) {
			try {
				unreadMessages = unreadMessagesQueue.take();
				break;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// processing all the unread mail the server sent
		unreadMessages.forEach(m -> m.handle(this));
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

		// waiting for a response
		while (true) {
			try {
				logoutQueue.take();
				break;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		communicator.stop();
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
	public void handle(FriendReplyMessage message) {
		friendshipReplyConsumer.accept(message.from, message.answer);
	}

	@Override
	public void handle(FriendRequestMessage message) {
		send(new FriendReplyMessage(myAddress, message.from,
				friendshipRequestHandler.apply(message.from)));
	}

	@Override
	public void handle(OnlineCheckReplyMessage message) {
		try {
			isOnlineQueue.put(message);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void handle(LoginReplyMessage message) {
		try {
			unreadMessagesQueue.put(message.unsentMessages);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void handle(OurChatMessage message) {
		messageConsumer.accept(new InstantMessage(message.from, message.to,
				message.content));
	}

	public Optional<Boolean> askIfOnline(String who) {
		// sending a request
		send(new OnlineCheckRequestMessage(myAddress, who));

		// waiting for a response
		while (true) {
			try {
				return isOnlineQueue.take().answer;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void handle(LogoutReplyMessage message) {
		try {
			logoutQueue.put(message);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean isInRoom(String room) {
		return myRooms.contains(room);
	}

}
