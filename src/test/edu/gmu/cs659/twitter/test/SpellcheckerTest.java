package edu.gmu.cs659.twitter.test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import junit.framework.Assert;

import org.apache.lucene.search.spell.JaroWinklerDistance;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

import edu.gmu.cs659.twitter.AutoCorrectUtil;

public class SpellcheckerTest {
	
	@Test
	public void AutoCorrectUtilTest(){
		
		Assert.assertEquals("hello", AutoCorrectUtil.autoCorrect("hello"));
		Assert.assertEquals("support", AutoCorrectUtil.autoCorrect("wupport"));
		
		String improvement = AutoCorrectUtil.autoCorrect("improvment");
		Assert.assertNotNull(improvement);
	}
	
	
//	@Test
	public void testLibrary() throws IOException{
		
		//reference dictionary file
		File dir = new File("dictDir");
		File file = new File(dir, "fulldictionary00.txt");
		
		//set up the spell checker using non-default JaroWinklerDistance
		Directory directory = FSDirectory.open(dir);
		SpellChecker spellChecker = new SpellChecker(directory);
		spellChecker.setStringDistance(new JaroWinklerDistance());
		PlainTextDictionary dictionary = new PlainTextDictionary(file);
		spellChecker.indexDictionary(dictionary);
		
        String word = "improvment";
        
        //tests for correct spelling
        if ( spellChecker.exist(word) ){
        	System.out.println("word is valid");
        	spellChecker.close();
        	return;
        }
        
        //store the commonly misspelled words
        Scanner scanner = new Scanner(new FileReader(new File("common.txt")));
        Map<String, String> correctionMap = new HashMap<String, String>((int)(4156/.74));
        while ( scanner.hasNext() ){
        	String s = scanner.nextLine();
        	String[] words = s.split("->");
        	correctionMap.put(words[0], words[1]);
        }
        
        //check to see if the misspelling is in the commonly misspelled map
        String correct = correctionMap.get(word);
        if ( correct != null ){
        	System.out.println("'"+word+"' has been replaced with '"+correct+"'");
        	spellChecker.close();
        	return;
        }
        
        //use the spell checker for suggestions
        int suggestionsNumber = 5;
	    String[] suggestions = spellChecker.suggestSimilar(word, suggestionsNumber);
	    
        if (suggestions!=null && suggestions.length>0) {
            for (String s : suggestions) {
                System.out.println("Did you mean:" + s);
            }
        }
        else {
            System.out.println("No suggestions found for word:"+word);
        }
        
        spellChecker.close();
				
	}

}
