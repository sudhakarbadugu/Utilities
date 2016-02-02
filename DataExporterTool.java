package com.test;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.db.test.DBUtils;
import com.db.test.PropertyFileReader;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * This class exports the table data to csv file.
 * @author sudhakar_b
 *
 */
public class DataExporterTool {
	private static Properties properties;
	
	/**
	 * @param args
	 * @throws SQLException
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("Started exporting the table data to the files");
		new DataExporterTool().process();
		System.out.println("Successfully exported files");
	}

	private void process() throws Exception {
		try {

			List<String> tableNamesList = getTableNames();
			exportToFiles(tableNamesList);
		} catch (Exception an) {
			an.printStackTrace();
			System.out.println("Failed while processing the tables.");
			throw an;
		}

	}

	private List<String> getTableNames() {
		Properties properties = PropertyFileReader.getPropertyFile(PropertyFileReader.PROPERTY_FILE_NAME);
		String tableNames = properties.getProperty("tableNames");
		ArrayList<String> tableNamesList = new ArrayList<String>();
		if (tableNames != null && !tableNames.trim().equals("")) {
			tableNamesList.addAll(Arrays.asList(tableNames.split(",")));
		}

		try {
			// get all the tables from db and return it.
			if (tableNamesList.isEmpty()) {
				Connection connection = DBUtils.getConnection(properties);
				Statement st = connection.createStatement();

				// this query gets all the tables in your database
				ResultSet res = st.executeQuery("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES");

				// Preparing List of table Names
				while (res.next()) {
					tableNamesList.add(res.getString(1));
				}
			}
		} catch (Exception exception) {
			System.out.println("Error while getting the table names.");
		}
		System.out.println(tableNamesList);
		return tableNamesList;
	}

	/**
	 * This method exports the files to csv using java io library.
	 * @param tableNameList
	 * @throws Exception
	 */
	private void exportToFiles(List<String> tableNameList) throws Exception {

		FileWriter fw = null;
		Properties properties = PropertyFileReader.getPropertyFile(PropertyFileReader.PROPERTY_FILE_NAME);
		
		// path to the folder where you will save your csv files
		String filename = properties.getProperty("destinationFilesDirectory");

		// start iterating on each table to fetch its data and save in a .csv file
		Connection connection = DBUtils.getConnection(properties);
		for (String tableName : tableNameList) {
			System.out.println(tableName);

			Statement st = connection.createStatement();

			// select all data from table
			ResultSet res = st.executeQuery("select * from " + tableName);

			// column count is necessary as the tables are dynamic and we need to figure out the numbers of columns
			int colunmCount = DBUtils.getColumnCount(res);

			try {
				fw = new FileWriter(filename + "" + tableName + ".csv");

				// this loop is used to add column names at the top of file ,
				// if you do not need it just comment this loop
				for (int i = 1; i <= colunmCount; i++) {
					fw.append(res.getMetaData().getColumnName(i));
					fw.append(",");

				}

				fw.append(System.getProperty("line.separator"));

				while (res.next()) {
					for (int i = 1; i <= colunmCount; i++) {
						if (res.getObject(i) != null) {
							String data = res.getObject(i).toString();
							fw.append(data);
							fw.append(",");
						} else {
							String data = "null";
							fw.append(data);
							fw.append(",");
						}

					}
					// new line entered after each row
					fw.append(System.getProperty("line.separator"));
				}
				fw.flush();
				fw.close();

			} catch (IOException e) {
				e.printStackTrace();
			} catch (SQLException ex) {
				System.err.println("SQLException information");
			} finally {
				if (fw != null) {
					fw.close();
				}
			}
		}

		// Close the connection.
		DBUtils.closeConnection(connection);
	}
	
	/**
	 * This method uses the opencsv files.
	 * @param tableNameList
	 * @throws Exception
	 */
	private void exportToCsvFiles(List<String> tableNameList) throws Exception {
		
		// path to the folder where you will save your csv files
		Properties properties = PropertyFileReader.getPropertyFile(PropertyFileReader.PROPERTY_FILE_NAME);
		
		// path to the folder where you will save your csv files
		String filename = properties.getProperty("destinationFilesDirectory");

		// start iterating on each table to fetch its data and save in a .csv file
		Connection connection = DBUtils.getConnection(properties);
		
		for (String tableName : tableNameList) {
			System.out.println("Exporting the data of "+ tableName);
			Statement st = connection.createStatement();
			
			// select all data from table
			ResultSet res = st.executeQuery("select * from " + tableName);
			
			try {
				CSVWriter writer = new CSVWriter(new FileWriter(filename + "" + tableName + ".csv"), ',');
				writer.writeAll(res, true);
				writer.flush();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
			}
			System.out.println("Successfully exported the "+ tableName + " data");
		}
		
		// Close the connection.
		DBUtils.closeConnection(connection);
	}
	
	/**
	 * Loads the given property file if already not loaded.
	 * 
	 * @param propFileName
	 * @return
	 * @throws IOException 
	 */
	public static Properties getPropertyFile(String propFileName) throws IOException {
		if (properties == null) {
			properties = new Properties();
			try (InputStream inputStream = new PropertyFileReader().getClass().getClassLoader()
					.getResourceAsStream(propFileName);) {

				if (inputStream != null) {
					properties.load(inputStream);
				} else {
					throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
				}
			} catch (IOException e) {
				properties = null;
				System.out.println("Exception: " + e);
				throw e;
			}
		}

		return properties;
	}
}
