package com.browser.service;

import com.browser.model.Page;
import java.util.HashMap;
import java.util.Map;

/**
 * Service gérant le cache temporaire des pages web.
 */
public class CacheService {

    // Stockage en mémoire : URL -> Objet Page
    private static final Map<String, Page> cachePages = new HashMap<>();

    /**
     * Stocke une page dans le cache.
     */
    public void mettreEnCache(Page page) {
        if (page != null && page.getUrl() != null) {
            cachePages.put(page.getUrl(), page);
        }
    }

    /**
     * Récupère une page depuis le cache via son URL.
     */
    public Page recupererCache(String url) {
        return cachePages.get(url);
    }

    /**
     * Vide le cache (utile pour les paramètres).
     */
    public void viderCache() {
        cachePages.clear();
    }
}