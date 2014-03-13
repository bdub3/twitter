package edu.gmu.cs659.twitter.writer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.gmu.cs659.twitter.Tweet;

public class CaptureLogger {
	public static final Logger logger = LoggerFactory
			.getLogger(CaptureLogger.class);

	// clean up data for time series analysis
	public static final boolean SORT_AND_REMOVE_DUPLICATES = true;
	
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

	public void writeData(List<Tweet> tweets) {
		logger.debug("Writing capture file");

		writeRow(Tweet.FIELDS);

		Collection<Tweet> tweetsToWrite = tweets;
		if(SORT_AND_REMOVE_DUPLICATES) {
			tweetsToWrite = orderTweets(tweets);
		}
		
		for(Tweet tweet : tweetsToWrite) {
			this.writeRow(tweet.getAttributes());
		}
	}

	// putting in timestamp order and removing any that happen at the same second
	// wlm - this is kind of garbage, just getting some potential data that can
	// play in time series stuff
	private Collection<Tweet> orderTweets(List<Tweet> tweets) {
		SortedMap<Long, Tweet> orderedTweets = new TreeMap<Long, Tweet>();
		
		for(Tweet tweet : tweets) {
			long timeInSeconds = tweet.getTimeStamp() / 1000;
			orderedTweets.put(timeInSeconds,  tweet);
		}
		return orderedTweets.values();
	}
}
