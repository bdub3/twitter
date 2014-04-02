package edu.gmu.cs659.twitter.words;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ShortWordFilter implements WordChecker {

	public static final int MIN_LENGTH = 3;
	
	private int minLength;
	
	public ShortWordFilter() {
		this(MIN_LENGTH);
	}

	public ShortWordFilter(int minLength) {
		this.minLength = minLength;
	}

	public Set<String> checkWord(String word) {
		
		if(word.length() < minLength) {
			return Collections.emptySet();
		}
		
		return new HashSet<String>(Arrays.asList(word));
	}

}
