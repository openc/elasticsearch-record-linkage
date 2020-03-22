package com.opencorporates.record_linkage.scoring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.ImpactsEnum;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.TermState;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.BytesRef;

/**
 * A simple wrapper to create Terms from a list of tokens.
 *
 */
public class TermsStub extends Terms {
    
    List<List<String>> tokens;
    Map<String, Integer> docFreqs;
    
    public TermsStub(List<List<String>> tokens, Map<String, Integer> docFreqs) {
        this.tokens = tokens;
        this.docFreqs = docFreqs;
    }

    @Override
    public TermsEnum iterator() throws IOException {
        return new TermsEnumStub(tokens, docFreqs);
    }
    
    public static class TermsEnumStub extends TermsEnum {
        
        private List<String> tokens;
        private int position;
        private Map<String, Integer> docFreqs;
        private Map<String, List<Integer>> postings;
        private int positionIncrementGap = 1;
        
        public TermsEnumStub(List<List<String>> tokens, Map<String, Integer> docFreqs) {
            position = 0;
            this.tokens = new ArrayList<>();
            this.postings = new HashMap<>();
            
            int cursor = 0;
            for(List<String> value : tokens) {
                for(String token : value) {
                    List<Integer> currentPostings = postings.get(token);
                    if (currentPostings == null) {
                        currentPostings = new ArrayList<>();
                        postings.put(token, currentPostings);
                    }
                    currentPostings.add(cursor);
                    if (!this.tokens.contains(token)) {
                        this.tokens.add(token);
                    }
                    cursor++;
                }
                cursor += positionIncrementGap ;
            }
            this.docFreqs = docFreqs;
        }

        @Override
        public BytesRef next() throws IOException {
            if (position >= tokens.size()) {
                return null;
            }
            BytesRef ref = new BytesRef(tokens.get(position));
            position++;
            return ref;
        }

        @Override
        public AttributeSource attributes() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean seekExact(BytesRef text) throws IOException {
            String token = text.utf8ToString();
            position = tokens.indexOf(token);
            if (position == -1) {
                position = 0;
                return false;
            } else {
                return true;
            }
        }

        @Override
        public SeekStatus seekCeil(BytesRef text) throws IOException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void seekExact(long ord) throws IOException {
            position = (int) ord;
        }

        @Override
        public void seekExact(BytesRef term, TermState state) throws IOException {
            // TODO Auto-generated method stub
        }

        @Override
        public BytesRef term() throws IOException {
            return new BytesRef(tokens.get(position));
        }

        @Override
        public long ord() throws IOException {
            return position;
        }

        @Override
        public int docFreq() throws IOException {
            return docFreqs.get(tokens.get(position));
        }

        @Override
        public long totalTermFreq() throws IOException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public PostingsEnum postings(PostingsEnum reuse, int flags) throws IOException {
            return new TermVectorPostingsEnum(postings.get(tokens.get(position-1)));
        }

        @Override
        public ImpactsEnum impacts(int flags) throws IOException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public TermState termState() throws IOException {
            // TODO Auto-generated method stub
            return null;
        }
        
    }

    @Override
    public long size() throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getSumTotalTermFreq() throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getSumDocFreq() throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getDocCount() throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean hasFreqs() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean hasOffsets() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean hasPositions() {
        return true;
    }

    @Override
    public boolean hasPayloads() {
        // TODO Auto-generated method stub
        return false;
    }
    
    public static class TermVectorPostingsEnum extends PostingsEnum {
        
        int cursor;
        List<Integer> positions;
        
        public TermVectorPostingsEnum(List<Integer> positions) {
            cursor = -1;
            this.positions = positions;
        }

        @Override
        public int freq() throws IOException {
            return positions.size();
        }

        @Override
        public int nextPosition() throws IOException {
            int value = positions.get(cursor);
            cursor++;
            return value;
        }

        @Override
        public int startOffset() throws IOException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int endOffset() throws IOException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public BytesRef getPayload() throws IOException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int docID() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public int nextDoc() throws IOException {
            return (++cursor);
        }

        @Override
        public int advance(int target) throws IOException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public long cost() {
            // TODO Auto-generated method stub
            return 0;
        }
        
    }

}
