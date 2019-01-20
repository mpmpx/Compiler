package utilities;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class FileReaderWrapper {
	
	BufferedReader bufferedReader;
	String fileName;
	
	public FileReaderWrapper(String fileName) {
		
		this.fileName = fileName;
		
		try {
			bufferedReader = new BufferedReader(new FileReader(fileName));
		}
		catch (FileNotFoundException e) {
			System.out.println("Warning: \"" + fileName + "\" cannot be found.");
			e.printStackTrace();
		}
		
	}
	
	public char read() {
		char c = ' ';
		try {
			c = (char) bufferedReader.read();
		} catch (IOException e) {
			System.out.println("Warning: \"" + fileName + "\" cannot be read.");
			e.printStackTrace();
		}
		
		return c;
	}
	
	public void close() {
		try {
			bufferedReader.close();
		}
		catch (IOException e) {
			System.out.println("Warning: \"" + fileName + "\" cannot be closed." );
			e.printStackTrace();
		}
	}
	
	public String getFileName() {
		return fileName.substring(fileName.lastIndexOf('\\') + 1, fileName.lastIndexOf('.'));
	}
}
