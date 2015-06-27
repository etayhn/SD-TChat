package il.ac.technion.cs.sd.app.chat;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;

public class ClientDataTest {

	public ClientData clientData;
	
	@Before
	public void setUp() {
		clientData = new ClientData();
	}
	
//	@Test
//	public void isFriendsWithShouldFailIfNotFriends() {
//		assertFalse(clientData.isFriendsWith("Avner"));
//		assertFalse(clientData.isFriendsWith("Itay"));
//		assertFalse(clientData.isFriendsWith("Gal"));
//	}
//	
//	@Test
//	public void isFriendsWithShouldSuccessIfFriends() {
//		assertFalse(clientData.isFriendsWith("Avner"));
//		clientData.addFriend("Avner");
//		assertTrue(clientData.isFriendsWith("Avner"));
//	}
//	
//	@Test
//	public void addingAFriendShouldAddHimOnly() {
//		assertFalse(clientData.isFriendsWith("Avner"));
//		assertFalse(clientData.isFriendsWith("Itay"));
//		clientData.addFriend("Avner");
//		assertTrue(clientData.isFriendsWith("Avner"));
//		// Still not friends
//		assertFalse(clientData.isFriendsWith("Itay"));
//	}
//	
//	@Test
//	public void beforeAddingToUnsentMessagesItShouldBeEmpty() {
//		assertTrue(clientData.getUnsentMessages().isEmpty());
//	}
//	
//	@Test
//	public void aMessageThatWasAddedToUnsentMessagesShouldBeReturnedWithGetUnsentMessages() {
//		IMessage mockMessage = Mockito.mock(IMessage.class);
//		clientData.addMessageToUnsentQueue(mockMessage);
//		List<IMessage> messages = clientData.getUnsentMessages();
//		assertFalse(messages.isEmpty());
//		assertEquals(mockMessage, messages.get(0));	
//	}
//	
//	@Test
//	public void callingGetUnsentMessagesTwiceInARowShouldReturnAnEmptyListTheSecondTime() {
//		IMessage mockMessage = Mockito.mock(IMessage.class);
//		clientData.addMessageToUnsentQueue(mockMessage);
//		assertFalse(clientData.getUnsentMessages().isEmpty());
//		assertTrue(clientData.getUnsentMessages().isEmpty());
//	}
	
	@Test
	public void clientShouldStartOnline() {
		assertTrue(clientData.isOnline());
	}
	
	@Test
	public void setOnlineShouldInfluenceTheResultsOfGetOnline() {
		Random random = new Random();
		for (int i=0; i<300; i++) {
			if (random.nextInt() % 2 == 0) {
				clientData.setOnline(true);
				assertTrue(clientData.isOnline());
			} else {
				clientData.setOnline(false);
				assertFalse(clientData.isOnline());
			}
		}

	}
	
}
