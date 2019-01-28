package lexer.test;


import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

import lexer.Driver;

public class test {

	private String rootDir;
	private String testDir;
	private Driver driver;
	
	@Before
	public void before() {
		rootDir = (Paths.get("").toAbsolutePath().toString());
		testDir = rootDir + "\\src\\lexer\\test\\";
		System.out.println();
	}
	
	@Test
	public void testValidID() {
		String testFile = testDir + "t_valid_id.txt";
		driver = new Driver();
		driver.run(testFile);
	}
	
	@Test
	public void testInvalidID() {
		String testFile = testDir + "t_invalid_id.txt";
		driver = new Driver();
		driver.run(testFile);
	}
	
	@Test
	public void testValidNum() {
		String testFile = testDir + "t_valid_num.txt";
		driver = new Driver();
		driver.run(testFile);
	}
	
	@Test
	public void testInvalidNum() {
		String testFile = testDir + "t_invalid_num.txt";
		driver = new Driver();
		driver.run(testFile);
	}
	
	@Test
	public void testDigits() {
		String testFile = testDir + "t_digits.txt";
		driver = new Driver();
		driver.run(testFile);
	}

	@Test
	public void testLetters() {
		String testFile = testDir + "t_letters.txt";
		driver = new Driver();
		driver.run(testFile);
	}
	
	@Test
	public void testOperator() {
		String testFile = testDir + "t_operator.txt";
		driver = new Driver();
		driver.run(testFile);
	}
	
	@Test
	public void testPunctuation() {
		String testFile = testDir + "t_punctuation.txt";
		driver = new Driver();
		driver.run(testFile);
	}
	
	@Test
	public void testReservedKeysComment() {
		String testFile = testDir + "t_reserved_keys_comment.txt";
		driver = new Driver();
		driver.run(testFile);
	}
	
	@Test
	public void testInvalidComment() {
		String testFile = testDir + "t_invalid_comment.txt";
		driver = new Driver();
		driver.run(testFile);
	}

}
