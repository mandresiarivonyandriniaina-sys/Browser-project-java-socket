package com.browser.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class BrowserServer extends Thread {
    private int portConnexion;
    private boolean estActif = true;

    public BrowserServer(int port) {
        this.portConnexion = port;
    }

    @Override
    public void run() {
        try (ServerSocket socketServeur = new ServerSocket(portConnexion)) {
            System.out.println("[SERVEUR] Serveur local actif sur le port " + portConnexion);
            while (estActif) {
                Socket socketClient = socketServeur.accept();
                // Chaque requête est gérée dans un nouveau thread (multithreading)
                new Thread(new RequestHandler(socketClient)).start();
            }
        } catch (IOException e) {
            if (estActif) System.err.println("[SERVEUR] Erreur : " + e.getMessage());
        }
    }

    public void arreterServeur() {
        this.estActif = false;
    }
}