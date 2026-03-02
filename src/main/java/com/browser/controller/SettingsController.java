package com.browser.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import com.browser.service.CacheService;

public class SettingsController {

    // Variables de l'interface
    @FXML private CheckBox activerCache;
    @FXML private TextField definirPageAccueil;
    @FXML private Button boutonSauvegarder;
    
    private CacheService cacheService;

    public SettingsController() {
        // Initialisation des services nécessaires
        this.cacheService = new CacheService();
    }

    @FXML
    public void sauvegarderParametres() { //
        // Exemple de logique de sauvegarde
        boolean cacheActif = activerCache.isSelected();
        String pageAccueil = definirPageAccueil.getText();
        
        System.out.println("Paramètres sauvegardés : Cache=" + cacheActif + ", Accueil=" + pageAccueil);
        
        // TODO: Appeler CacheService ou BrowserService pour appliquer ces paramètres
    }
}