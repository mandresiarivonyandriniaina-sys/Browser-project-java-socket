package com.browser.network;

import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private int codeStatut;
    private String messageStatut;
    private final Map<String, String> entetes;
    private String corpsHTML;
    private long tempsReponse;

    /**
     * Constructeur par défaut
     */
    public HttpResponse() {
        this.entetes = new HashMap<>();
        this.corpsHTML = "";
        this.codeStatut = 0;
        this.messageStatut = "";
        this.tempsReponse = 0;
    }

    /**
     * Parse la réponse HTTP brute
     * @param reponseBrute La réponse HTTP complète
     */
    public void parserReponse(String reponseBrute) {
        if (reponseBrute == null || reponseBrute.isEmpty()) {
            this.codeStatut = 500;
            this.messageStatut = "Empty Response";
            return;
        }

        long debutParsing = System.currentTimeMillis();
        
        try {
            
            String[] parties = reponseBrute.split("\r\n\r\n", 2);
            String headersPart = parties[0];
            
            String[] lignes = headersPart.split("\r\n");
            
            if (lignes.length > 0) {
                parserLigneStatut(lignes[0]);
            }
            
            parserHeaders(lignes);
            
            if (parties.length > 1) {
                this.corpsHTML = parties[1];
            }
            
            detecterRedirection();
            
        } catch (Exception e) {
            System.err.println("[HTTP] Erreur de parsing de la réponse : " + e.getMessage());
            this.codeStatut = 502; // Bad Gateway
            this.messageStatut = "Parsing Error";
            this.corpsHTML = "<html><body><h1>Erreur de parsing</h1><p>" + e.getMessage() + "</p></body></html>";
        }
        
        this.tempsReponse = System.currentTimeMillis() - debutParsing;
    }

    //Parse la ligne de statut HTTP
    private void parserLigneStatut(String ligneStatut) {
        String[] parties = ligneStatut.split(" ", 3);
        if (parties.length >= 2) {
            try {
                this.codeStatut = Integer.parseInt(parties[1]);
                this.messageStatut = (parties.length == 3) ? parties[2] : "";
            } catch (NumberFormatException e) {
                this.codeStatut = 500;
                this.messageStatut = "Invalid Status Code";
            }
        }
    }

    //Parse les headers HTTP
    private void parserHeaders(String[] lignes) {
        for (int i = 1; i < lignes.length; i++) {
            String ligne = lignes[i];
            int separatorIndex = ligne.indexOf(": ");
            if (separatorIndex > 0) {
                String cle = ligne.substring(0, separatorIndex).trim();
                String valeur = ligne.substring(separatorIndex + 2).trim();
                entetes.put(cle, valeur);
            }
        }
    }

    //Détecte les redirections HTTP
    private void detecterRedirection() {
        if (codeStatut >= 300 && codeStatut < 400) {
            String location = entetes.get("Location");
            if (location != null && !location.isEmpty()) {
                corpsHTML = "<html><body>"
                          + "<h1>Redirection " + codeStatut + "</h1>"
                          + "<p>Redirection vers : <a href=\"" + location + "\">" + location + "</a></p>"
                          + "</body></html>";
            }
        }
    }

    /**
     * Vérifie si la réponse est un succès
     * @return true si code 2xx
     */
    public boolean estSucces() {
        return codeStatut >= 200 && codeStatut < 300;
    }

    /**
     * Vérifie si c'est une redirection
     * @return true si code 3xx
     */
    public boolean estRedirection() {
        return codeStatut >= 300 && codeStatut < 400;
    }

    /**
     * Vérifie si c'est une erreur client
     * @return true si code 4xx
     */
    public boolean estErreurClient() {
        return codeStatut >= 400 && codeStatut < 500;
    }

    /**
     * Vérifie si c'est une erreur serveur
     * @return true si code 5xx
     */
    public boolean estErreurServeur() {
        return codeStatut >= 500 && codeStatut < 600;
    }

    /**
     * Récupère l'URL de redirection
     * @return URL ou null
     */
    public String getUrlRedirection() {
        return entetes.get("Location");
    }

    /**
     * Récupère le type de contenu
     * @return Content-Type ou null
     */
    public String getTypeContenu() {
        return entetes.get("Content-Type");
    }

    /**
     * Récupère la longueur du contenu
     * @return Content-Length ou -1
     */
    public int getLongueurContenu() {
        try {
            String lengthStr = entetes.get("Content-Length");
            return lengthStr != null ? Integer.parseInt(lengthStr) : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // Getters
    public int getCodeStatut() { return codeStatut; }
    public String getMessageStatut() { return messageStatut; }
    public Map<String, String> getEntetes() { return new HashMap<>(entetes); }
    public String getCorpsHTML() { return corpsHTML; }
    public long getTempsReponse() { return tempsReponse; }
    
    /**
     * Représentation textuelle de la réponse
     */
    @Override
    public String toString() {
        return "HTTP " + codeStatut + " " + messageStatut 
             + " (" + getLongueurContenu() + " bytes, " + tempsReponse + "ms)";
    }
}