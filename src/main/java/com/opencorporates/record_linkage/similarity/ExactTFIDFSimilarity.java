package com.opencorporates.record_linkage.similarity;

import java.util.List;
import java.util.Map;

/**
 * A string similarity which returns a positive score
 * only if the query and field match exactly and zero otherwise.
 * For an exact match, the value is the TFIDF weight of the query.
 */
public class ExactTFIDFSimilarity implements StringSimilarity {

	@Override
	public double compute(String query, List<String> queryTokens, Map<String, Long> queryFreqs,
			List<String> fieldTokens, Map<String, Long> fieldFreqs, int numDocs) {
		// Determine if query and field are equal
		if (!queryTokens.equals(fieldTokens)) {
			return 0.;
		}
		
		// Compute the TFIDF weight of the query
		return queryFreqs.values().stream().mapToDouble(f -> -Math.log((1.0 + f) / (1.0 + numDocs))).sum();
	}

}
