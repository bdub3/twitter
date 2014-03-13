package edu.gmu.cs659.twitter.writer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.gmu.cs659.twitter.Tweet;

public class TermCapture extends CaptureLogger {
	private static final Logger logger = LoggerFactory
			.getLogger(TermCapture.class);

	public TermCapture(String file) {
		super(file);
	}

	@Override
	public void writeData(List<Tweet> tweets) {
		logger.debug("Writing term file");
		
		SortedSet<String> terms = new TreeSet<String>();
		
		// add all terms from tweets
		for(Tweet tweet : tweets) {
			terms.addAll(tweet.getTerms());
		}

		// remove all class terms from term list
		for(Tweet tweet : tweets) {
			terms.remove(tweet.getTweetClass());
		}

		// just in case there is a term which matches out class name
		terms.remove("class");
		
		List<String> list = new ArrayList<String>();
		list.addAll(terms);
		list.add("class");
		this.writeRow(list);

		List<Object> counts = new ArrayList<Object>();
		for(Tweet tweet : tweets) {
			counts.clear();
			for(String term : terms) {
				counts.add(Collections.frequency(tweet.getTerms(), term));
			}
			counts.add(tweet.getTweetClass());
			this.writeRow(counts);
		}
	}
}
