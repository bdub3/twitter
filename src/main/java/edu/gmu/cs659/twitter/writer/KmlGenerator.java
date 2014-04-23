package edu.gmu.cs659.twitter.writer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Icon;
import de.micromata.opengis.kml.v_2_2_0.IconStyle;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Style;
import de.micromata.opengis.kml.v_2_2_0.StyleSelector;
import edu.gmu.cs659.twitter.Tweet;

public class KmlGenerator implements TweetWriter {
	
	private static final String[] pins = new String[] { "images/pin_blue.png",
			"images/pin_black.png", "images/pin_yellow.png",
			"images/pin_red.png" };

	public static final Logger logger = LoggerFactory
			.getLogger(KmlGenerator.class);

	private Kml kml;
	private Document document;

	private Map<String, Folder> folders;
	private Map<String, String> pinMapping;

	private File file;

	public KmlGenerator(String file) {
		setFileWriter(file);
		kml = new Kml();
		document = kml.createAndSetDocument().withName("Tweets");

		List<StyleSelector> styleSelector = new ArrayList<StyleSelector>();
		//StyleSelector styleMap = new StyleMap();
		
		for(int i = 0; i < pins.length; i++) {
			Style style = new Style();
			Icon icon = new Icon();
			icon.setHref(pins[i]);
			IconStyle iStyle = style.createAndSetIconStyle();
			iStyle.setIcon(icon);
			iStyle.setScale(1D);
			style.setId(Integer.toString(i));

			styleSelector.add(style);
		}
		
		document.setStyleSelector(styleSelector);
		folders = new HashMap<String, Folder>();
		pinMapping = new HashMap<String, String>();
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

	public void writeData(Collection<Tweet> tweets) {
		for(Tweet tweet : tweets) {
			if(tweet.getLocation() != null) {
				writeTweet(tweet);
			}
		}
	}

	private void writeTweet(Tweet tweet) {

		Placemark mark = getFolder(tweet).createAndAddPlacemark();
		mark.setStyleUrl(getPin(tweet));
		mark	//.withName(tweet.getTweetStatus())  // too noisy
				.createAndSetPoint()
				.addToCoordinates(tweet.getLocation().getLongitude(),
						tweet.getLocation().getLatitude());
	}
	
	private Folder getFolder(Tweet tweet) {
		String cls = tweet.getTweetClass();
		if(cls == null) {
			cls = "Unknown Class";
		}
		if(! folders.containsKey(cls)) {
			folders.put(cls, document.createAndAddFolder().withName(cls));
		}
		return folders.get(cls);
	}
	
	private String getPin(Tweet tweet) {
		String cls = tweet.getTweetClass();
		if(cls == null) {
			cls = "Unknown Class";
		}
		if(! pinMapping.containsKey(cls)) {
			int nextIndex = pinMapping.size() % pins.length;
			pinMapping.put(cls, Integer.toString(nextIndex));
		}
		return pinMapping.get(cls);
	}

}
