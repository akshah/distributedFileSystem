package Data;

import java.io.Serializable;

public class ChunkData implements Serializable {

	private CSData[] cs = new CSData[3];
	private String chunkname;

	public synchronized void setCS(CSData newCS, int i){
		this.cs[i] = newCS;
	}
	
	public synchronized void setChunkName(String name){
		this.chunkname = name;
	}
	
	public synchronized String getChunkName(){
		return chunkname;
	}
	
	public synchronized CSData getCSInfo(int i){
		return cs[i];
	}
	
	public synchronized int CSNum(){
		int count = 0 ;
		for(int i=0; i<3; i++){
			if(cs[i]!=null)
				count++;
		}
		return count;
	}
}
