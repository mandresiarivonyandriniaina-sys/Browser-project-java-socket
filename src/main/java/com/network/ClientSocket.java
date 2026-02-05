package com.browser.network;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

 // Classe ClientSocket - Gère la connexion au serveur et l'envoi/réception des requêtes HTTP
 
public class ClientSocket {
    

    private Socket socketConnexion;
    private BufferedReader lecteurHTTP;
    private BufferedWriter ecrivainHTTP;
    private String adresseServeur;
    private int portConnexion;
    private boolean estConnecte;
    
    // Constantes
    private static final int TIMEOUT_CONNEXION = 10000; // 10 secondes
    private static final int TIMEOUT_LECTURE = 30000;   // 30 secondes
    
    /**
     * Constructeur de ClientSocket
     * @param adresseServeur Adresse IP ou nom d'hôte du serveur
     * @param portConnexion Port de connexion
     */
    public ClientSocket(String adresseServeur, int portConnexion) {
        this.adresseServeur = adresseServeur;
        this.portConnexion = portConnexion;
        this.estConnecte = false;
    }
    
    /**
     * Établir la connexion TCP avec le serveur
     * @throws IOException Si la connexion échoue
     */
    public void connecter() throws IOException {
        try {
            // Créer le socket avec timeout
            socketConnexion = new Socket(adresseServeur, portConnexion);
            socketConnexion.setSoTimeout(TIMEOUT_LECTURE);
            
            // Initialiser les flux de lecture/écriture
            lecteurHTTP = new BufferedReader(
                new InputStreamReader(socketConnexion.getInputStream())
            );
            ecrivainHTTP = new BufferedWriter(
                new OutputStreamWriter(socketConnexion.getOutputStream())
            );
            
            estConnecte = true;
            System.out.println("Connexion établie avec " + adresseServeur + ":" + portConnexion);
            
        } catch (UnknownHostException e) {
            throw new IOException("Serveur inconnu : " + adresseServeur, e);
        } catch (SocketTimeoutException e) {
            throw new IOException("Timeout de connexion au serveur", e);
        } catch (IOException e) {
            throw new IOException("Erreur lors de la connexion : " + e.getMessage(), e);
        }
    }
    
    /**
     * Envoyer une requête HTTP au serveur
     * @param requete La requête HTTP à envoyer
     * @throws IOException Si l'envoi échoue ou si non connecté
     */
    public void envoyerRequete(HttpRequest requete) throws IOException {
        if (!estConnecte) {
            throw new IOException("Non connecté au serveur");
        }
        
        try {
            String requeteTexte = requete.genererRequete();
            ecrivainHTTP.write(requeteTexte);
            ecrivainHTTP.flush();
            System.out.println("Requête envoyée : " + requete.getMethode() + " " + requete.getUrl());
            
        } catch (IOException e) {
            estConnecte = false;
            throw new IOException("Erreur lors de l'envoi de la requête : " + e.getMessage(), e);
        }
    }
    
    /**
     * Recevoir et parser la réponse du serveur
     * @return HttpResponse contenant la réponse parsée
     * @throws IOException Si la réception échoue ou si non connecté
     */
    public HttpResponse recevoirReponse() throws IOException {
        if (!estConnecte) {
            throw new IOException("Non connecté au serveur");
        }
        
        try {
            // Lire la réponse complète
            StringBuilder reponseBrute = new StringBuilder();
            String ligne;
            
            while ((ligne = lecteurHTTP.readLine()) != null) {
                if (ligne.isEmpty()) {
                    break;
                }
                reponseBrute.append(ligne).append("\r\n");
            }
            
            reponseBrute.append("\r\n");
            
            HttpResponse reponse = new HttpResponse();
            reponse.parserReponse(reponseBrute.toString());
            
            System.out.println("Réponse reçue : " + reponse.getCodeStatut() + " " + reponse.getMessageStatut());
            return reponse;
            
        } catch (SocketTimeoutException e) {
            throw new IOException("Timeout lors de la lecture de la réponse", e);
        } catch (IOException e) {
            estConnecte = false;
            throw new IOException("Erreur lors de la réception de la réponse : " + e.getMessage(), e);
        }
    }
    
    // Fermer la connexion
    public void fermerConnexion() {
        try {
            if (ecrivainHTTP != null) ecrivainHTTP.close();
            if (lecteurHTTP != null) lecteurHTTP.close();
            if (socketConnexion != null && !socketConnexion.isClosed()) {
                socketConnexion.close();
            }
            estConnecte = false;
            System.out.println("Connexion fermée");
            
        } catch (IOException e) {
            System.err.println("Erreur lors de la fermeture de la connexion : " + e.getMessage());
        }
    }
    
    public boolean estConnecte() {
        return estConnecte;
    }
    
    public String getAdresseServeur() {
        return adresseServeur;
    }
    
    public int getPortConnexion() {
        return portConnexion;
    }
}