package com.opencorporates.record_linkage.scoring;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.LetterTokenizer;

public class AnalyzerStub extends Analyzer {

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        return new TokenStreamComponents(new LetterTokenizer());
    }

}
