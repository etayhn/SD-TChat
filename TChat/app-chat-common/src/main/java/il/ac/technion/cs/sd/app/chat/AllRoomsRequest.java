package il.ac.technion.cs.sd.app.chat;

public class AllRoomsRequest implements IMessage {

	public final String who;
	
	public AllRoomsRequest(String who) {
		this.who = who;
	}

	@Override
	public void handle(IMessageHandler messageHandler) {
		messageHandler.handle(this);
	}

}
