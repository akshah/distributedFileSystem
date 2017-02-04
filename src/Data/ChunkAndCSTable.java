package Data;

import java.util.ArrayList;

public class ChunkAndCSTable {
	public ArrayList<ChunkData> cAndcs_table = new ArrayList<ChunkData>();

	public synchronized void add(ChunkData csdata) {
		cAndcs_table.add(csdata);
	}

	public synchronized void remove(ChunkData csdata) {
		// int index = cstable.indexOf(cs);
		cAndcs_table.remove(csdata);
	}

	public int getLength() {
		return cAndcs_table.size();
	}

	public synchronized ChunkData getElement(int i) {
		return cAndcs_table.get(i);
	}
	
	public synchronized int exist(String ckname){
		for(int i=0; i<cAndcs_table.size(); i++){
			if(ckname.equals(cAndcs_table.get(i).getChunkName())){
				return i;
			}
		}
		return -1;
	}

	public void display() {
		System.out.println("Chunk&CS_table:");
		System.out.println("______________________________________________________________________________");
		for (int i = 0; i < cAndcs_table.size(); i++) {
			ChunkData ckdata = cAndcs_table.get(i);
			int n = ckdata.CSNum();
			//System.out.println("ChunkData::"+ckdata.getChunkName()+" has "+n+" cs");
			System.out.print("| " + i + " | "+cAndcs_table.get(i).getChunkName() + " | ");
			for(int j=0; j<n; j++){
				CSData csdata = ckdata.getCSInfo(j);
				System.out.print(csdata.getCSIP() +":"+csdata.getCSPort()+" |");
			}
			System.out.println();
		}
		System.out.println("______________________________________________________________________________");
	}
}
