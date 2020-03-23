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

First, you need to index your documents in ElasticSearch, using an index profile
which enables term vectors on the fields that you want to use the similarities on.
For instance, the following index profile enables term vectors for the `name` and `address`
field, and defines a custom analyzer for the field `name`:
```json
{
    "settings": {
        "number_of_shards": 3,
        "analysis": {
            "analyzer": {
                "my_analyzer": {
                    "tokenizer": "standard",
                    "filter": [
                        "lowercase",
                        "asciifolding"
                    ]
                }
            }
        }
    },
    "mappings": {
        "properties": {
            "name": {
                "type": "text",
                "analyzer": "my_analyzer",
                "term_vector": "with_positions"
            },
            "address": {
                "type": "text",
                "term_vector": "with_positions"
            },
            "created": {
                "type": "date"
            }
        }
    }
}
```


The plugin declares a new scripting language `record_linkage_scorer` that lets
you compute string similarities in various contexts in your search queries.

The ElasticSearch manual has [some background about scripting in queries](https://www.elastic.co/guide/en/elasticsearch/reference/7.5/modules-scripting-using.html).
In this language, the source code of a script consists only of an identifier
for a scoring function, as follows:

```json
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

For instance, you can retrieve similarity scores, without influencing how search results are retrieved and ordered:

```json
{
  "query": {
    "term": {
      "name": "GreenTech distribution Ltd."
    }
  },
  "script_fields": {
    "name_tfidf": {
      "script": {
        "source": "tfidf",
        "lang": "record_linkage_scorer",
        "params": {
          "query": "GreenTech distribution Ltd.",
          "field": "name",
          "analyzer": "my_analyzer",
          "query_norm_exponent": 0
        }
      }
    },
    "name_tfidf_querynorm": {
      "script": {
        "source": "tfidf",
        "lang": "record_linkage_scorer",
        "params": {
          "query": "GreenTech distribution Ltd.",
          "field": "name",
          "analyzer": "my_analyzer",
          "query_norm_exponent": 1
        }
      }
    }
  }
}
```

This will add two extra fields to each search result, returning the value of the similarity heuristic on each document.
You can also use the similarity metrics to rescore the results. Since the computation of these similarity metrics is
more costly than ElasticSearch's default scoring, we recommend that you only rescore the n best results.

```json
{
  "query": {
    "term": {
      "name": "GreenTech distribution Ltd."
    }
  },
  "rescore": {
    "window_size": 30,
    "query": {
      "rescore_query": {
        "function_score": {
          "functions": [
            {
              "script_score": {
                "script": {
                  "source": "tfidf",
                  "lang": "record_linkage_scorer",
                  "params": {
                    "query": "38 Station Approach",
                    "field": "address",
                    "query_norm_exponent": 1
                  }
                }
              },
              "weight": 0.0955815
            },
            {
              "script_score": {
                "script": {
                  "source": "tfidf",
                  "lang": "record_linkage_scorer",
                  "params": {
                    "query": "GreenTech distribution Ltd.",
                    "field": "name",
                    "analyzer": "company_names",
                    "query_norm_exponent": 0
                  }
                }
              },
              "weight": 1.10106162
            }
          ],
          "score_mode": "sum",
          "boost_mode": "replace"
        }
      },
      "score_mode": "total",
      "query_weight": 0,
      "rescore_query_weight": 1
    }
  }
}
```

The query above will rescore the 30 best results using a linear combination of similarity heuristics on the `name` and `address` fields.


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

See also
--------

* [elasticsearch-entity-resolution](https://github.com/YannBrrd/elasticsearch-entity-resolution) also adds scoring metrics designed for record linkage to ElasticSearch. These metrics are taken from the [Duke](https://github.com/larsga/Duke) deduplication engine. The main difference is that these metrics cannot access the statistics collected in the search index, which makes it impossible to implement metrics based on TF-IDF for instance;
* [elasticsearch-learning-to-rank](https://github.com/o19s/elasticsearch-learning-to-rank) makes it easier to tune scripted weights via a Learning to Rank approach integrated in ElasticSearch itself. It is geared towards optimizing the ranking of search results and does not contain scoring metrics that are specifically geared towards record linkage.
