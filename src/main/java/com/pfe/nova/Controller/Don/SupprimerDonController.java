package com.pfe.nova.Controller.Don;

import com.pfe.nova.models.Don;
import com.pfe.nova.services.DonService;

import java.sql.SQLException;

public class SupprimerDonController {

    public boolean supprimerDon(Don don) {
        DonService donService = new DonService();
        try {
            donService.supprimer(don.getId());
            return true;
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression du don: " + e.getMessage());
            return false;
        }
    }
}

