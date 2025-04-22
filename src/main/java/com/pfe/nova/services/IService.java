package com.pfe.nova.services;

import java.sql.SQLException;
import java.util.List;

public interface IService<T> {
    void ajouter(T t) throws SQLException;
    boolean modifier(T t) throws SQLException;
    void supprimer(int id) throws SQLException;
    List<T> recuperer() throws SQLException;

    // Déclaration de la méthode getAll
    List<T> getAll() throws SQLException;
}

