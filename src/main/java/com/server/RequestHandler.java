package com.browser.server;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RequestHandler implements Runnable {
    private final Socket socketClient;
    private static final String SERVER_NAME = "JavaLocalServer/2.0";
    private static final String HTML_TEMPLATE = 
        "<!DOCTYPE html>\n" +
        "<html lang=\"fr\">\n" +
        "<head>\n" +
        "    <meta charset=\"UTF-8\">\n" +
        "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
        "    <title>%s</title>\n" +
        "    <style>\n" +
        "        body { font-family: Arial, sans-serif; margin: 40px; background: #f5f5f5; }\n" +
        "        .container { max-width: 800px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }\n" +
        "        h1 { color: #333; border-bottom: 2px solid #4CAF50; padding-bottom: 10px; }\n" +
        "        .info { background: #e8f5e9; padding: 15px; border-radius: 5px; margin: 20px 0; }\n" +
        "        .error { background: #ffebee; color: #c62828; padding: 15px; border-radius: 5px; margin: 20px 0; }\n" +
        "        table { width: 100%; border-collapse: collapse; margin: 20px 0; }\n" +
        "        th, td { padding: 10px; text-align: left; border-bottom: 1px solid #ddd; }\n" +
        "        th { background: #f2f2f2; }\n" +
        "        .method { font-weight: bold; padding: 2px 8px; border-radius: 3px; }\n" +
        "        .get { background: #e3f2fd; color: #1565c0; }\n" +
        "        .post { background: #f3e5f5; color: #7b1fa2; }\n" +
        "    </style>\n" +
        "</head>\n" +
        "<body>\n" +
        "    <div class=\"container\">\n" +
        "        %s\n" +
        "        <div class=\"info\">\n" +
        "            <p><strong>Date :</strong> %s</p>\n" +
        "            <p><strong>Client :</strong> %s</p>\n" +
        "            <p><strong>Serveur :</strong> %s</p>\n" +
        "        </div>\n" +
        "        %s\n" +
        "    </div>\n" +
        "</body>\n" +
        "</html>";

    /**
     * Constructeur du gestionnaire de requêtes
     * @param socket Socket client à gérer
     */
    public RequestHandler(Socket socket) {
        this.socketClient = socket;
    }

    @Override
    public void run() {
        long debutTraitement = System.currentTimeMillis();
        
        try (BufferedReader entree = new BufferedReader(
                new InputStreamReader(socketClient.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter sortie = new PrintWriter(
                new OutputStreamWriter(socketClient.getOutputStream(), StandardCharsets.UTF_8), true)) {
            
            String ligneRequete = entree.readLine();
            if (ligneRequete == null || ligneRequete.isEmpty()) {
                envoyerReponseErreur(sortie, 400, "Requête vide");
                return;
            }

            String[] partiesRequete = ligneRequete.split(" ");
            if (partiesRequete.length < 3) {
                envoyerReponseErreur(sortie, 400, "Requête mal formée");
                return;
            }

            String methode = partiesRequete[0];
            String chemin = partiesRequete[1];
            String version = partiesRequete[2];

            Map<String, String> headers = lireHeaders(entree);

            System.out.printf("[SERVEUR] %s %s %s (Client: %s)%n", 
                methode, chemin, version, socketClient.getInetAddress().getHostAddress());

            traiterRequete(methode, chemin, headers, sortie);

            long tempsTraitement = System.currentTimeMillis() - debutTraitement;
            System.out.printf("[SERVEUR] Requête traitée en %d ms%n", tempsTraitement);

        } catch (IOException e) {
            System.err.println("[SERVEUR] Erreur de traitement : " + e.getMessage());
        } finally {
            fermerSocket();
        }
    }

    //Lit les headers de la requête
    private Map<String, String> lireHeaders(BufferedReader entree) throws IOException {
        Map<String, String> headers = new HashMap<>();
        String ligne;
        
        while ((ligne = entree.readLine()) != null && !ligne.isEmpty()) {
            int separatorIndex = ligne.indexOf(": ");
            if (separatorIndex > 0) {
                String cle = ligne.substring(0, separatorIndex);
                String valeur = ligne.substring(separatorIndex + 2);
                headers.put(cle, valeur);
            }
        }
        
        return headers;
    }

    //Traite la requête selon la méthode et le chemin
    private void traiterRequete(String methode, String chemin, Map<String, String> headers, PrintWriter sortie) {
        switch (methode.toUpperCase()) {
            case "GET":
                traiterGET(chemin, headers, sortie);
                break;
            case "POST":
                traiterPOST(chemin, headers, sortie);
                break;
            case "HEAD":
                traiterHEAD(chemin, sortie);
                break;
            default:
                envoyerReponseErreur(sortie, 405, "Méthode non supportée");
        }
    }

    //Traite une requête GET
    private void traiterGET(String chemin, Map<String, String> headers, PrintWriter sortie) {
        String titre;
        String contenu;
        
        switch (chemin) {
            case "/":
            case "/index.html":
                titre = "Serveur Local Java - Accueil";
                contenu = genererPageAccueil(headers);
                break;
            case "/info":
                titre = "Serveur Local Java - Informations";
                contenu = genererPageInfo(headers);
                break;
            case "/api/test":
                envoyerReponseJSON(sortie, 200, "{\"status\":\"success\",\"message\":\"API Test OK\"}");
                return;
            default:
                if (chemin.startsWith("/fichier/")) {
                    String nomFichier = chemin.substring(9);
                    envoyerReponseFichier(sortie, nomFichier);
                    return;
                }
                envoyerReponseErreur(sortie, 404, "Page non trouvée: " + chemin);
                return;
        }
        
        envoyerReponseHTML(sortie, 200, titre, contenu);
    }

    //Traite une requête POST
    private void traiterPOST(String chemin, Map<String, String> headers, PrintWriter sortie) {
        envoyerReponseHTML(sortie, 200, "POST Reçu", 
            "<h1>Requête POST Reçue</h1>" +
            "<p>Cette fonctionnalité POST est simulée.</p>" +
            "<p>Dans une version complète, vous pourriez traiter des formulaires ici.</p>");
    }

    //Traite une requête HEAD
    private void traiterHEAD(String chemin, PrintWriter sortie) {
        envoyerHeaders(sortie, 200, "text/html", 0);
    }

    //Génère la page d'accueil
    private String genererPageAccueil(Map<String, String> headers) {
        StringBuilder tableHeaders = new StringBuilder();
        if (!headers.isEmpty()) {
            tableHeaders.append("<h2>Headers de la requête</h2>");
            tableHeaders.append("<table>");
            tableHeaders.append("<tr><th>Header</th><th>Valeur</th></tr>");
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                tableHeaders.append("<tr><td>").append(entry.getKey())
                           .append("</td><td>").append(entry.getValue())
                           .append("</td></tr>");
            }
            tableHeaders.append("</table>");
        }
        
        return "<h1>Bienvenue sur le serveur local Java</h1>" +
               "<p>Ce serveur simule les réponses HTTP pour le navigateur Java.</p>" +
               "<p>Essayez ces pages :</p>" +
               "<ul>" +
               "<li><a href=\"/info\">Informations du serveur</a></li>" +
               "<li><a href=\"/api/test\">API Test (JSON)</a></li>" +
               "</ul>" +
               tableHeaders.toString();
    }


    private String genererPageInfo(Map<String, String> headers) {
        return "<h1>Informations du Serveur</h1>" +
               "<p><strong>Version :</strong> 2.0</p>" +
               "<p><strong>Date :</strong> " + new Date() + "</p>" +
               "<p><strong>Statut :</strong> En ligne</p>" +
               "<p><strong>Usage :</strong> Simulation de serveur web pour tests</p>";
    }

    private void envoyerReponseHTML(PrintWriter sortie, int code, String titre, String contenu) {
        String date = new Date().toString();
        String client = socketClient.getInetAddress().getHostAddress();
        String pageComplete = String.format(HTML_TEMPLATE, titre, 
            "<h1>" + titre + "</h1>" + contenu, date, client, SERVER_NAME, "");
        
        envoyerHeaders(sortie, code, "text/html; charset=utf-8", pageComplete.length());
        sortie.print(pageComplete);
    }

    //Envoie une réponse JSON
    private void envoyerReponseJSON(PrintWriter sortie, int code, String json) {
        envoyerHeaders(sortie, code, "application/json", json.length());
        sortie.print(json);
    }

    //Envoie une réponse de fichier
    private void envoyerReponseFichier(PrintWriter sortie, String nomFichier) {
        String contenu = "<h1>Contenu du fichier : " + nomFichier + "</h1>" +
                        "<p>Ceci est une simulation. Dans une version réelle, " +
                        "le fichier serait servi depuis le disque.</p>";
        
        envoyerReponseHTML(sortie, 200, "Fichier: " + nomFichier, contenu);
    }

    //Envoie une réponse d'erreur
    private void envoyerReponseErreur(PrintWriter sortie, int code, String message) {
        String titre = "Erreur " + code;
        String contenu = "<div class=\"error\">" +
                        "<h2>Erreur " + code + "</h2>" +
                        "<p>" + message + "</p>" +
                        "</div>" +
                        "<p><a href=\"/\">Retour à l'accueil</a></p>";
        
        envoyerReponseHTML(sortie, code, titre, contenu);
    }

    //Envoie les headers HTTP
    private void envoyerHeaders(PrintWriter sortie, int code, String contentType, int contentLength) {
        sortie.print("HTTP/1.1 " + code + " " + getMessageStatut(code) + "\r\n");
        sortie.print("Server: " + SERVER_NAME + "\r\n");
        sortie.print("Date: " + new Date() + "\r\n");
        sortie.print("Content-Type: " + contentType + "\r\n");
        if (contentLength > 0) {
            sortie.print("Content-Length: " + contentLength + "\r\n");
        }
        sortie.print("Connection: close\r\n");
        sortie.print("\r\n");
    }

    //Retourne le message de statut HTTP
    private String getMessageStatut(int code) {
        switch (code) {
            case 200: return "OK";
            case 201: return "Created";
            case 400: return "Bad Request";
            case 404: return "Not Found";
            case 405: return "Method Not Allowed";
            case 500: return "Internal Server Error";
            default: return "Unknown";
        }
    }

    //Ferme proprement le socket
    private void fermerSocket() {
        try {
            if (socketClient != null && !socketClient.isClosed()) {
                socketClient.close();
            }
        } catch (IOException e) {
            System.err.println("[SERVEUR] Erreur lors de la fermeture du socket : " + e.getMessage());
        }
    }
}