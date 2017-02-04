package Data;
import java.io.Serializable;

public class CSData implements Serializable {
	public String csIP;
	public int csPort;
	public long freespace;
	public int totalNumOfChunks;
	
	public void setCSIP(String ip){
		this.csIP = ip;
	}
	
	public void setCSPort(int port){
		this.csPort = port;
	}
	
	public String getCSIP(){
		return csIP;
	}
	
	public int getCSPort(){
		return csPort;
	}
	
	public void setFreeSpace(long freeSpace){
		this.freespace = freeSpace;
	}
	
	public long getFreeSpace(){
		return freespace;
	}
	
}
