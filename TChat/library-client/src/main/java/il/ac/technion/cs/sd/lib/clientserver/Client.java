package il.ac.technion.cs.sd.lib.clientserver;

import il.ac.technion.cs.sd.msg.MessengerException;

import java.lang.reflect.Type;
import java.util.function.Consumer;

/**
 * Represents a client that can communicate (reliably) with a single server.
 * 
 * The server can send a message to a client either synchronically (while it is blocking until a 
 * response from the server is received), or asynchronously. 
 * 
 * Each message sent/received synchronously can consist of an any object type you chose.
 * All messages sent/received asynchronously consist of the same type of your choice.
 * 
 * This class is not thread-safe (meaning you must not access an object of this class from multiple 
 * threads simultaneously). 
 */
public class Client {

	private String _serverAddress;
	private ReliableHost _reliableHost;
	
	/**
	 * Creates a new client.
	 * A call to {@link #start(String, Consumer, Type)} must be made before sending/receiving any 
	 * messages with this client.
	 * @param address The address of the new client.
	 */
	public Client(String address)
	{
		_reliableHost = new ReliableHost(address);
	}
	
	/**
	 * returns the address of this client.
	 */
	public String getAddress()
	{
		return _reliableHost.getAddress();
	}
	
	
	/**
	 * Starts listening for incoming messages from the server. You can't use the client for any
	 * communication before starting it. 
	 * After the client is started, each message received from the server invokes the consumer's 
	 * callback function.
	 * @param serverAddress The server's address.
	 * @param consumer The consumer who's callback will be invoked for each message received from the server.
	 * While the consumer's callback is running - the listen loop is frozen, so the code in the 
	 * callback shouldn't wait for a new message to be received by this client. 

	 * If the type is generic, for example, a list of Integers, you should pass as 'dataType' 
	 * something created with the following pattern:
	 * {@code new TypeToken<List<Integer>>(){}.getType())}
	 * @throws InvalidMessage Invalid message received from the server 
	 * For example: the object sent as message data was not of type 'type'.
	 * @throws InvalidOperation When the listen loop is already running when calling this method.
	 */
	public void start(String serverAddress, Consumer<Object> consumer)
	{
		String originalServerAddress = _serverAddress;
		_serverAddress = serverAddress;
		
		try {
			_reliableHost.start((fromAddress,data) -> {
				consumer.accept(Utils.fromXStreamerStrToObject(data));
			});
		} catch (MessengerException e) {
			_serverAddress = originalServerAddress;
			throw new CommunicationFailure(e.getMessage());
		}
		
	}
	
	
	/**
	 * Stop the listen loop of the client (messages sent from server will no longer be consumed.
	 * @throws InvalidOperation When the listen loop was not running when calling this method.
	 */
	public void stopListenLoop()
	{
		_reliableHost.stop();
	}

	/**
	 * Sends a message to the server.
	 * @param data The object to be sent to the server (as message data).
	 * Parametric types of data are not supported.
	 */
	public void send(Object data) {
		try {
			String payload = Utils.fromObjectToXStreamerStr(data);
			_reliableHost.send(_serverAddress, payload, false);
		} catch (MessengerException e) {
			throw new InvalidOperation();
		} 
	}
	
	
	/**
	 * Sends a message to the server, and blocks until a response message is received.
	 * @param data The object to be sent to the server (as message data).
	 * 
	 * @return The response message data. 
	 * The response message is guaranteed to be the response to the message sent by this method
	 * (and not some other unrelated message the server sent the client).
	 * @throws InvalidMessage Invalid message was received back from the server. 
	 */
	public Object sendAndBlockUntilResponseArrives(Object data)
	{
		try {
			String str = _reliableHost.sendAndBlockUntilResponseArrives(
					_serverAddress, Utils.fromObjectToXStreamerStr(data));
			
			return Utils.fromXStreamerStrToObject(str);
		} catch (MessengerException e) {
			throw new InvalidOperation();
		}
		
	}
	
	@Override
	public String toString() {

		return _reliableHost.getAddress();
	}
	
}
