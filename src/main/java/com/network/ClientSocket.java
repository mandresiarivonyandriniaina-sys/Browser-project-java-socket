package com.browser.network;

import java.io.*;
import java.net.Socket;
import java.net.InetSocketAddress;

public class ClientSocket {
    private Socket socketConnexion;
    private BufferedReader lecteurHTTP;
    private BufferedWriter ecrivainHTTP;
    private String adresseServeur;
    private int portConnexion;

    public ClientSocket(String adresse, int port) {
        this.adresseServeur = adresse;
        this.portConnexion = port;
    }

    public void connecter() throws IOException {
        socketConnexion = new Socket();
        // Timeout de 5 secondes pour éviter le blocage
        socketConnexion.connect(new InetSocketAddress(adresseServeur, portConnexion), 5000);

        lecteurHTTP = new BufferedReader(new InputStreamReader(socketConnexion.getInputStream(), "UTF-8"));
        ecrivainHTTP = new BufferedWriter(new OutputStreamWriter(socketConnexion.getOutputStream(), "UTF-8"));
    }

    public void envoyerRequete(HttpRequest requete) throws IOException {
        if (ecrivainHTTP != null) {
            ecrivainHTTP.write(requete.genererRequete());
            ecrivainHTTP.flush();
        }
    }

    public HttpResponse recevoirReponse() throws IOException {
        StringBuilder reponseBrute = new StringBuilder();
        String ligne;

        // Lecture des headers
        while ((ligne = lecteurHTTP.readLine()) != null && !ligne.isEmpty()) {
            reponseBrute.append(ligne).append("\r\n");
        }
        reponseBrute.append("\r\n");

        // Lecture du corps si disponible
        while (lecteurHTTP.ready() && (ligne = lecteurHTTP.readLine()) != null) {
            reponseBrute.append(ligne);
        }

        HttpResponse reponse = new HttpResponse();
        reponse.parserReponse(reponseBrute.toString());
        return reponse;
    }

    public void fermerConnexion() {
        try {
            if (lecteurHTTP != null) lecteurHTTP.close();
            if (ecrivainHTTP != null) ecrivainHTTP.close();
            if (socketConnexion != null) socketConnexion.close();
        } catch (IOException e) {
            System.err.println("Erreur fermeture socket : " + e.getMessage());
        }
    }
}