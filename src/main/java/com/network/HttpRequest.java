package com.browser.network;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

public class HttpRequest {
    private final String methode;
    private final String url;
    private final Map<String, String> entetes;
    private String corps;
    private static final String VERSION_HTTP = "HTTP/1.1";
    private static final String USER_AGENT = "JavaBrowser/2.0";
    private static final String ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
    private static final String ACCEPT_LANGUAGE = "fr,en;q=0.9,en-US;q=0.8";
    private static final String ACCEPT_ENCODING = "gzip, deflate, br";
    private static final String CONNECTION = "keep-alive";

    /**
     * Constructeur pour créer une requête HTTP
     * @param methode Méthode HTTP (GET, POST, etc.)
     * @param url URL de la requête
     */
    public HttpRequest(String methode, String url) {
        this.methode = methode.toUpperCase();
        this.url = url;
        this.entetes = new HashMap<>();
        this.corps = "";
        
        // Entêtes par défaut
        initialiserEntetesParDefaut();
    }

    /**
     * Initialise les en-têtes HTTP par défaut
     */
    private void initialiserEntetesParDefaut() {
        this.entetes.put("User-Agent", USER_AGENT);
        this.entetes.put("Accept", ACCEPT);
        this.entetes.put("Accept-Language", ACCEPT_LANGUAGE);
        this.entetes.put("Accept-Encoding", ACCEPT_ENCODING);
        this.entetes.put("Connection", CONNECTION);
        this.entetes.put("Cache-Control", "no-cache");
        this.entetes.put("Pragma", "no-cache");
    }

    /**
     * Ajoute ou modifie un en-tête HTTP
     * @param cle Nom de l'en-tête
     * @param valeur Valeur de l'en-tête
     */
    public void ajouterEntete(String cle, String valeur) {
        if (cle != null && !cle.trim().isEmpty() && valeur != null) {
            this.entetes.put(cle.trim(), valeur.trim());
        }
    }

    /**
     * Définit le corps de la requête
     * @param corps Contenu du corps
     */
    public void setCorps(String corps) {
        if (corps != null) {
            this.corps = corps;
            this.entetes.put("Content-Length", String.valueOf(corps.getBytes().length));
            this.entetes.put("Content-Type", "application/x-www-form-urlencoded");
        }
    }

    /**
     * Génère la requête HTTP complète sous forme de chaîne
     * @return La requête HTTP formatée
     */
    public String genererRequete() {
        StringBuilder sb = new StringBuilder();
        
        // Ligne de requête
        sb.append(methode).append(" ").append(url).append(" ").append(VERSION_HTTP).append("\r\n");
        
        // En-têtes
        Iterator<Map.Entry<String, String>> it = entetes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entree = it.next();
            sb.append(entree.getKey()).append(": ").append(entree.getValue()).append("\r\n");
        }
        
        // Ligne vide séparatrice
        sb.append("\r\n");
        
        // Corps (si présent)
        if (corps != null && !corps.isEmpty()) {
            sb.append(corps);
        }
        
        return sb.toString();
    }

    /**
     * Génère une requête GET standard
     * @param url URL à récupérer
     * @return Requête HTTP GET
     */
    public static HttpRequest creerRequeteGET(String url) {
        HttpRequest requete = new HttpRequest("GET", url);
        requete.ajouterEntete("Host", extraireHost(url));
        return requete;
    }

    /**
     * Génère une requête POST standard
     * @param url URL de destination
     * @param donnees Données à envoyer
     * @return Requête HTTP POST
     */
    public static HttpRequest creerRequetePOST(String url, String donnees) {
        HttpRequest requete = new HttpRequest("POST", url);
        requete.ajouterEntete("Host", extraireHost(url));
        requete.setCorps(donnees);
        return requete;
    }

    /**
     * Extrait le host d'une URL
     */
    private static String extraireHost(String url) {
        try {
            if (url.startsWith("http://")) {
                url = url.substring(7);
            } else if (url.startsWith("https://")) {
                url = url.substring(8);
            }
            
            int slashIndex = url.indexOf('/');
            if (slashIndex > 0) {
                return url.substring(0, slashIndex);
            }
            return url;
        } catch (Exception e) {
            return "localhost";
        }
    }

    // Getters
    public String getMethode() { return methode; }
    public String getUrl() { return url; }
    public Map<String, String> getEntetes() { return new HashMap<>(entetes); }
    public String getCorps() { return corps; }
}