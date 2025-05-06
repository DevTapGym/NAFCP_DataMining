package com.example.source_code.Controller;
import com.example.source_code.Model.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class HelloController {

    @FXML
    private Button btnLoadData;

    @FXML
    private TextFlow textFlow;

    @FXML
    private TableView<Transaction> tableView;

    @FXML
    private TableColumn<Transaction, Integer> colId;

    @FXML
    private TableColumn<Transaction, String> colItems;

    @FXML
    private Slider slider;

    @FXML
    private Slider slider1;

    @FXML
    private Slider slider2;

    @FXML
    private PieChart pieChart;

    @FXML
    private Label labelMinSup;

    @FXML
    private Label labelTransactionItem;

    @FXML
    private Label labelTransactionCount;

    @FXML
    private Label labelSupportCount;

    @FXML
    private Label labelMinConfidence;

    @FXML
    private Label labelLift;

    @FXML
    private Pane paneDraw;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private ScrollPane scrollPane1;

    private List<List<String>> transactions = new ArrayList<>();
    private double minSup = 50.0;
    private double confidence = 0.5;
    private double lift = 1.0;

    @FXML
    public void initialize() {
        labelMinSup.setText("Min-Supporst: "+minSup +" %");
        labelMinConfidence.setText("Min-Confidence: "+confidence);
        labelLift.setText("Lift: "+ lift);

        updateLabel();

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

        slider1.setMin(0.5);
        slider1.setMax(1);
        slider1.setValue(0.5);
        slider1.setShowTickMarks(true);
        slider1.setShowTickLabels(true);
        slider1.setSnapToTicks(true);
        slider1.setMajorTickUnit(0.1);
        slider1.setMinorTickCount(0);
        slider1.setBlockIncrement(0.1);

        slider2.setMin(1);
        slider2.setMax(2);
        slider2.setValue(1);
        slider2.setShowTickMarks(true);
        slider2.setShowTickLabels(true);
        slider2.setSnapToTicks(true);
        slider2.setMajorTickUnit(0.25);
        slider2.setMinorTickCount(0);
        slider2.setBlockIncrement(0.25);

        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double rounded = Math.round(newVal.doubleValue() / 10) * 10;
            slider.setValue(rounded);
            minSup = rounded;
            labelMinSup.setText("Min-Supporst: "+minSup +" %");
            updateLabel();
        });

        slider1.valueProperty().addListener((obs, oldVal, newVal) -> {
            slider1.setValue(newVal.doubleValue());
            confidence = newVal.doubleValue();
            labelMinConfidence.setText("Min-Confidence: "+confidence);
            updateLabel();
        });

        slider2.valueProperty().addListener((obs, oldVal, newVal) -> {
            slider2.setValue(newVal.doubleValue());
            lift = newVal.doubleValue();
            labelLift.setText("Lift: "+ lift);
            updateLabel();
        });

        Platform.runLater(() -> {
            scrollPane.setHvalue(0.49);
            scrollPane.setVvalue(0.0);
        });

        scrollPane1.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    }

    private void updateLabel() {
        int transactionItem = transactions.stream()
                .mapToInt(List::size)
                .sum();
        int transactionCount = transactions.size();
        int threshold = (int) Math.ceil((minSup/100) * transactionCount);

        labelTransactionItem.setText("Transaction item: "+ transactionItem);
        labelTransactionCount.setText("Transaction count: "+ transactionCount);
        labelSupportCount.setText("Supporst count: "+threshold);
    }

    @FXML
    private void handleLoadData(ActionEvent ignoredEvent) {
        deleteOldData();
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
            showErrorAlert("Lỗi dữ liệu", "Tải dữ liệu lên thất bại. Vui lòng thực hiện lại");
        }
    }

    @FXML
    private void dataMining(ActionEvent ignoredEvent) {
        if(transactions.isEmpty())
        {
            showErrorAlert("Lỗi dữ liệu","Bạn chưa tải dữ liệu lên. Vui lòng kiểm tra lại tập dữ liệu.");
        }
        else{
            deleteOldData();

            NAFCP nafcp = new NAFCP();
            nafcp.buildPPCTree(transactions, minSup / 100);
            List<String> frequentItems = nafcp.getFrequentItems();

            appendTextToFlow("Các tập thõa mãn ngưỡng min-sup: \n",true);
            for (String item : frequentItems) {
                appendTextToFlow(item+" ",false);
            }

            appendTextToFlow("\nKhởi tạo PPC-tree: \n",true);
            drawTree(nafcp.getRoot(),paneDraw,2000, 50, 240);

            appendTextToFlow("Khởi tạo N-List: \n",true);
            nafcp.generateNLists();
            for (Map.Entry<String, List<PPCode>> entry : nafcp.getnListMap().entrySet()) {
                appendTextToFlow(entry.getKey() + ": " + entry.getValue()+"\n",false);
            }

            appendTextToFlow("Các tập phổ biến đóng sau khi khai thác: \n",true);
            nafcp.findFCIs();

            for (Set<String> itemset : nafcp.getFrequentClosedItemsets()) {
                appendTextToFlow(itemset + " #SUP: " + nafcp.calculateSupport(nafcp.getNList(itemset))+"\n",false);
            }
            appendTextToFlow("Các tập phổ biến được tạo từ tập phổ biến đóng: \n",true);
            List<Set<String>> frequentClosedItemsets = nafcp.getFrequentClosedItemsets();

            Map<Set<String>, Integer> supportMap = new HashMap<>();
            for (Set<String> itemset : frequentClosedItemsets) {
                List<PPCode> nList = nafcp.getNList(itemset);
                int support = nafcp.calculateSupport(nList);
                supportMap.put(itemset, support);
            }

            AssociationRuleMiner miner = new AssociationRuleMiner(supportMap, transactions.size() , confidence, lift); // minConfidence = 50%
            List<Rule> rules = miner.generateRules();
            Map<Set<String>, Integer> frequentItemsets = miner.getFrequentItemsets();
            for (Map.Entry<Set<String>, Integer> entry : frequentItemsets.entrySet()) {
                appendTextToFlow("[" + String.join(", ", entry.getKey()) + "] #SUP: " + entry.getValue()+"\n",false);
            }

            appendTextToFlow("Các luật kết hợp được khai thác những tập phổ biến: \n",true);
            for (Rule rule : rules) {
                appendTextToFlow(rule.toString()+"\n",false);
            }
        }
    }

    @FXML
    private void handleDelete(ActionEvent ignoredEvent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận");
        alert.setHeaderText("Bạn có chắc chắn muốn xóa toàn bộ dữ liệu");
        alert.setContentText("Tất cả dữ liệu sẽ bị xóa.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                minSup = 50.0;
                slider.setValue(50);
                slider1.setValue(0.5);
                slider2.setValue(1);
                deleteOldData();
                tableView.getItems().clear();
                transactions = new ArrayList<>();
                updatePieChart(transactions);
                updateLabel();
            }
        });

    }

    public void deleteOldData(){
        textFlow.getChildren().clear();
        paneDraw.getChildren().clear();
    }

    public void appendTextToFlow(String content, boolean isBold) {
        Text text = new Text(content);
        text.setFont(Font.font("System", isBold ? FontWeight.BOLD : FontWeight.NORMAL, 14));
        textFlow.getChildren().add(text);
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
                    // Kiểm tra tiêu đề cột
                    String[] header = line.split(",", 2);
                    if (header.length != 2 || !"TransactionID".equals(header[0].trim()) || !"ItemDescription".equals(header[1].trim())) {
                        showErrorAlert("Lỗi định dạng", "Tiêu đề file không đúng định dạng. Phải có 2 cột: 'TransactionID' và 'ItemDescription'.");
                        return;
                    }
                    continue; // Bỏ qua dòng tiêu đề
                }


                String[] parts = line.split(",", 2);
                if (parts.length != 2) {
                    showErrorAlert("Lỗi định dạng", "File dữ liệu không đúng định dạng. Mỗi dòng phải có 2 cột.");
                    return;
                }

                int id = Integer.parseInt(parts[0].trim());
                String description = parts[1].trim();

                data.add(new Transaction(id, description));

                String descriptions = parts[1].trim().replaceAll("^\"|\"$", "");
                String[] items = descriptions.split(",");
                List<String> itemList = new ArrayList<>();
                for (String item : items) {
                    if (!item.trim().isEmpty()) {
                        itemList.add(item.trim());
                    }
                }
                transactions.add(itemList);
            }


            tableView.setItems(data);
            updatePieChart(transactions);
            for (List<String> items : transactions) {
                for (String item : items) {
                    System.out.print(item + " ");
                }
                System.out.println();
            }
            updateLabel();

        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
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

            pieChartData.add(new PieChart.Data(item + " ("+count+" - " + String.format("%.1f", percentage) + "%)", count));
        }

        pieChart.setData(pieChartData);
        pieChart.setTitle("Tỷ lệ xuất hiện món hàng");
    }

    public Group createVisualNode(PPCNode node, double x, double y) {
        Circle circle = new Circle(x, y, 22);
        circle.setFill(Color.LIGHTBLUE);
        circle.setStroke(Color.DARKBLUE);
        circle.setStrokeWidth(2);

        // Text item bên trong circle
        Text itemText = new Text(node.item);
        itemText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        itemText.setX(x - itemText.getLayoutBounds().getWidth() / 2);
        itemText.setY(y + 5);

        Group group = new Group(circle, itemText);
        Tooltip tooltip = new Tooltip("pre: " + node.preOrder +
                "\npost: " + node.postOrder+
                "\nfreq: " + node.frequency );
        tooltip.setShowDelay(Duration.millis(100));
        tooltip.setHideDelay(Duration.millis(200));
        tooltip.setShowDuration(Duration.seconds(10));
        Tooltip.install(group, tooltip); // gán vào cả group
        return group;
    }


    public void drawTree(PPCNode node, Pane pane, double x, double y, double xOffset) {
        Group visualNode = createVisualNode(node, x, y);
        pane.getChildren().add(visualNode);

        double childX = x - (xOffset * (node.children.size() - 1)) / 2;
        for (PPCNode child : node.children) {
            double childY = y + 100;

            // vẽ đường từ cha đến con
            Line line = new Line(x, y + 22, childX, childY - 22);
            pane.getChildren().add(line);

            drawTree(child, pane, childX, childY, xOffset / 1.5);
            childX += xOffset;
        }
    }
}