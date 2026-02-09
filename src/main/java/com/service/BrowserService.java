package com.browser.service;

import com.browser.model.Page;
import com.browser.model.Tab;
import com.browser.network.ClientSocket; // Assurez-vous que ce fichier existe via Personne 1

/**
 * Service gérant la logique de navigation.
 */
public class BrowserService {

    private final HistoryService historyService = new HistoryService();
    private final CacheService cacheService = new CacheService();

    /**
     * Charge une page à partir d'une URL et met à jour l'onglet.
     * @param onglet L'onglet cible
     * @param url L'adresse web à charger
     */
    public void chargerPage(Tab onglet, String url) {
        try {
            // Vérification du cache en premier
            Page pageEnCache = cacheService.recupererCache(url);

            if (pageEnCache != null) {
                onglet.setPageCourante(pageEnCache);
            } else {
                // Simulation d'appel réseau via ClientSocket
                // Note : ClientSocket doit être implémenté par la Personne 1
                String contenu = ClientSocket.envoyerRequete(url);
                Page nouvellePage = new Page(url, "Titre de " + url, contenu);

                onglet.setPageCourante(nouvellePage);
                historyService.ajouterHistorique(nouvellePage);
                cacheService.mettreEnCache(nouvellePage);
            }
            System.out.println("Page chargée avec succès : " + url);
        } catch (Exception e) {
            System.err.println("Erreur de chargement pour l'URL " + url + " : " + e.getMessage());
        }
    }

    /**
     * Retourne la page courante d'un onglet.
     */
    public Page obtenirPage(Tab onglet) {
        return (onglet != null) ? onglet.getPageCourante() : null;
    }
}