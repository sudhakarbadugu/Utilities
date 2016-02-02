package com.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * This file compares the csv files.
 * 
 * @author sudhakar_b
 *
 */
public class CSVFileComparisionTool {
	private static CSVReader reader;
	private static Map<String, String[]> resultFileMap, expectedFileMap;
	private static CSVWriter resultFileWriter;
	private static List<String> expectedHeader;
	private static List<String> resultHeader;

	private void compare(String resultFile, String expectedFile, String reportGenerationDirectory, int keyIndex) {
		System.out.println(new Date() + ": ***********************Comparison Begins*****************");
		System.out.println(new Date() + ": Result File : " + resultFile);
		System.out.println(new Date() + ": Expected Result File : " + expectedFile);

		List<String[]> resultList = new ArrayList<String[]>(), expectedList = new ArrayList<String[]>();
		resultFileMap = new HashMap<String, String[]>();
		expectedFileMap = new HashMap<String, String[]>();

		String detailsFile = getReportlFileName(reportGenerationDirectory, expectedFile);
		try {
			reader = new CSVReader(new FileReader(resultFile));
			resultList = reader.readAll();
			reader = new CSVReader(new FileReader(expectedFile));
			expectedList = reader.readAll();
			resultHeader = Arrays.asList(resultList.get(0));
			expectedHeader = Arrays.asList(expectedList.get(0));
			resultList.remove(0);
			expectedList.remove(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (String[] actual : resultList) {
			String keyStr = actual[keyIndex];
			resultFileMap.put(keyStr, actual);
		}
		for (String[] expected : expectedList) {
			String keyStr = expected[keyIndex];
			expectedFileMap.put(keyStr, expected);
		}
		try {
			resultFileWriter = new CSVWriter(new FileWriter(detailsFile));
			System.out.println(new Date() + ": Details File : " + detailsFile + " Created");
			String resultFileHeaderStr = "DataFile";
			for (int i = 0; i < resultHeader.size(); i++) {
				resultFileHeaderStr = resultFileHeaderStr + "," + resultHeader.get(i);
			}
			resultFileHeaderStr = resultFileHeaderStr + "Status,Reason";
			resultFileWriter.writeNext(resultFileHeaderStr.split(","));
			System.out.println(new Date() + ": Header = [" + resultFileHeaderStr + "] Written to the Details File - '"
					+ detailsFile + "'");
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(new Date() + ": =================================================");
		System.out.println(new Date() + ": Starts Writting into Details File - '" + detailsFile + "'");
		if (!resultFileMap.keySet().isEmpty()) {
			for (String key : resultFileMap.keySet()) {
				String[] tempActualArr = resultFileMap.get(key);
				String[] tempExpectedArr = expectedFileMap.get(key);
				createResultFile(tempActualArr, tempExpectedArr, resultFile, expectedFile);
				resultFileWriter.writeNext(new String[0]);
			}
		} else {
			for (String key : expectedFileMap.keySet()) {
				String[] tempActualArr = resultFileMap.get(key);
				String[] tempExpectedArr = expectedFileMap.get(key);
				createResultFile(tempActualArr, tempExpectedArr, resultFile, expectedFile);
				resultFileWriter.writeNext(new String[0]);
			}
		}
		System.out.println(new Date() + ": Details File - '" + detailsFile + "' created Successfully");
		System.out.println(new Date() + ": =================================================");
	}

	private static void createResultFile(String[] resultList, String[] expectedList, String resultFile,
			String expectedFile) {
		int excepectedLength = 2;
		if (expectedList == null) {
			expectedList = new String[resultList.length];
		}
		if (resultList == null) {
			resultList = new String[expectedList.length];
		}

		excepectedLength = expectedList.length + 1;
		List<String> tempResultList = new ArrayList<String>(resultList.length);
		String[] tempExpectedList = new String[excepectedLength];
		tempResultList.add(0, resultFile);
		tempExpectedList[0] = expectedFile;
		for (int i = 1, j = 0; i < resultList.length; i++, j++) {
			tempResultList.add(i, resultList[j]);
		}
		for (int i = 1, j = 0; i < tempExpectedList.length; i++, j++) {
			tempExpectedList[i] = expectedList[j];
		}

		// Update the status of each row as last column.
		try {
			String status = "Pass";
			String mismatchedColumns = "";
			for (int i = 0; (i < expectedList.length && i < resultList.length); i++) {
				String value1 = resultList[i];
				String value2 = expectedList[i];
				if (!(value1 != null && value2 != null && value1.equals(value2))) {
					if (i < resultHeader.size()) {
						mismatchedColumns += resultHeader.get(i) + ",";
					} else if (i < expectedHeader.size()) {
						mismatchedColumns += expectedHeader.get(i) + ",";
					}
					status = "Fail";
				}
			}
			tempResultList.add(status);
			tempResultList.add(mismatchedColumns);

		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			resultFileWriter.writeNext(tempExpectedList);
			resultFileWriter.writeNext(tempResultList.toArray(new String[] {}));
			resultFileWriter.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void closeAll() {
		try {
			if (resultFileWriter != null) {
				resultFileWriter.close();
			}
			System.out.println(new Date() + ": All instances Closed");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the details file name.
	 * 
	 * @param expectedFileName
	 * @param expectedFile
	 * @return
	 */
	private static String getReportlFileName(String reportGenerationDirectory, String expectedFileName) {
		File file = new File(expectedFileName);
		String name = file.getName();
		String detailsExpectedFileName = reportGenerationDirectory + "//" + name.substring(0, name.indexOf('.'))
				+ "_report.csv";
		file = new File(reportGenerationDirectory);
		if (!file.exists()) {
			file.mkdir();
		}
		file = new File(detailsExpectedFileName);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException an) {
				an.printStackTrace();
			}
		}
		return detailsExpectedFileName;
	}

	/**
	 * Process comparing the files.
	 * 
	 * @param sourceFileDirectroy
	 * @param destinationFileDir
	 * @param reportGenerationDirectory
	 * @throws FileNotFoundException
	 */
	private void process(String sourceFileDirectroy, String destinationFileDir, String reportGenerationDirectory)
			throws FileNotFoundException {
		File[] sourceFileDirectroyFiles = getFiles(sourceFileDirectroy);
		File[] destinationFileDirFiles = getFiles(destinationFileDir);

		Set<String> comparedFiles = new HashSet<String>();
		Set<String> totalFiles = new HashSet<String>();
		totalFiles.addAll(Arrays.asList(getFilesList(sourceFileDirectroy)));
		totalFiles.addAll(Arrays.asList(getFilesList(destinationFileDir)));

		for (File destinationFile : destinationFileDirFiles) {
			for (File sourceFile : sourceFileDirectroyFiles) {
				if (destinationFile.getName().equals(sourceFile.getName())) {
					compare(destinationFile.getAbsolutePath(), sourceFile.getAbsolutePath(), reportGenerationDirectory,
							0);
					comparedFiles.add(destinationFile.getName());
					break;
				}
			}
		}

		totalFiles.removeAll(comparedFiles);
		System.out.println(new Date() + " Un compared files are: " + totalFiles);
		closeAll();
	}

	/**
	 * Returns the list of files with in that dir.
	 * 
	 * @param fileDirectroy
	 * @return
	 * @throws FileNotFoundException
	 */
	private File[] getFiles(String fileDirectroy) throws FileNotFoundException {
		File[] files = new File[1];
		File file = new File(fileDirectroy);

		if (file.exists()) {
			if (file.isDirectory()) {
				files = file.listFiles();
			} else {
				files[0] = file;
			}
		} else {
			throw new FileNotFoundException("File is not available :" + fileDirectroy);
		}

		return files;
	}

	/**
	 * Returns the list of files with in that dir.
	 * 
	 * @param fileDirectroy
	 * @return
	 * @throws FileNotFoundException
	 */
	private String[] getFilesList(String fileDirectroy) throws FileNotFoundException {
		String[] files = new String[1];
		File file = new File(fileDirectroy);

		if (file.exists()) {
			if (file.isDirectory()) {
				files = file.list();
			} else {
				files[0] = fileDirectroy;
			}
		} else {
			throw new FileNotFoundException("File is not available :" + fileDirectroy);
		}

		return files;
	}

	public static void main(String[] args) throws FileNotFoundException {
		if (args.length < 2) {
			System.out.println("No inputs for this tool to process.");
			System.out.println(
					"Usage: ToolName SourcefileName/SourceDirName DestinationfileName/DestinationDirName reportGenerationDirectory");
			System.out.println("EX: CSVFileComparisionTool sample.csv sample1.csv D:\\db2Reports");
			System.out.println("or EX: CSVFileComparisionTool D:\\samples D:\\samples1 D:\\db2Reports");
			System.exit(0);
		}

		String sourceFile = null, destinationFile = null, reportGenerationDirectory = null;
		if (args.length > 0) {
			sourceFile = args[0];
		}

		if (args.length > 1) {
			destinationFile = args[1];
		}

		if (args.length > 2) {
			reportGenerationDirectory = args[2];
		}

		CSVFileComparisionTool comparator = new CSVFileComparisionTool();
		comparator.process(sourceFile, destinationFile, reportGenerationDirectory);
	}
}
