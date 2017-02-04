import java.io.*;
import java.io.IOException;

public class Client {

	public static String hostname = null, ip = null, portno = "2964";
	public static String filename;
	public static String ConIP,ConPort="3157";

	public static void main(String[] args) throws IOException {

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String ch;
		int chint;
		ClientCore core;
		Boolean wrngchoice = true;
		System.out.println("Enter Controller IP/Name:");
		ConIP=br.readLine();
		while(true){
		do {
			System.out.println("1. Store Data");
			System.out.println("2. Retrieve Data");
			ch = br.readLine();
			chint = Integer.parseInt(ch);
			switch (chint) {
			case 1:
				wrngchoice = false;
				System.out.println("Enter File Name you want to Store/Update:");
				ch = br.readLine();
				core = new ClientCore(ch,ConIP,ConPort);
				core.StoreData();
				break;
			case 2:
				wrngchoice = false;
				System.out.println("Enter File Name you want to Retrieve:");
				ch = br.readLine();
				core = new ClientCore(ch,ConIP,ConPort);
				core.RetrieveData();
				break;
			default:
				wrngchoice = true;
				System.out.println("Illegal Selection");
				break;
			}
		} while (wrngchoice);
		}
	}// End of Main
}// End of class Client

