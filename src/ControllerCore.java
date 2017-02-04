import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.ArrayList;
import java.util.*;

import Data.*;
import Data.CSData;
import Packets.*;

public class ControllerCore extends Thread {

	public ChunkServTable cstable;
	public String recv;
	public static String hostname = null, ip = null;
	public int portno = 3157;

	public ChunkAndCSTable cAndcs;

	public ControllerCore(ChunkServTable cst, ChunkAndCSTable ccst) {
		this.cstable = cst;
		this.cAndcs = ccst;
	}

	public void run() {
		try {
			ServerSocket s;
			Socket soc;

			// Get IP Address
			ip = InetAddress.getLocalHost().getHostAddress();
			// Get hostname
			hostname = InetAddress.getLocalHost().getHostName();
			Random randomGenerator = new Random();
			/*
			 * portno = randomGenerator.nextInt(55555); if (portno < 1024)
			 * portno = portno + 3235;
			 */
			s = new ServerSocket(portno);
			System.out.println("Controller at " + hostname + " " + ip + "/"
					+ portno);

			while (true) {
				// System.out.println("[Controller]-Ready...");
				soc = s.accept();
				MsgType type;
				ObjectInputStream in = new ObjectInputStream(soc
						.getInputStream());
				type = (MsgType) in.readObject();

				switch (type.MType) {
				case 1:
					ConRegisterCS(soc, in);
					break;
				case 2:
					ConMinorHB(soc, in);
					break;
				case 3:
					ConMajorHB(soc, in);
					break;
				case 4:
					ConCSList(soc, in);
					break;
				case 8:
					ConRetrieve(soc, in);
					break;
				case 12:
					ConOkay(soc, in);
					break;
				default:
					break;
				}

				// Close Socket
				in.close();
				soc.close();

			}// End of While
		} catch (Exception e) {
		}
	}

	// Receive Msg 1
	public void ConRegisterCS(Socket soc, ObjectInputStream in)
			throws Exception {
		// System.out.println("someone register");
		// Store Details of Chunk Server
		RegisterCS MsgObj1;
		MsgObj1 = (RegisterCS) in.readObject();

		CSData csitem = new CSData();
		csitem.setCSIP(MsgObj1.MyIP);
		csitem.setCSPort(MsgObj1.MyPort);
		csitem.setFreeSpace(MsgObj1.FreeSpace);
		if (!cstable.exist(csitem)) {
			cstable.add(csitem);
		} else {
			System.out.println("[Controller]-this cs already exists!");
		}

		cstable.display();
	}

