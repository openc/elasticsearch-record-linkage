package com.opencorporates.record_linkage.similarity;

import java.util.List;
import java.util.Map;

/**
 * A string similarity which returns a positive score
 * only if the query and field match exactly and zero otherwise.
 * For an exact match, the value is the TFIDF weight of the query.
 */
public class ExactTFIDFSimilarity implements StringSimilarity {

	/**
	 * Compute the similarity between a query and a document, given by the frequencies
	 * of all tokens appearing in that field.
	 * @param query
	 *     the original query string supplied by the user
	 * @param queryTokens
	 *     the query parsed into an ordered list of tokens
	 * @param queryFreqs
	 *     the frequency (number of documents appeared in) of each token in the query
	 * @param fieldTokens
	 *     the field value parsed into an ordered list of tokens
	 * @param fieldFreqs
	 *     the frequency (number of documents appeared in) of each token in the matched field.
	 * @param numDocs
	 *     the number of documents in which the field is present
	 * @return a non-negative score
	 */
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
