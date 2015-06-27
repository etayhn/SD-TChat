package il.ac.technion.cs.sd.app.chat;

import static org.junit.Assert.*;
import il.ac.technion.cs.sd.app.chat.RoomAnnouncement.Announcement;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.*;

import org.junit.*;
import org.junit.rules.ExpectedException;

public class OurTChatTest {
	private static final String AVNER = "Avner";
	private static final String ITAY = "Itay";
	private static final String ROOM = "room";
	private static final String SERVER_ADDRESS = "Server";

	private ServerChatApplication server = new ServerChatApplication(
			SERVER_ADDRESS);
	private Collection<ClientChatApplication> clients = new LinkedList<>();

	// all listened to incoming messages will be written here
	private Map<String, BlockingQueue<RoomAnnouncement>> announcements = new HashMap<>();
	private Map<String, BlockingQueue<ChatMessage>> messages = new HashMap<>();

	private ClientChatApplication loginUser(String name) {
		ClientChatApplication $ = new ClientChatApplication(
				server.getAddress(), name);
		announcements.put(name, new LinkedBlockingQueue<>());
		messages.put(name, new LinkedBlockingQueue<>());
		$.login(x -> messages.get(name).add(x), x -> announcements.get(name)
				.add(x));
		clients.add($);
		return $;
	}

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() {
		server.start(); // non-blocking
	}

	@After
	public void teardown() {
		clients.forEach(ClientChatApplication::logout);
		clients.forEach(ClientChatApplication::stop);
		server.stop();
		server.clean();
	}

	@Test(timeout = 10000)
	public void basicTest() throws Exception {
		ClientChatApplication itay = loginUser(ITAY);
		ClientChatApplication avner = loginUser(AVNER);
		itay.joinRoom(ROOM);
		avner.joinRoom(ROOM);
		assertEquals(announcements.get(ITAY).take(), new RoomAnnouncement(
				AVNER, ROOM, Announcement.JOIN));
		avner.sendMessage(ROOM, "Hi all");
		assertEquals(messages.get(ITAY).take(), new ChatMessage(AVNER, ROOM,
				"Hi all"));
		avner.logout();
		assertEquals(announcements.get(ITAY).take(), new RoomAnnouncement(
				AVNER, ROOM, Announcement.DISCONNECT));
		itay.logout();
	}

	@Test(timeout = 10000)
	public void clientShouldBeAbleToLoginAfterLogout() throws Exception {
		ClientChatApplication itay = loginUser(ITAY);
		itay.logout();

		// should succeed
		itay.login(x -> messages.get(ITAY).add(x), x -> announcements.get(ITAY)
				.add(x));
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
		ServerChatApplication secondServer = new ServerChatApplication("Second");
		assertEquals("Second", secondServer.getAddress());
		secondServer.start();
		assertEquals("Second", secondServer.getAddress());
		secondServer.stop();
		secondServer.clean();
	}

	@Test(timeout = 10000)
	public void sendingAChatMessageToANonExistingRoomShouldFail()
			throws Exception {
		ClientChatApplication itay = loginUser(ITAY);

		// should fail
		thrown.expect(NotInRoomException.class);
		itay.sendMessage(ROOM, "something");

		itay.logout();
	}

	@Test(timeout = 10000)
	public void rightAfterLoggingInTheClientShouldNotBeInAnyRoom()
			throws Exception {
		ClientChatApplication itay = loginUser(ITAY);

		assertTrue(itay.getAllRooms().isEmpty());

		itay.logout();
	}

	@Test(timeout = 10000)
	public void joiningARoomImNotInShouldSucceed() throws Exception {
		ClientChatApplication itay = loginUser(ITAY);

		// should succeed
		itay.joinRoom(ROOM);

		itay.logout();
	}

	@Test(timeout = 10000)
	public void afterJoiningARoomGetJoinedRoomsShouldContainIt()
			throws Exception {
		ClientChatApplication itay = loginUser(ITAY);
		itay.joinRoom(ROOM);

		assertEquals(Arrays.asList(ROOM), itay.getAllRooms());

		itay.logout();
	}

	@Test(timeout = 10000)
	public void leavingARoomImNotInShouldFail() throws Exception {
		ClientChatApplication itay = loginUser(ITAY);

		// should fail
		thrown.expect(NotInRoomException.class);
		itay.leaveRoom(ROOM);

		itay.logout();
	}

