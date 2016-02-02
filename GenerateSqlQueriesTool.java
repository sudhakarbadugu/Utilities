package com.test;

import java.io.FileReader;

import au.com.bytecode.opencsv.CSVReader;

/**
 * 
 * This tool generates the sql insert statements based on the csv file.
 * @author sudhakar_b
 *
 */
public class GenerateSqlQueriesTool {

	private static String INSERT_PREFIX = "";

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		if (args.length == 0) {
			System.out.println("No feed to tool to process.");
			System.out.println("Usage: ToolName fileName tableName columnNumbersToGenerateDeclarationStmts");
			System.out.println("EX: GenerateSqlQueriesTool sample.csv mytable 1");
			System.exit(0);
		}

		CSVFileModel fileModel = new CSVFileModel();
		if (args.length > 0) {
			fileModel.setFileName(args[0]);
		}

		if (args.length > 1) {
			fileModel.setTableName(args[1]);
		}

		// parse 3rd argement to prepare the declare statements for specified column numbers.
		int[] columnNumbers = new int[0];
		String aColumnNumberToGenerateSqlVariableStmts = "";
		if (args.length > 2) {
			aColumnNumberToGenerateSqlVariableStmts = args[2];

			String[] columnNumbersString = aColumnNumberToGenerateSqlVariableStmts.split(" ");
			columnNumbers = new int[columnNumbersString.length];
			try {
				for (int i = 0; i < columnNumbersString.length; i++) {
					String aColumnNumber = columnNumbersString[i];
					int columnNumber = Integer.parseInt(aColumnNumber);
					columnNumbers[i] = columnNumber;
				}
			} catch (Exception e) {
				// ignore.
			}
		}
		fileModel.setColumnNumbers(columnNumbers);

		// process the file.
		//System.out.println("----------Started processing the file.------");
		process(fileModel);
		//System.out.println("----------Successfully completed.------");
	}

	/**
	 * parse the file.
	 * 
	 * @param fileName
	 * @throws Exception
	 */
	public static CSVReader parseFileAndGetCsvReader(String fileName, int lineNumber) throws Exception {
		CSVReader reader = new CSVReader(new FileReader(fileName), ',', '"', lineNumber);

		return reader;
	}

	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void process(CSVFileModel fileModel) throws Exception {

		// Load the prefix statement 
		loadInsertPrefixStatement(fileModel);
		
		// 
		try (CSVReader csvReader = parseFileAndGetCsvReader(fileModel.getFileName(), 1)) {

			// Read CSV line by line and use the string array as you want
			String[] nextLine;
			while ((nextLine = csvReader.readNext()) != null) {
				if (nextLine != null) {
					String insertQuery = getInsertQuery(nextLine, fileModel);
					System.out.println(insertQuery);
				}
			}
		} catch (Exception e) {
		}

	}

	/**
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void loadInsertPrefixStatement(CSVFileModel fileModel) throws Exception {

		try (CSVReader csvReader = parseFileAndGetCsvReader(fileModel.getFileName(), 0)) {

			// Read CSV line by line and use the string array as you want
			String[] nextLine = csvReader.readNext();
			if (nextLine != null) {
				INSERT_PREFIX = getInsertQueryPrefix(nextLine, fileModel.getTableName());
				// System.out.println(INSERT_PREFIX);
			}
		} catch (Exception e)
		{
		}
	}

	/**
	 * Returns the Insert query prefix.
	 * 
	 * <pre>
	 * 	INSERT INTO table_name (col1, col2, col3, col4) VALUES (
	 * </pre>
	 * 
	 * @param headers
	 * @param theTableName
	 * @return
	 */
	private static String getInsertQueryPrefix(String[] headers, String theTableName) {
		StringBuilder aPrefix = new StringBuilder("INSERT INTO " + theTableName + "(");

		for (String header : headers) {
			aPrefix.append(header).append(",");
		}
		String substring = aPrefix.substring(0, aPrefix.length() - 1) + ") VALUES (";

		return substring;
	}

	/**
	 * Returns the insert query.
	 * 
	 * @param values
	 * @return
	 */
	private static String getInsertQuery(String[] values, CSVFileModel fileModel) {
		StringBuilder aPrefix = new StringBuilder(INSERT_PREFIX);
		String declareVarStmt = "DECLARE ";
		
		int[] columnNumbers = fileModel.getColumnNumbers();
		
		for (int i =0; i< values.length ; i++) {
			
			String header = values[i];
			
			// for declared variables should not add the ' symbol
			if (header.trim().indexOf("@") != -1 && header.trim().indexOf("@") == 0) {
				
				for(int j=0; j< columnNumbers.length ; j++)
				{
					if(i==j)
					{
						declareVarStmt += header + " VARCHAR(36) = NEWID()";
						System.out.println(declareVarStmt);
					}
				}

				aPrefix.append(header).append(",");
				continue;
			}

			// for sql methods should not add the ' symbol
			if (header.endsWith("()")) {
				aPrefix.append(header).append(",");
			} else {
				aPrefix.append("'").append(header).append("',");
			}
		}
		String substring = aPrefix.substring(0, aPrefix.length() - 1) + ")";

		return substring;
	}

	private static class CSVFileModel {
		String fileName, tableName;
		int[] columnNumbers;

		/**
		 * @return the columnNumbers
		 */
		public int[] getColumnNumbers() {
			return columnNumbers;
		}

		/**
		 * @param columnNumbers
		 *            the columnNumbers to set
		 */
		public void setColumnNumbers(int[] columnNumbers) {
			this.columnNumbers = columnNumbers;
		}

		/**
		 * @return the fileName
		 */
		public String getFileName() {
			return fileName;
		}

		/**
		 * @param fileName
		 *            the fileName to set
		 */
		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		/**
		 * @return the tableName
		 */
		public String getTableName() {
			return tableName;
		}

		/**
		 * @param tableName
		 *            the tableName to set
		 */
		public void setTableName(String tableName) {
			this.tableName = tableName;
		}
	}
}
