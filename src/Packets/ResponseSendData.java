package Packets;
import java.io.Serializable;
//Packet 9
public class  ResponseSendData implements Serializable 
{
public int MsgType=9;
public byte[] Chunk;
}
