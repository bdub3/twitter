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

public class InternetShortHandChecker implements WordChecker {

	private static final Map<String, Set<String>> internetLookup;

	private static final String DATA_FILE = "internetShortHand.txt";

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

		Map<String, Set<String>> map = new HashMap<String, Set<String>>();

		if (scanner != null) {
			while (scanner.hasNext()) {
				String s = scanner.nextLine();
				String[] items = s.split("\\t");
				if (items.length == 2) {
					map.put(items[0].trim(),
							new HashSet<String>(Arrays.asList(items[1]
									.split("\\s+"))));
				}
			}
			scanner.close();
		}

		internetLookup = Collections.unmodifiableMap(map);
	}

	public Set<String> checkWord(String word) {
		if (internetLookup.containsKey(word)) {
			return internetLookup.get(word);
		}
		return new HashSet<String>(Arrays.asList(word));
	}

}
