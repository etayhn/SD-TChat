package il.ac.technion.cs.sd.app.msg;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.*;

import org.junit.*;

public class OurTMsgTest {
	private static final String AVNER = "Avner";
	private static final String ITAY = "Itay";
	private static final String SERVER_ADDRESS = "Server";
	private ServerMailApplication server = new ServerMailApplication(
			SERVER_ADDRESS);
	// all listened to incoming messages will be written here
	// a blocking queue is used to overcome threading issues
	private BlockingQueue<Boolean> itaysFriendshipReplies = new LinkedBlockingQueue<>();
	private BlockingQueue<InstantMessage> itaysMessages = new LinkedBlockingQueue<>();
	private BlockingQueue<Boolean> avnersFriendshipReplies = new LinkedBlockingQueue<>();
	private BlockingQueue<InstantMessage> avnersMessages = new LinkedBlockingQueue<>();

	private static final int DEFAULT_STRING_LENGTH = 10;
	private final Random random = new Random();

	private static String generateRandomString(int length) {
		if (length <= 0)
			return "";
		Random randomGenerator = new Random();
		char[] buffer = new char[length];
		int numLettersInEnglish = 26;
		for (int i = 0; i < buffer.length; i++) {
			buffer[i] = (char) (randomGenerator.nextInt(numLettersInEnglish) + 'a');
		}
		return new String(buffer);
	}

	private static String generateRandomString() {
		return generateRandomString(DEFAULT_STRING_LENGTH);
	}

	private ClientMsgApplication buildClient(String login) {
		return new ClientMsgApplication(server.getAddress(), login);
	}

	@Before
	public void setUp() {
		server.start(); // non-blocking
	}

	@After
	public void teardown() {
		server.stop();
		server.clean();
	}

	@Test(timeout = 10000)
	public void basicTest() throws Exception {
		ClientMsgApplication itay = buildClient(ITAY);
		itay.login(x -> {
		}, x -> true, (x, y) -> itaysFriendshipReplies.add(y));
		assertEquals(Optional.empty(), itay.isOnline(AVNER)); // Itay isn't a
																// friend
		itay.sendMessage(AVNER, "Hi");
		ClientMsgApplication avner = buildClient(AVNER);
		avner.login(x -> avnersMessages.add(x), x -> true, (x, y) -> {
		});
		assertEquals(avnersMessages.take(), new InstantMessage(ITAY, AVNER,
				"Hi")); // Itay received the message as soon as he logged in
		itay.requestFriendship(AVNER);
		assertEquals(true, itaysFriendshipReplies.take()); // itay auto replies
															// yes
		assertEquals(Optional.of(true), itay.isOnline(AVNER)); // itay is a
																// friend and is
																// online
		avner.logout();
		assertEquals(Optional.of(false), itay.isOnline(AVNER)); // itay is a
																// friend and is
																// offline
		itay.logout();
	}

	@Test(timeout = 10000)
	public void clientShouldBeAbleToLoginAfterLogout() throws Exception {
		ClientMsgApplication itay = buildClient(ITAY);
		itay.login(x -> itaysMessages.add(x), x -> true,
				(x, y) -> itaysFriendshipReplies.add(y));
		itay.logout();

		// should succeed
		itay.login(x -> itaysMessages.add(x), x -> true,
				(x, y) -> itaysFriendshipReplies.add(y));
		itay.logout();
	}

	@Test(timeout = 10000)
	public void serverShouldBeAbleToStartAfterStop() throws Exception {
		server.stop();
		server.start();
	}

	@Test(timeout = 10000)
	public void serversGetAddressShouldReturnItsAddress() throws Exception {
		assertEquals(SERVER_ADDRESS, server.getAddress());

		// different server should return different address
		ServerMailApplication secondServer = new ServerMailApplication("Second");
		assertEquals("Second", secondServer.getAddress());
		secondServer.stop();
		secondServer.clean();
	}

	@Test(timeout = 10000)
	public void sendingAnInstantMessageToANonExistingClientShouldSucceed()
			throws Exception {
		ClientMsgApplication itay = buildClient(ITAY);
		itay.login(x -> itaysMessages.add(x), x -> true,
				(x, y) -> itaysFriendshipReplies.add(y));

		// should succeed
		itay.sendMessage(AVNER, "Hi");

		itay.logout();
	}

