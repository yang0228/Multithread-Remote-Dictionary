
/*
 *@DictClient.java 
 *
 *@A UDP dictionary client program.
 *
 *@Author: QINGYANG HONG 
 *@ID: 629379
 *
 */

import java.net.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
public class DictClient extends Frame implements ActionListener {
	
	private static final int TIMEOUT = 2000;   // Resend timeout (milliseconds)
	private static final int MAX_TRIES = 6;     // Maximum retransmissions
	 
	static InetAddress aHost;
	static int serverPort;
	static byte[] m;
	static DatagramSocket aSocket;
	 
	Label label = new Label("Press \"Enter\" to look up");
	TextField tf = new TextField(20);
	static TextArea ta = new TextArea();
	Panel panel = new Panel();//panel object

	
	public DictClient(String args[]) {
		super("DictClient");
		setSize(500, 180);
		panel.add(label);// add label on panel
		panel.add(tf);// add textfield on panel
		tf.addActionListener(this);// register
		add("North", panel);// add panel to window
		add("Center", ta);// add textarea
		addWindowListener(new WindowAdapter() {
   		public void windowClosing(WindowEvent e) {
    		System.exit(0);
   		}
		});
		show();	

		try{
			
			aSocket = new DatagramSocket();
			aSocket.setSoTimeout(TIMEOUT);  // Maximum receive blocking time (milliseconds)	    
			aHost = InetAddress.getByName(args[0]);
			serverPort = Integer.valueOf(args[1]).intValue();
			m = args[2].getBytes();
			byte[] buffer = new byte[1000];
			DatagramPacket request = new DatagramPacket(m, m.length, aHost, serverPort);
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
			int tries = 0;      // Packets may be lost, so we have to keep trying
			boolean receivedflag = false;
	  
			/* 
			 * resend mechanism with timeout and max-tries constraints
			 */  
			do {
				aSocket.send(request);            // Send request
				try {
					aSocket.receive(reply);       // Attempt receive reply 
					if (!reply.getAddress().equals(aHost)){// Check host
						throw new IOException("Received packet from an unknown source");
					}
					receivedflag = true;		      
				} 
				catch (InterruptedIOException e) {  
					tries += 1;
					ta.setText("");
					ta.append("Timed out, " + (MAX_TRIES - tries) + " more tries...\n");
				}
			} while ((!receivedflag) && (tries < MAX_TRIES));
	  
			if (receivedflag) {
				ta.setText("");
				ta.append("Reply: " + new String(reply.getData()).trim());
				//System.out.println("Reply: " + new String(reply.getData()).trim()+'\n');
			} else {
				ta.setText("");
				ta.append("No response, host unaccessible...\n");
				//System.out.println("No response, giving up...");
			}
			//throw new SocketException("Testing SocketException");
			//throw new IOException("Testing IOException");
		}
		catch (SocketException e) {
			ta.setText("");
			ta.append("Socket ERROR: " + e.getMessage()+'\n');
			//System.out.println("Socket ERROR: " + e.getMessage());
		}
		catch (IOException e) {
			ta.setText("");
			ta.append("IO ERROR1: "+ e.getMessage()+'\n');
			//System.out.println("IO ERROR: "+ e.getMessage());
		}
		catch (NumberFormatException nfe) {
		    System.out.println("NumberFormatE: " + nfe.getMessage());
		    System.exit(-1);
		}
		finally {
			if (aSocket != null) 
			aSocket.close();
		}
	}
	
	
	public static void main(String args[]){
	// args give message contents and server hostname
	    if (args.length != 3) {
		System.out.println( "Usage: java DictClient <address> <port> <word-to-look-for>");
		System.exit(1);
	    } 
		new DictClient(args);
	}    
 
}
