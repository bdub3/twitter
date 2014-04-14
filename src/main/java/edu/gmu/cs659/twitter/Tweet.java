package edu.gmu.cs659.twitter;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import twitter4j.GeoLocation;

import edu.gmu.cs659.twitter.words.AutoCorrectUtil;
import edu.gmu.cs659.twitter.words.InternetShortHandChecker;
import edu.gmu.cs659.twitter.words.NumericFilter;
import edu.gmu.cs659.twitter.words.ShortWordFilter;
import edu.gmu.cs659.twitter.words.StopWordFilter;
import edu.gmu.cs659.twitter.words.WordChecker;
import edu.gmu.cs659.twitter.words.WordSanitizer;

public class Tweet {

	private List<String> metaAttributes;
	
	private String tweetStatus;

	private Long timeStamp;
	
	private String tweetClass;
	
	private GeoLocation location;
	
	private Set<String> terms;
	
	private static final List<WordChecker> checkers;
	
	static {
		List<WordChecker> c = new ArrayList<WordChecker>();
		// note, order matters, these are run serially
		c.add(new InternetShortHandChecker());
		c.add(new WordSanitizer());
		c.add(new NumericFilter());
		c.add(new ShortWordFilter());
		c.add(new StopWordFilter());
		c.add(new AutoCorrectUtil());
		c.add(new WordSanitizer());  // sanitize again incase autocorrect added stuff, tick marks, etc
		
		checkers = Collections.unmodifiableList(c);
	}
	
	public static final List<String> FIELDS = Arrays.asList("trend", "time",
			"timeStamp", "dayTimePeriod", "userTimeZone", "userLocation",
			"source", "text", "cleanText", "lang", "accessLevel", "user",
			"favCount", "lat", "lng", "place", "retweetCount", "hashtaglist",
			"mediaEntries", "userMentions", "hashTagCount", "mediaCount",
			"userMentionsCount");
	
	public Tweet() {
		metaAttributes = new ArrayList<String>();
		terms = new HashSet<String>();
	}

	public void setTweetClass(String tweetClass) {
		this.tweetClass = tweetClass;
	}
	
	public String getTweetClass() {
		return tweetClass;
	}

	public String getSafeTweetClass() {
		return sanitizeItem(tweetClass);
	}

	public Set<String> getTweetClassTerms() {
		return termifyString(getSafeTweetClass());
	}

	public void addAttribute(Object o) {
		addAttribute(o, false);
	}
	
	public void addAttribute(Object o, boolean sanitize) {
		if (o == null) {
			metaAttributes.add("");
		} else {
			if(sanitize) {
				metaAttributes.add(sanitizeItem(o.toString()));
			} else {
				metaAttributes.add(o.toString());
			}
		}
	}

	public String getTweetStatus() {
		return tweetStatus;
	}

	public void setTweetStatus(String tweetStatus) {
		this.tweetStatus = tweetStatus;
		terms = termifyString(tweetStatus);
	}

	public GeoLocation getLocation() {
		return location;
	}

	public void setLocation(GeoLocation location) {
		this.location = location;
	}

	private Set<String> termifyString(String tweetStatus) {
		Set<String> termSet = new HashSet<String>();
		if (tweetStatus != null) {
			// chop into words
			List<String> words = Arrays.asList(tweetStatus.split("\\s+"));

			for(WordChecker wc : checkers) {
				List<String> newWords = new ArrayList<String>();

				for(String word : words) {
					if(word != null) {
						newWords.addAll(wc.checkWord(word));
					}
				}
				
				words = newWords;
			}
			termSet.addAll(words);
		}
		
		return termSet;
	}

	public Long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Long timeStamp) {
		this.timeStamp = timeStamp;
	}


	public List<String> getAttributes() {
		return Collections.unmodifiableList(metaAttributes);
	}

	public Set<String> getTerms() {
		return Collections.unmodifiableSet(terms);
	}	

	public String getCleanedText() {
		Set<String> set = new HashSet<String>();
		set.addAll(getTerms());
		set.removeAll(getTweetClassTerms());
		
		return StringUtils.join(set, " ");
	}
	
	private String sanitizeItem(String item) {
		if(item == null) {
			return "";
		}
		// first replaceAll removes characters with empty string (want empty string to turn don't into dont
		// second replaceAll removes all characters not listed
		// trim and the final replaceAll reduces whitespace, no leading or trailing, and all interior is only a single space
		//return item.replaceAll("[\"']", "").replaceAll("[^A-Za-z0-9:#<>/\\\\.?!=@_-]", " ").trim().replaceAll(" +", " ");
		return item.replaceAll("[\"']", "").replaceAll("[^A-Za-z0-9]", " ").trim().replaceAll(" +", " ");
	}
}
