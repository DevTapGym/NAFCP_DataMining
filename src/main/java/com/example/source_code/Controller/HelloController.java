package com.example.source_code.Controller;
import com.example.source_code.Model.Transaction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HelloController {

    @FXML
    private Button btnLoadData;

    @FXML
    private TableView<Transaction> tableView;

    @FXML
    private TableColumn<Transaction, Integer> colId;

    @FXML
    private TableColumn<Transaction, String> colItems;

    private List<List<String>> transactions;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colItems.setCellValueFactory(new PropertyValueFactory<>("itemDescription"));

        colId.setPrefWidth(130);
        colId.setResizable(false);
        colItems.setPrefWidth(240);
        colItems.setResizable(false);
        //tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @FXML
    private void handleLoadData(ActionEvent ignoredEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn file CSV");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv")
        );

        File file = fileChooser.showOpenDialog(btnLoadData.getScene().getWindow());
        if (file != null) {
            loadCSVData(file);
        }
    }

    private void loadCSVData(File file) {
        ObservableList<Transaction> data = FXCollections.observableArrayList();
        transactions = new ArrayList<>(); // reset danh sách

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // Bỏ qua dòng tiêu đề
                }

                String[] parts = line.split(",", 2);
                if (parts.length < 2) continue;

                int id = Integer.parseInt(parts[0].trim());
                String description = parts[1].trim();

                // Thêm vào bảng TableView
                data.add(new Transaction(id, description));

                // Tách description thành danh sách item (List<String>)
                String[] items = description.split(" ");
                List<String> itemList = new ArrayList<>();
                for (String item : items) {
                    if (!item.trim().isEmpty()) {
                        itemList.add(item.trim());
                    }
                }

                // Thêm vào danh sách transactions
                transactions.add(itemList);
            }

            // In transactions ra console để kiểm tra
            System.out.println("Transactions:");
            for (List<String> transaction : transactions) {
                System.out.println(transaction);
            }

            tableView.setItems(data);

        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }


}