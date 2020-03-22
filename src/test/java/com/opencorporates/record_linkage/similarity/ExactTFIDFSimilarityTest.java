package com.opencorporates.record_linkage.similarity;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ExactTFIDFSimilarityTest extends StringSimilarityTestBase {

    @Before
    public void setUp() {
        SUT = new ExactTFIDFSimilarity();
    }
    
    @Test
    public void testExactTFIDF() {
        double score = invoke("greentech services ltd",
                Arrays.asList("greentech", "services", "ltd"),
                Arrays.asList("greentech", "services", "ltd"));
        Assert.assertEquals(15.41, score, 0.01);
    }
    
    @Test
    public void testExactTFIDFMismatch() {
        double score = invoke("greentech services ltd",
                Arrays.asList("greentech", "services", "ltd"),
                Arrays.asList("greentech", "services"));
        Assert.assertEquals(0.0, score, 0.01);
    }
}
