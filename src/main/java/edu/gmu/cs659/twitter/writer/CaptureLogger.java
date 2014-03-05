package edu.gmu.cs659.twitter.writer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CaptureLogger {
	public static final Logger logger = LoggerFactory
			.getLogger(CaptureLogger.class);

	private Writer writer;

	public CaptureLogger(String file) {
		setFileWriter(file);
	}

	public void writeRow(List<? extends Object> list) {
		try {			
			String row = StringUtils.join(sanitizeObjects(list), ",");
			writer.write(sanitizeRow(row) + System.lineSeparator());
		} catch (IOException e) {
			logger.error("Failed to write Row", e);
		}
	}

	public void closeWriter() {
		try {
			writer.close();
		} catch (IOException e) {
			logger.error("Failed to close writer", e);
		}
	}

	private void setFileWriter(String fileName) {
		try {
			writer = new FileWriter(fileName);
		} catch (IOException e) {
			logger.error("Failed to open writer", e);
		}
	}

	/**
	 * Changes required to generate a clean comma delimited file
	 * 
	 * cleans up newline characters... replaces them with spaces and removes quotations 
	 * 
	 * @param row
	 *            The original row
	 * @return the result
	 */
	private String sanitizeRow(String row) {
		return row.replaceAll("\\r\\n|\\r|\\n", " ").replace("\"", "").replace("'", "");
	}

	/**
	 * Changes required to generate a clean comma delimited file
	 * 
	 * cleans up commas in object text... replaces them with spaces 
	 * 
	 * @param row
	 *            The original row
	 * @return the result
	 */
	private List<String> sanitizeObjects(List<? extends Object> row) {
		List<String> list = new ArrayList<String>();
		for(Object o : row) {
			list.add(o.toString().replaceAll(",", " "));
		}
		
		return list;
	}
}
