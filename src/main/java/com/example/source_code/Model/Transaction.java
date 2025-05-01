package com.example.source_code.Model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Transaction {
    private final SimpleIntegerProperty id;
    private final SimpleStringProperty itemDescription;

    public Transaction(int id, String itemDescription) {
        this.id = new SimpleIntegerProperty(id);
        this.itemDescription = new SimpleStringProperty(itemDescription);
    }

    public int getId() {
        return id.get();
    }

    public String getItemDescription() {
        return itemDescription.get();
    }
}