	@Test(timeout = 10000)
	public void sendingAMessageBetweenOnlineUsersShouldSucceed()
			throws Exception {
		ClientMsgApplication itay = buildClient(ITAY);
		itay.login(x -> itaysMessages.add(x), x -> true,
				(x, y) -> itaysFriendshipReplies.add(y));
		ClientMsgApplication avner = buildClient(AVNER);
		avner.login(x -> avnersMessages.add(x), x -> true,
				(x, y) -> avnersFriendshipReplies.add(y));

		itay.sendMessage(AVNER, "Hi");
		assertEquals(new InstantMessage(ITAY, AVNER, "Hi"),
				avnersMessages.take());

		itay.logout();
		avner.logout();
	}

	@Test(timeout = 10000)
	public void clientShouldGetUnerceivedMailOnLogin() throws Exception {
		ClientMsgApplication itay = buildClient(ITAY);
		itay.login(x -> itaysMessages.add(x), x -> true,
				(x, y) -> itaysFriendshipReplies.add(y));

		itay.sendMessage(AVNER, "Hi"); // waits on server

		ClientMsgApplication avner = buildClient(AVNER);

		avner.login(x -> avnersMessages.add(x), x -> true,
				(x, y) -> avnersFriendshipReplies.add(y)); // supposed to get
															// mail here
		assertEquals(new InstantMessage(ITAY, AVNER, "Hi"),
				avnersMessages.take());

		itay.logout();
		avner.logout();
	}

	@Test(timeout = 10000)
	public void sendingAFriendRequestToANonExistingClientShouldSucceed()
			throws Exception {
		ClientMsgApplication itay = buildClient(ITAY);
		itay.login(x -> itaysMessages.add(x), x -> true,
				(x, y) -> itaysFriendshipReplies.add(y));

		// should succeed
		itay.requestFriendship(AVNER);

		itay.logout();
	}

	@Test(timeout = 10000)
	public void askingServerIfANonExistingClientIsOnlineShouldReturnNull()
			throws Exception {
		ClientMsgApplication itay = buildClient(ITAY);
		itay.login(x -> itaysMessages.add(x), x -> true,
				(x, y) -> itaysFriendshipReplies.add(y));

		// Avner doesn't exists, which obviously means we are not friends
		assertEquals(Optional.empty(), itay.isOnline(AVNER));

		itay.logout();
	}

	@Test(timeout = 10000)
	public void odedShouldRefuseAFriendshipRequestFromItayOnly()
			throws Exception {
		ClientMsgApplication itay = buildClient(ITAY);
		itay.login(x -> itaysMessages.add(x), x -> true,
				(x, y) -> itaysFriendshipReplies.add(y));
		ClientMsgApplication avner = buildClient(AVNER);
		avner.login(x -> avnersMessages.add(x), x -> true,
				(x, y) -> avnersFriendshipReplies.add(y));
		ClientMsgApplication oded = buildClient("Oded");
		oded.login(x -> {
		}, x -> x.equals(ITAY) ? false : true, (x, y) -> {
		});

		itay.requestFriendship("Oded");
		assertFalse(itaysFriendshipReplies.take());
		avner.requestFriendship("Oded");
		assertTrue(avnersFriendshipReplies.take());

		avner.logout();
		itay.logout();
		oded.logout();
	}

	@Test(timeout = 10000)
	public void odedShouldAcceptAFriendshipRequestFromItayOnly()
			throws Exception {
		ClientMsgApplication itay = buildClient(ITAY);
		itay.login(x -> itaysMessages.add(x), x -> true,
				(x, y) -> itaysFriendshipReplies.add(y));
		ClientMsgApplication avner = buildClient(AVNER);
		avner.login(x -> avnersMessages.add(x), x -> true,
				(x, y) -> avnersFriendshipReplies.add(y));
		ClientMsgApplication oded = buildClient("Oded");
		oded.login(x -> {
		}, x -> x.equals(ITAY) ? true : false, (x, y) -> {
		});

		itay.requestFriendship("Oded");
		assertTrue(itaysFriendshipReplies.take());
		avner.requestFriendship("Oded");
		assertFalse(avnersFriendshipReplies.take());

		avner.logout();
		itay.logout();
		oded.logout();
	}

