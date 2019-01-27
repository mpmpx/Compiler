package lexer.test;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Paths;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.jupiter.api.Test;

import lexer.Driver;
import lexer.Scanner;
import token.Token;
import token.TokenType;
import utilities.FileWriterWrapper;

class test {

	private static String rootDir = (Paths.get("").toAbsolutePath().toString());
	private String testDir = rootDir + "\\src\\lexer\\test\\";
	private Driver driver;
	
	@Test
	public void test1() {
		String testFile = testDir + "t1.txt";
		driver = new Driver();
		driver.run(testFile);
	}
	
	@Test
	public void test2() {
		String testFile = testDir + "t2.txt";
		driver = new Driver();
		driver.run(testFile);
	}
}
