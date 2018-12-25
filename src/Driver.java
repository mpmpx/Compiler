import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

public class Driver {

	public static void main(String[] args) {
		JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
		fileChooser.setDialogTitle("Select a file");
		fileChooser.setAcceptAllFileFilterUsed(false);
		
		int returnValue = fileChooser.showOpenDialog(null);
		
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();	
			FileReaderWrapper fileReader = new FileReaderWrapper(selectedFile.getAbsolutePath());
			Scanner scanner = new Scanner(fileReader);
			
			Token token = scanner.nextToken();
			int lineNum = 1;
			
			while (token.type != TokenType.EOF) {
				if (token.lineNum > lineNum) {
					System.out.println();
					lineNum++;
				}
				System.out.print("["+token.type + ", " + token.value + ", " + token.lineNum + "]");

				token = scanner.nextToken();
			}
			
			System.out.println("\n[" + token.type + ", " + token.value + "]");

		}
	}
}
