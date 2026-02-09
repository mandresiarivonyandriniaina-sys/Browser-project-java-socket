package com.browser.service;

import com.browser.model.Page;
import java.util.ArrayList;
import java.util.List;

/**
 * Service de gestion de l'historique.
 */
public class HistoryService {

    // Liste statique pour partager l'historique entre toutes les instances de service
    private static final List<Page> historiqueGlobal = new ArrayList<>();

    /**
     * Ajoute une page à l'historique en évitant les doublons successifs.
     */
    public void ajouterHistorique(Page page) {
        if (page != null) {
            // Logique simple pour éviter d'ajouter deux fois la même URL de suite
            if (historiqueGlobal.isEmpty() || !historiqueGlobal.get(historiqueGlobal.size() - 1).getUrl().equals(page.getUrl())) {
                historiqueGlobal.add(page);
            }
        }
    }

    /**
     * Retourne la liste des pages visitées.
     */
    public List<Page> getHistorique() {
        return new ArrayList<>(historiqueGlobal);
    }
}