	@Test(timeout = 10000)
	public void leavingARoomImInShouldSucceed() throws Exception {
		ClientChatApplication itay = loginUser(ITAY);
		itay.joinRoom(ROOM);

		// should succeed
		itay.leaveRoom(ROOM);

		itay.logout();
	}

	@Test(timeout = 10000)
	public void afterLeavingARoomGetJoinedRoomsShouldNotContainIt()
			throws Exception {
		ClientChatApplication itay = loginUser(ITAY);
		itay.joinRoom(ROOM);
		itay.leaveRoom(ROOM);

		assertTrue(itay.getAllRooms().isEmpty());

		itay.logout();
	}

	@Test(timeout = 10000)
	public void joiningARoomImInShouldFail() throws Exception {
		ClientChatApplication itay = loginUser(ITAY);
		itay.joinRoom(ROOM);

		// should fail
		thrown.expect(AlreadyInRoomException.class);
		itay.joinRoom(ROOM);

		itay.logout();
	}

	@Test(timeout = 10000)
	public void sendingAChatMessageToARoomImNotInShouldFail() throws Exception {
		ClientChatApplication itay = loginUser(ITAY);

		// should fail
		thrown.expect(NotInRoomException.class);
		itay.sendMessage(ROOM, "something");

		itay.logout();
	}

	@Test(timeout = 10000)
	public void sendingAMessageToARoomImInShouldSucceed() throws Exception {
		ClientChatApplication itay = loginUser(ITAY);

		itay.joinRoom(ROOM);
		itay.sendMessage(ROOM, "something");

		itay.logout();
	}

	@Test(timeout = 10000)
	public void sendingAMessageToARoomImInShouldNotSendItBackToMe()
			throws Exception {
		ClientChatApplication itay = loginUser(ITAY);

		itay.joinRoom(ROOM);
		itay.sendMessage(ROOM, "something");

		Thread.sleep(1000);
		assertTrue(messages.get(ITAY).isEmpty());

		itay.logout();
	}

	@Test(timeout = 10000)
	public void otherClientsInTheRoomShouldGetMyMessage() throws Exception {
		ClientChatApplication itay = loginUser(ITAY);
		ClientChatApplication avner = loginUser(AVNER);

		itay.joinRoom(ROOM);
		avner.joinRoom(ROOM);

		itay.sendMessage(ROOM, "Hi");
		assertEquals(new ChatMessage(ITAY, ROOM, "Hi"), messages.get(AVNER)
				.take());

		itay.logout();
		avner.logout();
	}

	@Test(timeout = 10000)
	public void otherClientsInTheRoomShouldGetAnAnnouncementWhenIJoin()
			throws Exception {
		ClientChatApplication itay = loginUser(ITAY);
		ClientChatApplication avner = loginUser(AVNER);
		avner.joinRoom(ROOM);

		itay.joinRoom(ROOM);
		assertEquals(new RoomAnnouncement(ITAY, ROOM, Announcement.JOIN),
				announcements.get(AVNER).take());

		itay.logout();
		avner.logout();
	}

	@Test(timeout = 10000)
	public void otherClientsInTheRoomShouldGetAnAnnouncementWhenILeave()
			throws Exception {
		ClientChatApplication itay = loginUser(ITAY);
		ClientChatApplication avner = loginUser(AVNER);
		avner.joinRoom(ROOM);

		itay.joinRoom(ROOM);
		assertEquals(new RoomAnnouncement(ITAY, ROOM, Announcement.JOIN),
				announcements.get(AVNER).take());

		itay.leaveRoom(ROOM);
		assertEquals(new RoomAnnouncement(ITAY, ROOM, Announcement.LEAVE),
				announcements.get(AVNER).take());

		itay.logout();
		avner.logout();
	}

	@Test(timeout = 10000)
	public void otherClientsInTheRoomShouldGetAnAnnouncementWhenIDisconnect()
			throws Exception {
		ClientChatApplication itay = loginUser(ITAY);
		ClientChatApplication avner = loginUser(AVNER);
		avner.joinRoom(ROOM);

		itay.joinRoom(ROOM);
		assertEquals(new RoomAnnouncement(ITAY, ROOM, Announcement.JOIN),
				announcements.get(AVNER).take());

		itay.logout();
		assertEquals(new RoomAnnouncement(ITAY, ROOM, Announcement.DISCONNECT),
				announcements.get(AVNER).take());

		itay.logout();
		avner.logout();
	}

