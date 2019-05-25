package com.bednarskar;

import com.bednarskar.engine.Indexer;
import com.bednarskar.engine.SearchableDocumentsProcessor;
import com.bednarskar.models.SearchableDocument;
import com.bednarskar.utils.DocumentsLoader;

import java.util.*;

/**
 * 1. wczytuje dokumenty z csv, docelowo z jakiejś lokalizacji.
 * 2. zamienia je na rdd, zapisuje wordcount dla każdego w SearchableDocument.
 * 3. generuje ze wszystkich index - w postaci mapy: token, Lista<Id>
 * 4. podanie danego tokenu spowoduje zwrócenie listy dokumentów z tym tokenem, sortowane obiekty w kolejności wordcount.
 *
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
        SearchableDocumentsProcessor documentsProcessor = SearchableDocumentsProcessor.getInstance();
        documents = documentsProcessor.preProcess(documents);

        Double numberOfAllDocuments = Double.valueOf(documents.size());
        Indexer indexer = new Indexer(documents);
        index = indexer.index();

        index.entrySet().forEach(e -> {
                    System.out.println("TOKENY Z QUICKINDEX:" + e.getKey() + "NAJBARDZIEJ PASUJĄCE:");
                    e.getValue().forEach(el -> System.out.println(el));
                }
        );

        System.out.println("Enter query you want to search.");
        String query = scanner.nextLine();  // Read user input

    }
}
