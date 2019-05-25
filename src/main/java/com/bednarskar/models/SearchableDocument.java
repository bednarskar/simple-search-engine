package com.bednarskar.models;

import java.util.HashMap;
import java.util.Map;

public class SearchableDocument {

    private String id;
    private String content;
    private Map<String, Double> tokens;
    private Map<String, Double> tf;
    private Map<String, Double> tfIdf;

    public SearchableDocument(String id, String content, Map<String, Double> tokens, Map<String, Double> tf, Map<String, Double> tfIdf) {
        this.id = id;
        this.content = content;
        this.tokens = tokens;
        this.tf = tf;
        this.tfIdf = tfIdf;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Map<String, Double> getTokens() {
        return tokens;
    }

    public void setTokens(Map<String, Double> tokens) {
        this.tokens = tokens;
    }

    public Map<String, Double> getTfIdf() {
        if (tfIdf != null){
            return tfIdf;
        } else return new HashMap<>();
    }

    public void setTfIdf(Map<String, Double> tfIdf) {
        this.tfIdf = tfIdf;
    }

    public Map<String, Double> getTf() {
        if (tf!=null) {
            return tf;
        } else {
            return new HashMap<>();
        }
    }

    public void setTf(Map<String, Double> tf) {
        this.tf = tf;
    }

}
