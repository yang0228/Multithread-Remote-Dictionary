
/*
 *@DictServer.java 
 *
 *@A UDP dictionary server program.
 *
 *@Author: QINGYANG HONG 
 *@ID: 629379
 *
*/

import java.net.*;
import java.io.*;
import java.util.*;
public class DictServer extends ThreadGroup{
	
    static DatagramSocket aSocket = null;
    private boolean isClosed = false;  //threadpool closed   
    private LinkedList workQueue;      //workqueue 
    private static int threadPoolID = 1;  //threadpoolid 
    public DictServer(int poolSize) {  //poolSize   
  
        super(threadPoolID + "");      //ThreadGroup name  
        setDaemon(true);               //inherited method£¬setDaemon threadpool 
        workQueue = new LinkedList();  //set workQueue 
        for(int i = 0; i < poolSize; i++) {  
            new WorkThread(i).start();   //start workthread, based on poolSize  
        }  
    } 
	
    /* add task to workqueue */  
    public synchronized void execute(Runnable task) {  
        if(isClosed) {  
            throw new IllegalStateException();  
        }  
        if(task != null) {  
            workQueue.add(task);  
            notify();           //notify a workthread who's waiting to getTask  
        }  
    }  
      
    /* gettask from workqueue, workthread calls this method*/  
    private synchronized Runnable getTask(int threadid) throws InterruptedException {  
        while(workQueue.size() == 0) {  
            if(isClosed) return null;  
            System.out.println("workthread"+threadid+" is waiting... ");  
            wait();             //if no task in workqueue, wait   
        }  
        System.out.println("workthread"+threadid+" is executing a task...\n");  
        return (Runnable) workQueue.removeFirst(); //return the first task in queue and delete it from queue 
    }  
      
    /* closepool*/  
    public synchronized void closePool() {  
        if(! isClosed) {  
            waitFinish();          
            isClosed = true;  
            workQueue.clear();    
            interrupt();        //interrupt all workthreads,inherited from ThreadGroup  
        }  
    }  
      
    /* wait all tasks to finish*/  
    public void waitFinish() {  
        synchronized (this) {  
            isClosed = true;  
            notifyAll();            //call all workthreads to gettask   
        }  
        Thread[] threads = new Thread[activeCount()]; //activeCount(): number of active threads  
        int count = enumerate(threads); //enumerate(): inherited from ThreadGroup, get all workthreads  
        for(int i =0; i < count; i++) {  
            try {  
                threads[i].join();  //join threads  
            }catch(InterruptedException ex) {  
                ex.printStackTrace();  
            }  
        }  
    }  
  
    /* 
     * workthread, get a task from workqueue and execute 
     */  
    private class WorkThread extends Thread {  
        private int id;  
        public WorkThread(int id) {  
            //superclass constructor,add workthread to ThreadPool group 
            super(DictServer.this,id+"");  
            this.id =id;  
        }  
        public void run() {  
            while(! isInterrupted()) {  
                Runnable task = null;  
                try {  
                    task = getTask(id);       
                }catch(InterruptedException ex) {  
                    ex.printStackTrace();  
                }  
                //if getTask()return null or getTask() is interrupted, end this thread 
                if(task == null) return;  
                  
                try {  
                    task.run();   
                }catch(Throwable t) {  
                    t.printStackTrace();  
                }  
            }//  end while  
        }//  end run  
    }

