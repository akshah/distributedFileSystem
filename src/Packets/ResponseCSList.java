package Packets;

import java.io.Serializable;
import java.util.ArrayList;

import Data.CSData;

//Packet 6
public class ResponseCSList implements Serializable {
	
	private ArrayList<CSData> cslist = null;
	public String Chunkname;

	public ResponseCSList(){
		 cslist = new ArrayList<CSData>();
	}
	public void addItem(CSData cs){
		cslist.add(cs);
	}
	
	public void rmItem(CSData cs){
		cslist.remove(cs);
	}
	
	public void rmItem(int i){
		cslist.remove(i);
	}
	
	public CSData getItem(int i){
		return cslist.get(i);
	}
	
	public int getItemNum(){
		return cslist.size();
	}
	
	public void display(){
		System.out.println("returned cslist:");
		System.out.println("_______________________________");
		for(int i=0;i<cslist.size();i++){
			System.out.println("| " + i + " | "+cslist.get(i).getCSIP()+" | "+cslist.get(i).getCSPort()+" |");
		}
		System.out.println("_______________________________");
	}
	/*
	public String ipCS1;
	public int portCS1;
	public String ipCS2;
	public int portCS2;
	public String ipCS3;
	public int portCS3;
	*/
}
