package Packets;
import java.util.ArrayList;
import java.io.Serializable;
//Packet 2
public class  MinorHB implements Serializable 
{
public int MsgType=2;
public String CSIP;
public int CSPort;
public ArrayList<String> NewChunks;
public ArrayList<String> CorruptedChunks;
public int TotalNumberOfChunks; 
public long FreeSpace;//in Bytes 
}
