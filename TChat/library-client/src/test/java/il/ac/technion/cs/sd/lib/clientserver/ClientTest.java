package il.ac.technion.cs.sd.lib.clientserver;

import static org.junit.Assert.*;
import il.ac.technion.cs.sd.lib.clientserver.Client;
import il.ac.technion.cs.sd.lib.clientserver.InvalidOperation;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class ClientTest {
	Client client;

	@Before
	public void setUp() throws Exception {
		client = new Client("Idan");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testBuildClient() {
		assertEquals("Idan", client.getAddress());
	}
	
	@Test(expected=InvalidOperation.class)
	public void startANewLoopTwiceThrowsException(){
		try{
			client.start("T2", x->{});
			client.start("T2", x->{});
		}catch(Exception e){
			throw e;
			
		}finally{
			client.stopListenLoop();
		}
	}
	
	@Test(expected=InvalidOperation.class)
	public void sendWithThrowWhenParmetric(){
		client.send("Hello");
	}
	
	@Test
	public void shouldntThrowWhenStopWithLoop() throws InterruptedException{
		client.start("T2", x->{});
		Thread.sleep(200);
		client.stopListenLoop();
	}
	
	@Test(expected=InvalidOperation.class)
	public void shouldThrowWhenStopNoLoop(){
		client.stopListenLoop();
		client.stopListenLoop();
	}
	
	@Test(expected=InvalidOperation.class)
	public void sendShouldntThrowNoParam(){
		client.send(new MessageData("Hello"));
	}


}