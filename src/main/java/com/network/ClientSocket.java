package com.browser.network;

import java.io.*;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

public class ClientSocket {
    private Socket socketConnexion;
    private BufferedReader lecteurHTTP;
    private BufferedWriter ecrivainHTTP;
    private final String adresseServeur;
    private final int portConnexion;
    private static final int TIMEOUT_CONNEXION = 10000; // 10 secondes
    private static final int TIMEOUT_LECTURE = 15000; // 15 secondes

    /**
     * Constructeur pour créer un client socket
     * @param adresse Adresse du serveur
     * @param port Port de connexion
     */
    public ClientSocket(String adresse, int port) {
        this.adresseServeur = adresse;
        this.portConnexion = port;
    }

    /**
     * Établit la connexion avec le serveur
     * @throws IOException Si la connexion échoue
     */
    public void connecter() throws IOException {
        try {
            socketConnexion = new Socket();
            // Configuration des timeouts
            socketConnexion.setSoTimeout(TIMEOUT_LECTURE);
            
            // Tentative de connexion avec timeout
            socketConnexion.connect(new InetSocketAddress(adresseServeur, portConnexion), TIMEOUT_CONNEXION);
            
            // Initialisation des flux avec UTF-8
            lecteurHTTP = new BufferedReader(
                new InputStreamReader(socketConnexion.getInputStream(), StandardCharsets.UTF_8));
            ecrivainHTTP = new BufferedWriter(
                new OutputStreamWriter(socketConnexion.getOutputStream(), StandardCharsets.UTF_8));
            
            System.out.println("[CLIENT] Connexion établie avec " + adresseServeur + ":" + portConnexion);
            
        } catch (ConnectException e) {
            throw new IOException("Impossible de se connecter au serveur: " + adresseServeur + ":" + portConnexion, e);
        } catch (SocketTimeoutException e) {
            throw new IOException("Timeout de connexion dépassé (" + TIMEOUT_CONNEXION + "ms)", e);
        }
    }

    /**
     * Envoie une requête HTTP au serveur
     * @param requete La requête HTTP à envoyer
     * @throws IOException Si l'envoi échoue
     * @throws IllegalStateException Si non connecté
     */
    public void envoyerRequete(HttpRequest requete) throws IOException {
        if (socketConnexion == null || !socketConnexion.isConnected()) {
            throw new IllegalStateException("Le client n'est pas connecté au serveur");
        }
        
        if (ecrivainHTTP != null) {
            String requeteComplete = requete.genererRequete();
            ecrivainHTTP.write(requeteComplete);
            ecrivainHTTP.flush();
            System.out.println("[CLIENT] Requête envoyée : " + requete.getMethode() + " " + requete.getUrl());
        }
    }

    /**
     * Reçoit et parse la réponse HTTP du serveur
     * @return L'objet HttpResponse contenant la réponse
     * @throws IOException Si la lecture échoue
     */
    public HttpResponse recevoirReponse() throws IOException {
        if (lecteurHTTP == null) {
            throw new IllegalStateException("Aucun lecteur disponible pour lire la réponse");
        }

        StringBuilder reponseBrute = new StringBuilder();
        String ligne;

        if ((ligne = lecteurHTTP.readLine()) != null) {
            reponseBrute.append(ligne).append("\r\n");
        } else {
            throw new IOException("Le serveur a fermé la connexion sans réponse");
        }

        while ((ligne = lecteurHTTP.readLine()) != null && !ligne.isEmpty()) {
            reponseBrute.append(ligne).append("\r\n");
        }
        reponseBrute.append("\r\n");

        String contentLengthStr = getContentLengthFromHeaders(reponseBrute.toString());
        if (contentLengthStr != null) {
            try {
                int contentLength = Integer.parseInt(contentLengthStr);
                char[] buffer = new char[contentLength];
                int lus = lecteurHTTP.read(buffer, 0, contentLength);
                if (lus > 0) {
                    reponseBrute.append(new String(buffer, 0, lus));
                }
            } catch (NumberFormatException e) {
                lireCorpsFallback(lecteurHTTP, reponseBrute);
            }
        } else {
            lireCorpsFallback(lecteurHTTP, reponseBrute);
        }

        // Parser la réponse
        HttpResponse reponse = new HttpResponse();
        reponse.parserReponse(reponseBrute.toString());
        
        System.out.println("[CLIENT] Réponse reçue : " + reponse.getCodeStatut());
        return reponse;
    }

    //Méthode fallback pour lire le corps de la réponse
    private void lireCorpsFallback(BufferedReader lecteur, StringBuilder reponseBrute) throws IOException {
        char[] buffer = new char[1024];
        int lus;
        while (lecteur.ready() && (lus = lecteur.read(buffer)) != -1) {
            reponseBrute.append(buffer, 0, lus);
        }
    }

    //Extrait la valeur Content-Length des headers
    private String getContentLengthFromHeaders(String headers) {
        String[] lignes = headers.split("\r\n");
        for (String ligne : lignes) {
            if (ligne.toLowerCase().startsWith("content-length:")) {
                return ligne.substring(15).trim();
            }
        }
        return null;
    }

    //Ferme proprement la connexion
    public void fermerConnexion() {
        try {
            if (lecteurHTTP != null) lecteurHTTP.close();
            if (ecrivainHTTP != null) ecrivainHTTP.close();
            if (socketConnexion != null && !socketConnexion.isClosed()) {
                socketConnexion.close();
            }
            System.out.println("[CLIENT] Connexion fermée");
        } catch (IOException e) {
            System.err.println("[CLIENT] Erreur lors de la fermeture de la connexion : " + e.getMessage());
        }
    }

    /**
     * Vérifie si la connexion est active
     * @return true si connecté
     */
    public boolean estConnecte() {
        return socketConnexion != null && socketConnexion.isConnected() && !socketConnexion.isClosed();
    }
}