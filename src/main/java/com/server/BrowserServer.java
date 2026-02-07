package com.browser.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BrowserServer extends Thread {
    private final int portConnexion;
    private volatile boolean estActif = true;
    private ServerSocket socketServeur;
    private final ExecutorService poolThreads;
    private static final int MAX_THREADS = 10;
    private static final int TIMEOUT_SHUTDOWN = 30; // secondes

    /**
     * Constructeur du serveur
     * @param port Port d'écoute
     */
    public BrowserServer(int port) {
        this.portConnexion = port;
        this.poolThreads = Executors.newFixedThreadPool(MAX_THREADS);
    }

    @Override
    public void run() {
        try {
            socketServeur = new ServerSocket(portConnexion);
            socketServeur.setReuseAddress(true);
            System.out.println("[SERVEUR] Serveur local actif sur le port " + portConnexion);
            System.out.println("[SERVEUR] Prêt à accepter les connexions...");

            while (estActif) {
                try {
                    Socket socketClient = socketServeur.accept();
                    socketClient.setSoTimeout(30000); // Timeout de 30 secondes
                    
                    System.out.println("[SERVEUR] Nouvelle connexion de " 
                        + socketClient.getInetAddress().getHostAddress());
                    
                    // Soumettre le traitement à un thread du pool
                    poolThreads.submit(new RequestHandler(socketClient));
                    
                } catch (IOException e) {
                    if (estActif) {
                        System.err.println("[SERVEUR] Erreur lors de l'acceptation : " + e.getMessage());
                    }
                }
            }
            
        } catch (IOException e) {
            System.err.println("[SERVEUR] Erreur d'initialisation : " + e.getMessage());
        } finally {
            arreterServeurProprement();
        }
    }

    //Arrête proprement le serveur
    public void arreterServeur() {
        System.out.println("[SERVEUR] Arrêt du serveur en cours...");
        this.estActif = false;
        
        if (socketServeur != null && !socketServeur.isClosed()) {
            try {
                socketServeur.close();
            } catch (IOException e) {
                System.err.println("[SERVEUR] Erreur lors de la fermeture du socket : " + e.getMessage());
            }
        }
        
        arreterPoolThreads();
    }

    //Arrête proprement le pool de threads
    private void arreterPoolThreads() {
        poolThreads.shutdown();
        try {
            if (!poolThreads.awaitTermination(TIMEOUT_SHUTDOWN, TimeUnit.SECONDS)) {
                poolThreads.shutdownNow();
                if (!poolThreads.awaitTermination(TIMEOUT_SHUTDOWN, TimeUnit.SECONDS)) {
                    System.err.println("[SERVEUR] Le pool de threads n'a pas pu s'arrêter correctement");
                }
            }
        } catch (InterruptedException e) {
            poolThreads.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    //Arrêt complet et propre
    private void arreterServeurProprement() {
        arreterPoolThreads();
        System.out.println("[SERVEUR] Serveur arrêté");
    }

    //Vérifie si le serveur est actif
    public boolean estActif() {
        return estActif;
    }

    //Récupère le port d'écoute
    public int getPort() {
        return portConnexion;
    }

    //Méthode main pour tester le serveur
    public static void main(String[] args) {
        BrowserServer serveur = new BrowserServer(8080);
        serveur.start();
        
        // Ajouter un hook pour arrêter proprement à la fermeture
        Runtime.getRuntime().addShutdownHook(new Thread(serveur::arreterServeur));
        
        try {
            serveur.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}