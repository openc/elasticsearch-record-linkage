package com.opencorporates.record_linkage.similarity;

import java.util.List;
import java.util.Map;
import org.apache.commons.text.similarity.LevenshteinDistance;

/**
 * A simple string similarity which returns 100 - d(q,f)
 * where d(q,f) is the Levenshtein distance between the query
 * and the field value.
 */
public class LevenshteinSimilarity implements StringSimilarity {
	
	protected LevenshteinDistance distance = new LevenshteinDistance();

	@Override
	public double compute(String query, List<String> queryTokens, Map<String, Long> queryFreqs,
			List<String> fieldTokens, Map<String, Long> fieldFreqs, int numDocs) {
		String parsedQuery = String.join(" ", queryTokens);
	    String parsedField = String.join(" ", fieldTokens);
	    int moves = distance.apply(parsedQuery, parsedField);
	    return (double) Math.max(0, 100-moves);
	}

}
