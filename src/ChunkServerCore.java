import java.io.*;
import java.util.ArrayList;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.Timer;
import java.io.RandomAccessFile;
import java.security.MessageDigest;

import Data.*;
import Packets.*;

public class ChunkServerCore extends Thread {
	public static String hostname = null, ip = null, ConIP, ConPort;
	public int portno;
	public ChunkList cklist;

	public ChunkServerCore(String Con_IP, String Con_Port, ChunkList ck) {
		ConIP = Con_IP;
		ConPort = Con_Port;
		cklist = ck;
	}

	public void run() {
		try {
			Thread.sleep(100);
			ServerSocket s;
			//Socket soc;

			// Get IP Address
			ip = InetAddress.getLocalHost().getHostAddress();
			// Get hostname
			hostname = InetAddress.getLocalHost().getHostName();
			Random randomGenerator = new Random();
			portno = randomGenerator.nextInt(55555);
			if (portno < 1024)
				portno = portno + 3235;

			s = new ServerSocket(portno);
			CSRegisterCS();

			// /Start HeartBeat
			Timer timer = new Timer();
			timer.schedule(new ChunkServerHeartbeat(ConIP, ConPort, ip, portno,
					cklist), 10, 30000);
			// /

			System.out.println("Chunk Server at " + hostname + " " + ip + "/"
					+ portno);

			boolean listening = true;
			while (listening)
				new ChunkServerAssisstant(s.accept(), ConIP,ConPort,ip,portno,cklist)
						.start();

			s.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}// End of run
	
	public void CSRegisterCS() throws Exception {
		// Take IP and Port of Controller from User
		Socket skt = new Socket(ConIP, Integer.parseInt(ConPort));
		ObjectOutputStream out = new ObjectOutputStream(skt.getOutputStream());
		// ///msg type////
		MsgType type = new MsgType(1);
		out.writeObject(type);
		// //msg/////
		RegisterCS regInfo = new RegisterCS();
		regInfo.MyIP = ip;
		regInfo.MyPort = portno;
		File dir = new File("/tmp");
		regInfo.FreeSpace = dir.getFreeSpace();
		out.writeObject(regInfo);
	}

}