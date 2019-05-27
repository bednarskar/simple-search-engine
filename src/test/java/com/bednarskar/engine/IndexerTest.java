package com.bednarskar.engine;

import com.bednarskar.models.SearchableDocument;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;

public class IndexerTest {

    private Indexer indexer;

    @Before
    public void setUp(){

        Map<String, SearchableDocument> documents = new HashMap<>();
        SearchableDocument document = Mockito.mock(SearchableDocument.class);
        when(document.getContent()).thenReturn("a a b c");
        when(document.getId()).thenReturn("id1");
        Map<String, Double> tfMap1 = new HashMap<>();
        tfMap1.put("a", 0.5);
        tfMap1.put("a", 0.5);
        tfMap1.put("b", 0.25);
        tfMap1.put("c", 0.25);
        when(document.getTf()).thenReturn(tfMap1);
        Map<String, Double> tokensMap1 = new HashMap<>();
        tokensMap1.put("a", 2.0);
        tokensMap1.put("b", 1.0);
        tokensMap1.put("c", 1.0);
        when(document.getTokens()).thenReturn(tokensMap1);
        Map<String, Double> tfidfMap1 = new HashMap<>();
        tfidfMap1.put("a", 0.5 * (Math.log(3/2)));
        tfidfMap1.put("b", 0.33 * (Math.log(3/1)));
        tfidfMap1.put("c", 0.33 * (Math.log(3/1)));
        when(document.getTfIdf()).thenReturn(tfidfMap1);

        SearchableDocument document2 = Mockito.mock(SearchableDocument.class);
        when(document2.getContent()).thenReturn("a d e");
        when(document2.getId()).thenReturn("id2");
        Map<String, Double> tfMap2 = new HashMap<>();
        tfMap2.put("a", 0.33);
        tfMap2.put("d", 0.33);
        tfMap2.put("e", 0.33);
        when(document2.getTf()).thenReturn(tfMap2);
        Map<String, Double> tokensMap2 = new HashMap<>();
        tokensMap2.put("a", 1.0);
        tokensMap2.put("d", 1.0);
        tokensMap2.put("e", 1.0);
        when(document2.getTokens()).thenReturn(tokensMap2);
        Map<String, Double> tfidfMap2 = new HashMap<>();
        tfidfMap2.put("a", 0.33 * (Math.log(3/3)));
        tfidfMap2.put("d", 0.33 * (Math.log(3/1)));
        tfidfMap2.put("e", 0.33 * (Math.log(3/1)));
        when(document2.getTfIdf()).thenReturn(tfidfMap2);

        SearchableDocument document3 = Mockito.mock(SearchableDocument.class);
        when(document3.getContent()).thenReturn("f g c");
        when(document3.getId()).thenReturn("id3");
        Map<String, Double> tfMap3 = new HashMap<>();
        tfMap3.put("f", 0.33);
        tfMap3.put("g", 0.33);
        tfMap3.put("c", 0.33);
        when(document3.getTf()).thenReturn(tfMap3);
        Map<String, Double> tokensMap3 = new HashMap<>();
        tokensMap3.put("f", 1.0);
        tokensMap3.put("g", 1.0);
        tokensMap3.put("c", 1.0);
        when(document3.getTokens()).thenReturn(tokensMap3);
        Map<String, Double> tfidfMap3 = new HashMap<>();
        tfidfMap3.put("f", 0.33 * (Math.log(3/1)));
        tfidfMap3.put("g", 0.33 * (Math.log(3/1)));
        tfidfMap3.put("c", 0.33 * (Math.log(3/2)));
        when(document3.getTfIdf()).thenReturn(tfidfMap3);

        documents.put("id1", document);
        documents.put("id2", document2);
        documents.put("id3", document3);
        this.indexer = new Indexer(documents);

    }
    @Test
    public void shouldContainCorrectTokensInIndex(){
        Map<String, List<String>>  result = this.indexer.index();
        Assert.assertTrue(result.size() == 7);
        Assert.assertTrue(result.containsKey("a"));
        Assert.assertTrue(result.containsKey("b"));
        Assert.assertTrue(result.containsKey("c"));
        Assert.assertTrue(result.containsKey("d"));
        Assert.assertTrue(result.containsKey("e"));
        Assert.assertTrue(result.containsKey("f"));
        Assert.assertTrue(result.containsKey("g"));

    }
    @Test
    public void shouldContainCorrectDocumentsInIndex(){
        Map<String, List<String>>  result = this.indexer.index();
        Assert.assertTrue(result.size() == 7);
        Assert.assertTrue(result.get("a").size() == 2);
        Assert.assertTrue(result.get("b").contains("id1"));
        Assert.assertTrue(result.get("c").contains("id1") && result.get("c").contains("id3") && result.get("c").size() == 2);
        Assert.assertTrue(result.get("d").contains("id2") && result.get("d").size() == 1);
        Assert.assertTrue(result.get("e").contains("id2") && result.get("e").size() == 1);
        Assert.assertTrue(result.get("f").contains("id3") && result.get("e").size() == 1);
        Assert.assertTrue(result.get("g").contains("id3") && result.get("g").size() == 1);

    }

    @Test
    public void shouldContainDocumentsInCorrectOrderInIndex(){
        Map<String, List<String>>  result = this.indexer.index();
        Assert.assertTrue(result.get("a").size() == 2);
        Assert.assertTrue(result.get("a").get(0).contains("id1"));
        Assert.assertTrue(result.get("a").get(1).contains("id2"));
    }
}
