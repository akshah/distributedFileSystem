import java.util.TimerTask;
import java.util.ArrayList;
import java.io.*;
import java.net.*;

import Data.*;
import Packets.*;

public class ControllerHeartbeat extends TimerTask {
	public ChunkServTable cstable;
	public ChunkAndCSTable cAndcs;

	public ControllerHeartbeat(ChunkServTable cst, ChunkAndCSTable ccst) {
		this.cstable = cst;
		this.cAndcs = ccst;
	}

	public void run() {
		// Send Msg 5
		for (int i = 0; i < cstable.getLength(); i++) {
			try {
				Socket skt = new Socket(cstable.getElement(i).getCSIP(),
						cstable.getElement(i).getCSPort());
				ObjectOutputStream out = new ObjectOutputStream(skt
						.getOutputStream());
				// ///msg type////
				MsgType type = new MsgType(5);
				out.writeObject(type);
				// Receive Msg 12
				ObjectInputStream in = new ObjectInputStream(skt
						.getInputStream());
				MsgType typeRev = (MsgType) in.readObject();
			} catch (Exception e) {
				// CS is Dead, remove its name from CSTable and CAndCS, Maintain
				// Replication Level
				String deadCS = cstable.cstable.get(i).getCSIP();
				System.out.println("ChunkServer at "
						+ deadCS + " is Dead");
				// RemoveThisCS
				System.out.println("ChunkServer at "
						+ deadCS + " Removed");
				cstable.removeCS(deadCS);
				System.out.println("ChunkServer Table:");
				cstable.display();
				try {
					Thread.sleep(10);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				MaintainReplicationLevel(deadCS);
				
			}
			
		}// Loop for Next CS

	}

	public void MaintainReplicationLevel(String csIP) {
		/*
		 * Loop through ChunkAndChunkServer Table, For All the Chunks that have
		 * this CS: Choose a CS replace is IP and Port in place of the dead CS
		 * and tell New to CS to get the chunk[Send 13]
		 */
		for (int i = 0; i < cAndcs.cAndcs_table.size(); i++) {
			try {
				for (int j = 0; j < 3; j++) {
					String CheckIP = cAndcs.getElement(i).getCSInfo(j).getCSIP();
					String ChunknameToReplace;
///replace the IP but not with the other two IP
					if (CheckIP.equals(csIP)) {

						ChunknameToReplace = cAndcs.getElement(i)
								.getChunkName();
						// /Get Suitable CS
						int index1, index2;
						int cs1=-1,cs2=-1;
						if(j==0){
							index1 = 1;
							index2 = 2;
							cs1 = cstable.getIndex(cAndcs.getElement(i).getCSInfo(index1).getCSIP());
							cs2 = cstable.getIndex(cAndcs.getElement(i).getCSInfo(index2).getCSIP());
						}else if(j==1){
							index1 = 0;
							index2 = 2;
							cs1 = cstable.getIndex(cAndcs.getElement(i).getCSInfo(index1).getCSIP());
							cs2 = cstable.getIndex(cAndcs.getElement(i).getCSInfo(index2).getCSIP());
						}else if(j==2){
							index1 = 0;
							index2 = 1;
							cs1 = cstable.getIndex(cAndcs.getElement(i).getCSInfo(index1).getCSIP());
							cs2 = cstable.getIndex(cAndcs.getElement(i).getCSInfo(index2).getCSIP());
						}
						int indexOfSuitableCS = 0;
						if(cs1!=cs2)
							indexOfSuitableCS = cstable.getSuitableCS(cs1, cs2);
						cstable.getElement(indexOfSuitableCS).totalNumOfChunks ++;//must add 1, or everytime get the same index
						/////////////////////
						
						String NewCSIP = cstable.getElement(indexOfSuitableCS)
								.getCSIP();
						int NewCSPort = cstable.getElement(indexOfSuitableCS)
								.getCSPort();
						CSData csd = new CSData();
						csd.setCSIP(NewCSIP);
						csd.setCSPort(NewCSPort);
						cAndcs.getElement(i).setCS(csd, j);
						Socket skt = new Socket(NewCSIP, NewCSPort);
						ObjectOutputStream out = new ObjectOutputStream(skt.getOutputStream());
						ResponseCSList rlist = new ResponseCSList();
						/*********************************************/
						int pos = cAndcs.exist(ChunknameToReplace);

						if (pos != -1) {
							for (int n = 0; n < 3; n++) {
								rlist.addItem(cAndcs.getElement(pos).getCSInfo(
										n));
							}
						}
						/********************************************/

						//System.out.println("[Controller]-rlist's length is "+ rlist.getItemNum());
						rlist.display();
						/*
						 * rlist.ipCS1 =
						 * cstable.getElement(random[1]).getCSIP();
						 * rlist.portCS1 =
						 * cstable.getElement(random[1]).getCSPort();
						 */
			
						rlist.Chunkname = ChunknameToReplace;
						MsgType type = new MsgType(13);
						out.writeObject(type);
						out.flush();
						out.writeObject(rlist);
						System.out.println("[Controller]-cslist sent for Maintaining the Replication level");
						out.flush();
						out.close();
					}
				}
				//Thread.sleep(2000);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(0);
			}
		}
	}
}
