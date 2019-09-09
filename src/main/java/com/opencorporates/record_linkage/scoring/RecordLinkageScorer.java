package com.opencorporates.record_linkage.scoring;

import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.script.FieldScript;
import org.elasticsearch.script.ScoreScript;
import org.elasticsearch.script.ScriptContext;
import org.elasticsearch.script.ScriptEngine;
import org.elasticsearch.search.lookup.SearchLookup;

import com.opencorporates.record_linkage.similarity.ExactTFIDFSimilarity;
import com.opencorporates.record_linkage.similarity.LevenshteinSimilarity;
import com.opencorporates.record_linkage.similarity.StringSimilarity;
import com.opencorporates.record_linkage.similarity.TFIDFSimilarity;

/**
 * Exposes various name similarity scoring methods as custom scripts,
 * for use in search candidate rescoring.
 * 
 * The string similarity algorithms are supplied with the tokens present in both
 * strings, as tokenized by the analyzer supplied, and their frequencies in the 
 * index. To add support for a new metric, implement {@class StringSimilarity}
 * and add your class to the registeredSimilarities map.
 * 
 * @author Antonin Delpeuch
 */
public class RecordLinkageScorer implements ScriptEngine {
	
	// map of all known similarities. The key is the string input by the user to select it.
	public static Map<String, StringSimilarity> registeredSimilarities = new HashMap<>();
	
    static {
    	registeredSimilarities.put("tfidf", new TFIDFSimilarity());
    	registeredSimilarities.put("levenshtein", new LevenshteinSimilarity());
    	registeredSimilarities.put("exact_tfidf", new ExactTFIDFSimilarity());
    }

	@Override
	public String getType() {
		return "record_linkage_scorer";
	}

	@Override
	public <FactoryType> FactoryType compile(String name, String code, ScriptContext<FactoryType> context,
			Map<String, String> params) {
		// Lookup similarity (the "source" code supplied by the user must be one of the registered similarities)
		StringSimilarity selectedSimilarity = registeredSimilarities.get(code);
		if (selectedSimilarity == null) {
			String usage = String.join(", ", registeredSimilarities.keySet());
			throw new IllegalArgumentException("Invalid source, available similarities are: "+usage);
		}
		
		// Ensure the script is used in a supported context
		if (context.equals(ScoreScript.CONTEXT)) {
			ScoreScript.Factory factory = new ScoreScript.Factory() {
				
				@Override
				public ScoreScript.LeafFactory newFactory(Map<String, Object> params, SearchLookup lookup) {
					SimilarityScriptFactory wrapped = new SimilarityScriptFactory(params, lookup, selectedSimilarity);
					return wrapped.toScoreScriptFactory();
				}
			};
	        return context.factoryClazz.cast(factory);
		} else if (context.equals(FieldScript.CONTEXT)) {
			FieldScript.Factory factory = new FieldScript.Factory() {
				
				@Override
				public FieldScript.LeafFactory newFactory(Map<String, Object> params,
						SearchLookup lookup) {
					SimilarityScriptFactory wrapped = new SimilarityScriptFactory(params, lookup, selectedSimilarity);
					return wrapped.toFieldScriptFactory();
				}
			};
			return context.factoryClazz.cast(factory);
        } else {
        	throw new IllegalArgumentException(getType()
                    + " scripts cannot be used for context ["
                    + context.name + "]");
        }
	}

}
