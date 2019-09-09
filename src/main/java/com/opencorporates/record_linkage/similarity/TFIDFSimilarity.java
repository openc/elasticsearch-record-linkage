package com.opencorporates.record_linkage.similarity;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * A simple TFIDF-based similarity following Cohen et al.,
 * "A Comparison of String Metrics for Matching Names and Records".
 * 
 * Their implementation in the SecondString Java package
 * normalizes the query and field vectors by Euclidian norm.
 * This implementation makes it possible to disable either similarities
 * by setting the corresponding norm exponents to 1.0 (instead of 0.0 by default).
 * 
 * This makes it possible to encode a form of "confidence" in the similarity:
 * two strings which match perfectly but only contain very common words
 * (such as "Direct Services") get a lower score than strings containing
 * rarer words (such as "Northumbria Breweries").
 */
public class TFIDFSimilarity implements StringSimilarity {
	
	private Logger logger = Logger.getLogger("TFIDF");
	
	protected double queryNormExponent = 0;
	protected double docNormExponent = 0;
	
	public TFIDFSimilarity() {
		// default constructor
	}
	
	public TFIDFSimilarity(double queryNormExponent, double docNormExponent) {
		this.queryNormExponent = queryNormExponent;
		this.docNormExponent = docNormExponent;
	}
	
	/**
	 * Reads a numeric parameter from the user-supplied parameters
	 */
	private double readDouble(Map<String, Object> parameters, String key, double defaultValue) {
		Object v = parameters.get(key);
		if(v == null) {
			return defaultValue;
		}
		try {
			return Double.valueOf(v.toString());
		} catch(NumberFormatException e) {
			return defaultValue;
		}
	}
	
	@Override
	public TFIDFSimilarity withParameters(Map<String, Object> parameters) {
		return new TFIDFSimilarity(readDouble(parameters, "query_norm_exponent", 0.), readDouble(parameters, "doc_norm_exponent", 0.));
	}

	@Override
	public double compute(String query, List<String> queryTokens, Map<String, Long> queryFreqs,
			List<String> fieldTokens, Map<String, Long> fieldFreqs, int numDocs) {
		Map<String, Double> queryVector = weightVector(queryFreqs, numDocs, queryNormExponent);
		Map<String, Double> docVector = weightVector(fieldFreqs, numDocs, docNormExponent);
		double result = 0;
		Set<String> tokens = new HashSet<>();
		tokens.addAll(queryTokens);
		tokens.addAll(fieldTokens);
		for(String token : tokens) {
			double fieldScore = 0.;
			if (fieldTokens.contains(token)) {
				fieldScore = docVector.getOrDefault(token, 0.);
			}
			result += queryVector.getOrDefault(token, 0.) * fieldScore;
		}
		return result;
	}
	
	public Map<String, Double> weightVector(Map<String, Long> frequencies, int numDocs, double normExponent) {
		Map<String, Double> rawWeights = frequencies.entrySet().stream().collect(Collectors.toMap(Entry::getKey,
				f -> -Math.log((1.0 + f.getValue()) / (1.0 + numDocs))));
		if (normExponent == 1.0) {
			return rawWeights;
		}
		Double l2Norm = Math.sqrt(rawWeights.values().stream().mapToDouble(w -> w * w).sum());
		if (l2Norm == 0) {
			return rawWeights;
		}
		return rawWeights.entrySet().stream().collect(Collectors.toMap(Entry::getKey, w -> w.getValue() / Math.pow(l2Norm, 1.0 - normExponent)));
	}

}