	@Test(timeout = 10000)
	public void aClientThatLogsIntoAServerThatWasRestartedShouldReceiveWaitingCommonInstantMessages()
			throws Exception {
		ClientMsgApplication itay = buildClient(ITAY);
		itay.login(x -> itaysMessages.add(x), x -> true,
				(x, y) -> itaysFriendshipReplies.add(y));

		// this message should remain in the server since Avner is not logged in
		itay.sendMessage(AVNER, "Hi");

		itay.logout();
		server.stop(); // should backup
		server.start(); // should restore backup

		ClientMsgApplication avner = buildClient(AVNER);
		avner.login(x -> avnersMessages.add(x), x -> true,
				(x, y) -> avnersFriendshipReplies.add(y));

		// Avner should receive the message as soon as he logs in
		assertEquals(new InstantMessage(ITAY, AVNER, "Hi"),
				avnersMessages.take());

		avner.logout();
	}

	@Test(timeout = 10000)
	public void aClientThatLogsIntoAServerThatWasRestartedShouldReceiveWaitingFriendshipRequests()
			throws Exception {
		ClientMsgApplication itay = buildClient(ITAY);
		itay.login(x -> itaysMessages.add(x), x -> true,
				(x, y) -> itaysFriendshipReplies.add(y));

		// this message should remain in the server since Avner is not logged in
		itay.requestFriendship(AVNER);

		itay.logout();
		server.stop(); // should backup
		server.start(); // should restore backup

		ClientMsgApplication avner = buildClient(AVNER);
		avner.login(x -> avnersMessages.add(x), x -> true,
				(x, y) -> avnersFriendshipReplies.add(y));

		itay.login(x -> itaysMessages.add(x), x -> true,
				(x, y) -> itaysFriendshipReplies.add(y));

		// Avner should receive the message as soon as he logs in
		assertTrue(itaysFriendshipReplies.take());

		itay.logout();
		avner.logout();
	}

	@Test(timeout = 10000)
	public void aClientThatLogsIntoAServerThatWasRestartedShouldReceiveWaitingAllMessagesFromAllTypes()
			throws Exception {
		ClientMsgApplication itay = buildClient(ITAY);
		itay.login(x -> itaysMessages.add(x), x -> true,
				(x, y) -> itaysFriendshipReplies.add(y));

		// this message should remain in the server since Avner is not logged in
		itay.sendMessage(AVNER, "Hi");
		itay.requestFriendship(AVNER);

		itay.logout();
		server.stop(); // should backup
		server.start(); // should restore backup

		ClientMsgApplication avner = buildClient(AVNER);
		avner.login(x -> avnersMessages.add(x), x -> true,
				(x, y) -> avnersFriendshipReplies.add(y));

		itay.login(x -> itaysMessages.add(x), x -> true,
				(x, y) -> itaysFriendshipReplies.add(y));

		// Avner should receive these message as soon as he logs in
		assertEquals(new InstantMessage(ITAY, AVNER, "Hi"),
				avnersMessages.take());
		assertTrue(itaysFriendshipReplies.take());

		itay.logout();
		avner.logout();
	}

	@Test(timeout = 10000)
	public void checkingIfAStrangerIsOnlineShouldReturnNull() throws Exception {
		ClientMsgApplication itay = buildClient(ITAY);
		itay.login(x -> itaysMessages.add(x), x -> true,
				(x, y) -> itaysFriendshipReplies.add(y));
		ClientMsgApplication avner = buildClient(AVNER);
		avner.login(x -> avnersMessages.add(x), x -> true,
				(x, y) -> avnersFriendshipReplies.add(y));

		assertEquals(Optional.empty(), itay.isOnline(AVNER));

		itay.logout();
		avner.logout();
	}

	@Test(timeout = 10000)
	public void checkingIfAnOnlineFriendIsOnlineShouldReturnTrue()
			throws Exception {
		ClientMsgApplication itay = buildClient(ITAY);
		itay.login(x -> itaysMessages.add(x), x -> true,
				(x, y) -> itaysFriendshipReplies.add(y));
		ClientMsgApplication avner = buildClient(AVNER);
		avner.login(x -> avnersMessages.add(x), x -> true,
				(x, y) -> avnersFriendshipReplies.add(y));

		itay.requestFriendship(AVNER);
		assertTrue(itaysFriendshipReplies.take());

		// avner (who is a friend) is online now, so it should return true
		assertEquals(Optional.of(true), itay.isOnline(AVNER));

		itay.logout();
		avner.logout();
	}

