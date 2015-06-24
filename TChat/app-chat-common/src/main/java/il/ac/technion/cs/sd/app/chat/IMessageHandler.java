package il.ac.technion.cs.sd.app.msg;

/**
 * This interface, along with IMessage, are used to implement the visitor design
 * pattern. We use it in order to define different behavior of the handler to
 * each type of message.
 */
public interface IMessageHandler {

	/**
	 * Defines behavior for a FriendReplyMessage. Default implementation is:
	 * <i>throw new UnsupportedOperationException();</i>
	 * 
	 * @param messageHandler
	 *            the message to handle
	 */
	default public void handle(FriendReplyMessage message) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Defines behavior for a FriendRequestMessage. Default implementation is:
	 * <i>throw new UnsupportedOperationException();</i>
	 * 
	 * @param messageHandler
	 *            the message to handle
	 */
	default public void handle(FriendRequestMessage message) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Defines behavior for a LoginReplyMessage. Default implementation is:
	 * <i>throw new UnsupportedOperationException();</i>
	 * 
	 * @param messageHandler
	 *            the message to handle
	 */
	default public void handle(LoginReplyMessage message) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Defines behavior for a LoginRequestMessage. Default implementation is:
	 * <i>throw new UnsupportedOperationException();</i>
	 * 
	 * @param messageHandler
	 *            the message to handle
	 */
	default public void handle(LoginRequestMessage message) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Defines behavior for a OnlineCheckRequestMessage. Default implementation
	 * is: throw new UnsupportedOperationException();
	 * 
	 * @param messageHandler
	 *            the message to handle
	 */
	default public void handle(OnlineCheckRequestMessage message) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Defines behavior for a OnlineCheckReplyMessage. Default implementation
	 * is: throw new UnsupportedOperationException();
	 * 
	 * @param messageHandler
	 *            the message to handle
	 */
	default public void handle(OnlineCheckReplyMessage message) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Defines behavior for a CommonInstantMessage. Default implementation is:
	 * <i>throw new UnsupportedOperationException();</i>
	 * 
	 * @param messageHandler
	 *            the message to handle
	 */
	default public void handle(CommonInstantMessage message) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Defines behavior for a LogoutRequestMessage. Default implementation is:
	 * <i>throw new UnsupportedOperationException();</i>
	 * 
	 * @param messageHandler
	 *            the message to handle
	 */
	default public void handle(LogoutRequestMessage message) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Defines behavior for a LogoutReplyMessage. Default implementation is:
	 * <i>throw new UnsupportedOperationException();</i>
	 * 
	 * @param messageHandler
	 *            the message to handle
	 */
	default public void handle(LogoutReplyMessage message) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Defines behavior for a IMessage. Default implementation is: <i>throw new
	 * UnsupportedOperationException();</i> Good practice would be
	 * <i><strong>not</strong></i> to override this method.
	 * 
	 * @param messageHandler
	 *            the message to handle
	 */
	default public void handle(IMessage message) {
		throw new UnsupportedOperationException();
	}
}
