package com.bednarskar.engine;

import com.bednarskar.models.SearchableDocument;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import java.util.Map;

public class Indexer {

    @Before
    public void setUp(){

        Map<String, SearchableDocument> documents;
        SearchableDocument document = Mockito.mock(SearchableDocument.class);
        when(document.getContent()).thenReturn("Some content.");
    }
    @Test
    public void testIndex(){

    }
}
