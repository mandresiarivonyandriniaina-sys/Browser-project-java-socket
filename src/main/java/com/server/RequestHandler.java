package com.browser.server;

import java.io.*;
import java.net.Socket;

public class RequestHandler implements Runnable {
    private Socket socketClient;

    public RequestHandler(Socket socket) {
        this.socketClient = socket;
    }

    @Override
    public void run() {
        try (BufferedReader entree = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));
             PrintWriter sortie = new PrintWriter(new OutputStreamWriter(socketClient.getOutputStream(), "UTF-8"), true)) {

            String ligneRequete = entree.readLine();
            if (ligneRequete != null) {
                System.out.println("[SERVEUR] Requête reçue : " + ligneRequete);

                // Réponse HTTP standard
                sortie.print("HTTP/1.1 200 OK\r\n");
                sortie.print("Content-Type: text/html; charset=utf-8\r\n");
                sortie.print("Server: JavaLocalServer\r\n");
                sortie.print("\r\n");
                sortie.print("<html><body><h1>Bienvenue sur le serveur local</h1><p>Ceci est une page simulée.</p></body></html>");
                sortie.flush();
            }
        } catch (IOException e) {
            System.err.println("[SERVEUR] Erreur de traitement : " + e.getMessage());
        } finally {
            try { socketClient.close(); } catch (IOException e) {}
        }
    }
}