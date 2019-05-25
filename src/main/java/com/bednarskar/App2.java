package com.bednarskar;

import com.bednarskar.engine.Indexer;
import com.bednarskar.engine.SearchableDocumentsProcessor;
import com.bednarskar.models.SearchableDocument;
import com.bednarskar.utils.DocumentsLoader;

import java.util.List;
import java.util.Map;
import java.util.Scanner;


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
    private static Scanner scanner = new Scanner(System.in);


    private static final String ENTER_QUERY = "Enter query you want to search or :q to quit.";
    private static final String NO_DOCUMENTS = "No documents. Do you want to retry and type path again(y/n)?";
    private static final String YES ="Y";
    private static final String ENTER_PATH = "Enter path to csv with documents to feed search tool.";
    private static final String NO_RESULTS = "No results found for given query.";
    private static final String QUIT = ":q";

    public static void main( String[] args ) {
        System.out.println("Simple search engine started!");
        getUserInputAndLoadData();
        

        SearchableDocumentsProcessor documentsProcessor = SearchableDocumentsProcessor.getInstance();
        documents = documentsProcessor.preProcess(documents);

        Indexer indexer = new Indexer(documents);
        index = indexer.index();

        queryTerm();
    }

    /**
     * interact with user, get user input and read data from given file.
     */
    private static void getUserInputAndLoadData() {
        System.out.println(ENTER_PATH);
        String path = scanner.nextLine();  // Read user input

        documents = DocumentsLoader.loadDocuments(path);

        while (documents.isEmpty()) {
            System.out.println(NO_DOCUMENTS);
            String decision = scanner.nextLine();

            if (decision.equalsIgnoreCase(YES)) {
                System.out.println(ENTER_PATH);
                path = scanner.nextLine();  // Read user input
                documents = DocumentsLoader.loadDocuments(path);
            } else {
                System.exit(0);
            }
        }
    }

    /**
     * query term and present results or exit
     */
    private static void queryTerm() {
        while (true) {
            System.out.println(ENTER_QUERY);
            String query = scanner.nextLine();  // Read user input

            if (query.equals(QUIT)) {
                System.exit(0);
            }

            List<String> results = index.get(query.toLowerCase());

            if (results!= null && !index.isEmpty()) {
                System.out.println("Found " + results.size() + " documents: ");

                for (String docId : results) {
                    System.out.println("Id: " + docId);
                    System.out.println(documents.get(docId).getContent() + "\n");
                }

            } else {
                System.out.println(NO_RESULTS);
            }
        }
    }
}