    private static Runnable createTask(final DatagramPacket request, final String[] s) {  
        return new Runnable() {  
            public void run() {  
            	boolean foundflag = false;
            	DatagramPacket reply = null;
            	for(int p = 0; s[p] != null; p++){
            		//System.out.println("new String(request.getData(),0,s[p].length()).trim():"+new String(request.getData(),0,request.getLength()).trim()+'\n');
            		//System.out.println("s[p].indexOf(new String(request.getData()).trim()):"+s[p].indexOf(new String(request.getData(),0,request.getLength()).trim(),0)+'\n');
            		//System.out.println("s[p].indexOf('/'):"+s[p].indexOf('/')+'\n');
            		//System.out.println("request.getLength():"+request.getLength()+'\n');
            		//System.out.println("string.Length():"+new String(request.getData(),0,request.getLength()).length()+'\n');
            		if(s[p].indexOf(new String(request.getData(),0,request.getLength()).trim(),0) == 0&&s[p].indexOf('/') == request.getLength()){
            			foundflag = true;
            			System.out.println(" Find item ->"+s[p]+'\n');
            			reply = new DatagramPacket(s[p].getBytes(),
					  s[p].length(),request.getAddress(),
					  request.getPort());
            			break;
            		}
            		else{
            			
            			reply = new DatagramPacket("Sorry~ Word not found...".getBytes(),
			 		   24,request.getAddress(),
			 		   request.getPort());

			 		   		}
            	}
            	if(!foundflag){
            		System.out.println("Not find item ->"+new String(request.getData(),0,request.getLength()).trim()+'\n');
            	}	
            	try{
            		System.out.println("Hi master, message been sent!!!!!");
            		aSocket.send(reply);
            	}catch (IOException e) {
            		System.out.println("IO: " + e.getMessage());
		}
            }  
        };  
    }

    public static void main(String args[]) throws InterruptedException{
    	
	
    	if (args.length != 2) {
    		System.out.println("Usage: java DictServer <port> <dictionary-file>");
    		System.exit(1);
    	}
	
		
    	FileReader fr = null;
    	BufferedReader br = null;
	
    	DictServer dictServer = new DictServer(3); //establish ThreadPool with 3 workthreads 
    	Thread.sleep(500); //sleep for 500 millisecond,so that all workthreads start 
    
    	//for (int i = 0; i <=5 ; i++) { // start 6 tasks  
    	//    dictServer.execute(createTask(i));  
    	//}  
    	//dictServer.waitFinish();  
    	//dictServer.closePool(); 
    
    	try {
				int socket_no = Integer.valueOf(args[0]).intValue();
				if (socket_no>65535 || socket_no<1) {
					System.out.println("port number: 1~65535, except reserved ports");
    			System.exit(1);
				}    		
    		File f = new File("./dict/"+args[1]);
    		aSocket = new DatagramSocket(socket_no);
    		byte[] buffer = new byte[1000];
	    
    		fr = new FileReader(f);
    		br = new BufferedReader(fr);
    		String[] s = new String[65535];

    		for(int p = 0; p < s.length; p++){
			s[p] = br.readLine();
    	}
    		//for(int p = 0; p < 30; p++){
			//	System.out.println("sp"+p+s[p]+'\n');
    		//	}
    		while(true) {
	    	
    			DatagramPacket request = new DatagramPacket(buffer,
    					buffer.length);
    			aSocket.receive(request);
    			DatagramPacket reply = null;
    			dictServer.execute(createTask(request, s));

    		}
    		//throw new SocketException("Testing SocketException");
		    //throw new IOException("Testing IOException");
    	}
    	catch (SocketException se) {
    		System.out.println("Socket: " + se.getMessage());
    	}
    	catch (FileNotFoundException fnfe) {
    		System.err.println("File not found: " + args[1]);
    		System.exit(-1);
    	}
    	catch (IOException ioe) {
    		System.out.println("IO: " + ioe.getMessage());
    	}
    	catch (NumberFormatException nfe) {
    		System.out.println("NumberFormatE: " + nfe.getMessage());
    		System.exit(-1);
    	}
    	finally {
    	       	try {
		    br.close();
		} 
		catch (IOException e) {
		    System.out.println("CloseIO: " + e.getMessage());
		}
		dictServer.waitFinish();  
		dictServer.closePool(); 
	        if (aSocket != null) 
		    aSocket.close();
    	}
    }
}
