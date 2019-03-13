package parser;

import java.nio.file.Paths;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

import AST.AST;

public class Driver {
	private String rootDir;
	private Parser parser;
	
	/**
	 * Constructor. Initialize the root directory and output directory.
	 */
	public Driver() {
		rootDir = (Paths.get("").toAbsolutePath().toString());
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
		AST ast = new AST(AST.makeNode());
		parser = new Parser(selectedFile);
		if (parser.parse(ast)) {
			System.out.println("No Error");
			ast.print();
		}
		else {
			System.out.println("Parsing fails.");
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
	
	public static void main(String[] args) {
		Driver driver = new Driver();
		if (args.length == 0) {
			driver.run(null);
		} else {
			driver.run(args[0]);
		}
	}
}
