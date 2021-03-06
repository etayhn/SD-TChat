package il.ac.technion.cs.sd.app.chat;

/**
 * This class represents a regular message, sent from one client to another. The
 * reason we do not use InstantMessage is that we want it to implement IMessage
 * (in order to work with the Visitor Design Pattern), and we weren't sure if we
 * are allowed to do that.
 */
public class OurChatMessage implements IMessage {
	/**
	 * The sender of the message
	 */
	public final String who;
	
	/**
	 * The recipient of the message
	 */
	public final String room;
	
	/**
	 * The content of the message.
	 */
	public final String content;


	public OurChatMessage(String from, String room, String content) {
		this.who = from;
		this.room = room;
		this.content = content;
	}

	public OurChatMessage(ChatMessage chatMessage) {
		this(chatMessage.from, chatMessage.room, chatMessage.content);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + ((who == null) ? 0 : who.hashCode());
		result = prime * result + ((room == null) ? 0 : room.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OurChatMessage other = (OurChatMessage) obj;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		if (who == null) {
			if (other.who != null)
				return false;
		} else if (!who.equals(other.who))
			return false;
		if (room == null) {
			if (other.room != null)
				return false;
		} else if (!room.equals(other.room))
			return false;
		return true;
	}

	@Override
	public void handle(IMessageHandler messageHandler) {
		messageHandler.handle(this);
	}

}