	@Test(timeout = 10000)
	public void loggingOutAndTheInAgainShouldKeepMyRooms() throws Exception {
		ClientChatApplication itay = loginUser(ITAY);
		itay.joinRoom(ROOM);
		itay.logout();

		itay.login(x -> messages.get(ITAY).add(x), x -> announcements.get(ITAY)
				.add(x));

		assertEquals(Arrays.asList(ROOM), itay.getAllRooms());

		itay.logout();
	}

	@Test(timeout = 10000)
	public void afterLoggingOutIShouldNotGetMessages() throws Exception {
		ClientChatApplication itay = loginUser(ITAY);
		ClientChatApplication avner = loginUser(AVNER);

		itay.joinRoom(ROOM);
		itay.logout();

		avner.joinRoom(ROOM);

		Thread.sleep(1000);
		assertTrue(messages.get(ITAY).isEmpty());
		assertTrue(announcements.get(ITAY).isEmpty());

		itay.logout();
		avner.logout();
	}

	private static <T> boolean equalWithoutOrder(Collection<T> collection1,
			Collection<T> collection2) {
		return collection1.containsAll(collection2)
				&& collection2.containsAll(collection1);
	}

	@Test(timeout = 10000)
	public void getJoinedRoomsShouldReturnAllJoinedRooms() throws Exception {
		ClientChatApplication itay = loginUser(ITAY);

		itay.joinRoom(ROOM);
		assertEquals(Arrays.asList(ROOM), itay.getJoinedRooms());

		itay.joinRoom("room2");
		assertTrue(equalWithoutOrder(Arrays.asList("room2", ROOM),
				itay.getJoinedRooms()));

		itay.joinRoom("room3");
		assertTrue(equalWithoutOrder(Arrays.asList("room3", "room2", ROOM),
				itay.getJoinedRooms()));

		itay.logout();
	}

	@Test(timeout = 10000)
	public void getAllRoomsShouldReturnEmptyListIfNoRoomExists()
			throws Exception {
		ClientChatApplication itay = loginUser(ITAY);

		assertTrue(itay.getAllRooms().isEmpty());

		itay.logout();
	}

	@Test(timeout = 10000)
	public void getAllRoomsShouldReturnAllRooms() throws Exception {
		ClientChatApplication itay = loginUser(ITAY);
		ClientChatApplication avner = loginUser(AVNER);

		itay.joinRoom(ROOM);
		assertTrue(equalWithoutOrder(Arrays.asList(ROOM), itay.getAllRooms()));

		avner.joinRoom("room2");
		assertTrue(equalWithoutOrder(Arrays.asList(ROOM, "room2"),
				itay.getAllRooms()));

		itay.joinRoom("room3");
		assertTrue(equalWithoutOrder(Arrays.asList("room3", "room2", ROOM),
				itay.getAllRooms()));

		avner.joinRoom("room4");
		assertTrue(equalWithoutOrder(
				Arrays.asList("room4", "room3", "room2", ROOM),
				itay.getAllRooms()));

		itay.logout();
		avner.logout();
	}

	@Test(timeout = 10000)
	public void getClientsInRoomShouldThrowExceptionIfRoomDoesntExist()
			throws Exception {
		ClientChatApplication itay = loginUser(ITAY);

		thrown.expect(NoSuchRoomException.class);
		itay.getClientsInRoom(ROOM);

		itay.logout();
	}

	@Test(timeout = 10000)
	public void getClientsInRoomShouldThrowExceptionIfNoClientsInRoom()
			throws Exception {
		ClientChatApplication itay = loginUser(ITAY);
		itay.joinRoom(ROOM);
		itay.leaveRoom(ROOM);

		thrown.expect(NoSuchRoomException.class);
		itay.getClientsInRoom(ROOM);

		itay.logout();
	}

	@Test(timeout = 10000)
	public void getClientsInRoomShouldReturnAllClientsInRoom() throws Exception {
		ClientChatApplication itay = loginUser(ITAY);
		ClientChatApplication avner = loginUser(AVNER);

		itay.joinRoom(ROOM);
		assertTrue(equalWithoutOrder(Arrays.asList(ITAY),
				itay.getClientsInRoom(ROOM)));

		avner.joinRoom(ROOM);
		assertTrue(equalWithoutOrder(Arrays.asList(ITAY, AVNER),
				itay.getClientsInRoom(ROOM)));

		avner.leaveRoom(ROOM);
		assertTrue(equalWithoutOrder(Arrays.asList(ITAY),
				itay.getClientsInRoom(ROOM)));

		itay.logout();
		avner.logout();
	}

}
