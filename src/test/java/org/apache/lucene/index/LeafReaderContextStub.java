package org.apache.lucene.index;

public class LeafReaderContextStub {
    public static LeafReaderContext create(LeafReader reader) {
        return new LeafReaderContext(reader);
    }
}
