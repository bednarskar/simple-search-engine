package com.bednarskar.utils;

import com.bednarskar.models.SearchableDocument;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocumentsLoader {

    private static final String COMMA_DELIMITER = ",";

    public Map<String, SearchableDocument> loadDocuments(String csvPath) {
        Map<String, SearchableDocument> documents = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(COMMA_DELIMITER);
                if(values.length==2) {
                    documents.put(values[0], new SearchableDocument(values[0], values[1], null, null, null));
                }
            }
        } catch (IOException e) {
            //TODO logger
        }
        return documents;
    }
}
