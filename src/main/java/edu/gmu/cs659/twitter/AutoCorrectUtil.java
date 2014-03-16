package edu.gmu.cs659.twitter;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.lucene.search.spell.JaroWinklerDistance;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class AutoCorrectUtil {
	
	private static boolean isLoaded = false;
	private static SpellChecker spellChecker;
	private static Map<String, String> correctionMap;
	
	/**
	 * @param word
	 * @return the same word if it exists in the dictionary,
	 * otherwise the corrected word if it is in the list of common misspelled words,
	 * or the first suggestion of the spell checker 
	 */
	public static String autoCorrect(String word){
		try {
			if ( !isLoaded ){
				loadDictionary();
				isLoaded = true;
			}
		
			if ( spellChecker.exist(word) )
		    	return word;
		    
			String corrected = correctionMap.get(word); 
			if ( corrected != null )
				return corrected;
			
			String[] suggestions = spellChecker.suggestSimilar(word, 1);
			return suggestions[0];
		
		} catch (IOException e){
			return word;
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
		PlainTextDictionary dictionary = new PlainTextDictionary(file);
		spellChecker.indexDictionary(dictionary);
		
        //store the commonly misspelled words
        Scanner scanner = new Scanner(new FileReader(new File("common.txt")));
        correctionMap = new HashMap<String, String>((int)(4160/.75));
        while ( scanner.hasNext() ){
        	String s = scanner.nextLine();
        	String[] words = s.split("->");
        	correctionMap.put(words[0], words[1]);
        }
	}
	
}
