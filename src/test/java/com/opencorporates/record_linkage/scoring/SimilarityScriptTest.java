package com.opencorporates.record_linkage.scoring;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.TermsEnum;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.opencorporates.record_linkage.LeafReaderStub;
import com.opencorporates.record_linkage.similarity.StringSimilarity;

public class SimilarityScriptTest {
    
    private SimilarityScript SUT;
    private LeafReaderStub reader;
    private StringSimilarity similarity;
    private List<String> queryTokens;
    
    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        reader = new LeafReaderStub();
        reader.setDocId(4);
        similarity = Mockito.mock(StringSimilarity.class);
        Mockito.when(similarity.compute(Mockito.anyString(), Mockito.anyList(), Mockito.anyMap(), Mockito.anyList(), Mockito.anyMap(), Mockito.anyInt()))
            .thenReturn(0.5);
        queryTokens = Arrays.asList("greentech", "distribution", "ltd");
        SUT = new SimilarityScript(reader, "name", "greentech distribution ltd", queryTokens, 1, 1234, similarity);
    }
    
    @Test
    public void testGetFrequencies() {
        Map<String, Long> frequencies = SUT.getFrequencies(queryTokens);
        Assert.assertEquals(12L, (long)frequencies.get("greentech"));
    }
    
    @Test
    public void testGetFieldTokensFromTermVector() throws IOException {
        List<List<String>> values = Arrays.asList(Arrays.asList("greentech", "distribution"), Arrays.asList("greentech", "ltd"));
        
        TermsEnum termsEnum = new TermsStub.TermsEnumStub(values, Collections.emptyMap());
        List<List<String>> result = SUT.getFieldTokensFromTermVector(termsEnum);
        
        Assert.assertEquals(values, result);
    }
    
    @Test
    public void testGetFieldValues() {
        SUT.setDocument(5);
        
        List<List<String>> values = Collections.singletonList(Arrays.asList("distribution", "ltd"));
        Assert.assertEquals(values, SUT.getFieldValues());
    }
    
    @Test
    public void testCompute() {
        SUT.setDocument(4);
        
        Assert.assertEquals(0.5, SUT.execute(), 0.001);
    }
    
}
