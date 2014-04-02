package edu.gmu.cs659.twitter.words;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class WordSanitizer implements WordChecker {

	public Set<String> checkWord(String word) {
		String sanitized = word.replaceAll("[\"']", "")
				.replaceAll("[^A-Za-z0-9]", " ").trim().replaceAll(" +", " ");
		return new HashSet<String>(Arrays.asList(sanitized.split("\\s+")));
	}

}
