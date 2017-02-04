package Data;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.LineNumberReader;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Chunk {
	private String filename = null;
	
	public String getFileName(){
		return filename;
	}

	public int split(String file) {
		int nfile = 0;
		try {
			String filepath = file;
			//System.out.println("path is "+filepath);
			File openfile = new File(filepath);
			filename = openfile.getName();
			//System.out.println("filename is "+filename);

			FileInputStream fis = new FileInputStream(openfile);
			byte[] dataBytes = new byte[8*1024];

			int nread = 0;
			int count = 0;
			nfile = 1;
			int flag = 0;
			FileOutputStream out = new FileOutputStream(filename + "_chunk"
					+ nfile);

			while ((nread = fis.read(dataBytes)) != -1) {
				// md.update(dataBytes, 0, nread);
				if (flag == 1) {
					nfile++;
					out = new FileOutputStream(filename + "_chunk" + nfile);
					// System.out.println("create another file" + nfile);
					flag = 0;
				}
				count = count + nread;
				// System.out.println("count = " + count);
				out.write(dataBytes, 0, nread);
				if (count % (64*1024) == 0) {
					flag = 1;
					out.close();
				}
			}
			out.close();
			System.out.println("file num is " + nfile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return nfile;
	}

	public void reconstruct(String filename, int chunk_num) {
		try {
			// /////reconstruct the file////////////
			byte[] smallBytes = new byte[1024];
			FileOutputStream newfile = new FileOutputStream(filename);
			int nbytes = 0;
			for (int j = 1; j <= chunk_num; j++) {
				FileInputStream frs = new FileInputStream(filename + "_chunk"
						+ j);
				while ((nbytes = frs.read(smallBytes)) != -1) {
					newfile.write(smallBytes, 0, nbytes);
				}
				frs.close();
				File delete_chunks=new File(filename+"_chunk"+j);
				boolean success = delete_chunks.delete();	
			}
			newfile.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void createChecksum(String chunkname, int version) {
		//1st line: chunkname
		//2nd line: version
		//3nd line: filename
		//4rd line: sn
		//5th line: time 
		//6th line: checksume
		
		// SHA1 sha = new SHA1();
		int offset = chunkname.lastIndexOf('/');
		String datafile = chunkname.substring(offset+1, chunkname.length()).trim(); 
		//System.out.println("datafile is "+datafile);
		//File directory = new File(".");
		//System.out.println("absolute path is "+directory.getAbsolutePath()); 
		try {
			MessageDigest md = MessageDigest.getInstance("SHA1");
			//System.out.println("try to open");
			FileInputStream fis = new FileInputStream(chunkname);
			//System.out.println("cannot open?");
			byte[] dataBytes = new byte[1024];

			int nread = 0;
			int flag = 0;
			int count = 0;
			int nChecksum = 0;

			// FileOutputStream out = new
			// FileOutputStream("p123_0.pdf_chunk1.txt");


			BufferedWriter write2file = new BufferedWriter(new FileWriter(chunkname+"_metadata"));
					//chunkname + "_metadata"));
			
			/*---------write chunkname into metadata file-------*/
			write2file.write(datafile);
			write2file.newLine();
			/*----------------------------------------------------*/
			
			/*---------write version into metadata file-------*/
			Integer newver = new Integer(version); 
			write2file.write(newver.toString());
			write2file.newLine();
			/*----------------------------------------------------*/
			
			/*---------write filename into metadata file-------*/
			offset = datafile.lastIndexOf('_');
			String filename = datafile.substring(0, offset).trim();
			write2file.write(filename);
			write2file.newLine();
			/*----------------------------------------------------*/
			
			/*---------write sn into metadata file-------*/
			offset = datafile.lastIndexOf('k');
			String sn = datafile.substring(offset+1,datafile.length()).trim();
			write2file.write(sn);
			write2file.newLine();
			/*----------------------------------------------------*/
			
			/*---------write timestamp into metadata file-------*/
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");     
			Date curDate = new Date(System.currentTimeMillis());//local time     
			String localtime = formatter.format(curDate);     
			write2file.write(localtime);
			write2file.newLine();
			/*----------------------------------------------------*/
			
			while ((nread = fis.read(dataBytes)) != -1) {
				count = count + nread;
				md.update(dataBytes, 0, nread);
				if (count % (8*1024) == 0) {
					flag = 1;
				}
				if (flag == 1 || nread < 1024) {
					nChecksum++;
					// System.out.println("write a new checksum");
					String digest = getSHA1String(md);
					//System.out.println("Digest " + nChecksum + "::" + digest);
					write2file.write(digest);
					write2file.newLine();
					flag = 0;
					md.reset();
				}
			}
			write2file.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getSHA1String(MessageDigest md) {
		byte[] mdbytes = md.digest();
		StringBuffer sb = new StringBuffer("");
		for (int i = 0; i < mdbytes.length; i++) {
			sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16)
					.substring(1));
		}
		return sb.toString();
	}
	
	public String getLine(String chunkname, int line){
		String strline = "";
		//System.out.println(line);
		try{
			//FileReader readfromfile = new FileReader(chunkname);
			LineNumberReader lr = new LineNumberReader(new FileReader(chunkname));
			//lr.setLineNumber(line);
			//System.out.println("linenum is "+ lr.getLineNumber());
			while((strline=lr.readLine()) != null){
				if(lr.getLineNumber() == line){
					break;
				}
				strline = "";
			}
			//strline = lr.readLine();
			lr.close();
		}catch(Exception e){
			
		}
		return strline;
	}
}
