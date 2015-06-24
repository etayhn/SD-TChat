package il.ac.technion.cs.sd.app.msg;

import java.util.ArrayList;
import java.util.List;

/**
 * A class that saves a client's data on the server.
 */
public class ClientData {

	/**
	 * the list of friends of the client
	 */
	private List<String> friends;

	/**
	 * The messages that were yet to be sent to the client (since he is offline)
	 */
	private List<IMessage> unsentMessages;

	/**
	 * true iff the client is online, i.e. logged to the server
	 */
	private boolean isOnline;

	public ClientData() {
		friends = new ArrayList<>();
		unsentMessages = new ArrayList<>();
		isOnline = false;
	}

	/**
	 * @param name
	 *            the name of the (possible) friend
	 * @return true iff "name" and this are friends
	 */
	public boolean isFriendsWith(String name) {
		return friends.contains(name);
	}

	/**
	 * Adds a given message to the unsent queue (is called each time the server
	 * wants to send something to a client that's not logged in.
	 * 
	 * @param message
	 *            the message to add to the unsent messages queue
	 */
	public void addMessageToUnsentQueue(IMessage message) {
		unsentMessages.add(message);
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
	 * Adds "friend" to the client's list of friends
	 * 
	 * @param friend
	 *            the friend to add
	 */
	public void addFriend(String friend) {
		if (!friends.contains(friend))
			friends.add(friend);
	}

	/**
	 * Retrieves all of the unsent messages that the server has stored for the
	 * client when he wasn't logged in. After calling this function, there are
	 * no longer any unsent messages, i.e. Calling getUnsentMessages() twice
	 * would <i>always</i> result in the second call returning an empty list.
	 * 
	 * @return the list of all unsent messages
	 */
	public List<IMessage> getUnsentMessages() {
		List<IMessage> tmp = unsentMessages;
		unsentMessages = new ArrayList<>();
		return tmp;
	}

}
