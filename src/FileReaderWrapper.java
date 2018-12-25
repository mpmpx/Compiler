import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class FileReaderWrapper {
	
	BufferedReader bufferedReader;
	String fileName;
	
	public FileReaderWrapper(String filePath) {
		
		fileName = filePath;
		try {
			bufferedReader = new BufferedReader(new FileReader(filePath));
		}
		catch (FileNotFoundException e) {
			System.out.println("Warning: \"" + fileName + "\" cannot be opened.");
		}
		
	}
	
	public char read() {
		char c = ' ';
		try {
			c = (char) bufferedReader.read();
		} catch (IOException e) {
			System.out.println("Warning: \"" + fileName + "\" cannot be read.");
		}
		
		return c;
	}
	
	public void close() {
		try {
			bufferedReader.close();
		}
		catch (IOException e) {
			System.out.println("Warning: \"" + fileName + "\" cannot be closed." );
		}
	}
}