	@Test(timeout = 10000)
	public void checkingIfAnOfflineFriendIsOnlineShouldReturnFalse()
			throws Exception {
		ClientMsgApplication itay = buildClient(ITAY);
		itay.login(x -> itaysMessages.add(x), x -> true,
				(x, y) -> itaysFriendshipReplies.add(y));
		ClientMsgApplication avner = buildClient(AVNER);
		avner.login(x -> avnersMessages.add(x), x -> true,
				(x, y) -> avnersFriendshipReplies.add(y));

		itay.requestFriendship(AVNER);
		assertTrue(itaysFriendshipReplies.take());
		avner.logout();

		// avner (who is a friend) isn't online now, so it should return false
		assertEquals(Optional.of(false), itay.isOnline(AVNER));

		itay.logout();
	}

	@Test(timeout = 50000)
	public void loggingInAgainShouldSetTheConsumersToBeTheMostUpdatedOnes()
			throws Exception {
		ClientMsgApplication itay = buildClient(ITAY);
		itay.login(x -> itaysMessages.add(x), x -> true,
				(x, y) -> itaysFriendshipReplies.add(y));
		ClientMsgApplication avner = buildClient(AVNER);
		avner.login(x -> avnersMessages.add(x), x -> true,
				(x, y) -> avnersFriendshipReplies.add(y));
		ClientMsgApplication oded = buildClient("Oded");
		oded.login(x -> {}, x -> true, (x, y) -> {});

		avner.requestFriendship("Oded");
		assertTrue(avnersFriendshipReplies.take()); // oded should approve
		
		// oded changes his responses. now he rejects all friend requests 
		oded.logout();
		oded.login(x -> {}, x -> false, (x, y) -> {});
		
		avner.requestFriendship("Oded");
		assertFalse(avnersFriendshipReplies.take()); // oded should approve
		
		itay.logout();
		avner.logout();
		oded.logout();
	}

	class ClientWrapper {
		static final int NUM_MESSAGES = 30;
		String name;
		ClientMsgApplication client;
		BlockingQueue<InstantMessage> messages;
		BlockingQueue<Boolean> friendshipReplies;
		Thread thread;

		public ClientWrapper() {
			name = generateRandomString(); // the probability to get a name
											// twice is practically zero..
			messages = new LinkedBlockingQueue<>();
			friendshipReplies = new LinkedBlockingQueue<>();
			client = buildClient(name);
			client.login(x -> messages.add(x), x -> true,
					(x, y) -> friendshipReplies.add(y));
		}

		public void createThread(ArrayList<ClientWrapper> clients) {
			thread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						for (int i = 0; i < NUM_MESSAGES; i++) {
							ClientWrapper recipient = clients.get(random
									.nextInt(clients.size()));
							ClientWrapper.this.client.sendMessage(
									recipient.name, ClientWrapper.this.name
											+ ": Hi_" + i);
							assertEquals(new InstantMessage(
									ClientWrapper.this.name, recipient.name,
									ClientWrapper.this.name + ": Hi_" + i),
									recipient.messages.take());
						}
					} catch (InterruptedException e) {
					}
				}
			});
			thread.start();
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	public void checkSendingMessagesBetweenManyUsersSimultaniously(
			int numClients) throws Exception {

		ArrayList<ClientWrapper> clients = new ArrayList<>();

		for (int i = 0; i < numClients; ++i)
			clients.add(new ClientWrapper());

		clients.forEach(x -> x.createThread(clients));
		clients.forEach(x -> x.client.logout());
	}

	@Test(timeout = 50000)
	public void twoUsersShouldBeAbleToSendEachOtherMessagesSimultaniously()
			throws Exception {
		checkSendingMessagesBetweenManyUsersSimultaniously(2);
	}

	@Test(timeout = 50000)
	public void fiveUsersShouldBeAbleToSendEachOtherMessagesSimultaniously()
			throws Exception {
		checkSendingMessagesBetweenManyUsersSimultaniously(5);
	}

	@Test(timeout = 50000)
	public void tenUsersShouldBeAbleToSendEachOtherMessagesSimultaniously()
			throws Exception {
		checkSendingMessagesBetweenManyUsersSimultaniously(10);
	}

}
