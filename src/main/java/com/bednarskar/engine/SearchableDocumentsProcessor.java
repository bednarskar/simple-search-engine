package com.bednarskar.engine;

import com.bednarskar.models.SearchableDocument;
import opennlp.tools.tokenize.SimpleTokenizer;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class is preparing documents for indexing.
 */
public class SearchableDocumentsProcessor {

    private static SparkConf sparkConf;
    private static JavaSparkContext sparkContext;
    private static SearchableDocumentsProcessor searchableDocumentsProcessor = new SearchableDocumentsProcessor();

    public static SearchableDocumentsProcessor getInstance() {
        return searchableDocumentsProcessor;
    }
    private SearchableDocumentsProcessor() {
        sparkConf = new SparkConf().setMaster("local").setAppName("Tokenizer");
        sparkContext = new JavaSparkContext(sparkConf);
    }

    /**
     * Pre processing documents - invokes pipeline to count tokens for documents, calculates Term Frequency.
     * @param documents
     * @return
     */
    public Map<String, SearchableDocument> preProcess(Map<String, SearchableDocument> documents){
        Map<String, SearchableDocument> processed = new HashMap<>();
        for(String key : documents.keySet()) {

            SingleSearchableDocumentProcessor singleSearchableDocumentProcessor = new SingleSearchableDocumentProcessor(documents.get(key));

            processed.put(key, singleSearchableDocumentProcessor
                    .calculateTokens()
                    .calculateTermFrequency()
                    .getProcessedSearchableDocument());
        }
        return processed;
    }

    /**
     * postProcess - used when documents are added to index, but not yet ranked - to calculate TfIdf for them and make possible further ranking.
     * @param documents
     * @param idf
     * @param indexElement
     * @return
     */
    public Map<String, SearchableDocument> postProcess(Map<String, SearchableDocument> documents, Double idf, Map.Entry<String, List<String>> indexElement) {
        Map<String, SearchableDocument> processed = new HashMap<>();
        for(String key : documents.keySet()) {
            SingleSearchableDocumentProcessor singleSearchableDocumentProcessor = new SingleSearchableDocumentProcessor(documents.get(key));
            processed.put(key, singleSearchableDocumentProcessor
                    .calculateTfIdf(idf, indexElement)
                    .getProcessedSearchableDocument());
        }
        return processed;
    }

    /**
     * inner class with methods used in pipeline to calculate parameters for each SearchableDocument.
     */
    class SingleSearchableDocumentProcessor{
        private SearchableDocument document;
        private int tokensQuantity;

        SingleSearchableDocumentProcessor(SearchableDocument document) {
            this.document = document;
        }


        /**
         * Method generates tokens for document using SimpleTokenizer and than use Spark to generate word count.
         * @return SingleSearchableDocumentProcessor
         */
        public SingleSearchableDocumentProcessor calculateTokens() {
            SimpleTokenizer simpleTokenizer = SimpleTokenizer.INSTANCE;
            List<String> tokens = Arrays.asList(simpleTokenizer.tokenize(document.getContent().toLowerCase()));
            this.tokensQuantity = tokens.size();
            JavaRDD<String> wordsInDocument = sparkContext.parallelize(tokens,1);
            JavaPairRDD countData = wordsInDocument
                                            .mapToPair(t -> new Tuple2<>(t, 1.0))
                                                                .reduceByKey((x, y) -> (Double) x + (Double) y);
            Map<String, Double> tokenQuantityMap = countData.collectAsMap();
            this.document.setTokens(tokenQuantityMap);
            return this;
        }

        /**
         * Tf is a term frequency parameter.
         * TF(t) = (Number of times term t appears in a document) / (Total number of terms in the document).
         * Method calculates Tf parameter for each term found in it and populates map of tokens and their frequency in document.
         * @return
         */
        public SingleSearchableDocumentProcessor calculateTermFrequency() {
            //TF(t) = (Number of times term t appears in a document) / (Total number of terms in the document).
            Map<String, Double> tf = document
                    .getTokens()
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            e -> e.getKey(),
                            e -> e.getValue() / tokensQuantity));
            this.document.setTf(tf);
            return this;
        }

        /**
         * TfIdf = tf * idf
         * Calculates TfIdf parameter based on given idf.
         * @param idf
         * @param indexElement
         * @return
         */
        public SingleSearchableDocumentProcessor calculateTfIdf(Double idf, Map.Entry<String, List<String>> indexElement) {
            //tfidf
            //check if to this token in index is assigned given document
            if (indexElement.getValue().contains(document.getId())) {
                Map<String, Double> tfIdfMap = document.getTfIdf();
                // add to existing TfIdf statistics of this document statistics about TfIdf calculated for given index token.
                tfIdfMap.put(indexElement.getKey(), document.getTf().get(indexElement.getKey()) * idf);
                this.document.setTfIdf(tfIdfMap);
            }
            return this;
        }

        public SearchableDocument getProcessedSearchableDocument(){
            return this.document;
        }
    }
}
