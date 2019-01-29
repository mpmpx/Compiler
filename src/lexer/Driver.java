package lexer;
import java.io.File;
import java.nio.file.Paths;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

import token.Token;
import token.TokenType;
import utilities.FileWriterWrapper;

/**
 * 
 * @author Peixing Ma
 * This a driver class that repeatedly calls scanner to read a file in a loop.
 */

public class Driver {
	
	private Scanner scanner;
	private String rootDir;
	private String outputDir;
	
	/**
	 * Constructor. Initialize the root directory and output directory.
	 */
	public Driver() {
		rootDir = (Paths.get("").toAbsolutePath().toString());
		outputDir = rootDir + "\\src\\lexer\\output\\";
		File output = new File(outputDir);
		
		if (!output.exists()) {
			output.mkdirs();
		}
	}
	
	/**
	 * Creates an instance of scanner with the given file. Repeatedly calls the scanner and receives a token in each loop,
	 * recording the token in the corresponding output file or error file. Terminates if an error type token is received.
	 * Provides a file selector to select a file if the path name of a file is not given as the execution argument. Otherwise,
	 * the first execution argument will be used as the path of the file.
	 * @param fileName the name of the file to be scanned.
	 */
	public void run(String fileName) {
		String selectedFile = fileName;
		
		if (fileName == null) {
			selectedFile = selectFile();
		}

		String resultFileName = outputDir + getFileName(selectedFile) + "_out.txt";
		String errorFileName = outputDir + getFileName(selectedFile) + "_error.txt";
		FileWriterWrapper outputWriter = new FileWriterWrapper(resultFileName);
		FileWriterWrapper errorWriter = new FileWriterWrapper(errorFileName);

		scanner = new Scanner(selectedFile);
		Token token = scanner.nextToken();
		int lineNum = 1;

		while (token.type != TokenType.EOF) {
			if (token.lineNum > lineNum) {
				System.out.println();
				lineNum = token.lineNum;
				outputWriter.write("\r\n");
			}

			if (token.type == TokenType.ERROR_NUM) {
				errorWriter
						.write("Lexical Error: \"" + token.value + "\" is an invalid number at line " + lineNum + ".\r\n");
			} 
			else if (token.type == TokenType.ERROR_ID) {
				errorWriter.write(
						"Lexical Error: \"" + token.value + "\" is an invalid identifier at line " + lineNum + ".\r\n");
			} 
			else if (token.type == TokenType.ERROR_CMT) {
				errorWriter.write(
						"Lexical Error: incomplete multiple-line comment possibly missing \"*/\". Starting at line " + lineNum + ".\r\n");
			}
			else {
				outputWriter.write(token.type.toString().toLowerCase() + " ");
			}
			System.out.print(token);
			token = scanner.nextToken();
		}

		System.out.println("\n" + token);
		errorWriter.close();
		outputWriter.close();
		scanner.close();
	}
	
	/**
	 * Pop up a file selector window to manually select a file.
	 * @return the absolute path of the selected file.
	 */
	public static String selectFile() {
		String selectedFile = "";
		JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
		fileChooser.setDialogTitle("Select a file");
		fileChooser.setAcceptAllFileFilterUsed(false);
		
		int returnValue = fileChooser.showOpenDialog(null);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			selectedFile = fileChooser.getSelectedFile().getAbsolutePath();	
		}
		
		return selectedFile;
	}
	
	/**
	 * Returns the file name without its extension and path.
	 * @param fileName the absolute path of the file.
	 * @return the file name without its extension and path.
	 */
	public String getFileName(String fileName) {
		fileName = new File(fileName).getName();
		return fileName.substring(0, fileName.lastIndexOf('.'));
	}
	
	/**
	 * The main function runs the driver.
	 * @param args execution arguments.
	 */
	public static void main(String[] args) {
		Driver driver = new Driver();
		if (args.length == 0) {
			driver.run(null);
		} else {
			driver.run(args[1]);
		}
	}
}
