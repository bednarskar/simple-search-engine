package com.bednarskar;

import com.bednarskar.engine.Indexer;
import com.bednarskar.engine.SearchableDocumentsProcessor;
import com.bednarskar.models.SearchableDocument;
import com.bednarskar.utils.DocumentsLoader;

import java.io.IOException;
import java.util.*;

/**
 * Simple in memory search engine based on TfIdf (frequency-inverse document frequency).
 * To feed search tool with data user is requested for path to CSV.
 * It should be csv delimited by , with stucture documentId,content
 * This search tool is case insensitive and can be queried by typing single terms.
 */
public class App2
{
    private static Map<String, SearchableDocument> documents;
    private static Map<String, List<String>> index;


    public static void main( String[] args ) {
        System.out.println("Simple search engine started!");
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter path to csv with documents to feed search tool.");
        String path = scanner.nextLine();  // Read user input

        DocumentsLoader documentsLoader = new DocumentsLoader();

            documents = documentsLoader.loadDocuments(path);
            while (documents.isEmpty()) {
                System.out.println("Please check if file exists and if path is correct. Do you want to retry (y/n)?");
                String decision = scanner.nextLine();
                if (decision.equalsIgnoreCase("Y")) {
                    System.out.println("Enter path to csv with documents to feed search tool.");
                    path = scanner.nextLine();  // Read user input
                    documents = documentsLoader.loadDocuments(path);

                } else {
                    System.exit(0);
                }
            }

        SearchableDocumentsProcessor documentsProcessor = SearchableDocumentsProcessor.getInstance();
        documents = documentsProcessor.preProcess(documents);

        Double numberOfAllDocuments = Double.valueOf(documents.size());
        Indexer indexer = new Indexer(documents);
        index = indexer.index();

        while (true) {
            System.out.println("Enter term you want to search.");
            String query = scanner.nextLine();  // Read user input
            List<String> results = index.get(query);
            System.out.println("Found " + results.size() + " documents: ");
            for (String docId : results) {
                System.out.println("Id: " + docId);
                System.out.println(documents.get(docId).getContent() + "\n");
            }
        }
    }
}
