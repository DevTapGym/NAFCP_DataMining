package com.example.source_code.Controller;
import com.example.source_code.Model.NAFCP;
import com.example.source_code.Model.Transaction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HelloController {

    @FXML
    private Button btnLoadData;

    @FXML
    private Button btnDataMining;

    @FXML
    private TextArea txtData;

    @FXML
    private TableView<Transaction> tableView;

    @FXML
    private TableColumn<Transaction, Integer> colId;

    @FXML
    private TableColumn<Transaction, String> colItems;

    @FXML
    private Slider slider;

    @FXML
    private PieChart pieChart;

    @FXML
    private Label labelMinSup;

    private List<List<String>> transactions = new ArrayList<>();
    private double minSup = 50.0;

    @FXML
    public void initialize() {
        labelMinSup.setText("Min-Supporst: "+minSup +" %");

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colItems.setCellValueFactory(new PropertyValueFactory<>("itemDescription"));

        colId.setPrefWidth(120);
        colItems.setPrefWidth(240);
        colId.setResizable(false);
        colItems.setResizable(false);
        colId.setReorderable(false);
        colItems.setReorderable(false);

        slider.setMin(10);
        slider.setMax(100);
        slider.setValue(50);
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        slider.setSnapToTicks(true);
        slider.setMajorTickUnit(10);
        slider.setMinorTickCount(0);
        slider.setBlockIncrement(10);

        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double rounded = Math.round(newVal.doubleValue() / 10) * 10;
            slider.setValue(rounded);
            minSup = rounded;
            labelMinSup.setText("Min-Supporst: "+minSup +" %");
        });
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
        else{
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setContentText("Tải dữ liệu lên thất bại. Vui lòng thực hiện lại");
            alert.showAndWait();
        }
    }

    @FXML
    private void dataMining(ActionEvent ignoredEvent) {
        if(transactions.isEmpty())
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText("Không thể khai thác tập phổ biến đóng");
            alert.setContentText("Vui lòng kiểm tra lại tập dữ liệu.");
            alert.showAndWait();
        }
        else{
            NAFCP nafcp = new NAFCP();
            nafcp.buildPPCTree(transactions, 0.5); // minSup = 50%
            nafcp.generateNLists();
            nafcp.findFCIs();
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

                String descriptions = parts[1].trim().replaceAll("^\"|\"$", ""); // Loại dấu ngoặc kép
                String[] items = descriptions.split(",");
                List<String> itemList = new ArrayList<>();
                for (String item : items) {
                    if (!item.trim().isEmpty()) {
                        itemList.add(item.trim());
                    }
                }

                transactions.add(itemList);

                // Thêm vào danh sách transactions
                transactions.add(itemList);
            }

            // In transactions ra console để kiểm tra
            System.out.println("Transactions:");
            for (List<String> transaction : transactions) {
                for (String item : transaction) {
                    System.out.print(item+ " - ");
                }
                System.out.println();
            }

            tableView.setItems(data);
            updatePieChart(transactions);

        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void updatePieChart(List<List<String>> transactions) {
        Map<String, Integer> itemCount = new HashMap<>();
        int totalItems = 0;

        for (List<String> transaction : transactions) {
            for (String item : transaction) {
                item = item.trim();
                itemCount.put(item, itemCount.getOrDefault(item, 0) + 1);
                totalItems++;
            }
        }

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entry : itemCount.entrySet()) {
            String item = entry.getKey();
            int count = entry.getValue();
            double percentage = (double) count / totalItems * 100;

            pieChartData.add(new PieChart.Data(item + " (" + String.format("%.1f", percentage) + "%)", count));
        }

        pieChart.setData(pieChartData);
        pieChart.setTitle("Tỷ lệ xuất hiện món hàng");
    }


}