	// Receive Msg 2
	public void ConMinorHB(Socket soc, ObjectInputStream in) {
		try {
			MinorHB MsgObj2;
			MsgObj2 = (MinorHB) in.readObject();
			//System.out.println("MinorHB of ChunkServer at " + MsgObj2.CSIP+" Received.");
			// Check if there is Entry if this CS
			CSData csitem = new CSData();
			csitem.setCSIP(MsgObj2.CSIP);
			csitem.setCSPort(MsgObj2.CSPort);
			csitem.setFreeSpace(MsgObj2.FreeSpace);
			csitem.totalNumOfChunks = MsgObj2.TotalNumberOfChunks;
			// If there are Corrupted Chunks Send 6
			if (MsgObj2.CorruptedChunks.size() > 0) {
				for (int i = 0; i < MsgObj2.CorruptedChunks.size(); i++) {
					try {
						Socket skt = new Socket(MsgObj2.CSIP, MsgObj2.CSPort);
						ObjectOutputStream out = new ObjectOutputStream(skt
								.getOutputStream());
						// ///msg type////
						MsgType type6 = new MsgType(6);
						out.writeObject(type6);
						// /Send 6
						ResponseCSList rlist = new ResponseCSList();
						/*********************************************/
						int pos = cAndcs.exist(MsgObj2.CorruptedChunks.get(i));

						if (pos != -1) {
							for (int r = 0; r < 3; r++) {
								rlist.addItem(cAndcs.getElement(pos).getCSInfo(
										r));
							}
						}
						/********************************************/

						rlist.display();
						/*
						 * rlist.ipCS1 =
						 * cstable.getElement(random[1]).getCSIP();
						 * rlist.portCS1 =
						 * cstable.getElement(random[1]).getCSPort();
						 */
						rlist.Chunkname = MsgObj2.CorruptedChunks.get(i);

						out.flush();
						out.writeObject(rlist);
						System.out
								.println("[Controller]-cslist has sent to update corrupted chunks");
						out.close();
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			}
			// }
			// If No then make an Entry and Send 11
			if (!cstable.exist(csitem)) {
				cstable.add(csitem);
				cstable.display();
				// Send 11
				Socket skt = new Socket(MsgObj2.CSIP, MsgObj2.CSPort);
				ObjectOutputStream out = new ObjectOutputStream(skt
						.getOutputStream());
				// ///msg type////
				MsgType type = new MsgType(11);
				out.writeObject(type);
				out.close();
				// Check If there is Entry for all the chunks for this CS
				// System.out.println("Numberof New Chunks at " + MsgObj2.CSIP+
				// " are " + MsgObj2.NewChunks.size());
				for (int i = 0; i < MsgObj2.NewChunks.size(); i++) {
					ChunkData ckdata = new ChunkData();
					int flag = cAndcs.exist(MsgObj2.NewChunks.get(i));
					// If No Make Entry
					if (flag == -1) {
						ckdata.setChunkName(MsgObj2.NewChunks.get(i));
						ckdata.setCS(csitem, 0);
						cAndcs.add(ckdata);
						cAndcs.display();
					}
					// If Yes then return
					else {
						ChunkData ckDataInTable = cAndcs.getElement(flag);
						int csnum = ckDataInTable.CSNum();
						if (csnum == 1) {
							cAndcs.getElement(flag).setCS(csitem, 1);
							cAndcs.display();
						} else if (csnum == 2) {
							cAndcs.getElement(flag).setCS(csitem, 2);
							cAndcs.display();
						}
						/*
						 * if (ckdata.getCSInfo(1).equals(null))
						 * ckdata.setCS(csitem, 1); else { if
						 * (ckdata.getCSInfo(2).equals(null))
						 * ckdata.setCS(csitem, 2); } cAndcs.add(ckdata);
						 */						
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

	}

	// Receive Msg 3
	public void ConMajorHB(Socket soc, ObjectInputStream in) {
		try {
			MajorHB MsgObj3;
			MsgObj3 = (MajorHB) in.readObject();
			System.out.println("MajorHB of ChunkServer at " + MsgObj3.CSIP
					+ " Received.");
			// Check Entries (CS and Chunks hold by it) if there isn't then
			// update
			CSData csitem = new CSData();
			csitem.setCSIP(MsgObj3.CSIP);
			csitem.setCSPort(MsgObj3.CSPort);
			for (int i = 0; i < MsgObj3.MetaDataOfAllChunks.size(); i++) {
				ChunkData ckdata = new ChunkData();
				int flag = cAndcs.exist(MsgObj3.MetaDataOfAllChunks.get(i));
				// If No Make Entry
				if (flag == -1) {
					ckdata.setChunkName(MsgObj3.MetaDataOfAllChunks.get(i));
					ckdata.setCS(csitem, 0);
					cAndcs.add(ckdata);
					cAndcs.display();
				}
				// If Yes then return
				else {
					ChunkData ckDataInTable = cAndcs.getElement(flag);
					int csnum = ckDataInTable.CSNum();
					if (csnum == 1) {
						cAndcs.getElement(flag).setCS(csitem, 1);
						cAndcs.display();
					} else if (csnum == 2) {
						cAndcs.getElement(flag).setCS(csitem, 2);
						cAndcs.display();
					}
					//cAndcs.display();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

	}

	// Receive Msg 4
	public void ConCSList(Socket soc, ObjectInputStream in) {
		// System.out.println("[Controller]-store query, should choose 3 cs and return 6");
		// System.out.println("[Controller]-now, the cstable has " +
		// cstable.getLength());
		// Pick 3 CS and Send 6

		try {
			ResponseCSList rlist = new ResponseCSList();

			RequestCSList MsgObj4 = new RequestCSList();
			MsgObj4 = (RequestCSList) in.readObject();
			String chunkname = MsgObj4.ChunkName;
			/****************/
			// check if the chunkname in the cktable or not
			int flag = cAndcs.exist(chunkname);
			if (flag == -1) {
				// System.out.println("a new chunk");

				// System.out.println("chunkname is "+MsgObj4.ChunkName);
				// int random1 = getRandom();
				// System.out.println("random is "+random1);

				// get 3 random num, select on free space, not random num
				int csTableLen = cstable.getLength();
				if (csTableLen >= 3) {
					int[] random = new int[3];
					random[0] = cstable.getSuitableCS(-1, -1);
					// System.out.println("first num is "+random[0]);
					random[1] = cstable.getSuitableCS(random[0], -1);
					// System.out.println("second num is "+random[1]);
					random[2] = cstable.getSuitableCS(random[0], random[1]);
					// System.out.println("third num is "+random[2]);

					/*
					 * int[] random = new int[3]; random[0] = getRandom(); while
					 * ((random[1] = getRandom()) == random[0]) { //
					 * System.out.println
					 * ("[Controller]-regenerate the random num2"); } while
					 * (true) { random[2] = getRandom(); if (random[2] ==
					 * random[1] || random[2] == random[0]) { //
					 * System.out.println
					 * ("[Controller]-regenerate the random num3"); } else
					 * break; }
					 */
					// /////all random num done////////////

					// ///msg type////
					for (int i = 0; i < 3; i++) {
						// System.out.println("The "+i+" num is "+random[i]);
						/*************** test ********************/
						cstable.getElement(random[i]).totalNumOfChunks = cstable
								.getElement(random[i]).totalNumOfChunks + 1;
						// System.out.println("The "+random[i]+" pos is "+cstable.getElement(random[i]).totalNumOfChunks);
						/*************** test ********************/
						rlist.addItem(cstable.getElement(random[i]));
					}
					rlist.display();

					// //////////////////////////////////////////////////////////////////////////
					/*------------------------------*/
					// add chunk and cs list into table
					ChunkData ckdata = new ChunkData();
					ckdata.setChunkName(MsgObj4.ChunkName);
					for (int i = 0; i < 3; i++) {
						ckdata.setCS(cstable.getElement(random[i]), i);
					}
					cAndcs.add(ckdata);
					cAndcs.display();
				}
				/*------------------------------*/
				// //////////////////////////////////////////////////////////////////////////
			} else {

				// get cs to store chunk, and add into rlist to return
				
				for (int i = 0; i < 3; i++) {
					rlist.addItem(cAndcs.getElement(flag).getCSInfo(i));
				}
				// rlist.display();
			}
			/*
			 * rlist.ipCS1 = cstable.getElement(random[1]).getCSIP();
			 * rlist.portCS1 = cstable.getElement(random[1]).getCSPort();
			 */
			OutputStream out2Client = soc.getOutputStream();
			ObjectOutputStream objectout2Client = new ObjectOutputStream(
					out2Client);
			objectout2Client.flush();
			objectout2Client.writeObject(rlist);
			System.out.println("[Controller]-cslist has sent");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Receive Msg 8
	public void ConRetrieve(Socket soc, ObjectInputStream in) {
		System.out
				.println("[Controller]-retrive, should choose 3 cs and return 6");
		boolean check = false;// check means if there is a cs holds the chunk

		// Pick 3 CS and Send 6
		try {
			/*-----------step 1------------------*/
			// get the chunkname
			DataInputStream inputStream = new DataInputStream(
					new BufferedInputStream(soc.getInputStream()));
			String chunkname = inputStream.readUTF();
			//System.out.println("chunk name is " + chunkname);
			/*-----------step 2------------------*/
			// should check the ck&cstable, whether the chunkname is in this
			// table,yes->check is true
			// if (cstable.getLength() >= 3)
			// check = true;
			/*
			 * int pos = 0; for (int i = 0; i < cAndcs.getLength(); i++) { if
			 * (chunkname.equals(cAndcs.getElement(i).getChunkName())) { check =
			 * true; pos = i; } }
			 */
			/*-----------step 3------------------*/
			// add cs which contains chunk into cslist
			ResponseCSList rlist = new ResponseCSList();
			/*********************************************/
			int pos = cAndcs.exist(chunkname);

			if (pos != -1) {
				for (int i = 0; i < 3; i++) {
					rlist.addItem(cAndcs.getElement(pos).getCSInfo(i));
				}
			}
			/********************************************/
			/*
			 * System.out.println("[Controller]-rlist's length is " +
			 * rlist.getItemNum()); rlist.display();
			 */
			/*
			 * rlist.ipCS1 = cstable.getElement(random[1]).getCSIP();
			 * rlist.portCS1 = cstable.getElement(random[1]).getCSPort();
			 */
			OutputStream out2Client = soc.getOutputStream();
			ObjectOutputStream objectout2Client = new ObjectOutputStream(
					out2Client);
			objectout2Client.flush();
			objectout2Client.writeObject(rlist);
			System.out.println("[Controller]-cslist has sent");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Receive Msg 12
	public void ConOkay(Socket soc, ObjectInputStream in) {
		System.out.println("[Controller]-cs return still alive");
		// Return
	}

	public int getRandom() {
		Random random = new Random();
		return Math.abs(random.nextInt() % cstable.getLength());
	}
}
