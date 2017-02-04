package Data;

public class ChunkInfo {
	private String ckname;
	private int version = 0;
	public boolean corrupted = false;
	public boolean NewChunk = true;
	
	public ChunkInfo(String name){
		this.ckname = name;
	}
	
	public String getChunkName(){
		return this.ckname;
	}
	
	public void addVersion(){
		version++;
	}
	
	public int getVersion(){
		return version;
	}
	
	public void setCorruptedflag(boolean flag){//true means corruped
		this.corrupted = flag;
	}
	
	public boolean getCorruptedflag(){
		return this.corrupted;
	}

	public void setNewflag(boolean flag){//true means New Chunk
		this.NewChunk = flag;
	}
	
	public boolean getNewflag(){
		return this.NewChunk;
	}
}
