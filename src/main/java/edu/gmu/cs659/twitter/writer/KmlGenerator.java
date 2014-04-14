package edu.gmu.cs659.twitter.writer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.micromata.opengis.kml.v_2_2_0.Kml;
import edu.gmu.cs659.twitter.Tweet;

public class KmlGenerator implements TweetWriter {

	public static final Logger logger = LoggerFactory
			.getLogger(KmlGenerator.class);

	private Kml kml;
	
	private File file;

	public KmlGenerator(String file) {
		setFileWriter(file);
		kml = new Kml();
	}

	/* (non-Javadoc)
	 * @see edu.gmu.cs659.twitter.writer.TweetWriter#closeWriter()
	 */
	public void closeWriter() {
		if(file == null) {
			logger.error("File set to null");
		} else{
			try {
				kml.marshal(file);
			} catch (FileNotFoundException e) {
				logger.error("Failed to write file", e);
			}
		}
	}

	private void setFileWriter(String fileName) {
		file = new File(fileName);
	}

	public void writeData(List<Tweet> tweets) {
		for(Tweet tweet : tweets) {
			if(tweet.getLocation() != null) {
				writeTweet(tweet);
			}
		}
	}

	private void writeTweet(Tweet tweet) {

		kml.createAndSetPlacemark().withName(tweet.getTweetStatus())
				.createAndSetPoint()
				.addToCoordinates(tweet.getLocation().getLongitude(),
						tweet.getLocation().getLatitude());
	}
}
