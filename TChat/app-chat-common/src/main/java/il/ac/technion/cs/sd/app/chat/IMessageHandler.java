package il.ac.technion.cs.sd.app.chat;

/**
 * This interface, along with IMessage, are used to implement the visitor design
 * pattern. We use it in order to define different behavior of the handler to
 * each type of message.
 */
public interface IMessageHandler {

	/**
	 * Defines behavior for a AllRoomsReply. Default implementation is: <i>throw
	 * new UnsupportedOperationException();</i>
	 * 
	 * @param messageHandler
	 *            the message to handle
	 */
	default public void handle(AllRoomsReply message) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Defines behavior for a AllRoomsRequest. Default implementation is:
	 * <i>throw new UnsupportedOperationException();</i>
	 * 
	 * @param messageHandler
	 *            the message to handle
	 */
	default public void handle(AllRoomsRequest message) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Defines behavior for a ClientsInRoomReply. Default implementation is:
	 * <i>throw new UnsupportedOperationException();</i>
	 * 
	 * @param messageHandler
	 *            the message to handle
	 */
	default public void handle(ClientsInRoomReply message) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Defines behavior for a ClientsInRoomReply. Default implementation is:
	 * <i>throw new UnsupportedOperationException();</i>
	 * 
	 * @param messageHandler
	 *            the message to handle
	 */
	default public void handle(ClientsInRoomRequest message) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Defines behavior for a ClientsInRoomReply. Default implementation is:
	 * <i>throw new UnsupportedOperationException();</i>
	 * 
	 * @param messageHandler
	 *            the message to handle
	 */
	default public void handle(JoinRoomRequest message) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Defines behavior for a ClientsInRoomReply. Default implementation is:
	 * <i>throw new UnsupportedOperationException();</i>
	 * 
	 * @param messageHandler
	 *            the message to handle
	 */
	default public void handle(LeaveRoomRequest message) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Defines behavior for a ClientsInRoomReply. Default implementation is:
	 * <i>throw new UnsupportedOperationException();</i>
	 * 
	 * @param messageHandler
	 *            the message to handle
	 */
	default public void handle(LoginRequestMessage message) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Defines behavior for a ClientsInRoomReply. Default implementation is:
	 * <i>throw new UnsupportedOperationException();</i>
	 * 
	 * @param messageHandler
	 *            the message to handle
	 */
	default public void handle(LogoutRequestMessage message) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Defines behavior for a ClientsInRoomReply. Default implementation is:
	 * <i>throw new UnsupportedOperationException();</i>
	 * 
	 * @param messageHandler
	 *            the message to handle
	 */
	default public void handle(MyOnlineRoomsReply message) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Defines behavior for a ClientsInRoomReply. Default implementation is:
	 * <i>throw new UnsupportedOperationException();</i>
	 * 
	 * @param messageHandler
	 *            the message to handle
	 */
	default public void handle(MyOnlineRoomsRequest message) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Defines behavior for a ClientsInRoomReply. Default implementation is:
	 * <i>throw new UnsupportedOperationException();</i>
	 * 
	 * @param messageHandler
	 *            the message to handle
	 */
	default public void handle(OurChatMessage message) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Defines behavior for a ClientsInRoomReply. Default implementation is:
	 * <i>throw new UnsupportedOperationException();</i>
	 * 
	 * @param messageHandler
	 *            the message to handle
	 */
	default public void handle(OurRoomAnnouncement message) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Defines behavior for a JoinRoomReply. Default implementation is: <i>throw
	 * new UnsupportedOperationException();</i>
	 * 
	 * @param messageHandler
	 *            the message to handle
	 */
	default public void handle(JoinRoomReply message) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Defines behavior for a LeaveRoomReply. Default implementation is:
	 * <i>throw new UnsupportedOperationException();</i>
	 * 
	 * @param messageHandler
	 *            the message to handle
	 */
	default public void handle(LeaveRoomReply message) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Defines behavior for a LeaveRoomReply. Default implementation is:
	 * <i>throw new UnsupportedOperationException();</i>
	 * 
	 * @param messageHandler
	 *            the message to handle
	 */
	default public void handle(OurChatMessageReply message) {
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
