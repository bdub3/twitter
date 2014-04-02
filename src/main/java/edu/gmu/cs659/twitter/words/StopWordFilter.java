package edu.gmu.cs659.twitter.words;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StopWordFilter implements WordChecker {

	private static final Set<String> stopWords;

	private static final String DATA_FILE = "stopwordsweka.txt";

	private static final Logger logger = LoggerFactory
			.getLogger(InternetShortHandChecker.class);

	static {
		Scanner scanner = null;
		try {
			scanner = new Scanner(new FileReader(new File(DATA_FILE)));
		} catch (FileNotFoundException e) {
			logger.warn("Failed to file to load internet shorthand: {}",
					DATA_FILE);
		}

		Set<String> set = new HashSet<String>();

		if (scanner != null) {
			while (scanner.hasNext()) {
				String s = scanner.nextLine();
				set.add(s.trim());
			}
			scanner.close();
		}

		stopWords = Collections.unmodifiableSet(set);
	}


	public Set<String> checkWord(String word) {

		if(stopWords.contains(word)) {
			return Collections.emptySet();
		}
		
		return new HashSet<String>(Arrays.asList(word));
	}

}
