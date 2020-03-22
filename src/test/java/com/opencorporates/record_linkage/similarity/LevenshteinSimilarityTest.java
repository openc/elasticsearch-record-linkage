package com.opencorporates.record_linkage.similarity;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class LevenshteinSimilarityTest {
    
    private StringSimilarity SUT;
    private Map<String, Long> frequencies;
    
    @Before
    public void setUp() {
        SUT = new LevenshteinSimilarity();
        frequencies = new HashMap<>();
    }
    
    @Test
    public void testLevenshtein() {
        double score = SUT.compute("is this a test?",
                Arrays.asList("is", "this", "a", "test?"),
                Collections.emptyMap(),
                Arrays.asList("this", "is", "a", "test"),
                frequencies, 1234);
        Assert.assertEquals(score, 95.0, 0.01);
    }
    
    @Test
    public void testParameters() {
        Assert.assertEquals(SUT, SUT.withParameters(Collections.emptyMap()));
    }
}
