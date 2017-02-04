package Packets;
import java.io.Serializable;
//Packet 1
public class  RegisterCS implements Serializable 
{
public int MsgType=1;
public String MyIP;
public int MyPort; 
public long FreeSpace;
}
