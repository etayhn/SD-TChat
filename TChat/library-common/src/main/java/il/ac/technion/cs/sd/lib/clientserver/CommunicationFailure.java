package il.ac.technion.cs.sd.lib.clientserver;

public class CommunicationFailure extends RuntimeException {
	private static final long serialVersionUID = 4;
	
	public CommunicationFailure(String message) {
		super(message);
	}

}
