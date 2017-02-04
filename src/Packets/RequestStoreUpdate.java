package Packets;
import java.io.Serializable;
//Packet 7
public class  RequestStoreUpdate implements Serializable 
{
public int MsgType=7;
public String ChunkName;
public byte[] Chunk;
public String ipCS1;
public int portCS1;
public String ipCS2;
public int portCS2;
public String ipCS3;
public int portCS3;
}
