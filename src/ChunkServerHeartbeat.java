import java.util.TimerTask;
import java.util.ArrayList;
import java.io.*;
import java.net.*;

import Data.*;
import Packets.*;

public class ChunkServerHeartbeat extends TimerTask {
	public String ConIP, ConPort, MyIP;
	public int MyPort, timecount = 0;
	public ChunkList cklist;

	public ChunkServerHeartbeat(String Con_IP, String Con_Port, String My_IP,
			int My_Port, ChunkList ck_list) {
		ConIP = Con_IP;
		ConPort = Con_Port;
		MyIP = My_IP;
		MyPort = My_Port;
		cklist = ck_list;
	}

	public void run() {
		try {
			ArrayList<String> newchunklist = new ArrayList<String>();
			ArrayList<String> corruptedchunklist = new ArrayList<String>();
			timecount++;
			// Check if 5 mins if no then Send 2 else Send 3
			if (timecount < 10) {
				// /MinorHB
				Socket skt = new Socket(ConIP, Integer.parseInt(ConPort));
				ObjectOutputStream out = new ObjectOutputStream(skt
						.getOutputStream());
				// ///msg type////
				MsgType type = new MsgType(2);
				out.writeObject(type);
				// //msg/////
				MinorHB MsgObj2 = new MinorHB();
				MsgObj2.CSIP = MyIP;
				MsgObj2.CSPort = MyPort;

				// / Find New Chunks
				for (int i = 0; i < cklist.cslist.size(); i++) {
					if (cklist.cslist.get(i).getNewflag()) {
						newchunklist.add(cklist.cslist.get(i).getChunkName());
						cklist.cslist.get(i).setNewflag(false);
					}
				}
				MsgObj2.NewChunks = newchunklist;

				// / Find Corrupted Chunks
				for (int i = 0; i < cklist.cslist.size(); i++) {
					if (cklist.cslist.get(i).getCorruptedflag()) {
						corruptedchunklist.add(cklist.cslist.get(i)
								.getChunkName());
					}
				}
				MsgObj2.CorruptedChunks = corruptedchunklist;

				// / Find Total Chunks
				MsgObj2.TotalNumberOfChunks = cklist.cslist.size();

				// / Find FreeSpace
				File dir = new File("/tmp");
				MsgObj2.FreeSpace = dir.getFreeSpace();

				out.writeObject(MsgObj2);
				out.flush();
				//System.out.println("[ChunkServer::"+MyIP+"]-Minor HeartBeat Sent to Controller");
			} else {
				// /MajorHB
				ArrayList<String> allchunklist = new ArrayList<String>();
				timecount = 0;
				Socket skt = new Socket(ConIP, Integer.parseInt(ConPort));
				ObjectOutputStream out = new ObjectOutputStream(skt
						.getOutputStream());
				// ///msg type////
				MsgType type = new MsgType(3);
				out.writeObject(type);
				// //msg/////
				MajorHB MsgObj3 = new MajorHB();
				
				MsgObj3.CSIP = MyIP;
				MsgObj3.CSPort = MyPort;

				// / Find MetaData
				for (int i = 0; i < cklist.cslist.size(); i++) {
					allchunklist.add(cklist.cslist.get(i).getChunkName());
				}
				MsgObj3.MetaDataOfAllChunks = allchunklist;
				// / Find Total Chunks
				MsgObj3.NumberOfChunks = cklist.cslist.size();
				// / Find FreeSpace
				File dir = new File("/tmp");
				MsgObj3.FreeSpace = dir.getFreeSpace();

				out.writeObject(MsgObj3);
				out.flush();
				System.out.println("[ChunkServer::"+MyIP+"]-Major HeartBeat at "+MsgObj3.CSIP+" Sent to Controller");
			}

		} catch (Exception e) {
			System.out.println("Controller is Dead");

		}

	}
}
