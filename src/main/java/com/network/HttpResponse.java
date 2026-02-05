package com.browser.network;

import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private int codeStatut;
    private String messageStatut;
    private Map<String, String> entetes = new HashMap<>();
    private String corpsHTML = "";

    public void parserReponse(String reponseBrute) {
        if (reponseBrute == null || reponseBrute.isEmpty()) return;

        try {
            String[] parties = reponseBrute.split("\r\n\r\n", 2);
            String[] lignesEntete = parties[0].split("\r\n");

            // Parsing de la ligne de statut
            String[] premiereLigne = lignesEntete[0].split(" ", 3);
            if (premiereLigne.length >= 2) {
                this.codeStatut = Integer.parseInt(premiereLigne[1]);
                this.messageStatut = (premiereLigne.length == 3) ? premiereLigne[2] : "";
            }

            // Parsing des headers
            for (int i = 1; i < lignesEntete.length; i++) {
                String[] ligne = lignesEntete[i].split(": ", 2);
                if (ligne.length == 2) {
                    entetes.put(ligne[0], ligne[1]);
                }
            }

            // Récupération HTML
            if (parties.length > 1) {
                this.corpsHTML = parties[1];
            }
        } catch (Exception e) {
            System.err.println("Erreur de parsing de la réponse : " + e.getMessage());
        }
    }

    public String getCorpsHTML() { return corpsHTML; }
    public int getCodeStatut() { return codeStatut; }
}
