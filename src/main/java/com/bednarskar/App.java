package com.bednarskar;

import com.bednarskar.engine.SearchableDocumentsProcessor;
import com.bednarskar.models.SearchableDocument;
import com.bednarskar.utils.DocumentsLoader;
import opennlp.tools.tokenize.SimpleTokenizer;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 1. wczytuje dokumenty z csv, docelowo z jakiejś lokalizacji.
 * 2. zamienia je na rdd, zapisuje wordcount dla każdego w SearchableDocument.
 * 3. generuje ze wszystkich index - w postaci mapy: token, Lista<Id>
 * 4. podanie danego tokenu spowoduje zwrócenie listy dokumentów z tym tokenem, sortowane obiekty w kolejności wordcount.
 *
 */
public class App 
{
    private static Map<String, SearchableDocument> documents;

    public static void main( String[] args ) {
        System.out.println("Simple search engine started!");
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter path to csv with documents to feed search tool.");
        String path = scanner.nextLine();  // Read user input

        DocumentsLoader documentsLoader = new DocumentsLoader();
        documents = documentsLoader.loadDocuments(path);
//        SearchableDocumentsProcessor documentsProcessor = new SearchableDocumentsProcessor();
//        documents = documentsProcessor.preProcess(documents);
//REFACTORED
        SparkConf sparkConf = new SparkConf().setMaster("local").setAppName("Tokenizer");
        JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);

        for(SearchableDocument d : documents.values()) {
            SimpleTokenizer simpleTokenizer = SimpleTokenizer.INSTANCE;
            String tokens[] = simpleTokenizer.tokenize(d.getContent().toLowerCase());
            List<String> tokeny = Arrays.asList(tokens);
            JavaRDD<String> wordsInDocument = sparkContext.parallelize(tokeny,1);
            JavaPairRDD countData = wordsInDocument.mapToPair(t -> new Tuple2<>(t, 1.0)).reduceByKey((x, y) -> (Double) x + (Double) y);
            Map<String, Double> tokenNums = countData.collectAsMap();
            d.setTokens(tokenNums);
            //TF(t) = (Number of times term t appears in a document) / (Total number of terms in the document).
            Map<String, Double> tf = tokenNums.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(),
                    e-> e.getValue() / tokens.length));
            d.setTf(tf);
        }
//
        Double numberOfAllDocuments = Double.valueOf(documents.size());

        Map<String, List<String>> quickIndex = new HashMap<>();

        Map<String, List<SearchableDocument>> index = new HashMap<>();
        for(SearchableDocument d: documents.values()) {
            System.out.println(d.getId() + "  TAKIE TOKENY W DOKUMENCIE:" + d.getTokens());
            for(String s : d.getTokens().keySet()) {
                if (index.containsKey(s)) {
                    List<SearchableDocument> docs = index.get(s);
                    docs.add(d);
                    index.put(s, docs);

                    List<String> docIds = quickIndex.get(s);
                    docIds.add(d.getId());
                    quickIndex.put(s, docIds);
                } else {
                    List<SearchableDocument> docs = new ArrayList<>();
                    docs.add(d);
                    index.put(s, docs);

                    List<String> docIds = new ArrayList<>();
                    docIds.add(d.getId());
                    quickIndex.put(s, docIds);
                }
            }
        }
      //TODO dla każdego elementu z indexu przeliczyć idf i tfidf
        //idf to ilość dokumentów/ilość dokumentów z tym słowem. a więc map.put(s, ilośc dokumentów/sizelisty)
        //idf każdorazowo pobierane i przypisywane na nowo.
        //tfidf to map.put(s, tf(s) * idf(s)
        index.entrySet().forEach(el -> {
            Double idf = Math.log(numberOfAllDocuments/el.getValue().size());
            el.getValue().stream().forEach(e -> {
                Map<String, Double> tfidfMap = e.getTfIdf();
                tfidfMap.put(el.getKey(), e.getTf().get(el.getKey()) * idf);
                e.setTfIdf(tfidfMap);
            });
        });
//WERSJA Z QUICKINDEX
        quickIndex.entrySet().forEach(el -> {
            Double idf = Math.log(numberOfAllDocuments/el.getValue().size());

            documents.values().stream().forEach(doc ->
            {
                if (el.getValue().contains(doc.getId())){
                    Map<String, Double> tfIdfMap = doc.getTfIdf();
                    tfIdfMap.put(el.getKey(), doc.getTf().get(el.getKey())*idf);
                    doc.setTfIdf(tfIdfMap);
                }
            });
        });
//        quickIndex.entrySet().forEach(el -> {
//            el.setValue(el.getValue().stream().sorted(new Comparator<String>() {
//                @Override
//                public int compare(String o1, String o2) {
//                    return Double.compare(documents.values().stream()
//                            .filter(e -> e.getId().equals(o2)).collect(Collectors.toList()).get(0).getTfIdf().get(el.getKey()),
//                            documents.values().stream()
//                                    .filter(e -> e.getId().equals(o1)).collect(Collectors.toList()).get(0).getTfIdf().get(el.getKey()));
//                }
//            }).collect(Collectors.toList()));
//        });

        quickIndex.entrySet().forEach(el -> {
            el.setValue(el.getValue().stream().sorted(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return Double.compare(documents.get(o2).getTfIdf().get(el.getKey()),
                            documents.get(o1).getTfIdf().get(el.getKey()));
                }
            }).collect(Collectors.toList()));
        });
//
        index.entrySet().forEach(el -> {
            el.setValue(el.getValue().stream().sorted(new Comparator<SearchableDocument>() {
                @Override
                public int compare(SearchableDocument o1, SearchableDocument o2) {
                        return Double.compare(o2.getTfIdf().get(el.getKey()), o1.getTfIdf().get(el.getKey()));
                }
            }).collect(Collectors.toList()));
        });


        index.entrySet().forEach(e -> {
            System.out.println("TOKEN:" + e.getKey() + "NAJBARDZIEJ PASUJĄCE:");
            e.getValue().forEach(el -> System.out.println(el.getId() + " " + el.getTfIdf()) );
        });

        quickIndex.entrySet().forEach(e -> {
                    System.out.println("TOKENY Z QUICKINDEX:" + e.getKey() + "NAJBARDZIEJ PASUJĄCE:");

                    e.getValue().forEach(el -> System.out.println(el));
                }
                );

        System.out.println("Enter query you want to search.");
        String query = scanner.nextLine();  // Read user input

    }
}
