package com.browser.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import com.browser.model.Tab;
import com.browser.service.BrowserService;

public class BrowserController {

    // Déclaration des variables de l'interface
    @FXML private BorderPane racineFenetre;
    @FXML private TextField champAdresse;
    @FXML private Button boutonAller;
    @FXML private TabPane onglets;
    
    // Service pour gérer la logique de navigation
    private BrowserService browserService;
    private Tab ongletActif; //

    public BrowserController() {
        this.browserService = new BrowserService();
    }

    @FXML
    public void initialiserInterface() { //
        // Code exécuté au démarrage pour configurer la vue
        ouvrirNouvelOnglet();
    }

    @FXML
    public void ouvrirNouvelOnglet() { //
        // Logique pour créer et ajouter un nouvel onglet au TabPane
        System.out.println("Création d'un nouvel onglet...");
        // TODO: Charger tab.fxml et l'ajouter à 'onglets'
    }

    @FXML
    public void chargerPageDepuisChamp() { //
        String url = champAdresse.getText();
        if (url != null && !url.isEmpty() && ongletActif != null) {
            // Utilisation du service pour charger la page sans toucher au réseau ici
            browserService.chargerPage(ongletActif, url); 
        }
    }
}