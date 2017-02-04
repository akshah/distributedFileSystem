package Packets;
import java.io.Serializable;
//Packet 13
public class  RequestSendChunk implements Serializable 
{
public int MsgType=13;
public String ChunkName;
public String ipCS1;
public int portCS1;
public String ipCS2;
public int portCS2;
public String ipCS3;
public int portCS3;
}
