package com.browser.network;

import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private String methode;
    private String url;
    private Map<String, String> entetes;
    private String corps;

    public HttpRequest(String methode, String url) {
        this.methode = methode;
        this.url = url;
        this.entetes = new HashMap<>();
        this.corps = "";
        // Entêtes
        this.entetes.put("User-Agent", "JavaBrowser/1.0");
        this.entetes.put("Accept", "text/html");
        this.entetes.put("Connection", "close");
    }

    public void ajouterEntete(String cle, String valeur) {
        this.entetes.put(cle, valeur);
    }

    public String genererRequete() {
        StringBuilder sb = new StringBuilder();
        sb.append(methode).append(" ").append(url).append(" HTTP/1.1\r\n");
        for (Map.Entry<String, String> entree : entetes.entrySet()) {
            sb.append(entree.getKey()).append(": ").append(entree.getValue()).append("\r\n");
        }
        sb.append("\r\n");
        sb.append(corps);
        return sb.toString();
    }
}