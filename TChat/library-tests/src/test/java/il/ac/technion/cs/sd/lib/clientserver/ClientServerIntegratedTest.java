package il.ac.technion.cs.sd.lib.clientserver;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ClientServerIntegratedTest {

	final static int CLIENTS_NUM = 4;

	private Server server1;
	private List<Client> clients = createClients(CLIENTS_NUM);


	Random rnd = new Random(); 

	int tmp; // for lambda closure.

	List<Client> createClients(int clientsNum)
	{
		List<Client> $ = new LinkedList<>();

		for (int i=0; i<clientsNum; i++)
		{
			$.add(new Client("client_"+UUID.randomUUID().toString()));
		}
		return $;
	}



	private POJO1 pojo1_a;
	private POJO1 pojo1_b;

	private POJO2 pojo2_a;

	private class  Pair<F,S>
	{
		Pair(F first, S second) {
			this.first = first;
			this.second = second;
		}
		F first;
		S second;
	}

	BlockingQueue<Pair<Object,String>> biConsumer1_bq;
	private BiConsumer<Object,String> biConsumer1 = (p,from) ->
	{
		biConsumer1_bq.add(new Pair<Object,String>(p,from));
	};

	BlockingQueue<Object> consumer1_bq;
	private Consumer<Object> consumer1 = p ->
	{
		consumer1_bq.add(p);
	};

	BlockingQueue<Object> consumer2_bq;
	private Consumer<Object> consumer2 = p ->
	{
		consumer2_bq.add(p);
	};


	@Before
	public void setUp() throws Exception {

		pojo1_a = new POJO1(1, "hi");
		pojo1_b = new POJO1(2, "bye");

		List<POJO1> pojos = new LinkedList<>();
		pojos.add(pojo1_a);
		pojos.add(pojo1_b);

		pojo2_a = new POJO2(-19,"oh yea!",pojos);


		server1 = new Server("server1_"+UUID.randomUUID().toString());
		server1.clearPersistentData();

		biConsumer1_bq = new LinkedBlockingQueue<>(); 
		consumer1_bq = new LinkedBlockingQueue<>();
		consumer2_bq = new LinkedBlockingQueue<>();
	}

	@After
	public void tearDown() throws Exception {
		server1.clearPersistentData();
	}

	@Test
	public void saveAndThenLoadInreger() {

		Integer x = 4;

		server1.saveObjectToFile("Integer", x);

		Optional<Object> $ = server1.readObjectFromFile("Integer");
		assertEquals($.get(), x);
	}

	
	@Test
	public void saveAndThenLoadSimpleObject() {

		POJO1 pojo1 = new POJO1(1, "hi");

		server1.saveObjectToFile("pojo1", pojo1);

		Optional<Object> $ = server1.readObjectFromFile("pojo1");
		assertEquals($.get(), pojo1);
	}

	@Test
	public void saveAndThenLoadTwoSimpleObjects() {

		server1.saveObjectToFile("pojo1", pojo1_a);
		server1.saveObjectToFile("pojo2", pojo1_b);

		Optional<Object> $ = server1.readObjectFromFile("pojo1");
		assertEquals($.get(), pojo1_a);

		$ = server1.readObjectFromFile("pojo2");
		assertEquals($.get(), pojo1_b);
	}


	@Test
	public void saveAndThenLoadComplexObjects() {		


		server1.saveObjectToFile("c", pojo2_a);

		Optional<Object> $ = server1.readObjectFromFile("c" );
		//Optional<Object> $ = server1.readObjectFromFile("c", new POJO2().getClass() );

		assertEquals($.get(), pojo2_a);
	}

	@Test
	public void saveAndThenLoadFromNewServerWithSameName() {

		POJO1 pojo1 = new POJO1(1, "hi");

		server1.start(biConsumer1);
		server1.saveObjectToFile("pojo1", pojo1);
		server1.stop();


		Server s = new Server(server1.getAddress());
		s.start(biConsumer1);
		Optional<Object> $ = s.readObjectFromFile("pojo1");
		s.stop();

		assertEquals($.get(), pojo1);
	}


	@Test
	public void saveAndThenLoadAfterClear() {

		server1.saveObjectToFile("pojo1", pojo1_a);

		server1.clearPersistentData();

		Optional<Object> $ = server1.readObjectFromFile("pojo1");
		assertFalse($.isPresent());
	}


	@Test(timeout=5000)
	public void clientSendsToServerMessage() throws InterruptedException {

		for (int k=0; k<5; k++)
		{
			clients.get(0).start(server1.getAddress(), consumer1);
			server1.start(biConsumer1);

			for (int i=0; i<4; i++)
			{
				clients.get(0).send(pojo1_a);
				Pair<Object,String> $ = biConsumer1_bq.take();
				assertEquals($.first, pojo1_a);
				assertEquals($.second, clients.get(0).getAddress());
			}

			clients.get(0).stopListenLoop();
			server1.stop();
		}
	}

	@Test(timeout=10000)
	public void serverSendsToClientMessage() throws InterruptedException {

		for (int k=0; k<3; k++)
		{
			clients.get(0).start(server1.getAddress(), consumer1);
			server1.start(biConsumer1);

			for (int i=0; i<10; i++)
			{
				server1.send(clients.get(0).getAddress(), pojo1_a, false);
				Object $ = consumer1_bq.take();
				assertEquals($, pojo1_a);
			}

			clients.get(0).stopListenLoop();
			server1.stop();
		}
	}

	@Test(timeout=20000)
	public void clientAndServerSendMessagesBackAndForth() throws InterruptedException {

		for (int i=0; i<3; i++)
		{
			tmp = 0; // messages count;
			final int messagesNumToSend = 7;

			clients.get(0).start(server1.getAddress(), (x) ->
			{
				POJO1 p = (POJO1) x;
				tmp++;
				if (p.i > 0)
				{
					POJO1 p2 = new POJO1(p.i-1, "");
					clients.get(0).send(p2);
				}
			});


			server1.start( (x, from) ->
			{
				POJO1 p = (POJO1) x;

				tmp++;
				if (p.i > 0)
				{
					POJO1 p2 = new POJO1(p.i-1, "");
					server1.send(clients.get(0).getAddress(), p2, false);
				}

				biConsumer1_bq.add(new Pair<>(p,from));
			});


			clients.get(0).send(new POJO1(messagesNumToSend-1, "aaa"));
			Thread.sleep(200 * messagesNumToSend);

			assertEquals(tmp, messagesNumToSend);

			clients.get(0).stopListenLoop();
			server1.stop();
		}
	}


	@Test(timeout=8000)
	public void serverSendsResponseBackToClient() throws InterruptedException {

		for (int k=0; k<2; k++)
		{
			clients.get(0).start(server1.getAddress(), consumer1);
			server1.start((pojo, str) ->
			{
				assertEquals(str, clients.get(0).getAddress());
				server1.send(clients.get(0).getAddress(), pojo1_b, true);
			});


			for (int i=0; i<6; i++)
			{			
				Object $ = clients.get(0).sendAndBlockUntilResponseArrives(pojo1_a);
				assertEquals($,pojo1_b);
			}			

			clients.get(0).stopListenLoop();
			server1.stop();
		}
	}


	@Test(timeout=5000)
	public void serverSendsComplexResponseBackToClient() throws InterruptedException {
		clients.get(0).start(server1.getAddress(), consumer2);
		server1.start((pojo, str) ->
		{
			assertEquals(str, clients.get(0).getAddress());
			server1.send(clients.get(0).getAddress(), pojo2_a, true);
		});



		Object $ = clients.get(0).sendAndBlockUntilResponseArrives(pojo2_a);
		assertEquals($,pojo2_a);


		clients.get(0).stopListenLoop();
		server1.stop();
	}



	@Test (timeout=100000)
	public void serverRandomlyComunicatesWithTwoClients() throws InterruptedException {


		for (int k=0; k<3; k++)
		{
			for (int i=0; i<CLIENTS_NUM; i++)
			{
				clients.get(i).start(server1.getAddress(), consumer1);
			}


			server1.start(   (x, str) ->
			{
				POJO1 pojo = (POJO1) x;
				server1.send(str, pojo, pojo.i > 0);
			});


			int expectedCharsNum = 0;
			int expectedQueueSize = 0;
			for (int i=0; i<20; i++)
			{	
				int r = rnd.nextInt(2);


				if (r == 0)
				{
					if (rnd.nextInt(2) == 0)
					{
						String str = "aaaaaaaaaa".substring(0,rnd.nextInt(5)+1);
						POJO1 p1 = new POJO1(0,str);
						expectedCharsNum += str.length();
						clients.get(rnd.nextInt(CLIENTS_NUM)).send(p1);
						expectedQueueSize++;
					}


				} else
				{

					Thread t1 = new Thread( () -> {
						POJO1 p = new POJO1(1, "bbbbbbbbb".substring(0,rnd.nextInt(5)+1));
						Object $ = clients.get(0).sendAndBlockUntilResponseArrives(p);
						assertEquals($, p);
					});

					Thread t2 = new Thread( () -> {
						POJO1 p = new POJO1(1, "bbbbbbbbb".substring(0,rnd.nextInt(5)+1));
						Object $ = clients.get(1).sendAndBlockUntilResponseArrives(p);
						assertEquals($, p);
					});

					boolean use_t1 = (rnd.nextInt(2) == 0);
					boolean use_t2 = (rnd.nextInt(2) == 0);

					if (use_t1)
						t1.start(); 
					if (use_t2)
						t2.start(); 		

					if (use_t1)
						t1.join();
					if (use_t2)
						t2.join();		
				}
			}

			for (int i=0; i<expectedQueueSize; i++)
			{
				POJO1 p = (POJO1) consumer1_bq.take();
				expectedCharsNum -= p.str.length();
			}

			assertEquals(expectedCharsNum,0);
			assertTrue(consumer1_bq.isEmpty());


			for (int i=0; i<CLIENTS_NUM; i++)
			{
				clients.get(i).stopListenLoop();
			}
			server1.stop();
		}

	}

	
	@Test
	public void clientSendsToServerMessagesBothFromConsumerAndFromUserThread() throws InterruptedException {

		for (int k=0; k<2; k++)
		{
			
			final Integer messagesToSendFromClient_block = 3*2 + 1; // MUST BE ODD FOR THIS TEST!
			final Integer messagesToSendFromClient_nonblock = 6;
			final Integer messagesToSendFromClient_total = 
					messagesToSendFromClient_block + messagesToSendFromClient_nonblock;
			
			clients.get(0).start(server1.getAddress(), (x) ->
			{
				Integer i = (Integer) x;
				if (i > 0)
				{ 
					i--;
					tmp++;
					assert(i%2 == 0);

					Integer $ = (Integer) clients.get(0).sendAndBlockUntilResponseArrives(i);
					assertEquals((Integer)$, i);
						
					if (i>0)
					{
						i--;
						tmp++;
						assert(i%2 == 1);
						clients.get(0).send(i);
					}

				}
			});


			server1.start((x,from) ->
			{
				Integer i = (Integer) x;

				if (i<0)
				{
					tmp++;
					return;
				}
				boolean isResponse = (i%2 == 0);
				server1.send(clients.get(0).getAddress(), i, isResponse);
			});

			
			for (int i=0; i<2; i++)
			{
				/* counter of [messages From Client send from consumer + messages server received from
				 * user's thread */ 
				tmp = 0;
				
				server1.send(clients.get(0).getAddress(), messagesToSendFromClient_block , false);
				for (int s=0; s<messagesToSendFromClient_nonblock; s++)
				{
					clients.get(0).send(new Integer(-1));
				}
				
				int total_transmitions = 
						(3 * messagesToSendFromClient_block  + 2 * messagesToSendFromClient_nonblock );
				Thread.sleep( total_transmitions * 200 );
				
				assertEquals(messagesToSendFromClient_total, new Integer(tmp));
			}

			clients.get(0).stopListenLoop();
			server1.stop();
		}
	}
}