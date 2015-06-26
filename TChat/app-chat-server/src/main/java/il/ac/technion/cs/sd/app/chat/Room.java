package il.ac.technion.cs.sd.app.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Room {

	private final Map<String,ClientData> clientsInRoom;
	
	private final Map<String,ClientData> onlineClientsInRoom;

	public String name;
	
	public Room(String name) {
		
		this.name = name;
		clientsInRoom = new ConcurrentHashMap<String, ClientData>();
		
		onlineClientsInRoom = new ConcurrentHashMap<String, ClientData>();
	}
	
	public void addClient(String clientName, ClientData client){
		clientsInRoom.put(clientName,client);
		onlineClientsInRoom.put(clientName,client);
	}
	
	public void removeClient(String clientName){
		clientsInRoom.remove(clientName);
		onlineClientsInRoom.remove(clientName);
	}
	
	public void onClientLogin(String clientName, ClientData client){
		onlineClientsInRoom.put(clientName, client);
	}
	
	public void onClientLogout(String clientName){
		onlineClientsInRoom.remove(clientName);
	}

	public List<String> getClients() {
		return new ArrayList<>(clientsInRoom.keySet());
	}

	public List<String> getOnlineClients() {
		return new ArrayList<>(onlineClientsInRoom.keySet());
	}
	
	public boolean hasLoggedInUsers(){
		return onlineClientsInRoom.size() != 0;
	}
	
	public boolean isEmpty(){
		return clientsInRoom.size() == 0;
	}
	
}
