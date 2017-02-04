package Packets;

import java.io.Serializable;

//MsgType
public class MsgType implements Serializable {
	public int MType;

	public MsgType(int type) {
		MType = type;
	}
}
