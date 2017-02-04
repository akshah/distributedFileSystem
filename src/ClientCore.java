import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import Data.Chunk;
import Packets.*;

public class ClientCore {

	public String file;
	public String ConIP,ConPort;
	public ClientCore(String recv,String Con_IP, String Con_Port) {
		file = recv;
		ConIP=Con_IP;
		ConPort=Con_Port;
	}


	public void StoreData() {
		try{
		// Make Chunk Name
		Chunk newChunk = new Chunk();
		int chunkNum = newChunk.split(file);// split the file into chunks
		String filename = newChunk.getFileName();
		//System.out.println("filename is " + filename);
		for (int i = 1; i <= chunkNum; i++) {
			sendChunk(filename + "_chunk" + i);// test the one chunk
			File delete_chunks=new File(filename+"_chunk"+i);
			boolean success = delete_chunks.delete();	
			// should have a rest here
			//Thread.sleep(1000);
		}
		}catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}
	}

	public void RetrieveData() throws IOException {
		int chunkCount = 1;
		/*--------retrive files-------------*/
		while (true) {
			/*if (chunkCount > 10)// just for test, set less than 10
				break;
			else*/ 
			int flag = getChunk(file + "_chunk" + chunkCount);
			if (flag == 0)
				chunkCount++;
			else if(flag == -1){
				//System.out.println("no more");
				chunkCount--;
				break;
			}else if(flag == -2){
				System.out.println("Chunk broken");
				System.exit(0);
			}
		}
		/*---------combine files-------------*/
		// reconstruct all the files
		System.out.println("combine files");
		Chunk combineck = new Chunk();
		combineck.reconstruct("Retrieved Files/"+file, chunkCount);
	}

	public void sendChunk(String chunkstring) {
		System.out.println("[client]-will send " + chunkstring);
		try {
			/*-----------------------------*/
			// get cslist from controller
			// Send Message 4
			Socket askController = new Socket(ConIP, Integer.parseInt(ConPort));
			OutputStream out2Controller = askController.getOutputStream();
			ObjectOutputStream objectOut2Controller = new ObjectOutputStream(
					out2Controller);
			// ///msg type////
			MsgType type = new MsgType(4);
			objectOut2Controller.writeObject(type);
			objectOut2Controller.flush();

			RequestCSList MsgObj4= new RequestCSList();
			MsgObj4.ChunkName=chunkstring;
			objectOut2Controller.writeObject(MsgObj4);
			objectOut2Controller.flush();
			// objectOut.close();
			//System.out.println("[client]-type 4 sent");

			/*-----------------------------*/
			// Read Message 6

			// System.out.println("try to rec msg 6");
			InputStream in = askController.getInputStream();
			ObjectInputStream objectIn = new ObjectInputStream(in);
			// System.out.println("try to create object for rec msg 6");
			ResponseCSList rlist;
			rlist = (ResponseCSList) objectIn.readObject();
			rlist.display();

			//System.out.println("[client]-type 6 msg received");// +rlist.ipCS1);
			objectOut2Controller.close();
			objectIn.close();
			askController.close();
			/*-----------------------------*/
			// Send Message 7

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
			type = new MsgType(7);
			objectOut.writeObject(type);
			objectOut.flush();
			// objectOut.close();
			//System.out.println("[client]-type 7 sent");

			// send cslist
			objectOut.writeObject(rlist);
			/*-----------------------------*/

			// should send all the data
			File file = new File(chunkstring);
			DataInputStream readFromFile = new DataInputStream(
					new BufferedInputStream(new FileInputStream(chunkstring)));
			DataOutputStream putIntoSock = new DataOutputStream(out);
			putIntoSock.writeUTF(file.getName());
			putIntoSock.flush();
			//putIntoSock.writeLong((long) file.length());
			//putIntoSock.flush();

			int bufferSize = 10000;
			byte[] buf = new byte[bufferSize];
			int count = 0;
			while (true) {
				int read = 0;
				if (readFromFile != null) {
					read = readFromFile.read(buf);
				}
				if (read == -1) {
					break;
				}
				putIntoSock.write(buf, 0, read);
				//System.out.println("send datanum ::"+read);
				count = count + read;
			}
			putIntoSock.flush();
			readFromFile.close();
			sendck.close();
			 
			System.out.println("[client]-File " + chunkstring + "  transmited!");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public int getChunk(String chunkstring) {
		try {
			File chunk = new File(chunkstring);
			// Send Message 8
			// get cslist from controller
			// Send Message 4
			/*-----------step 1------------------*/
			Socket askController = new Socket(ConIP,Integer.parseInt(ConPort));
			OutputStream out2Controller = askController.getOutputStream();
			ObjectOutputStream objectOut2Controller = new ObjectOutputStream(
					out2Controller);
			// ///msg type////
			MsgType type = new MsgType(8);
			objectOut2Controller.writeObject(type);
			// objectOut.close();
			//System.out.println("[client]-type 8 sent");

			/*------------step 2-----------------*/
			// send the filename
			DataOutputStream name2Controller = new DataOutputStream(
					out2Controller);
			int offset = chunkstring.lastIndexOf('/');
			String chunkname = chunkstring.substring(offset + 1,
					chunkstring.length()).trim();
			name2Controller.writeUTF(chunkname);
			name2Controller.flush();

			/*-------------step 3----------------*/
			// Read Message 6
			InputStream in = askController.getInputStream();
			ObjectInputStream objectIn = new ObjectInputStream(in);
			// System.out.println("try to create object for rec msg 6");
			ResponseCSList rlist;
			rlist = (ResponseCSList) objectIn.readObject();

			//System.out.println("[client]-type 6 msg received");// +rlist.ipCS1);
			objectOut2Controller.close();
			objectIn.close();
			askController.close();
			// check if there are some cs hold the filename
			if (rlist.getItemNum() == 0){
				//System.out.println("[client]-the cslist received is empty->no data");
				return -1;
			}else{
				//System.out.println("[client]-the cslist has "+rlist.getItemNum()+" entries");
			}

			/*-------------step 4----------------*/
			// Connect to CS 1 (Make func for this)
			// Send Message 7
			// Read Message 9 or 10
			// If Message = 10
			// Connect to CS 2
			// Read Message 9 or 10
			// If Message = 10
			// Connect to CS 3
			// Read Message 9 or 10
			// If Message = 10
			// Connect to Controller and loop back
			// if chunklist != null, return true;
			for(int i=0; i<3; i++){
			/*-----------substep 1------------------*/
			//connect the first cs in the list//
			String csIP = rlist.getItem(0).getCSIP();
			int csPort = rlist.getItem(0).getCSPort();
			rlist.rmItem(0);
			
			Socket retrivck = new Socket(csIP, csPort);
			OutputStream out = retrivck.getOutputStream();
			ObjectOutputStream objectOut = new ObjectOutputStream(out);
			
			/*-----------substep 2------------------*/
			//send msg 8 to the cs//
			// ///msg type////
			type = new MsgType(8);
			objectOut.writeObject(type);
			objectOut.flush();
			// objectOut.close();
			//System.out.println("[client]-type 8 sent");
			
			// send the filename
			DataOutputStream name2cs = new DataOutputStream(out);
			name2cs.writeUTF(chunkname);
			name2cs.flush();
			
			/*-----------substep 3------------------*/
			///received msg type, if 9, store; if 10, connect the next one//
			//receive 10
			InputStream retrivIn = retrivck.getInputStream();
			/*
			ObjectInputStream retrivMsgIn = new ObjectInputStream(retrivIn);
			// System.out.println("try to create object for rec msg 10");
			MsgType msgtype = (MsgType) retrivMsgIn.readObject();
			*/
			int msgtype;
			DataInputStream dataFromcs = new DataInputStream(
					new BufferedInputStream(retrivIn));
			msgtype = dataFromcs.readInt();
			if(msgtype == 10){
				System.out.println("[client]-Data at CS is corrupted");// +rlist.ipCS1);
				dataFromcs.close();
				/*************************/
				//try next chunk
				/*************************/
				//return -2;
				continue;
			}				
			else if(msgtype == 9){
				
				//rec chunkdata
				/*-----------receive data------------*/
				//System.out.println("[client]-get msg 9");

				// get current path & create a directory new

				String savePath = "Retrieved Files/";
				//System.out.println("path is " + savePath);
				File mkTest = new File(savePath);
				mkTest.mkdir();


				int bufferSize = 10000;
				byte[] buf = new byte[bufferSize];
				int passedlen = 0;
				long len = 0;

				//String filename = inputStream.readUTF();
				//System.out.println("[client]-file name is " + filename);
				savePath += chunkname;
				DataOutputStream fileOut = new DataOutputStream(
						new BufferedOutputStream(new BufferedOutputStream(
								new FileOutputStream(savePath))));
				//len = inputStream.readLong();
				//System.out.println("[client]-retriving file'size is "+len);
				//System.out.println("[client]-retriving...");
				//int count = 0;
				while (true) {
					int read = 0;
					if (dataFromcs != null) {
						read = dataFromcs.read(buf);
						//System.out.println("readnum is ::"+read);
					}
					passedlen += read;
					if (read == -1) {
						break;
					}

					fileOut.write(buf, 0, read);
				}
				fileOut.close();
				//System.out.println("write len ::"+passedlen);
				System.out.println("[client]-retrived file ["+chunkname+"]");
				break;//out this loop
			}
				/*---------receive done-------------*/
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return 0;
	}
}
