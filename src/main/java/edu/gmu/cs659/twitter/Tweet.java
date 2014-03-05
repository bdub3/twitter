package edu.gmu.cs659.twitter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Tweet {
	List<Object> list;
	
	public static final List<String> FIELDS = Arrays.asList("trend", "time",
			"dayTimePeriod", "source", "text", "lang", "accessLevel", "user",
			"favCount", "lat", "lng", "place", "retweetCount", "hashtaglist",
			"mediaEntries", "userMentions");
	
	public Tweet() {
		list = new ArrayList<Object>();
	}

	public void addAttribute(Object o) {
		if (o == null) {
			list.add("");
		} else {
			list.add(o);
		}
	}
	
	public List<Object> getAttributes() {
		return Collections.unmodifiableList(list);
	}
}
