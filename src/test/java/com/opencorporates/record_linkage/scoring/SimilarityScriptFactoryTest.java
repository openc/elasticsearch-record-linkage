package com.opencorporates.record_linkage.scoring;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.LeafReaderContextStub;
import org.elasticsearch.index.analysis.AnalyzerScope;
import org.elasticsearch.index.analysis.IndexAnalyzers;
import org.elasticsearch.index.analysis.NamedAnalyzer;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.script.FieldScript;
import org.elasticsearch.script.ScoreScript;
import org.elasticsearch.search.lookup.DocLookup;
import org.elasticsearch.search.lookup.LeafSearchLookup;
import org.elasticsearch.search.lookup.SearchLookup;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.opencorporates.record_linkage.LeafReaderStub;
import com.opencorporates.record_linkage.similarity.StringSimilarity;

public class SimilarityScriptFactoryTest {
    
    private SimilarityScriptFactory SUT;
    private Map<String, Object> parameters;
    private SearchLookup lookup;
    private LeafSearchLookup leafLookup;
    private StringSimilarity similarity;
    private LeafReaderStub reader;
    
    @Before
    public void setUp() {
        parameters = new HashMap<>();
        parameters.put("field", "name");
        parameters.put("query", "Greentech Distribution Ltd.");
        
        similarity = Mockito.mock(StringSimilarity.class);
        Mockito.when(similarity.compute(Mockito.anyString(), Mockito.anyList(), Mockito.anyMap(), Mockito.anyList(), Mockito.anyMap(), Mockito.anyInt()))
            .thenReturn(0.5);
        Mockito.when(similarity.withParameters(Mockito.anyMap())).thenReturn(similarity);
        
        lookup = Mockito.mock(SearchLookup.class);
        DocLookup docLookup = Mockito.mock(DocLookup.class);
        MapperService mapperService = Mockito.mock(MapperService.class);
        IndexAnalyzers indexAnalyzers = new IndexAnalyzers(
                Collections.singletonMap("default", new NamedAnalyzer("default", AnalyzerScope.GLOBAL, new AnalyzerStub())),
                Collections.emptyMap(), Collections.emptyMap());
        Mockito.when(lookup.doc()).thenReturn(docLookup);
        Mockito.when(docLookup.mapperService()).thenReturn(mapperService);
        Mockito.when(mapperService.getIndexAnalyzers()).thenReturn(indexAnalyzers);
        
        leafLookup = Mockito.mock(LeafSearchLookup.class);
        Mockito.when(leafLookup.asMap()).thenReturn(Collections.emptyMap());
        Mockito.when(lookup.getLeafSearchLookup(Mockito.any())).thenReturn(leafLookup);
        
        reader = new LeafReaderStub();
        reader.setDocId(4);
        
        SUT = new SimilarityScriptFactory(parameters, lookup, similarity);
    }
    
    @Test
    public void testParseWithAnalyzer() {
        List<String> expectedTokens = Arrays.asList("foo", "bar");
        Assert.assertEquals(expectedTokens, SUT.parseWithAnalyzer("foo bar"));
    }
    
    @Test
    public void testScoreScript() throws IOException {
        ScoreScript.LeafFactory leafFactory = SUT.toScoreScriptFactory();
        Assert.assertFalse(leafFactory.needs_score());
        ScoreScript scoreScript = leafFactory.newInstance(LeafReaderContextStub.create(reader));
        scoreScript.setDocument(4);
        
        Assert.assertEquals(scoreScript.execute(), 0.5, 0.01);
    }
    
    @Test
    public void testFieldScript() throws IOException {
        FieldScript.LeafFactory leafFactory = SUT.toFieldScriptFactory();
        FieldScript fieldScript = leafFactory.newInstance(LeafReaderContextStub.create(reader));
        fieldScript.setDocument(5);

        Assert.assertEquals((double)fieldScript.execute(), 0.5, 0.01);
    }

}
