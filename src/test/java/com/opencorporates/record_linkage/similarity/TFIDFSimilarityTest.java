package com.opencorporates.record_linkage.similarity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TFIDFSimilarityTest extends StringSimilarityTestBase {

    @Before
    public void setUp() {
        SUT = new TFIDFSimilarity();
    }

    @Test
    public void testTDIDF() {
        double score = invoke("greetech services", Arrays.asList("greentech", "services"), Arrays.asList("greentech", "services", "ltd"));
        Assert.assertEquals(0.97, score, 0.01);
    }
    
    @Test
    public void testTFIDFFrequentWords() {
        double score = invoke(
                "services ltd",
                Arrays.asList("services", "ltd"),
                Arrays.asList("greentech", "services", "ltd"));
        Assert.assertEquals(0.52, score, 0.01);
    }
    
    @Test
    public void testQueryNorms() {
        Map<String, Object> map = new HashMap<>();
        map.put("query_norm_exponent", 1.0);
        SUT = SUT.withParameters(map);
        
        double score = invoke(
                "services ltd",
                Arrays.asList("services", "ltd"),
                Arrays.asList("greentech", "services", "ltd"));
        Assert.assertEquals(2.66, score, 0.01); 
    }
}
