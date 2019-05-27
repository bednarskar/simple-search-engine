package com.bednarskar.engine;

import com.bednarskar.models.SearchableDocument;
import org.apache.commons.math3.util.Precision;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class SearchableDocumentProcessorTest {

    private SearchableDocumentsProcessor processor;
    private Map<String, SearchableDocument> documents;
    private Map<String, SearchableDocument> documentsForPostProcessingTest;

    @Before
    public void setUp(){

        this.documents = new HashMap<>();
        SearchableDocument document = new SearchableDocument("id1", "a a b c", null, null, null);

        SearchableDocument document2 = new SearchableDocument("id2", "a d e", null, null, null);

        SearchableDocument document3 = new SearchableDocument("id3", "f g c", null, null, null);

        documents.put("id1", document);
        documents.put("id2", document2);
        documents.put("id3", document3);
        this.processor = SearchableDocumentsProcessor.getInstance();

        this.documentsForPostProcessingTest = new HashMap<>();
        Map<String, Double> tokens11 = new HashMap<>();
        tokens11.put("a", 2.0);
        tokens11.put("b", 1.0);
        tokens11.put("c", 1.0);
        Map<String, Double> tf11 = new HashMap<>();
        tf11.put("a", 2.0/4.0);
        tf11.put("b", 1.0/4.0);
        tf11.put("c", 1.0/4.0);
        SearchableDocument document11 = new SearchableDocument("id11", "a a b c", tokens11, tf11, null);

        Map<String, Double> tokens22 = new HashMap<>();
        tokens22.put("a", 1.0);
        tokens22.put("d", 1.0);
        tokens22.put("e", 1.0);
        Map<String, Double> tf22 = new HashMap<>();
        tf22.put("a", 1.0/3);
        tf22.put("d", 1.0/3);
        tf22.put("e", 1.0/3);
        SearchableDocument document22 = new SearchableDocument("id22", "a d e", tokens22, tf22, null);

        Map<String, Double> tokens33 = new HashMap<>();
        tokens33.put("f", 1.0);
        tokens33.put("g", 1.0);
        tokens33.put("c", 1.0);
        Map<String, Double> tf33 = new HashMap<>();
        tf33.put("f", 1.0/3);
        tf33.put("g", 1.0/3);
        tf33.put("c", 1.0/3);
        SearchableDocument document33 = new SearchableDocument("id33", "f g c", tokens33, tf33, null);

        documentsForPostProcessingTest.put("id11", document11);
        documentsForPostProcessingTest.put("id22", document22);
        documentsForPostProcessingTest.put("id33", document33);

    }

    @Test
    public void shouldCalculateTokensAndTermFrequency() {
        Map<String, SearchableDocument> preProcessedDocuments = this.processor.preProcess(documents);
        SearchableDocument id1 = preProcessedDocuments.get("id1");

        Assert.assertTrue(id1.getTokens().get("a") == 2);
        Assert.assertTrue(id1.getTokens().get("b") == 1);
        Assert.assertTrue(id1.getTokens().get("c") == 1);
        Assert.assertTrue(id1.getTokens().size() == 3);
        Assert.assertTrue(id1.getTf().get("a") == 0.5);
        Assert.assertTrue(id1.getTf().get("b") == 0.25);
        Assert.assertTrue(id1.getTf().get("c") == 0.25);

        SearchableDocument id2 = preProcessedDocuments.get("id2");
        Assert.assertTrue(id2.getTokens().get("a") == 1);
        Assert.assertTrue(id2.getTokens().get("d") == 1);
        Assert.assertTrue(id2.getTokens().get("e") == 1);
        Assert.assertTrue(id2.getTokens().size() == 3);
        Assert.assertTrue(Precision.round(id2.getTf().get("a"), 2) == 0.33);
        Assert.assertTrue(Precision.round(id2.getTf().get("d"), 2) == 0.33);
        Assert.assertTrue(Precision.round(id2.getTf().get("e"), 2) == 0.33);

        SearchableDocument id3 = preProcessedDocuments.get("id3");
        Assert.assertTrue(id3.getTokens().get("f") == 1);
        Assert.assertTrue(id3.getTokens().get("g") == 1);
        Assert.assertTrue(id3.getTokens().get("c") == 1);
        Assert.assertTrue(id3.getTokens().size() == 3);
        Assert.assertTrue(Precision.round(id3.getTf().get("f"), 2) == 0.33);
        Assert.assertTrue(Precision.round(id3.getTf().get("g"), 2) == 0.33);
        Assert.assertTrue(Precision.round(id3.getTf().get("c"), 2) == 0.33);

    }

    @Test
    public void shouldCalculateTfIdf(){

        TreeMap<String, List<String>> indexElement =new TreeMap<>();
        indexElement.put("a", Arrays.asList("id11", "id22"));
        Map.Entry<String, List<String>> indexElement1 = indexElement.firstEntry();
        // idf for term = log_e(number of documents / number of documents with term)
        Double idf = Math.log(documentsForPostProcessingTest.size() / 2.0);

        Map<String, SearchableDocument> postProcesed = processor.postProcess(documentsForPostProcessingTest, idf, indexElement1);
        Assert.assertTrue(postProcesed.get("id11").getTfIdf().get("a") == documents.get("id11").getTf().get("a") * idf);
    }

}
