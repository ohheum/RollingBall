package project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Server  implements Runnable {

	public ArrayBlockingQueue<MyMessage> serverQueue;
	
	protected InputStream inputStream;
	protected OutputStream outputStream;
	private static final int ByteQueueCapacity = 1000;
	
	protected BlockingQueue<MyMessage> queueByte = new ArrayBlockingQueue<MyMessage>(ByteQueueCapacity);
	
	public String hostIP;
	public int port;

	public static boolean isHost = true;
	public static ServerSocket hostServer = null;
	public static Socket socket = null;
	
	public static BufferedReader in = null;
	public static PrintWriter out = null;
	

	public Server( ArrayBlockingQueue<MyMessage> queue, String ipaddress, String port) throws IOException
	{
		serverQueue = queue;
		this.hostIP = ipaddress;
		this.port = Integer.parseInt(port.trim());
	}
	
	public boolean initializer() throws IOException
	{
		try {
			if (isHost) {
				hostServer = new ServerSocket(port);
				socket = hostServer.accept();
			}
			else {  // If guest, try to connect to the server
				socket = new Socket(hostIP, port);
			}
//			inputStream = socket.getInputStream();
//			outputStream = socket.getOutputStream();			
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);	
			return true;
		}
		catch (IOException e) {
			cleanUp();
			return false;
		}
	}

	public void start()
	{
		Thread thread = new Thread(this);
		thread.start();
	}

	public void cleanUp() throws IOException {
		try {
			if (hostServer != null && !hostServer.isClosed()) {
				hostServer.close();
				hostServer = null;
			}
		}
		catch (IOException e) { hostServer = null; }

		try {
			if (socket != null && !socket.isClosed()) {
				socket.close();
				socket = null;
			}
		}
		catch (IOException e) { socket = null; }

		try {
			if (inputStream != null ) {
				inputStream.close();
				inputStream = null;
			}
		}
		catch (IOException e) { inputStream = null; }

		if (outputStream != null) {
			outputStream.close();
			outputStream = null;
		}
		try {
			if (in != null) {
				in.close();
				in = null;
			}
		}
		catch (IOException e) { in = null; }

		if (out != null) {
			out.close();
			out = null;
		}
	}

	private void parseMessage(String msg)
	{
		if (msg==null || msg.length()<=0)
			return;
		String [] tokens = msg.trim().split("\\s+");
		if (tokens.length < 18) {
			System.out.println("Too short message...");
			return;
		}
		
		MyMessage myMsg = new MyMessage();
		int k=0;
		for ( int i=0; i<3; i++ ) {
			for (int j=0; j<3; j++ ) {
				myMsg.rotationMatrix[j][i] = Double.parseDouble(tokens[k++]);
			}
		}

		for ( int i=0; i<3; i++)
			myMsg.accelerationVector[i] = Double.parseDouble(tokens[k++]);		
		
		for ( int i=0; i<3; i++)
			myMsg.gyroVector[i] = Double.parseDouble(tokens[k++]);
		
		for ( int i=0; i<3; i++)
			myMsg.magneticVector[i] = Double.parseDouble(tokens[k++]);		
		
//		System.out.println("Parsing completed...");
		try {
			serverQueue.put( myMsg );
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void run()
	{      
		while (true) {
	        try
	        {
	            String message = (String) in.readLine();
	            if (message!=null) {
	            	parseMessage(message);
//	            	System.out.println("Message Received: " + message);
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	}
}



