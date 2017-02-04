package Data;

import java.util.ArrayList;

public class ChunkServTable {
	public ArrayList<CSData> cstable = new ArrayList<CSData>();

	// private int length;

	public synchronized void add(CSData cs) {
		cstable.add(cs);
	}

	public synchronized void removeCS(String cs) {
		// int index = cstable.indexOf(cs);
		for (int i = 0; i < cstable.size(); i++) {
			if ((cstable.get(i).getCSIP()).equals(cs)) {
				cstable.remove(i);
			}
		}
	}

	public synchronized int getLength() {
		return cstable.size();
	}

	public synchronized CSData getElement(int i) {
		return cstable.get(i);
	}
	
	public synchronized int getIndex(String csname){
		for (int i = 0; i < cstable.size(); i++) {
			if ((cstable.get(i).getCSIP()).equals(csname)) {
				return i;
			}
		}
		return -1;
	}

	public synchronized boolean exist(CSData cs) {
		boolean doesexits = false;
		for (int i = 0; i < cstable.size(); i++) {
			if ((cstable.get(i).getCSIP()).equals(cs.csIP))
				doesexits = true;
		}
		return doesexits;
	}

	public synchronized int getSuitableCS(int first, int second){
		//get the cs index who holds least chunknum, and not i or j, if the first or the second is not exist, set it -1 
		display();		
		int pos = 0;
		int i;
		if(first == -1 && second == -1){
			int tmp=cstable.get(0).totalNumOfChunks;		
			for(i=0; i<cstable.size(); i++){
				if(tmp>cstable.get(i).totalNumOfChunks){
					tmp = cstable.get(i).totalNumOfChunks;
					pos = i;
				}
			}
			return pos;
		}
		
		if(first != -1 && second == -1){
			int tmp=cstable.get(0).totalNumOfChunks;
			if(first == 0){
				tmp=cstable.get(1).totalNumOfChunks;
				pos = 1;
			}
			for(i=0; i<cstable.size(); i++){
				if(tmp>cstable.get(i).totalNumOfChunks && i != first){
					tmp = cstable.get(i).totalNumOfChunks;
					pos = i;
				}
			}
			return pos;
		}
		
		if(first != -1 && second != -1){
			int tmp=cstable.get(0).totalNumOfChunks;
			if(first == 0 || second == 0){
				if(first == 1 || second == 1){
					tmp=cstable.get(2).totalNumOfChunks;
					pos = 2;
				}
				else{
					tmp=cstable.get(1).totalNumOfChunks;
					pos = 1;
				}
			}
				
			for(i=0; i<cstable.size(); i++){
				if(tmp>cstable.get(i).totalNumOfChunks && i != first && i != second){
					tmp = cstable.get(i).totalNumOfChunks;
					pos = i;
				}
			}
			return pos;
		}
		return -1;
	}
	
	public synchronized void display() {
		System.out.println("cstable:");
		System.out.println("____________________________________________");
		for (int i = 0; i < cstable.size(); i++) {
			System.out.println("| " + i + " | " + cstable.get(i).getCSIP()
					+ " | " + cstable.get(i).getCSPort() + " |"
					+ cstable.get(i).getFreeSpace()+ " |"
					+cstable.get(i).totalNumOfChunks);
		}
		System.out.println("____________________________________________");
	}
}
