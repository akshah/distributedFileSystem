import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;

import Data.Chunk;
import Data.ChunkInfo;
import Data.ChunkList;
import Packets.MajorHB;
import Packets.MsgType;
import Packets.RegisterCS;
import Packets.ResponseCSList;


public class ChunkServerAssisstant extends Thread {
	public static String ip = null, ConIP, ConPort;
	public int portno;
	public ChunkList cklist;
	private Socket soc;

	public ChunkServerAssisstant(Socket sk, String Con_IP, String Con_Port, String My_IP,
			int My_Port, ChunkList ck){
		ConIP = Con_IP;
		ConPort = Con_Port;
		cklist = ck;
		soc = sk;
		ip = My_IP;
		portno = My_Port;
	}

	public void run() {
		try {
			//ip = InetAddress.getLocalHost().getHostAddress();
			//hostname = InetAddress.getLocalHost().getHostName();
			//portno = 18989;
				MsgType type;
				ObjectInputStream in = new ObjectInputStream(soc
						.getInputStream());
				type = (MsgType) in.readObject();

				switch (type.MType) {
				case 5:
					CSControllerHB(soc, in);
					break;
				case 6:
					CSgetCSList(soc, in);
					break;
				case 7:
					CSStoreUpdate(soc, in);
					break;
				case 8:
					CSRequestRetrieve(soc, in);
					break;
				case 11:
					CSRequestMajorHB(soc, in);
					break;
				case 13:
					CSgetCSListForMaintain(soc, in);
					break;
				default:
					break;
				}
				// Close Socket
				in.close();
				soc.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}// End of run

	// Receive Msg 5
	public void CSControllerHB(Socket soc, ObjectInputStream in) {
		try {
			// Send 12 and return
			MsgType type = new MsgType(12);
			ObjectOutputStream out = new ObjectOutputStream(soc
					.getOutputStream());
			out.writeObject(type);
		} catch (Exception e) {
		}
	}

	// Receive Msg 7
	public void CSStoreUpdate(Socket soc, ObjectInputStream in) {
		// Check if Store or Update
		System.out.println("type 7 received");

		// if Store : Create MetaData, Update Chunk table, call StoreData
		// if Update : Delete Old data, Update MetaData, call StoreData

		/*------now, it's just receive and store----------*/

		// first, received the cs list
		// in.close();
		// to receive, u must close the objectInputStream
		try {

			// ObjectInputStream objectIn = new ObjectInputStream(in);
			// System.out.println("try to create object for rec msg 6");
			/*-----------receive cslist------------*/
			ResponseCSList rlist;
			rlist = (ResponseCSList) in.readObject();

			// System.out.println("[CS]-cslist received: csnum has "+
			// rlist.getItemNum());

			/*-----------receive data------------*/

			DataInputStream inputStream = new DataInputStream(
					new BufferedInputStream(soc.getInputStream()));
			// get current path & create a directory tmp
			// File directory = new File(".");
			// String savePath = directory.getCanonicalPath() + "/tmp/";
			String savePath = "/tmp/";
			// System.out.println("path is " + savePath);
			File mkTest = new File(savePath);
			mkTest.mkdir();
			/*
			 * if (mkTest.exists()) System.out.println("create sucessfully");
			 */

			int bufferSize = 10000;
			byte[] buf = new byte[bufferSize];
			int passedlen = 0;
			// long len = 0;

			String filename = inputStream.readUTF();

			int pos = cklist.exist(filename);
			savePath += filename;
			//System.out.println("[CS]-file path is " + savePath);

			if (pos != -1) {
				System.out.println("this chunk has already exists!");
				/*-----------update chunk---------------*/
				System.out.println("[CS]-file path is " + savePath);
				byte updatebuf[] = new byte[1000];
				int read = 0;
				RandomAccessFile updatefile = new RandomAccessFile(savePath,
						"rw");
				while ((read = inputStream.read(updatebuf)) != -1) {
					if (read != -1) {
						// System.out.println("File.lelngth:" + (f.length()) +
						// "B");
						//System.out.println("read is :: " + read);
						//System.out.println("File PointPosition:"+ updatefile.getFilePointer());

						updatefile.write(updatebuf, 0, read);
					}
				}

				// inputStream.close();
				updatefile.close();
				System.out.println("update done");
				/*------------modify version-------------*/
				cklist.getElement(pos).addVersion();
				cklist.getElement(pos).setNewflag(true);
				cklist.display();
				/*------------update finished-----------*/
				// get new version
				int newversion = cklist.getElement(pos).getVersion();
				Chunk newCK = new Chunk();

				// /new version is used to createChecksum
				newCK.createChecksum("/tmp/" + filename, newversion);
				System.out.println("[CS::"+ConIP+"]-metadata completed!");
				/*---------------metadata is regenerated-------------------------*/

			} else {
				/******************************/
				// should add fileinfo here
				ChunkInfo newChunk = new ChunkInfo(filename);
				newChunk.setNewflag(true);
				cklist.add(newChunk);
				cklist.display();

				DataOutputStream fileOut = new DataOutputStream(
						new BufferedOutputStream(new BufferedOutputStream(
								new FileOutputStream(savePath))));
				// len = inputStream.readLong();

				// System.out.println("[CS::"+ip+"]-receiving...");

				while (true) {
					int read = 0;
					if (inputStream != null) {
						read = inputStream.read(buf);
					}
					passedlen += read;
					if (read == -1) {
						break;
					}

					fileOut.write(buf, 0, read);
				}
				fileOut.close();
				//System.out.println("cs- read :: " + passedlen);
				// inputStream.close();
				// soc.close();
				System.out.println("[CS::"+ip+"]-received file ["+filename+"]");
				Chunk newCK = new Chunk();

				// /version must add first and then pass into createChecksum
				newCK.createChecksum("/tmp/" + filename, 0);
				System.out.println("[CS::"+ip+"]-metadata completed!");
				/*---------receive done-------------*/
			}

			/*-----------transmit to others------------*/
			if (rlist.getItemNum() != 0) {
				// Send Message 7
				System.out.println("[CS::"+ip+"]-transmit to other cs in the cslist");
				// ///
				// send flag of type 7
				// choose the first cs, and connect
				String csIP = rlist.getItem(0).getCSIP();
				int csPort = rlist.getItem(0).getCSPort();
				// System.out.println("before delete, rlist 0: "+rlist.getItem(0).getCSIP());
				rlist.rmItem(0);
				// System.out.println("after delete, rlist 0: "+rlist.getItem(0).getCSIP());
				Socket sendck = new Socket(csIP, csPort);
				OutputStream out = sendck.getOutputStream();
				ObjectOutputStream objectOut = new ObjectOutputStream(out);

				// ///msg type////
				MsgType type = new MsgType(7);
				objectOut.writeObject(type);
				objectOut.flush();
				// objectOut.close();
				// System.out.println("[CS::"+ip+"]-type 7 sent");

				// send cslist
				objectOut.writeObject(rlist);
				/*-----------------------------*/

				// should send all the data
				// File file = new File(filename);
				DataInputStream readFromFile = new DataInputStream(
						new BufferedInputStream(new FileInputStream("/tmp/"
								+ filename)));
				DataOutputStream putIntoSock = new DataOutputStream(out);
				putIntoSock.writeUTF(filename);
				putIntoSock.flush();
				// putIntoSock.writeLong((long) file.length());
				// putIntoSock.flush();

				while (true) {
					int read = 0;
					if (readFromFile != null) {
						read = readFromFile.read(buf);
					}
					if (read == -1) {
						break;
					}
					putIntoSock.write(buf, 0, read);
				}
				putIntoSock.flush();
				readFromFile.close();
				sendck.close();
				System.out.println("[CS::"+ip+"]-File " + filename + "  transmited!");
			} else {
				//System.out.println("[CS::"+ip+"]-cslist empty");
			}
		} catch (Exception e) {
			e.printStackTrace();

			System.exit(0);
		}
		// Chunk achunk = new Chunk();
		// achunk.reconstruct("a.jpeg", 3);
	}

	public String getSHA1String(MessageDigest md) {
		byte[] mdbytes = md.digest();
		StringBuffer sb = new StringBuffer("");
		for (int i = 0; i < mdbytes.length; i++) {
			sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16)
					.substring(1));
		}
		return sb.toString();
	}

	public boolean isCorruped(String chunkname) {
		try {
			String fullchunkname = "/tmp/" + chunkname;
			String chunk_metadata = "/tmp/" + chunkname + "_metadata";
			Chunk ck = new Chunk();

			FileInputStream fis = new FileInputStream(fullchunkname);
			byte[] dataBytes = new byte[1024];
			int nread = 0, count = 0;
			int flag = 0, nChecksum = 0;
			MessageDigest md = MessageDigest.getInstance("SHA1");
			// String checksum[] = new String[100];
			while ((nread = fis.read(dataBytes)) != -1) {
				count = count + nread;
				md.update(dataBytes, 0, nread);
				if (count % (8 * 1024) == 0) {
					flag = 1;
				}
				if (flag == 1 || nread < 1024) {
					String digest = getSHA1String(md);

					String checksumInfile = ck.getLine(chunk_metadata,
							nChecksum + 6);

					if (!digest.equals(checksumInfile)) {
						System.out
								.println("***********************corruped!**************************");
						/*
						System.out.println("in real, the " + (nChecksum + 1)
								+ " checksum is::" + digest);
						System.out.println("in metadata, the "
								+ (nChecksum + 1) + " checksum is::"
								+ checksumInfile);
								*/
						System.out.println(chunkname+" is corrupted at slice ["+(nChecksum+1)+"]");
						System.out
								.println("***********************corruped!**************************");
						int pos = cklist.exist(chunkname);
						cklist.getElement(pos).setCorruptedflag(true);
						return true;
					}
					nChecksum++;
					flag = 0;
					md.reset();
				}
			}
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return false;
	}

	// Receive Msg 8
	public void CSRequestRetrieve(Socket soc, ObjectInputStream in) {
		
		try {
			// get the chunkname
			DataInputStream inputStream = new DataInputStream(
					new BufferedInputStream(soc.getInputStream()));
			String chunkname = inputStream.readUTF();
			System.out.println("[CS::"+ip+"]-receive the retrive quest ["+chunkname+"]");
			/*-----------------------------------*/
			// Check if the Chunk is corrupted or not
			// If yes Send 10 else Send 9
			boolean corruped = false;
			corruped = isCorruped(chunkname);
			/*-----------------------------------*/
			DataOutputStream putIntoSock = new DataOutputStream(soc
					.getOutputStream());
			if (corruped) {
				putIntoSock.writeInt(10);
				putIntoSock.close();

				return;
			} else {
				putIntoSock.writeInt(9);
				/*-----------------------------------*/

				File file = new File("/tmp/" + chunkname);
				// System.out.println("open " + file.getName());
				DataInputStream readFromFile = new DataInputStream(
						new BufferedInputStream(new FileInputStream("/tmp/"
								+ chunkname)));

				/*
				 * putIntoSock.writeUTF(file.getName()); putIntoSock.flush();
				 */
				// putIntoSock.writeLong((long) file.length());
				// putIntoSock.flush();

				int bufferSize = 10000;
				byte[] buf = new byte[bufferSize];
				int count = 0;
				while (true) {
					int readnum = 0;
					if (readFromFile != null) {
						readnum = readFromFile.read(buf);
						count = count + readnum;
					}
					if (readnum == -1) {
						break;
					}
					putIntoSock.write(buf, 0, readnum);
				}
				// System.out.println("count is " + count);
				putIntoSock.flush();
				readFromFile.close();
			}
			// sendck.close();
			// in.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

	}

	// Receive Msg 11
	public void CSRequestMajorHB(Socket soc, ObjectInputStream in) {
		try {
			ArrayList<String> newchunklist = new ArrayList<String>();
			ArrayList<String> corruptedchunklist = new ArrayList<String>();
			ArrayList<String> allchunklist = new ArrayList<String>();
			// Send MajorHB
			System.out
					.println("[ChunkServer::"+ip+"]-Received Request for Major HeartBeat from Controller");
			Socket skt = new Socket(ConIP, Integer.parseInt(ConPort));
			ObjectOutputStream out = new ObjectOutputStream(skt
					.getOutputStream());
			// ///msg type////
			MsgType type = new MsgType(3);
			out.writeObject(type);
			MajorHB MsgObj3 = new MajorHB();
			MsgObj3.CSIP = ip;
			MsgObj3.CSPort = portno;

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
			System.out
					.println("[ChunkServer::"+ip+"]-Major HeartBeat Sent to Controller");
		} catch (Exception e) {
		}
	}

	// Receive 6
	public void CSgetCSList(Socket soc, ObjectInputStream in) {
		try {
			// System.out.println("try to create object for rec msg 6");
			ResponseCSList rlist = new ResponseCSList();
			rlist = (ResponseCSList) in.readObject();
			String chunkname = rlist.Chunkname;
			System.out.println("I got Updation Msg for " + chunkname);
			// / After Receiving 6 Connect to another CS and Send 8 (Read 9 on
			// Server Socket)
			for (int i = 0; i < 3; i++) {
				if (!(rlist.getItem(i).getCSIP().equals(ip))) {

					Socket askCS = new Socket(rlist.getItem(i).getCSIP(), rlist
							.getItem(i).getCSPort());
					OutputStream out2CS = askCS.getOutputStream();
					ObjectOutputStream objectOut2CS = new ObjectOutputStream(
							out2CS);
					// ///msg type////
					MsgType typeObj = new MsgType(8);
					objectOut2CS.writeObject(typeObj);
					objectOut2CS.flush();
					// /Send 8
					DataOutputStream name2cs = new DataOutputStream(out2CS);
					name2cs.writeUTF(chunkname);
					name2cs.flush();

					// / Receive 9 or 10
					/*-----------substep 3------------------*/
					// /received msg type, if 9, store; if 10, connect the next
					// one//
					// receive 10
					InputStream retrivIn = askCS.getInputStream();
					/*
					 * ObjectInputStream retrivMsgIn = new
					 * ObjectInputStream(retrivIn); //
					 * System.out.println("try to create object for rec msg 10"
					 * ); MsgType msgtype = (MsgType) retrivMsgIn.readObject();
					 */
					int msgtype;
					DataInputStream dataFromcs = new DataInputStream(
							new BufferedInputStream(retrivIn));
					msgtype = dataFromcs.readInt();
					if (msgtype == 10) {
						System.out
								.println("[ChunkServer::"+ip+"]-Data at Other ChunkServer is Also Corrupted");// +rlist.ipCS1);
						System.out
								.println("[ChunkServer::"+ip+"]-Connecting to Next ChunkServer");
						dataFromcs.close();
						/*************************/
						// try next chunk
						/*************************/
						// return -2;
						continue;
					} else if (msgtype == 9) {

						// rec chunkdata
						/*-----------receive data------------*/
						System.out
								.println("[ChunkServer::"+ip+"]-Got the Correct Chunk");

						// get current path & create a directory new

						String savePath = "/tmp/";

						int bufferSize = 10000;
						byte[] buf = new byte[bufferSize];
						int passedlen = 0;
						long len = 0;

						// String filename = inputStream.readUTF();
						// System.out.println("[client]-file name is " +
						// filename);
						savePath += chunkname;
						DataOutputStream fileOut = new DataOutputStream(
								new BufferedOutputStream(
										new BufferedOutputStream(
												new FileOutputStream(savePath))));
						// len = inputStream.readLong();
						// System.out.println("[client]-retriving file'size is "+len);
						// System.out.println("[client]-retriving...");
						// int count = 0;
						while (true) {
							int read = 0;
							if (dataFromcs != null) {
								read = dataFromcs.read(buf);
								// System.out.println("readnum is ::"+read);
							}
							passedlen += read;
							if (read == -1) {
								break;
							}

							fileOut.write(buf, 0, read);
						}
						fileOut.close();
						// System.out.println("write len ::"+passedlen);
						System.out
								.println("[ChunkServer::"+ip+"]-Retrived Correct Chunk Successfully");
						Chunk newCK = new Chunk();
						String chunkversion = newCK.getLine("/tmp/" + chunkname
								+ "_metadata", 2);
						// /version must add first and then pass into
						// createChecksum
						newCK.createChecksum("/tmp/" + chunkname, Integer
								.parseInt(chunkversion));
						System.out.println("[ChunkServer::"+ip+"]-Metadata Updated!");
						int pos = cklist.exist(chunkname);
						cklist.getElement(pos).setCorruptedflag(false);
						cklist.display();
						break;// out this loop
					}
					/*---------receive done-------------*/
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Receive 13
	public void CSgetCSListForMaintain(Socket soc, ObjectInputStream in) {
		try {
			// System.out.println("try to create object for rec msg 6");
			ResponseCSList rlist = new ResponseCSList();
			rlist = (ResponseCSList) in.readObject();
			String chunkname = rlist.Chunkname;
			System.out.println("I got Msg to Maintain Replication Level of "+ chunkname);
			//rlist.display();
			// / After Receiving 6 Connect to another CS and Send 8 (Read 9 on
			// Server Socket)
			for (int i = 0; i < 3; i++) {
				if (!(rlist.getItem(i).getCSIP().equals(ip))) {
					System.out.println("try to connect "+rlist.getItem(i).getCSIP()+"::"+rlist.getItem(i).getCSPort());
					Socket askCS = new Socket(rlist.getItem(i).getCSIP(), rlist
							.getItem(i).getCSPort());
					
					OutputStream out2CS = askCS.getOutputStream();
					ObjectOutputStream objectOut2CS = new ObjectOutputStream(
							out2CS);
					// ///msg type////
					MsgType typeObj = new MsgType(8);
					objectOut2CS.writeObject(typeObj);
					objectOut2CS.flush();
					// /Send 8
					DataOutputStream name2cs = new DataOutputStream(out2CS);
					name2cs.writeUTF(chunkname);
					name2cs.flush();

					// / Receive 9 or 10
					/*-----------substep 3------------------*/
					// /received msg type, if 9, store; if 10, connect the next
					// one//
					// receive 10
					InputStream retrivIn = askCS.getInputStream();
					/*
					 * ObjectInputStream retrivMsgIn = new
					 * ObjectInputStream(retrivIn); //
					 * System.out.println("try to create object for rec msg 10"
					 * ); MsgType msgtype = (MsgType) retrivMsgIn.readObject();
					 */
					int msgtype;
					DataInputStream dataFromcs = new DataInputStream(
							new BufferedInputStream(retrivIn));
					msgtype = dataFromcs.readInt();
					if (msgtype == 10) {
						System.out
								.println("[ChunkServer::"+ip+"]-Data at Other ChunkServer is Corrupted");// +rlist.ipCS1);
						System.out
								.println("[ChunkServer::"+ip+"]-Connecting to Next ChunkServer");
						dataFromcs.close();
						/*************************/
						// try next chunk
						/*************************/
						// return -2;
						continue;
					} else if (msgtype == 9) {

						// rec chunkdata
						/*-----------receive data------------*/

						// get current path & create a directory new

						String savePath = "/tmp/";

						int bufferSize = 10000;
						byte[] buf = new byte[bufferSize];
						int passedlen = 0;
						long len = 0;

						// String filename = inputStream.readUTF();
						// System.out.println("[client]-file name is " +
						// filename);
						savePath += chunkname;
						DataOutputStream fileOut = new DataOutputStream(
								new BufferedOutputStream(
										new BufferedOutputStream(
												new FileOutputStream(savePath))));
						// len = inputStream.readLong();
						// System.out.println("[client]-retriving file'size is "+len);
						// System.out.println("[client]-retriving...");
						// int count = 0;
						while (true) {
							int read = 0;
							if (dataFromcs != null) {
								read = dataFromcs.read(buf);
								// System.out.println("readnum is ::"+read);
							}
							passedlen += read;
							if (read == -1) {
								break;
							}

							fileOut.write(buf, 0, read);
						}
						fileOut.close();
						//System.out.println("write len ::"+passedlen);
						Chunk newCK = new Chunk();
						newCK.createChecksum("/tmp/" + chunkname, 0);
						
						ChunkInfo getAChunk = new ChunkInfo(chunkname);
						getAChunk.setNewflag(true);
						cklist.add(getAChunk);
						cklist.display();

						System.out
								.println("[ChunkServer::"+ip+"]-Replication level Maintained ");
						askCS.close();
						break;// out this loop
					}
					/*---------receive done-------------*/
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
}