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

    class SingleSearchableDocumentProcessor{
        private SearchableDocument document;
        private int tokensQuantity;

        SingleSearchableDocumentProcessor(SearchableDocument document) {
            this.document = document;
        }


        public SingleSearchableDocumentProcessor calculateTokens() {
            SimpleTokenizer simpleTokenizer = SimpleTokenizer.INSTANCE;
            String tokens[] = simpleTokenizer.tokenize(document.getContent().toLowerCase());
            List<String> tokeny = Arrays.asList(tokens);
            this.tokensQuantity = tokens.length;
            JavaRDD<String> wordsInDocument = sparkContext.parallelize(tokeny,1);
            JavaPairRDD countData = wordsInDocument.mapToPair(t -> new Tuple2<>(t, 1.0)).reduceByKey((x, y) -> (Double) x + (Double) y);
            Map<String, Double> tokenNums = countData.collectAsMap();
            this.document.setTokens(tokenNums);
            return this;
        }

        public SingleSearchableDocumentProcessor calculateTermFrequency() {
            //TF(t) = (Number of times term t appears in a document) / (Total number of terms in the document).
            Map<String, Double> tf = document.getTokens().entrySet().stream().collect(Collectors.toMap(e -> e.getKey(),
                    e-> e.getValue() / tokensQuantity));
            this.document.setTf(tf);
            return this;
        }

        public SingleSearchableDocumentProcessor calculateTfIdf(Double idf, Map.Entry<String, List<String>> indexElement) {
            //tfidf
            if (indexElement.getValue().contains(document.getId())){
                Map<String, Double> tfIdfMap = document.getTfIdf();
                tfIdfMap.put(indexElement.getKey(), document.getTf().get(indexElement.getKey())*idf);
                this.document.setTfIdf(tfIdfMap);
            }
            return this;
        }

        public SearchableDocument getProcessedSearchableDocument(){
            return this.document;
        }
    }
}
