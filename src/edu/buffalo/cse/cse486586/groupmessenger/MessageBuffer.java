package edu.buffalo.cse.cse486586.groupmessenger;

import java.util.ArrayList;

/**
 * MessageBuffer class which will maintain Holdback Queue and Deliver Queue
 * for proper orderly message delivery
 * 
 * @author Babu 
 */
public class MessageBuffer {
	
	
	/**
	 * Holdback Queue which will hold messages until it gets the sequence number assigned
	 */
	private ArrayList<MessagePacket> holdBackQueue;
	
	/**
	 * Delivery Queue which will have messages with proper sequence number waiting to get delivered
	 * until the previous messages are arrived
	 */
	private ArrayList<MessagePacket> deliveryQueue;
	private int maxSeqNumber = 0;
	
	/**
	 * Default Constructor
	 */
	public MessageBuffer() {
		holdBackQueue = new ArrayList<MessagePacket>();
		deliveryQueue = new ArrayList<MessagePacket>();
	}
	
	/**
	 * Add to Message Packet to Holdbak Queue
	 */
	public void addToHoldbackQueue(MessagePacket messagePacket)
	{		
		holdBackQueue.add(messagePacket);
	}
	
	/**
	 * Get hold back Message from the holdback queue
	 * @param msgId - Message ID for holback message
	 * @return - MessagePacket if the messageId is found else null
	 */
	public MessagePacket getHoldBackMessage(String msgId)
	{
		for (MessagePacket  holdMsgPacket : holdBackQueue) {
			if(holdMsgPacket.getMsgId().equals(msgId))
			{
				holdBackQueue.remove(holdMsgPacket);
				return holdMsgPacket;
			}
		}
		return null;
	}
	
	/**
	 * Add to Message Packet to Holdbak Queue
	 */
	public void addToDeliveryQueue(MessagePacket messagePacket)
	{			
		deliveryQueue.add(messagePacket);
	}
	
	/**
	 * Check and deliver the message from the delivery queue 
	 * if the sequence number matches
	 * 
	 * @param deliverySeqNo - Sequence number of the message
	 * @return - MessagePacket if the sequenceNo matches else null
	 */
	public MessagePacket checkAndDeliver(String deliverySeqNo)
	{
		if(deliveryQueue != null)
		{
			if(deliveryQueue.size() > 0)
			{
				MessagePacket topItem = deliveryQueue.get(0);
				if(topItem.getSeqNumber().equals(deliverySeqNo))
				{
					deliveryQueue.remove(topItem);					
					return topItem;
				}
			}
		}
		return null;
	}
	

}
