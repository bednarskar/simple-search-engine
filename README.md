# simple-search-engine
simple search engine with inverted index

This is simple search engine based on inverted index and TfIdf (http://www.tfidf.com/)
As input to load data user is requested for path to csv file with structure like below 
(no headers, just id and content, each record separated with new line):

documentId,documentContent                                         
documentId2,documentContent
...


After documents load application is building inverted index (Map <String, List<String>) like that:
token -> list of documentIds with this token ordered by TfIdf 

index is storying only documentIds because it is more effective than storying whole documents with content
(as one document can be assigned to many tokens). 
documents are stored in another map (String documebtId -> SearchableDocument document) and when user is searching for term
SimpleSearchEngine is checking what document ids are connected with it and it's returning content of this documents 
from map with complete documents objects.

This SimpleSearchEngine is case insensitive. It can search for single tokens. query given by user is trimmed.

To build use maven, than fat-jar simple-search-engine-1.0-SNAPSHOT-jar-with-dependencies.jar will be created.
You can run it simply by java -jar simple-search-engine-1.0-SNAPSHOT-jar-with-dependencies.jar
:q is closing application.
