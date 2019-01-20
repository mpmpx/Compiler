package utilities;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public class FileWriterWrapper {
	
	BufferedWriter writer;
	String fileName;
	
	public FileWriterWrapper(String fileName) {
		
		this.fileName = fileName;
		
		try {
			writer = new BufferedWriter(new FileWriter(fileName));
		}
		catch (FileNotFoundException e) {
			System.out.println("Warning: \"" + fileName + "\" cannot be found.");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Warning: \"" + fileName + "\" cannot be opened.");
			e.printStackTrace();
		}
		
	}
	
	public void write(String msg) {
		try {
			writer.write(msg);
		} catch (IOException e) {
			System.out.println("Warning: \"" + fileName + "\" cannot be written.");
			e.printStackTrace();
		}
	}
	
	public void close() {
		try {
			writer.close();
		}
		catch (IOException e) {
			System.out.println("Warning: \"" + fileName + "\" cannot be closed." );
			e.printStackTrace();
		}
	}
}
