package edu.buffalo.cse.cse486586.groupmessenger;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.UUID;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 * 
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {
	static final String TAG = GroupMessengerActivity.class.getSimpleName();	
	static final String MSG_DELIMITER = "---";
	MessageBuffer messageBuffer = null;
	int localSequenceNo = 0;
	Sequencer sequencer = null; 
    String deviceName = "";
    String devicePort = "";
    Object seqLock = new Object();
	Object msgLock = new Object();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);
        
        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());
        
        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));
        
        /*
         * Calculate the port number that this AVD listens on.
         * It is just a hack that I came up with to get around the networking limitations of AVDs.
         * The explanation is provided in the PA1 spec.
         */
        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        
        // Populate the Device Name from port number - Babu
        deviceName = DeviceInfo.getDeviceName(myPort);
        devicePort = myPort;
        
        // Initialize MessageBuffer - Babu
        messageBuffer = new MessageBuffer();
        
        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs in a total-causal order.
         */
        
        /*
         * Added by Babu - OnClickListener for sending the messages to 
         * all the process
         */
        // Listener for Enter key pressed after entering the message 
        final EditText editText = (EditText)findViewById(R.id.editText1);
		editText.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) 
			{
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) 
                {
                	String msgToBeSent = editText.getText().toString();
    				multicastMessage(UUID.randomUUID().toString(), msgToBeSent, MSG_TYPE.MESSAGE);				
    				editText.setText("");                	
    				return true;
                }
				return false;
			}
		});
		
        // Listener for button click  after entering the message
        Button btnSend = (Button)findViewById(R.id.button4);
        btnSend.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				String msgToBeSent = editText.getText().toString();
				multicastMessage(UUID.randomUUID().toString(), msgToBeSent, MSG_TYPE.MESSAGE);				
				editText.setText("");
			}
		});

        try {
            /*
             * Create a server socket as well as a thread (AsyncTask) that listens on the server
             * port. - Babu
             * */
            ServerSocket serverSocket = new ServerSocket(DeviceInfo.SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
          
            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }
        
        
    }

    /**     * 
     * Function to broadcast/multicast message to all the devices
     * @param id - Unique id for the message
     * @param msgToBeSent - Message to be sent
     * @param myPort  - Port Number of the sending device
     * 
     * @author Babu
     */
    protected void multicastMessage(String id, String msgContent, MSG_TYPE msgType) {

    	// Attach Unique ID to the messageMSG_DELIMITER
    	String msgToBeSent = id + MSG_DELIMITER + msgContent + MSG_DELIMITER + msgType.toString();
    	Log.v(TAG, "Multicast Message - " + msgContent);
    	new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msgToBeSent);
        		
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }
    
    
    /***
     * ServerTask is an AsyncTask that should handle incoming messages. It is created by
     * ServerTask.executeOnExecutor() call in SimpleMessengerActivity.
     * 
     * Please make sure you understand how AsyncTask works by reading
     * http://developer.android.com/reference/android/os/AsyncTask.html
     *
     

    /*** 
     * Server Task in each device to receive the message from the socket
     * and deliver based on the sequence algorithm 
     * 
     * @author Babu
     */
     private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];
			String msgRecieved = "";
                        
            /*
             * TODO: Fill in your server code that receives messages and passes them
             * to onProgressUpdate().
             */
			
			/*
             * Code to accept the client socket connection and read message from the socket. Message read  
             * from the socket is updated in the Server UI through ProgressUpdate Call.
             * @author - Babu
             */  
            try {            	
				while (true) {   // Infinite while loop in order to enable continuous two-way communication 
		            // Default Buffer Size is assigned as 128 as PA1 specifications
		            byte buffer[] = new byte[128];
					Socket socket = serverSocket.accept();					
					InputStream in = socket.getInputStream();
					if (in.read(buffer) != -1) {
						msgRecieved = new String(buffer);
						Log.d(TAG, "Message Recieved - " + msgRecieved); 
					}
					else
						Log.e(TAG, "Unable to read buffer data from Socket");
					in.close();
					msgRecieved = msgRecieved.trim();
					
					// De-compressing Message Packet
					String[] msgPacket = msgRecieved.split(MSG_DELIMITER);
					String msgId = msgPacket[0];
					String msgContent = msgPacket[1];
					MSG_TYPE msgType = MSG_TYPE.valueOf(msgPacket[2]);
					
					// Logic to calculate sequence					
					switch (msgType) 
					{
						case MESSAGE:
							/* Synchronized because Sequencer object access is restricted to 
							 * and modified by only one thread at a time
							*/
							synchronized (msgLock) 
							{							
								if(Sequencer.predefinedSeqDeviceName.equals(deviceName))
								{
									Log.v(TAG, "Message hits the Sequencer for ordering - " + msgContent);
									sequencer = Sequencer.getInstance(devicePort);
									String seqNo = String.valueOf(sequencer.getSequenceNumber());
									sequencer.incrementSequence();
									multicastMessage(msgId, seqNo, MSG_TYPE.SEQ_NO);
									Log.v(TAG, "Sequence multicasted for message - " + msgContent);
								}
								MessagePacket holdBackMsg = messageBuffer.getHoldBackMessage(msgId);
								if(holdBackMsg != null)
								{
									holdBackMsg.setMsgContent(msgContent);
									if(holdBackMsg.getSeqNumber().equals(String.valueOf(localSequenceNo)))  
									{										
										Log.v(TAG, "Message retreived from holdback buffer and sent for delivery - " + holdBackMsg.getMsgContent());
										publishProgress(holdBackMsg.getMsgId(), holdBackMsg.getMsgContent(), holdBackMsg.getSeqNumber());
										localSequenceNo++;
										
										/* A message has been delivered. So check whether any message was waiting
										 * the above one to get delivered. If any, then deliver them too. 
										 */										
										MessagePacket deliveryMsg = messageBuffer.checkAndDeliver(String.valueOf(localSequenceNo));
										while(deliveryMsg != null)
										{
											Log.v(TAG, "Message retreived from delivery buffer and sent for delivery - " + deliveryMsg.getMsgContent());
											publishProgress(deliveryMsg.getMsgId(), deliveryMsg.getMsgContent(), deliveryMsg.getSeqNumber());											
											deliveryMsg = messageBuffer.checkAndDeliver(String.valueOf(++localSequenceNo));
										}
									}
									else								
										messageBuffer.addToDeliveryQueue(holdBackMsg);
								}
								else								
									messageBuffer.addToHoldbackQueue(new MessagePacket(msgId,msgContent));
							}
							break;
						case SEQ_NO:
							/* Synchronized because local sequence number should be checked 
							 * and incremented by only one thread at a time
							*/
							synchronized (seqLock) 
							{
								MessagePacket holdBackMsg = messageBuffer.getHoldBackMessage(msgId);								
								if(holdBackMsg != null)
								{	
									Log.v(TAG, "Sequence Number received for message - " + msgContent);
									holdBackMsg.setSeqNumber(msgContent);
									// Deliver the message if the local sequence number and the one received matches
									if(holdBackMsg.getSeqNumber().equals(String.valueOf(localSequenceNo)))  
									{										
										Log.v(TAG, "Message retreived from holdback buffer and sent for delivery - " + holdBackMsg.getMsgContent());
										publishProgress(holdBackMsg.getMsgId(), holdBackMsg.getMsgContent(), holdBackMsg.getSeqNumber());
										localSequenceNo++;
										
										/* A message has been delivered. So check whether any message was waiting
										 * the above one to get delivered. If any, then deliver them too. 
										 */
										MessagePacket deliveryMsg = messageBuffer.checkAndDeliver(String.valueOf(localSequenceNo));
										while(deliveryMsg != null)
										{
											Log.v(TAG, "Message retreived from Delivery buffer and sent for delivery - " + deliveryMsg.getMsgContent());
											publishProgress(deliveryMsg.getMsgId(), deliveryMsg.getMsgContent(), deliveryMsg.getSeqNumber());											
											deliveryMsg = messageBuffer.checkAndDeliver(String.valueOf(++localSequenceNo));
										}
									}
									else								
										messageBuffer.addToDeliveryQueue(holdBackMsg);									
								}
								// If no holdback message then sequence has been received prior to message
								else								
									messageBuffer.addToHoldbackQueue(new MessagePacket(msgId,"",msgContent));																	
							}							
								
							break;	
						default:
							Log.e(TAG, "Invalid Message Type");
							break;
					}
										
					
					
				}
				
			} catch (IOException e) {				
				Log.e(TAG, "Error in socket connection.");
				e.printStackTrace();				
			}
            
            
            return null;
        }

        protected void onProgressUpdate(String...strings) {
            /*
             * The following code displays what is received in doInBackground().
             */
        	if(strings.length < 3)
        	{
        		Log.e(TAG, "Unable to deliver message");
        		return;
        	}
        	
        	String msgId = strings[0].trim();
            String msgContent = strings[1].trim();
            String seqNo = strings[2].trim();
            
            String msgPacket = msgId + MSG_DELIMITER + msgContent + MSG_DELIMITER + seqNo;
            
            TextView textView = (TextView) findViewById(R.id.textView1);
            textView.append(msgPacket + "\t\n");            
            
            /*
             * Code to insert message and its sequence number into the content provider
             * 
             * @author Babu               
             */
            ContentValues contentValue = new ContentValues();         
	        contentValue.put("key", seqNo);
	        contentValue.put("value", msgContent);
	        Uri newUri = getContentResolver().insert(Uri.parse("content://edu.buffalo.cse.cse486586.groupmessenger.provider"),contentValue);
	        
	        
            /*
             * The following code creates a file in the AVD's internal storage and stores a file.
             * 
             * For more information on file I/O on Android, please take a look at
             * http://developer.android.com/training/basics/data-storage/files.html
             */
            
            String filename = "GroupMessengerOutput";
            String string = msgPacket + "\n";
            FileOutputStream outputStream;

            try {
                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(string.getBytes());
                outputStream.close();
            } catch (Exception e) {
                Log.e(TAG, "File write failed");
            }

            return;
        }
    }

    /***
     * ClientTask is an AsyncTask that should send a string over the network.
     * It is created by ClientTask.executeOnExecutor() call whenever OnKeyListener.onKey() detects
     * an enter key press event.
     * 
     * @author stevko
     *
     */
    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
            try {               

                for (String remotePort : DeviceInfo.REMOTE_PORTS) 
                {				
	                Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
	                        Integer.parseInt(remotePort));
	                
	                String msgToSend = msgs[0]; // Message to be sent	                
	                /*
	                 * Code to get the outputstream from the socket created for client-server connection and sending 
	                 * the message through the socket 
	                 * @author - Babu
	                 */                
	                OutputStream out = socket.getOutputStream();
	                out.write(msgToSend.getBytes());                
	                out.flush();  
	                out.close();
	                Log.d(TAG, "Message Sent - " + msgToSend); 
	                socket.close();
                }
                
            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
            }

            return null;
        }
    }
    
}
