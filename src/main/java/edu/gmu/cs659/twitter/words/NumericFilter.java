package edu.gmu.cs659.twitter.words;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.math.NumberUtils;

public class NumericFilter implements WordChecker {

	public Set<String> checkWord(String word) {

		if (NumberUtils.isNumber(word)) {
			return Collections.emptySet();
		}

		return new HashSet<String>(Arrays.asList(word));
	}

}
