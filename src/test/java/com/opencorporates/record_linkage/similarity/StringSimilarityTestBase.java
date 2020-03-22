package com.opencorporates.record_linkage.similarity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Before;

public class StringSimilarityTestBase {
    protected StringSimilarity SUT;
    protected Map<String, Long> frequencies;
    
    @Before
    public void setUpFrequencies() {
        frequencies = new HashMap<>();
        frequencies.put("greentech", 1L);
        frequencies.put("ltd", 1000L);
        frequencies.put("services", 100L);
    }
    
    protected double invoke(String queryString, List<String> query, List<String> field) {
        Map<String, Long> queryFreqs = getFreqs(query);
        Map<String, Long> docFreqs = getFreqs(field);
        return SUT.compute(queryString, query, queryFreqs, field, docFreqs, 10000);
     }
    
    protected Map<String, Long> getFreqs(List<String> query) {
        return query.stream().collect(Collectors.toMap(s -> s, s -> frequencies.get(s)));
    }
}
