package edu.gmu.cs659.twitter.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import edu.gmu.cs659.twitter.words.AutoCorrectUtil;
import edu.gmu.cs659.twitter.words.InternetShortHandChecker;
import edu.gmu.cs659.twitter.words.NumericFilter;
import edu.gmu.cs659.twitter.words.ShortWordFilter;
import edu.gmu.cs659.twitter.words.StopWordFilter;
import edu.gmu.cs659.twitter.words.WordChecker;
import edu.gmu.cs659.twitter.words.WordSanitizer;

public class TestFilter {

	@Test 
	public void testFilter() {
		
		List<WordChecker> checkers = new ArrayList<WordChecker>();
		// note, order matters, these are run serially
		checkers.add(new InternetShortHandChecker());
		checkers.add(new WordSanitizer());
		checkers.add(new NumericFilter());
		checkers.add(new ShortWordFilter());
		checkers.add(new StopWordFilter());
		checkers.add(new AutoCorrectUtil());
		checkers.add(new WordSanitizer());  // sanitize again incase autocorrect added stuff, tick marks, etc
			
			
		String tweetStatus = "matt clements22 exactly Matt a bunch of fags who have nothing else to do it was lowkey busy today";
		
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
		
		System.out.println(termSet);
	}
}
