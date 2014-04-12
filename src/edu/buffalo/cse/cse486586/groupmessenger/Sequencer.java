

package edu.buffalo.cse.cse486586.groupmessenger;

/**
 * Sequencer POJO class which will maintain the sequence for Total Order
 * Singleton Class for Sequencer as there is only one sequencer
 * 
 * Contains Synchronized methods as the sequencer has to be accessed by only one thread instance
 * @author Babu
 *
 */
public class Sequencer {
	
	private static Sequencer instance = null;
	private int sequenceNumber = 0;
	private String deviceName = "";	
	public static String predefinedSeqDeviceName = "avd0";
	
	// Private constructor to defeat instantiation
	private Sequencer(String deviceName)
	{
		this.deviceName = deviceName; 
	}
	
	
	
	/**
	 * Return Sequencer instance for AVD which has created it
	 * @return Sequencer 
	 */
	public synchronized static Sequencer getInstance(String deviceName)
	{
		if(!predefinedSeqDeviceName.equals(deviceName))
			if(instance == null)			
				instance = new Sequencer(deviceName);			
		return instance;
	}
	
	/**
	 * Get Sequence Number
	 * @return sequenceNumber
	 */
	public synchronized int getSequenceNumber()
	{
		return sequenceNumber;
	}
	
	/**
	 * Increment Sequence
	 */
	public synchronized void incrementSequence() {
		sequenceNumber++;
	}
	
	/**
	 * Set Sequence Number
	 * @param seqNo
	 */
	public synchronized void setSequenceNumber(int seqNo) {
		sequenceNumber = seqNo;
	}
	
	/**
	 * Returns the sequencer device name
	 * @return Device Name of Sequencer
	 */
	public synchronized String getSequencerPortNo()
	{
		String index = predefinedSeqDeviceName.split("vd")[1];
		return (index != null) ? DeviceInfo.REMOTE_PORTS[Integer.parseInt(index)] : "11108"; 
	}
	
	

}
