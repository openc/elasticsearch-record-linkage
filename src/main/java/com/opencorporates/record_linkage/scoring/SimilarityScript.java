package com.opencorporates.record_linkage.scoring;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.lucene.index.Fields;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;

import com.opencorporates.record_linkage.similarity.StringSimilarity;

/**
 * String similarity methods can be called from two contexts:
 * as scoring functions (to contribute to the score of a search result)
 * or as scripted fields (to expose the value of the string similarity to
 * the user).
 *
 * This class gathers logic that is common to both contexts, translating
 * the data required by the similarity heuristics from ElasticSearch's internal
 * representation to a more convenient representation required by {@class StringSimilarity}.
 */
public class SimilarityScript {
	private Logger logger = Logger.getLogger("RecordLinkage");
	
	protected LeafReader reader;
	protected String field;
	protected int currentDocId = -1;
	protected int positionIncrementGap;
	protected String query;
	protected List<String> queryTokens;
	protected Map<String, Long> queryFreqs;
	protected int totalDocs;
	protected StringSimilarity similarity;
	
	/**
	 * Creates a similarity script.
	 * 
	 * @param reader
	 * 		provides access to the search index to retrieve the frequencies
	 * @param field
	 *      name of the field to compute the string similarity against
	 * @param query
	 *      the query string which should be compared to the field value
	 * @param queryTokens
	 *      the same query parsed into a list of tokens by the analyzer
	 * @param positionIncrementGap
	 *      the amount by which token positions are shifted when a new field value starts,
	 *      in ElasticSearch's representation of token lists via offsets
	 * @param totalDocs
	 *      total number of documents in the index
	 * @param similarity
	 *      the similarity to use to compare query and field.
	 */
	public SimilarityScript(LeafReader reader, String field, String query,
			List<String> queryTokens, int positionIncrementGap, int totalDocs, StringSimilarity similarity) {
		this.reader = reader;
		this.field = field;
		this.positionIncrementGap = positionIncrementGap;
		this.query = query;
		this.queryTokens = queryTokens;
		this.queryFreqs = getFrequencies(queryTokens);
		this.totalDocs = totalDocs;
		this.similarity = similarity;
	}
	
	/**
	 * Shifts the script to a new document
	 */
    public void setDocument(int docid) {
        currentDocId = docid;
    }
    
    /**
     * Executes the similarity computation on the current document.
     * @return
     */
    public double execute() {
    	List<List<String>> fieldValues = getFieldValues();
    	
    	// We compute all the frequencies at once to save up on frequency lookups of common tokens between the multiple values
    	Map<String, Long> fieldFreqs = getFrequencies(fieldValues.stream().flatMap(List::stream).collect(Collectors.toSet()));
    	
    	// Take the maximum score over all field values (TODO: this could be configurable)
    	double maxScore = 0.;
    	for(List<String> fieldValue : fieldValues) {
    		Map<String, Long> tokenFrequencies = fieldValue.stream().distinct().collect(Collectors.toMap(k -> k, k -> fieldFreqs.get(k)));
    		
    		double s = similarity.compute(query, queryTokens, queryFreqs, fieldValue, tokenFrequencies, totalDocs);
    		if (s > maxScore) {
    			maxScore = s;
    		}
    	}
        return maxScore;
    }
    
    /**
     * Retrieves the values of the target field. Each value is represented as a list of tokens.
     */
    protected List<List<String>> getFieldValues() {
    	try {
    		Fields fields = reader.getTermVectors(currentDocId);
    		
			Terms terms = fields == null ? null : fields.terms(field);
			
			if(terms == null) {
				return Collections.emptyList();
			}
			
			if (!terms.hasPositions()) {
				logger.warning("Term vector positions missing for field '"+field+"'");
				return Collections.emptyList();
			}
			return getFieldTokensFromTermVector(terms.iterator());
		} catch (IOException e) {
			// return an empty map
		}
    	return Collections.emptyList();
    }
    
    /**
     * A field can contain multiple values, 
     * each of which contains multiple tokens. This
     * parses the corresponding nested list of tokens
     * from a term vector
     */
    public List<List<String>> getFieldTokensFromTermVector(TermsEnum termsEnum) throws IOException {
    	// First, index the tokens by position
    	Map<Integer, String> positionToToken = new HashMap<>();
		BytesRef term;
		PostingsEnum postings = null;
		while((term = termsEnum.next()) != null) {
			String token = term.utf8ToString();
			postings = termsEnum.postings(postings, PostingsEnum.POSITIONS);
			postings.nextDoc();
			for(int i = 0; i < postings.freq(); i++) {
				positionToToken.put(postings.nextPosition(), token);
			}
		}
		
		// Second, generate value lists (each of which is a list of tokens)
		// The positions between two value lists are separated by positionIncrementGap
		List<List<String>> fieldValues = new LinkedList<>();
		List<String> currentValue = new LinkedList<>();
		int currentPosition = 0;
		for(int i = 0; i < positionToToken.size(); i++) {
			if(positionToToken.containsKey(currentPosition)) {
				currentValue.add(positionToToken.get(currentPosition));
				currentPosition++;
			} else {
				fieldValues.add(currentValue);
				currentValue = new LinkedList<>();
				currentPosition += positionIncrementGap;
				i--;
			}
		}
		if (!currentValue.isEmpty()) {
			fieldValues.add(currentValue);
		} 
		return fieldValues;
    }
    
    /**
     * Given a collection of tokens, lookup their frequencies from the index
     */
    public Map<String, Long> getFrequencies(Collection<String> tokens) {
    	Map<String, Long> freqs = new HashMap<>();
    	for(String token : tokens) {
    		try {
				freqs.put(token, (long) reader.docFreq(new Term(field, token)));
			} catch (IOException e) {
				freqs.put(token, 1L);
			}
    	}
    	return freqs;
    }
    

}
