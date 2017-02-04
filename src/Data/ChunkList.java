package Data;

import java.util.ArrayList;

public class ChunkList {
	public ArrayList<ChunkInfo> cslist = new ArrayList<ChunkInfo>();
	private int corruptedCK = -1;//this is the pos of the new added one

	public synchronized void  add(ChunkInfo csinfo) {
		cslist.add(csinfo);
	}

	public synchronized void  remove(ChunkInfo csinfo) {
		// int index = cstable.indexOf(cs);
		cslist.remove(csinfo);
	}

	public synchronized int  getLength() {
		return cslist.size();
	}

	public synchronized ChunkInfo  getElement(int i) {
		return cslist.get(i);
	}
	
	public synchronized int  exist(String ckname){
		for(int i=0; i<cslist.size(); i++){
			if(ckname.equals(cslist.get(i).getChunkName())){
				return i;
			}
		}
		return -1;
	}
	
	public synchronized void  display() {
		System.out.println("ChunkList:");
		System.out.println("Num      Name        Version      Corruped     New");
		System.out.println("___________________________________________________");
		for (int i = 0; i < cslist.size(); i++) {
			System.out.println("| " + i + " | "+ cslist.get(i).getChunkName() + " | "+ cslist.get(i).getVersion()+" |"+ cslist.get(i).getCorruptedflag()+" |"+ cslist.get(i).getNewflag()+" |");
		}
		System.out.println("____________________________________________________");
	}
}
