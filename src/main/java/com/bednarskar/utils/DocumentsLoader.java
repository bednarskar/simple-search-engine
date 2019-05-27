package com.bednarskar.utils;

import com.bednarskar.models.SearchableDocument;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to populate Map where key is documentId and value is SearchableDocument with id and content.
 */
public class DocumentsLoader {

    private static final String COMMA_DELIMITER = ",";
    private static final String IO_EXCEPTION_MESSAGE = "Path incorrect. Please retry.";

    /**
     * loading data from file.
     * @param csvPath
     * @return
     */
    public static Map<String, SearchableDocument> loadDocuments(String csvPath) {

        Map<String, SearchableDocument> documents = new HashMap<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(csvPath));

            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(COMMA_DELIMITER);
                if (values.length == 2) {
                    documents.put(values[0], new SearchableDocument(values[0], values[1], null, null, null));
                }
            }
        } catch (IOException e) {
            System.out.println(IO_EXCEPTION_MESSAGE);
        }

        return documents;
    }
}
