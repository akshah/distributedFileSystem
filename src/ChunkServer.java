import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Timer;

import Data.ChunkList;

public class ChunkServer {
	//public String[][] ChunkTable = new String[500][3];
	
	public int FreeSpace;
	public static String ConIP,ConPort="3157";
	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Enter Controller IP/Name:");
		ConIP=br.readLine();
		
		ChunkList cklist = new ChunkList();
		ChunkServerCore Core = new ChunkServerCore(ConIP,ConPort,cklist); 
		Core.start();

		

	}// End of main
}// End of class ChunkServer

