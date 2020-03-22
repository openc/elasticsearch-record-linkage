package com.opencorporates.record_linkage.scoring;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.index.analysis.AnalyzerScope;
import org.elasticsearch.index.analysis.IndexAnalyzers;
import org.elasticsearch.index.analysis.NamedAnalyzer;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.script.FieldScript;
import org.elasticsearch.script.ScoreScript;
import org.elasticsearch.script.ScriptContext;
import org.elasticsearch.search.lookup.DocLookup;
import org.elasticsearch.search.lookup.SearchLookup;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class RecordLinkageScorerTest {
    
    private RecordLinkageScorer SUT;
    private Map<String, String> rootParameters;
    private Map<String, Object> leafParameters;
    private SearchLookup searchLookup;
    
    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        SUT = new RecordLinkageScorer();
        rootParameters = (Map<String,String>)Mockito.mock(Map.class);
        leafParameters = new HashMap<>();
        leafParameters.put("field", "name");
        leafParameters.put("query", "Greentech Distribution Ltd.");
        
        searchLookup = Mockito.mock(SearchLookup.class);
        DocLookup docLookup = Mockito.mock(DocLookup.class);
        MapperService mapperService = Mockito.mock(MapperService.class);
        IndexAnalyzers indexAnalyzers = new IndexAnalyzers(
                Collections.singletonMap("default", new NamedAnalyzer("default", AnalyzerScope.GLOBAL, new AnalyzerStub())),
                Collections.emptyMap(), Collections.emptyMap());
        Mockito.when(searchLookup.doc()).thenReturn(docLookup);
        Mockito.when(docLookup.mapperService()).thenReturn(mapperService);
        Mockito.when(mapperService.getIndexAnalyzers()).thenReturn(indexAnalyzers);
    }
    
    @Test
    public void testGetType() {
        Assert.assertEquals("record_linkage_scorer", SUT.getType());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidSource() {
        SUT.compile("my script", "some invalid source", ScoreScript.CONTEXT, Collections.emptyMap());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidContext() {
        SUT.compile("my script", "tfidf", new ScriptContext<>("some script", ScoreScript.Factory.class), Collections.emptyMap());
    }
    
    @Test
    public void testScoreScript() {
        ScoreScript.Factory result = SUT.compile("my script", "tfidf", ScoreScript.CONTEXT, rootParameters);
        Assert.assertTrue(result instanceof ScoreScript.Factory);
        ScoreScript.Factory factory = (ScoreScript.Factory)result;
        ScoreScript.LeafFactory leafFactory = factory.newFactory(leafParameters, searchLookup);
        Assert.assertNotNull(leafFactory);
    }
    
    @Test
    public void testFieldScript() {
        FieldScript.Factory result = SUT.compile("my script", "tfidf", FieldScript.CONTEXT, rootParameters);
        Assert.assertTrue(result instanceof FieldScript.Factory);
        FieldScript.Factory factory = (FieldScript.Factory)result;
        FieldScript.LeafFactory leafFactory = factory.newFactory(leafParameters, searchLookup);
        Assert.assertNotNull(leafFactory);
    }

}
