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

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;


public class SpellcheckerTest {
	
	@Test
	public void locationFeedTest(){
	
	    StatusListener listener = new StatusListener(){
	        public void onStatus(Status status) {
	            System.out.println("user location=" +status.getUser().getLocation()+",  isGeoEnabled="+ status.getUser().isGeoEnabled()
	            		+", geolocation="+ status.getGeoLocation().toString()+ ", screenName=" + status.getUser().getScreenName() + 
	            		", tweet=" + status.getText());
	        }
	        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}
	        public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}
	        public void onException(Exception ex) {ex.printStackTrace();}
			public void onScrubGeo(long arg0, long arg1) {}
			public void onStallWarning(StallWarning sw) { System.out.println("onStallWarning: "+sw.getMessage()); }
	    };
	    TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
	    
	    
	    twitterStream.addListener(listener);
		
	    FilterQuery query = new FilterQuery();
	    double[][] locations = { {-74.0, 40.0}, {-73.0, 41.0} };	//NYC area
	    query.locations(locations);
		twitterStream.filter(query);
	    
//	    twitterStream.sample();
	    
	    
	    try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {}
	    
	    twitterStream.cleanUp();
	}
	
	
	//@Test
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
		
        String word = "an";
        
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
        int suggestionsNumber = 1;
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
