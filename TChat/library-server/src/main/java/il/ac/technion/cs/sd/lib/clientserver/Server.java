package il.ac.technion.cs.sd.lib.clientserver;

import il.ac.technion.cs.sd.msg.MessengerException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.apache.commons.io.FileUtils;


/**
 * Represents a server that can communicate (reliably) with multiple clients, and save/load 
 * persistent data.
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

public class Server {

	private ReliableHost _reliableHost;
	
	
	public String getAddress() {
		return _reliableHost.getAddress();
	}


	/**
	 * Creates a new server. 
	 * A call to {@link #start(BiConsumer, Type)} must be made before sending/receiving any 
	 * messages with this server.
	 * @param address - the address of the new server. 
	 */
	public Server(String address)
	{
		_reliableHost = new ReliableHost(address);
	}


	/**
	 * Starts listening for incoming messages from clients. You can't use the server for any
	 * communication before starting it. 
	 * Each incoming message from a client invokes a provided callback function.
	 * @param consumer The consumer who's callback will be invoked for each message received from a client.
	 * The first argument it takes is the data sent, and the second argument is the sender's address.
	 * From within the callback function, you can synchronically send a response back to the client that invoked it
	 * via {@link #send(String, Object, boolean)} (the client is blocking until a response
	 * from the server arrives). 
	 * While the consumer's callback is running - the listen loop is frozen, so the code in the 
	 * callback shouldn't wait for a new messages to arrive at the server. 
	 * @param dataType The type of the object sent by the client in each message
	 * (i.e., the type of the object passed to the consumer's callback function).
	 * e.g.:
	 * 		Integer.class
	 * If the type is generic, for example, a list of Integers, you should pass as 'dataType' 
	 * something created with the following pattern:
	 * 		{@code new TypeToken<List<Integer>>(){}.getType())}
	 * (sorry for that - it's a requirement by underlying GSON library).
	 * @throws InvalidMessage If the listen loop is already running. 
	 */
	public void start(BiConsumer<Object,String> consumer) {
		
		try {
			_reliableHost.start((fromAddress, data) -> {
				consumer.accept(Utils.fromXStreamerStrToObject(data), fromAddress);
			});
		} catch (MessengerException e) {
			System.out.println(e.getMessage());
			throw new InvalidOperation();
		}
		
	}

	

	/**
	 * Stop the server.
	 * Messages sent from clients will no longer be consumed, and you can no longer use the server for 
	 * any communication, until starting the server again.
	 *  {@link #saveObjectToFile(String, Object, boolean)} and {@link #readObjectFromFile(String, Type, boolean)} 
	 *  will re-open streams, so you'll start reading/writing from the begining of the files).
	 * @throws InvalidOperation When the listen loop was not running when calling this method.
	 * 
	 */
	public void stop()
	{
		_reliableHost.stop();
	}
	

	/**
	 * Sends a message to a client.
	 * @param clientAddress The address of the client.
	 * @param data The data to be sent to the client.
	 * @param isResponse true iff 'data' is a response to a message previously sent by the client.
	 * When true, you must call this method only from the consumer of the listen loop (i.e., from the 
	 * callback function invoked by the message to which the response is for).
	 * This synchronically delivers the message to the client which is currently blocking until the 
	 * response arrives.
	 * @throws InvalidOperation Bad clientAddress address etc..
	 */
	public void send(String clientAddress, Object data, boolean isResponse)
	{
		try {
			_reliableHost.send(clientAddress, Utils.fromObjectToXStreamerStr(data), isResponse);
		} catch (MessengerException e) {
			throw new CommunicationFailure(e.getMessage());
		}
	}
	
	
	/**
	 * Clears all persistent data saved by this server.
	 */
	public void clearPersistentData()
	{
		File persistentDataDir = getServerPersistentDir();
		if (!persistentDataDir.exists())
		{
			return;
		}
		try {
			if (!persistentDataDir.exists())
				return;
				FileUtils.cleanDirectory(getServerPersistentDir());
				FileUtils.deleteDirectory(getServerPersistentDir());
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	
	/**
	 * Saves a an object to persistent memory (file).
	 * @param filename The filename, without path, of the file to save 'data' into.
	 * This file will hold a single object ('data'). Previous content, if the file already exists,
	 * will be lost.
	 * @param data The object to be saved to the file.  
	 */
	public void saveObjectToFile(String filename, Object data)
	{
		File allServersDir = getPesistentDirOfAllServers();
		if(!allServersDir.exists()){
			allServersDir.mkdir();
		}
		File dir = getServerPersistentDir();
		if(!dir.exists()){
			dir.mkdir();
		}
		try {
			Utils.writeToFile(data, getServerPersistentDir().getAbsolutePath() + "/" +filename);
		} catch (IOException e) {			
			throw new RuntimeException("Failed to write file: " + e.getMessage());
		}
	}
	 
	
	/**
	 * Reads an object from persistent memory (file).
	 * @param filename The filename of the file to read, without path. This file cotains a single object.
	 * @return The object read, or empty if the file doesn't exist.
	 * @throws BadFileContent If an unexpected file content was read.
	 */
	public Optional<Object> readObjectFromFile(String filename)
	{
		Optional<Object> $;
		
		try {
			$ = Optional.of(Utils.readFromFile(getServerPersistentDir().getAbsolutePath()+ "/"  + filename));
		} catch (IOException e) {
			$=Optional.empty();
		}
		return $;
	}
	
	// returns the directory holding the persistent files of the server.
	private File getServerPersistentDir() {
		return new File(getPesistentDirOfAllServers(), getServerPersistentDirName());
	}
	
	
	// returns the unique name (wihtout path) of the directory holding the persistent files of the server. 
	private String getServerPersistentDirName()
	{
		return Integer.toString(getAddress().hashCode());
	}
	
	private static File getPesistentDirOfAllServers()
	{
		return new File("./TMP___ServersData");
	}
	
	@SuppressWarnings("serial")
	public class BadFileContent extends RuntimeException {}
	
}
