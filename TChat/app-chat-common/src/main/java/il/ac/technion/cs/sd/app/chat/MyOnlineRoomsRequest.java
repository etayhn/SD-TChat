package il.ac.technion.cs.sd.app.chat;

public class MyOnlineRoomsRequest implements IMessage {

	public final String who;
	
	public MyOnlineRoomsRequest(String who) {
		this.who = who;
	}

	@Override
	public void handle(IMessageHandler messageHandler) {
		messageHandler.handle(this);
	}

}
