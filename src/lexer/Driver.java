package lexer;
import java.nio.file.Paths;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

import token.Token;
import token.TokenType;
import utilities.FileWriterWrapper;

public class Driver {
	
	public static void main(String[] args) {
		Scanner scanner;
		String rootDir = (Paths.get("").toAbsolutePath().toString());
		String outputDir = rootDir + "\\src\\lexer\\output\\";
		
		if (args.length == 0) {
			String selectedFile = selectFile();
			String resultFileName = outputDir +  getFileName(selectedFile) + "_out.txt";
			String errorFileName = outputDir +  getFileName(selectedFile) + "_error.txt";
			FileWriterWrapper outputWriter = new FileWriterWrapper(resultFileName);
			FileWriterWrapper errorWriter = new FileWriterWrapper(errorFileName);
			
			scanner = new Scanner(selectedFile);
			Token token = scanner.nextToken();
			int lineNum = 1;
			
			while (token.type != TokenType.EOF) {
				if (token.lineNum > lineNum) {
					System.out.println();
					lineNum++;
					outputWriter.write("\n");
				}
				
				if (token.type == TokenType.ERROR_NUM) {
					errorWriter.write("Lexical Error: \"" + token.value + "\" is a invalid number at line " + lineNum + ".\n");
				}
				else if (token.type == TokenType.ERROR_ID ) {
	            	errorWriter.write("Lexical Error: \"" + token.value + "\" is an invalid identifier at line " + lineNum + ".\n");
				}
				else {
					outputWriter.write(token.type.toString().toLowerCase() + " ");
				}
				System.out.print("["+token.type + ", " + token.value + ", " + token.lineNum + "]");

				token = scanner.nextToken();
			}
			
			System.out.println("\n[" + token.type + ", " + token.value + "]");
			errorWriter.close();
			outputWriter.close();
			scanner.close();
		}
	}
	
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
	
	public static String getFileName(String fileName) {
		return fileName.substring(fileName.lastIndexOf('\\') + 1, fileName.lastIndexOf('.'));
	}
}
