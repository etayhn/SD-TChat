package il.ac.technion.cs.sd.app.msg;

/**
 * This class represents a regular message, sent from one client to another. The
 * reason we do not use InstantMessage is that we want it to implement IMessage
 * (in order to work with the Visitor Design Pattern), and we weren't sure if we
 * are allowed to do that.
 */
public class CommonInstantMessage implements IMessage {
	/**
	 * The sender of the message
	 */
	public final String from;
	
	/**
	 * The recipient of the message
	 */
	public final String to;
	
	/**
	 * The content of the message.
	 */
	public final String content;

	public CommonInstantMessage(String from, String to, String content) {
		this.from = from;
		this.to = to;
		this.content = content;
	}
	
	public CommonInstantMessage(InstantMessage im) {
		this(im.from, im.to, im.content);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + ((to == null) ? 0 : to.hashCode());
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
		InstantMessage other = (InstantMessage) obj;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		if (from == null) {
			if (other.from != null)
				return false;
		} else if (!from.equals(other.from))
			return false;
		if (to == null) {
			if (other.to != null)
				return false;
		} else if (!to.equals(other.to))
			return false;
		return true;
	}

	@Override
	public void handle(IMessageHandler messageHandler) {
		messageHandler.handle(this);
	}

}
