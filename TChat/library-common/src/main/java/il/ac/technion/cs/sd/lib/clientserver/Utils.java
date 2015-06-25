package il.ac.technion.cs.sd.lib.clientserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.thoughtworks.xstream.XStream;

/**
 * Common utilities.
 */
class Utils {
	
	final static String ENCODING = "UTF-8";
	
	
	/**
	 * Deserializes a XStreamer string into an object.
	 * @param data A XStreamer string 
	 * @return the deserialized object.
	 */
	public static Object fromXStreamerStrToObject(String data)
	{
		if (data == null) 
			throw new IllegalArgumentException("data cannot be null");

		XStream xstream = new XStream();
		return xstream.fromXML(data);

	}
	
	
	public static void writeToFile(Object data, String fileName) throws IOException{
		
		if(fileName == null || data == null){
			throw new IllegalArgumentException("null parameters for write to file");
		}
		String dataAsJson = fromObjectToXStreamerStr(data);
		
		FileOutputStream outputStream = new FileOutputStream(fileName);
		ObjectOutputStream objectStream = new ObjectOutputStream(outputStream);
		objectStream.writeObject(dataAsJson);
		objectStream.close();
		outputStream.close();
		
	}

	public static Object readFromFile(String fileName) throws IOException{
		
		if(fileName == null ){
			throw new IllegalArgumentException("null parameters for read from file");
		}

		FileInputStream input = new FileInputStream(fileName);
        ObjectInputStream in = new ObjectInputStream(input);
        
        String receivedData = null;
        
        // shouldn't happen.
		try {
			receivedData = (String) in.readObject();

		} catch (ClassNotFoundException e) {
			throw new RuntimeException("couldn't find Serializable. WTF???");
		}finally{
			in.close();
			input.close();
		}
		
		return fromXStreamerStrToObject(receivedData);
	}

	
	/**
	 * Serializes an object into a XStreamer string.
	 * @param object The object to be serialized.
	 * @return The UTF-8 GSON string representing the object.
	 */
	public static String fromObjectToXStreamerStr(Object data)
	{
		if (data == null) 
			throw new IllegalArgumentException("data cannot be null");

		XStream xstream = new XStream();
		return xstream.toXML(data);

	}
	
	public static String showable(String str)
	{
		if (str.length() <= 15)
			return str;
		return str.substring(0, 15);
	}
	
	private static FileWriter logWriter = initLogWriter();
	private static FileWriter initLogWriter()  {
		try {
			return new FileWriter(new File("./TMP__DEBUG__log.txt"));
		} catch (IOException e) {
			throw new RuntimeException("failed to open log to write");
		}
	}
	public static void DEBUG_LOG_LINE(String line)
	{
//		synchronized(logWriter)
//		{
//			try {
//				logWriter.write(line + "\n");
//				logWriter.flush();
//			} catch (IOException e) {
//				throw new RuntimeException("failed to write to log");
//			}
//			
//		}
	}
}
