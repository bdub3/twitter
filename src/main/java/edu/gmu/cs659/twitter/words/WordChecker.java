package edu.gmu.cs659.twitter.words;

import java.util.Set;

public interface WordChecker {

	/**
	 * Checks a word, potentially replacing it with a new word or words or returning 
	 * an empty List to recommend not using the word
	 * 
	 * @param word
	 *            The word to check
	 * @return A list of new word values, which may optionally be empty
	 */
	public Set<String> checkWord(String word);

}
