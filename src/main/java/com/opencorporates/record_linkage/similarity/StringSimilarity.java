package com.opencorporates.record_linkage.similarity;

import java.util.List;
import java.util.Map;

public interface StringSimilarity {
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
	public double compute(
			String query,
			List<String> queryTokens,
			Map<String, Long> queryFreqs,
			List<String> fieldTokens,
			Map<String, Long> fieldFreqs,
			int numDocs);
	
	/**
	 * Gives an opportunity to the string similarity to read some user-supplied parameters,
	 * such as constants involved in the computation of the metric.
	 * These parameters are supplied at query time in the script parameters.
	 * This returns a new copy of the string similarity metric, configured appropriately.
	 */
	public default StringSimilarity withParameters(Map<String, Object> parameters) {
		// Does nothing by default
		return this;
	}
}
