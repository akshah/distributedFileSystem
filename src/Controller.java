import java.io.IOException;
import java.util.Timer;
import Data.*;

public class Controller {
	//public String[][] ChunkServerTable = new String[500][3];
	//public String[][] ChunkAndChunkServerTable = new String[500][4];
	
	public static void main(String[] args) throws IOException {
		ChunkServTable csTable = new  ChunkServTable();//cstable on controller
		ChunkAndCSTable cAndcs = new ChunkAndCSTable();
		ControllerCore Core = new ControllerCore(csTable,cAndcs);
		Core.start();

		Timer timer = new Timer();
		timer.schedule(new ControllerHeartbeat(csTable,cAndcs), 20, 30);
		
	}// End of main
}// End of class Controller

