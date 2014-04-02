package edu.gmu.cs659.twitter.words;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.lucene.search.spell.JaroWinklerDistance;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoCorrectUtil implements WordChecker {
	
	private static boolean isLoaded = false;
	private static SpellChecker spellChecker;
	private static Map<String, String> correctionMap;

	private static final Logger logger = LoggerFactory
			.getLogger(AutoCorrectUtil.class);

	/**
	 * @param word
	 * @return the same word if it exists in the dictionary,
	 * otherwise the corrected word if it is in the list of common misspelled words,
	 * or the first suggestion of the spell checker 
	 */
	public Set<String> checkWord(String word) {
		word = word.toLowerCase();
		try {
			if ( !isLoaded ){
				loadDictionary();
				isLoaded = true;
			}
		
			if ( spellChecker.exist(word) ) {
		    	return new HashSet<String>(Arrays.asList(word));
			}
			
			String corrected = correctionMap.get(word); 
			if ( corrected != null ) {
				logger.debug("Replacing {} with {}", word, corrected);
				return new HashSet<String>(Arrays.asList(corrected));
			}
			// this seems to make things uglier (e.g. microsoft -> micrsome, co -> cob)
			/*
			String[] suggestion = spellChecker.suggestSimilar(word, 1);
			if(suggestion.length > 0) { 
				logger.debug("Replacing {} with {}", word, suggestion[0]);
				return new HashSet<String>(Arrays.asList(suggestion[0])); 
			}*/
			
			logger.debug("No replacement found for {} ", word);
			return new HashSet<String>(Arrays.asList(word));
		} catch (IOException e){
			logger.warn("Problem using AutoCorrectUtil on {} : {}", word, e);				
			return new HashSet<String>(Arrays.asList(word));
		} catch(Exception e) {
			logger.warn("Problem using AutoCorrectUtil on {} : {}", word, e);				
			return new HashSet<String>(Arrays.asList(word));
		}
	}
	
	private static void loadDictionary() throws IOException{
		//reference dictionary file
		File dir = new File("dictDir");
		File file = new File(dir, "fulldictionary00.txt");
		
		//set up the spell checker using non-default JaroWinklerDistance
		Directory directory = FSDirectory.open(dir);
		spellChecker = new SpellChecker(directory);
		spellChecker.setStringDistance(new JaroWinklerDistance());
		spellChecker.setAccuracy(.9f);
		PlainTextDictionary dictionary = new PlainTextDictionary(file);
		spellChecker.indexDictionary(dictionary);
		
        //store the commonly misspelled words
        Scanner scanner = new Scanner(new FileReader(new File("common.txt")));
        correctionMap = new HashMap<String, String>((int)(4160/.75));
        while ( scanner.hasNext() ){
        	String s = scanner.nextLine();
        	String[] words = s.split("->");
        	String[] suggestions = words[1].split(",");
        	correctionMap.put(words[0].trim(), suggestions[0].trim());
        }
        scanner.close();
	}
	
}
