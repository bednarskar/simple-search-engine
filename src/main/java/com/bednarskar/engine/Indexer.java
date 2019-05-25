package com.bednarskar.engine;

import com.bednarskar.models.SearchableDocument;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Class for operations on inverted index.
 */
public class Indexer {
    // index is a map of token and assigned to them DocumentIds .
    private Map<String, List<String>> index;
    // documents is map where key is DocumentId and value is document with content and statistics used by search tool to rank results.
    private Map<String, SearchableDocument> documents;

    public Indexer(Map<String, SearchableDocument> documents){
        this.index = new HashMap<>();
        this.documents = documents;
    }

    /**
     * main method of indexer. Returns prepared reverted index sorted by ranks.
     * @return
     */
    public Map<String, List<String>> index(){
        IndexProvider indexProvider = new IndexProvider(this.index);
        this.index = indexProvider
                .populateIndex(this.documents)
                .getIndex();

        SearchableDocumentsProcessor documentsProcessor = SearchableDocumentsProcessor.getInstance();

        // calculate idf for each index token and tfidf  for each document to rank them in index
        index.entrySet().forEach(el -> {
            Double idf = Math.log(this.documents.size()/el.getValue().size());
            this.documents = documentsProcessor.postProcess(this.documents, idf, el);
        });

        this.index = indexProvider
                .rankIndexEntries(this.documents)
                .getIndex();

        return this.index;

    }

    /**
     * inner class to process index
     */
    class IndexProvider {

        private Map<String, List<String>> index;

        public IndexProvider(Map<String, List<String>> index){
            this.index = index;
        }

        /**
         * load documents to index initially.
         * @param searchableDocumentMap
         * @return
         */
        public IndexProvider populateIndex(Map<String, SearchableDocument> searchableDocumentMap){
            for(SearchableDocument d : searchableDocumentMap.values()) {

                for(String s : d.getTokens().keySet()) {
                    if (this.index.containsKey(s)) {
                        List<String> docIds = this.index.get(s);
                        docIds.add(d.getId());
                        this.index.put(s, docIds);
                    } else {
                        List<String> docIds = new ArrayList<>();
                        docIds.add(d.getId());
                        this.index.put(s, docIds);
                    }
                }
            }

            return this;
        }

        /**
         * sorting documents for each token by comparing TfIdf parameter.
         * @param documents
         * @return
         */
        public IndexProvider rankIndexEntries(Map<String, SearchableDocument> documents){
            this.index
                    .entrySet()
                    .forEach(el -> {
                            el.setValue(el
                                    .getValue()
                                    .stream()
                                    .sorted(new Comparator<String>() {
                                        @Override
                                        public int compare(String o1, String o2) {
                                            String token = el.getKey();
                                            return Double.compare(documents.get(o2).getTfIdf().get(token),
                                                                  documents.get(o1).getTfIdf().get(token));
                                        }
                                    }).collect(Collectors.toList()));
            });
            return this;
        }

        public  Map<String, List<String>> getIndex(){
            return this.index;
        }

    }

}
