package il.ac.technion.cs.sd.app.msg;

/**
 * This interface, along with IMessageHandler, are used to implement the visitor
 * design pattern. We use it in order to define different behaviour of the
 * handler to each type of message.
 */
public interface IMessage {

	/**
	 * The handle function used for the visitor Design Pattern. Every class that
	 * implements IMessage must override this method as follows:
	 * messageHandler.handle(this);
	 * 
	 * @param messageHandler
	 *            the message handler that sets the behavior.
	 */
	public void handle(IMessageHandler messageHandler);

}
