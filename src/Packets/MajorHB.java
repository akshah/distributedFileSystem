package Packets;
import java.util.ArrayList;
import java.io.Serializable;
//Packet 3
public class  MajorHB implements Serializable 
{
public int MsgType=3;
public String CSIP;
public int CSPort;
public ArrayList<String> MetaDataOfAllChunks;
public int  NumberOfChunks;
public long FreeSpace; //In bytes
}
