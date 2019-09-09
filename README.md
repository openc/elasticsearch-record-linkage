ElasticSearch record linkage plugin
===================================

This is a small ElasticSearch plugin which exposes scoring methods
useful for record linkage. This makes it possible to perform record
linkage scoring directly in ElasticSearch. This has multiple benefits:

* Access the token statistics stored in the index, such as the frequency
  of each token. This is useful to compute TFIDF-based similarity metrics.

* Access the analyzers used in the index. Similarity metrics are often
  token-based, therefore implementing them externally would require duplicating
  the implementation of the analyzer chain.

* Speed gains (by scoring directly during the retrieval phase and avoiding
  transmitting candidates which are eventually filtered out over HTTP).

Building the plugin
-------------------

The plugin requires Java 8+ and Maven to be built, with `mvn package`.
This creates a zip archive at 
`target/releases/elasticsearch-record-linkage-${version}.zip`

Installing the plugin
---------------------

Assuming the plugin has been downloaded at `/tmp/elasticsearch-record-linkage-0.0.1-SNAPSHOT.zip`, 
it can be installed in ElasticSearch with
`bin/elasticsearch-plugin install file:///tmp/elasticsearch-record-linkage-0.0.1-SNAPSHOT.zip`

It can be uninstalled with `bin/elasticsearch-plugin remove recordLinkage`.

Using the plugin
----------------

The plugin declares a new scripting language `record_linkage_scorer`.
In this language, the source code of a script consists only of an identifier
for a scoring function, as follows:

```
{
    "script": {
        "lang": "record_linkage_scorer",
        "source": "tfidf",
        "params": {
                "query": "Greentech distribution Ltd",
                "field": "name",
                "analyzer": "company_names",
                "query_norm_exponent": 1.0,
        }
}
```

The `params` object holds the following information:
* A `query`, the query string to compare against the field value (mandatory);
* A `field` name, the name of the field to retrieve the other values to be compared against (mandatory). This field must have been indexed with term vectors enabled, including token positions. This can be done by adding the `"term_vector":
  "with_positions"` parameter to the field declaration in the index.
* An `analyzer`, the identifier of an ElasticSearch analyzer to use to tokenize the query (optional). If not provided, the default search analyzer will be used;
* Other optional parameters which depend on the similarity heuristic used (in this case `query_norm_exponent`).

Such a script can be used either in a scoring context or as a scripted field (to retrieve the similarity value in the search results).

Available similarity methods
----------------------------

* `tfidf`: A simple TFIDF-based similarity following Cohen et al.,
  "A Comparison of String Metrics for Matching Names and Records".
  
  Their implementation in the SecondString Java package
  normalizes the query and field vectors by Euclidian norm.
  This implementation makes it possible to disable either similarities
  by setting the corresponding norm exponents to 1.0 (instead of 0.0 by default).
  
  This makes it possible to encode a form of "confidence" in the similarity:
  two strings which match perfectly but only contain very common words
  (such as "Direct Services") get a lower score than strings containing
  rarer words (such as "Northumbria Breweries"). To use this, set `query_norm_exponent` to 1.0.
  You can alternatively set `doc_norm_exponent` to 1.0 if you want to preserve the norm
  of the document instead (or both, or other exponent values). By default these exponents are
  set to 0.0.

* `exact_tfidf`: A string similarity which returns a positive score
  only if the query and field match exactly and zero otherwise.
  For an exact match, the value is the TFIDF weight of the query.

* `levenshtein`: a simple Levenshtein distance.

Other similarity methods can be implemented easily using the
`StringSimilarity` interface.

