package il.ac.technion.cs.sd.app.chat;


/**
 * The server side of the TMail application. <br>
 * This class is mainly used in our tests to start, stop, and clean the server
 */
public class ServerChatApplication {
	
	private Server server;
	
	private String myAddress;
	
    /**
     * Starts a new mail server. Servers with the same name retain all their information until
     * {@link ServerChatApplication#clean()} is called.
     *
     * @param myAddress The name of the server by which it is known.
     */

	public ServerChatApplication(String myAddress) {
		this.myAddress = myAddress;
		server = new Server(myAddress);
	}
	
	/**
	 * @return the server's address; this address will be used by clients connecting to the server
	 */
	public String getAddress() {
		return myAddress;
	}
	
	/**
	 * Starts the server; any previously sent mails, data and indices are loaded.
	 * This should be a <b>non-blocking</b> call.
	 */
	public void start() {
		server.start();
	}
	
	/**
	 * Stops the server. A stopped server can't accept messages, but doesn't delete any data (messages that weren't received).
	 */
	public void stop() {

		server.stop();
		server.saveData();
	}
	
	/**
	 * Deletes <b>all</b> previously saved data. This method will be used between tests to assure that each test will
	 * run on a new, clean server. you may assume the server is stopped before this method is called.
	 */
	public void clean() {
		server.removeData();
	}
}
