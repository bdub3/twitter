package edu.gmu.cs659.twitter.writer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

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
	
	public void writeRow(Collection<? extends Object> items) {
		try {			
			writer.write(StringUtils.join(items, ",") + System.lineSeparator());
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

}
