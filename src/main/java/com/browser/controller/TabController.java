package com.browser.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.web.WebView;
import com.browser.model.Page;
import com.browser.model.Tab;

public class TabController {

    // Variables de l'interface et du modèle
    private Tab onglet; 
    @FXML private WebView vuePage; 
    @FXML private Button boutonFermer; 

    @FXML
    public void afficherPage(Page page) { //
        // On récupère le contenu HTML de la page via le modèle pour l'afficher
        if (page != null && page.getContenuHTML() != null) {
            vuePage.getEngine().loadContent(page.getContenuHTML());
        }
    }

    @FXML
    public void fermerOnglet() { //
        // Logique pour fermer cet onglet et le retirer du BrowserController
        System.out.println("Fermeture de l'onglet en cours...");
    }
    
    // Setter pour lier le modèle à ce contrôleur
    public void setOnglet(Tab onglet) {
        this.onglet = onglet;
    }
}