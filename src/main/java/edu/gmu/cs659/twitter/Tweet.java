package edu.gmu.cs659.twitter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Tweet {
	List<String> list;
	
	public static final List<String> FIELDS = Arrays.asList("trend", "time",
			"dayTimePeriod", "source", "text", "lang", "accessLevel", "user",
			"favCount", "lat", "lng", "place", "retweetCount", "hashtaglist",
			"mediaEntries", "userMentions", "hashTagCount", "mediaCount",
			"userMentionsCount");
	
	public Tweet() {
		list = new ArrayList<String>();
	}

	public void addAttribute(Object o) {
		if (o == null) {
			list.add("");
		} else {
			list.add(sanitizeItem(o.toString()));
		}
	}
	
	public List<String> getAttributes() {
		return Collections.unmodifiableList(list);
	}
	
	private String sanitizeItem(String item) {
		return item.replaceAll("\\r\\n|\\r|\\n|,", " ").replace("\"|'", "");
	}
}
