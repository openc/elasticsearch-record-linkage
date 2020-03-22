package com.opencorporates.record_linkage.scoring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.util.BytesRefBuilder;
import org.elasticsearch.index.analysis.IndexAnalyzers;
import org.elasticsearch.script.FieldScript;
import org.elasticsearch.script.FieldScript.LeafFactory;
import org.elasticsearch.script.ScoreScript;
import org.elasticsearch.search.lookup.SearchLookup;

import com.opencorporates.record_linkage.similarity.StringSimilarity;

/**
 * This class handles the parsing of the query using the analyzer provided.
 * The analyzer should be the same as the one used to index the field.
 */
public class SimilarityScriptFactory {
	
    private final SearchLookup lookup;
    private final Map<String, Object> params;
    private final StringSimilarity similarity;
    private final List<String> queryTokens;
    private final String field;
    private final String query;
    private final Analyzer analyzer;
	private Logger logger = Logger.getLogger("RecordLinkage");
	private int positionIncrementGap;

	/**
	 * Constructs a factory.
	 * @param params
	 *     the similarity parameters supplied by the user in the query
	 * @param lookup
	 *     an entry point to the index
	 * @param similarity
	 *     the string similarity to use when comparing the query to the field
	 */
    protected SimilarityScriptFactory(Map<String, Object> params, SearchLookup lookup, StringSimilarity similarity) {
		if (!params.containsKey("query")) {
			throw new IllegalArgumentException("Missing argument 'query', containing the name to compare against.");
		}
		if (!params.containsKey("field")) {
			throw new IllegalArgumentException("Missing argument 'field', the field to compare the query against.");
		}
		this.query = params.get("query").toString();
		this.field = params.get("field").toString();
		this.params = params;
        this.lookup = lookup;
        
        // Configure similarity with user-supplied parameters
        Map<String, Object> similarityParameters = new HashMap<>(params);
        similarityParameters.remove("query");
        similarityParameters.remove("field");
        this.similarity = similarity.withParameters(similarityParameters);
        
        // Parse the query into tokens according to the analyzer supplied
		IndexAnalyzers indexAnalyzers = lookup.doc().mapperService().getIndexAnalyzers();
		if (params.containsKey("analyzer"))  {
			this.analyzer = indexAnalyzers.get(params.get("analyzer").toString());
		} else {
			this.analyzer = indexAnalyzers.getDefaultSearchAnalyzer();
		}
		this.positionIncrementGap = analyzer.getPositionIncrementGap(field);
        this.queryTokens = parseWithAnalyzer(query);
    }
    
    /**
     * Parses a string with the analyzer supplied to the script.
     * @param text
     *         the text to parse into tokens
     * @return
     *        the list of tokens
     */
    protected List<String> parseWithAnalyzer(String text) {
    	List<String> results = new ArrayList<>();

    	try (TokenStream source = analyzer.tokenStream(field, query)) {
            source.reset();
            CharTermAttribute termAtt = source.addAttribute(CharTermAttribute.class);
            BytesRefBuilder builder = new BytesRefBuilder();
            while (source.incrementToken()) {
            	builder.copyChars(termAtt);
            	results.add(builder.toBytesRef().utf8ToString());
            }
    	} catch (IOException e) {
			// Leave the list of tokens empty.
		}
		return results;
    }
    
    public SimilarityScript similarityScript(LeafReaderContext context) throws IOException {
    	int totalDocs = context.reader().getDocCount(field);
    	
    	return new SimilarityScript(context, field, query, queryTokens, positionIncrementGap, totalDocs, similarity);
    }
    
    public ScoreScript.LeafFactory toScoreScriptFactory() {
    	return new ScoreScript.LeafFactory() {
			@Override
			public boolean needs_score() {
				return false;
			}

			@Override
			public ScoreScript newInstance(LeafReaderContext ctx) throws IOException {
				return new ScoreScript(params, lookup, ctx) {
					
					SimilarityScript wrapped = similarityScript(ctx);
					
					@Override
					public void setDocument(int docId) {
						wrapped.setDocument(docId);
					}

					@Override
					public double execute(ExplanationHolder explanation) {
						return wrapped.execute();
					}
					
				};
			}		
		};

    }

	public LeafFactory toFieldScriptFactory() {
		return new FieldScript.LeafFactory() {

			@Override
			public FieldScript newInstance(LeafReaderContext ctx) throws IOException {
				return new FieldScript(params, lookup, ctx) {
					
					SimilarityScript wrapped = similarityScript(ctx);
					
					@Override
					public void setDocument(int docId) {
						wrapped.setDocument(docId);
					}

					@Override
					public Object execute() {
						return wrapped.execute();
					}
					
				};
			}		
		};
	}
  
}
