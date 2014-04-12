package edu.buffalo.cse.cse486586.groupmessenger;

import java.io.Serializable;

public class MessagePacket implements Serializable{
	/**
	 * Serial Version UID 
	 */
	private static final long serialVersionUID = 5393198541861509332L;
	private String msgId;
	private String msgContent;
	private String seqNumber;
	
	
	/** 
	 * Parameterized constructor initialize with Message ID and Message Content
	 * @param msgId
	 * @param msgContent
	 */
	public MessagePacket(String msgId, String msgContent) {
		super();
		this.msgId = msgId;
		this.msgContent = msgContent;		
	}
	
	/** 
	 * Parameterized constructor initialize with Message ID, Message Content and Sequence Number
	 * @param msgId - Message ID
	 * @param msgContent - Message Content
	 * @param seqNumber - Sequnce Number of the message
	 */
	public MessagePacket(String msgId, String msgContent, String seqNumber) {
		super();
		this.msgId = msgId;
		this.msgContent = msgContent;		
		this.seqNumber = seqNumber;
	}
	
	/**
	 * @return the msgId
	 */
	public String getMsgId() {
		return msgId;
	}
	/**
	 * @param msgId the msgId to set
	 */
	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}
	/**
	 * @return the msgContent
	 */
	public String getMsgContent() {
		return msgContent;
	}
	/**
	 * @param msgContent the msgContent to set
	 */
	public void setMsgContent(String msgContent) {
		this.msgContent = msgContent;
	}
	
	/**
	 * @return the seqNumber
	 */
	public String getSeqNumber() {
		return seqNumber;
	}
	/**
	 * @param seqNumber the seqNumber to set
	 */
	public void setSeqNumber(String seqNumber) {
		this.seqNumber = seqNumber;
	}		
}

/**
 * Enum to define the Message Type 
 * during the communicator between group
 * member and sequencer
 * 
 * @author Babu
 *
 */
enum MSG_TYPE
{
	MESSAGE,
	SEQ_NO
};

