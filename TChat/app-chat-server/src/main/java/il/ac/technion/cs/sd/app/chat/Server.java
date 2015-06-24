package il.ac.technion.cs.sd.app.msg;

import il.ac.technion.cs.sd.lib.server.communication.ServerCommunicator;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

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

	/**
	 * The ServerCommunicator with which the server speaks with the clients
	 */
	private ServerCommunicator communicator;

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

		start();
	}

	/**
	 * Stops the server
	 */
	public void stop() {
		communicator.stop();
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
					// TODO Auto-generated method stub
					communicator.send(to, message);
					
				}
			}).start();
		} else {
			clientData.addMessageToUnsentQueue(message);
		}
	}

	@Override
	public void handle(FriendReplyMessage message) {
		clients.get(message.from).addFriend(message.to);
		clients.get(message.to).addFriend(message.from);

		send(message.to, message);
	}

	@Override
	public void handle(FriendRequestMessage message) {
		send(message.to, message);
	}

	@Override
	public void handle(LoginRequestMessage message) {
		ClientData clientData = clients.get(message.myAddress);
		if (clientData == null) {
			clientData = new ClientData();
			clients.put(message.myAddress, clientData);
		}
		clientData.setOnline(true);
		send(message.myAddress,
				new LoginReplyMessage(clientData.getUnsentMessages()));
	}

	@Override
	public void handle(OnlineCheckRequestMessage message) {
		ClientData clientData = clients.get(message.whoIsChecking);
		Optional<Boolean> response = null;
		if (!clientData.isFriendsWith(message.whoIsBeingChecked)) {
			response = Optional.empty();
		} else {
			response = Optional.of(clients.get(message.whoIsBeingChecked)
					.isOnline());
		}
		send(message.whoIsChecking, new OnlineCheckReplyMessage(
				message.whoIsBeingChecked, response));
	}

	@Override
	public void handle(CommonInstantMessage message) {
		send(message.to, message);
	}

	@Override
	public void handle(LogoutRequestMessage message) {
		send(message.myAddress, new LogoutReplyMessage());
		clients.get(message.myAddress).setOnline(false);
	}

	public void start() {
		communicator = new ServerCommunicator(myAddress,
				new Consumer<Object>() {

					@Override
					public void accept(Object o) {
						((IMessage) o).handle(Server.this);
					}

				});

	}

}
