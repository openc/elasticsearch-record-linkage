package com.opencorporates.record_linkage;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.FieldInfos;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.LeafMetaData;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.index.PointValues;
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.index.SortedNumericDocValues;
import org.apache.lucene.index.SortedSetDocValues;
import org.apache.lucene.index.StoredFieldVisitor;
import org.apache.lucene.index.Terms;
import org.apache.lucene.util.Bits;

import com.opencorporates.record_linkage.scoring.TermsStub;

public class LeafReaderStub extends LeafReader {
    
    private int currentDocId = 0;
    private Map<String, Integer> docFreqs;
    private Map<Integer, List<List<String>>> docs;
    
    public LeafReaderStub() {
        docFreqs = new HashMap<>();
        docFreqs.put("greentech", 12);
        docFreqs.put("distribution", 21);
        docFreqs.put("services", 345);
        docFreqs.put("ltd", 1342);
        
        docs = new HashMap<>();
        docs.put(4, Collections.singletonList(Arrays.asList("greentech", "distribution", "ltd")));
        docs.put(5, Collections.singletonList(Arrays.asList("distribution", "ltd")));
    }
    
    public void setDocId(int newDocId) {
        currentDocId = newDocId;
    }
    
    @Override
    public Terms terms(String field) throws IOException {
        List<List<String>> allValues = docs.get(currentDocId);
        return new TermsStub(allValues, docFreqs);
    }
    
    @Override
    public Fields getTermVectors(int docID) throws IOException {
        return new Fields() {

            @Override
            public Iterator<String> iterator() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Terms terms(String field) throws IOException {
                List<List<String>> tokens = docs.get(docID);
                return new TermsStub(tokens, docFreqs);
            }

            @Override
            public int size() {
                // TODO Auto-generated method stub
                return 0;
            }
            
        };
    }

    @Override
    public CacheHelper getCoreCacheHelper() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public NumericDocValues getNumericDocValues(String field) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BinaryDocValues getBinaryDocValues(String field) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SortedDocValues getSortedDocValues(String field) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SortedNumericDocValues getSortedNumericDocValues(String field) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SortedSetDocValues getSortedSetDocValues(String field) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NumericDocValues getNormValues(String field) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FieldInfos getFieldInfos() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bits getLiveDocs() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PointValues getPointValues(String field) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void checkIntegrity() throws IOException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public LeafMetaData getMetaData() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int numDocs() {
        return 1248;
    }

    @Override
    public int maxDoc() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void document(int docID, StoredFieldVisitor visitor) throws IOException {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void doClose() throws IOException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public CacheHelper getReaderCacheHelper() {
        // TODO Auto-generated method stub
        return null;
    }

}
