package edu.gmu.cs659.twitter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Tweet {

	private List<String> metaAttributes;
	
	private String tweetStatus;

	private Long timeStamp;
	
	private String tweetClass;
	
	private List<String> terms;
	
	public static final List<String> FIELDS = Arrays.asList("trend", "time",
			"timeStamp", "dayTimePeriod", "source", "text", "lang",
			"accessLevel", "user", "favCount", "lat", "lng", "place",
			"retweetCount", "hashtaglist", "mediaEntries", "userMentions",
			"hashTagCount", "mediaCount", "userMentionsCount");
	
	public Tweet() {
		metaAttributes = new ArrayList<String>();
		terms = new ArrayList<String>();
	}

	public void setTweetClass(String tweetClass) {
		this.tweetClass = tweetClass;
	}
	
	public String getTweetClass() {
		return tweetClass;
	}
	
	public void addAttribute(Object o) {
		if (o == null) {
			metaAttributes.add("");
		} else {
			metaAttributes.add(sanitizeItem(o.toString()));
		}
	}

	public String getTweetStatus() {
		return tweetStatus;
	}

	public void setTweetStatus(String tweetStatus) {
		this.tweetStatus = tweetStatus;
		terms.clear();
		if (tweetStatus != null) {
			terms.addAll(Arrays.asList(sanitizeItem(tweetStatus).split(" ")));
		}
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

	public List<String> getTerms() {
		return Collections.unmodifiableList(terms);
	}	

	private String sanitizeItem(String item) {
		// first replaceAll removes characters with empty string (want empty string to turn don't into dont
		// second replaceAll removes all characters not listed
		// trim and the final replaceAll reduces whitespace, no leading or trailing, and all interior is only a single space
		//return item.replaceAll("[\"']", "").replaceAll("[^A-Za-z0-9:#<>/\\\\.?!=@_-]", " ").trim().replaceAll(" +", " ");
		return item.replaceAll("[\"']", "").replaceAll("[^A-Za-z0-9]", " ").trim().replaceAll(" +", " ");
	}
}
