package oficina.ui;

import oficina.IntegradorOCaml;
import oficina.Cart;
import oficina.CartItem;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class FXMain extends Application {
    private static final double BTN_WIDTH  = 220;
    private static final double BTN_HEIGHT = 100;

    @Override
    public void start(Stage stage) {
        // Root pane with gradient background
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-pane");

        // Header: title + car image
        Label title = new Label("🛠 Oficina do Ganâncio");
        title.setId("title-label");
        ImageView carImage = new ImageView(new Image(
            getClass().getResourceAsStream("car.png")
        ));
        carImage.setFitWidth(300);
        carImage.setPreserveRatio(true);
        VBox header = new VBox(10, title, carImage);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(20));
        root.setTop(header);

        // Menu buttons
        TilePane menu = new TilePane();
        menu.setHgap(20);
        menu.setVgap(20);
        menu.setPrefColumns(3);
        menu.setAlignment(Pos.CENTER);
        menu.setPadding(new Insets(20));

        menu.getChildren().addAll(
            makeButton("📦 Listar Itens do Inventário", () -> runAndShowTable("listar_items", "")),
            makeButton("💰 Peças Mais Lucrativas", this::showServicesLucrativos),
            makeButton("💰 Top-10 Mais Lucrativas", () -> runAndShowTable("top_n", "10")),
            makeButton("🏷️ Cálculo Mão de Obra", () -> askIdsAndShowTable("orcamento_mecanico")),
            makeButton("🏷️ Descontos nas Peças", this::showListaDescontos),
            makeButton("🏷️ Carrinho Consolidado", this::showConsolidado),
            makeButton("🚪 Sair", stage::close)
        );
        root.setCenter(menu);

        Scene scene = new Scene(root, 1000, 700);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Orçamento Mecânica do Ganâncio");
        stage.show();
    }

    private Button makeButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.getStyleClass().add("menu-button");
        btn.setPrefSize(BTN_WIDTH, BTN_HEIGHT);
        btn.setWrapText(true);
        btn.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        btn.setOnAction(e -> action.run());
        return btn;
    }

    private void runAndShowTable(String cmd, String arg) {
        try {
            List<String> lines = IntegradorOCaml.run(cmd, arg);
            showTableWindow(cmd, lines);
        } catch (IOException ex) {
            showError("Erro ao chamar OCaml", ex.getMessage());
        }
    }

    private void askIdsAndShowTable(String cmd) {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("Serviços");
        dlg.setHeaderText("Informe IDs dos serviços (ex.: 1,2):");
        dlg.setContentText("IDs:");
        dlg.showAndWait().filter(s -> !s.isBlank())
           .ifPresent(ids -> runAndShowTable(cmd, ids));
    }

    private void askIdsAndShowTwoTables(String c1, String c2) {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("Serviços");
        dlg.setHeaderText("Informe IDs dos serviços (ex.: 1,2):");
        dlg.setContentText("IDs:");
        dlg.showAndWait().filter(s -> !s.isBlank()).ifPresent(ids -> {
            runAndShowTable(c1, ids);
            runAndShowTable(c2, ids);
        });
    }

    private void showServicesLucrativos() {
        try {
            // 1) pega todos os IDs
            List<String> svs = IntegradorOCaml.run("listar_servicos", "");
            String ids = svs.stream()
                            .skip(1)                       // pula o cabeçalho “ID;Nome;PrecoBase”
                            .map(l -> l.split(";")[0])    // pega só a coluna ID
                            .collect(Collectors.joining(","));
            // 2) chama orcamento_items
            List<String> peças = IntegradorOCaml.run("orcamento_items", ids);
            // 3) exibe em tabela
            showTableWindow("Peças Mais Lucrativas", peças);
        } catch (IOException ex) {
            showError("Serviços Lucrativos", ex.getMessage());
        }
    }

    private void showCartWindow() {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("Carrinho Cliente");
        dlg.setHeaderText("Informe IDs dos serviços (ex.: 1,2):");
        dlg.setContentText("IDs:");
        dlg.showAndWait().filter(s -> !s.isBlank()).ifPresent(ids -> {
            try {
                List<String> rows = IntegradorOCaml.run("orcamento_items", ids);
                Cart cart = new Cart();
                for (int i = 1; i < rows.size(); i++) {
                    String[] c = rows.get(i).split(";");
                    cart.addItem(new CartItem(
                        Integer.parseInt(c[2]),
                        c[3], c[4], c[1],
                        Double.parseDouble(c[5]),
                        Double.parseDouble(c[6])
                    ));
                }
                showOutputWindow("Carrinho Cliente", cart.toString());
            } catch (IOException | NumberFormatException ex) {
                showError("Carrinho", ex.getMessage());
            }
        });
    }

    private void showTableWindow(String title, List<String> lines) {
        if (lines.isEmpty()) {
            showOutputWindow(title, "Nenhum dado encontrado.");
            return;
        }
        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle(title);
        String[] hdr = lines.get(0).split(";");
        TableView<ObservableList<String>> table = new TableView<>();
        for (int i = 0; i < hdr.length; i++) {
            final int idx = i;
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(hdr[i].trim());
            col.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().get(idx)));
            table.getColumns().add(col);
        }
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        for (int r = 1; r < lines.size(); r++) {
            ObservableList<String> row = FXCollections.observableArrayList();
            for (String c : lines.get(r).split(";")) row.add(c.trim());
            data.add(row);
        }
        table.setItems(data);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox box = new VBox(table);
        box.setPadding(new Insets(10));
        Scene sc = new Scene(box, 800, 600);
        sc.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        dlg.setScene(sc);
        dlg.showAndWait();
    }

    private void showOutputWindow(String title, String content) {
        Stage dlg = new Stage();
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle(title);
        TextArea ta = new TextArea(content);
        ta.setWrapText(true);
        ta.setEditable(false);
        Button close = new Button("Fechar");
        close.setOnAction(e -> dlg.close());
        VBox box = new VBox(10, ta, close);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(15));
        Scene sc = new Scene(box, 600, 400);
        sc.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        dlg.setScene(sc);
        dlg.showAndWait();
    }

    private void showError(String title, String detail) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(detail);
        alert.showAndWait();
    }

    private void showListaDescontos() {
        try {
            // Executa o comando OCaml sem args
            List<String> linhas = IntegradorOCaml.run("listar_descontos", "");
            // Abre a tabela com o resultado
            showTableWindow("Descontos nas Peças", linhas);
        } catch (IOException ex) {
            showError("Descontos Peças", ex.getMessage());
        }
    }

    private void showConsolidado() {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("Carrinho Consolidado");
        dlg.setHeaderText("Informe IDs dos serviços (ex.: 1,2):");
        dlg.setContentText("IDs:");
        dlg.showAndWait().filter(s -> !s.isBlank()).ifPresent(ids -> {
            try {
                List<String> discLines  = IntegradorOCaml.run("orcamento_desconto_items", ids);
                List<String> laborLines = IntegradorOCaml.run("orcamento_mecanico",     ids);
                List<String> fixLines   = IntegradorOCaml.run("orcamento_preco_fixo",   ids);

                double totalPecas     = 0, 
                       totalDescontos = 0, 
                       totalMo       = 0, 
                       totalFixo     = 0;
                StringBuilder sb = new StringBuilder();
                sb.append("--- Itens no Carrinho ---\n");

                // cada peça: f[1]=nome, f[2]=marca, f[7]=preço final, f[5]=pct ("10%"), f[6]=valor desconto
                for (int i = 1; i < discLines.size(); i++) {
                    String[] f         = discLines.get(i).split(";");
                    String nome        = f[1];
                    String marca       = f[2];
                    double precoFin    = Double.parseDouble(f[7]);
                    double valorDesc   = Double.parseDouble(f[6]);
                    String pctDesc     = f[5];
                    sb.append(String.format(
                      "%d. %s (%s) - €%.2f (Desc %s = €%.2f)\n",
                      i, nome, marca, precoFin, pctDesc, valorDesc));
                    totalPecas     += precoFin;
                    totalDescontos += valorDesc;
                }

                // soma mão de obra (coluna 6: Total)
                for (int i = 1; i < laborLines.size(); i++) {
                    String[] f = laborLines.get(i).split(";");
                    totalMo += Double.parseDouble(f[6]);
                }

                // soma preço fixo (coluna 2: PrecoFixo)
                for (int i = 1; i < fixLines.size(); i++) {
                    String[] f = fixLines.get(i).split(";");
                    totalFixo += Double.parseDouble(f[2]);
                }

                // resumo final incluindo total de descontos
                sb.append("\n");
                sb.append(String.format("Total das Peças:       €%.2f\n", totalPecas));
                sb.append(String.format("Total de Descontos:   €%.2f\n", totalDescontos));
                sb.append(String.format("Total da Mão de Obra:  €%.2f\n", totalMo));
                sb.append(String.format("Total de Preço Fixo:   €%.2f\n", totalFixo));
                sb.append("==============================\n");
                sb.append(String.format(
                  "TOTAL GERAL:           €%.2f\n",
                  totalPecas + totalMo + totalFixo));

                showOutputWindow("Carrinho Consolidado", sb.toString());
            } catch (IOException ex) {
                showError("Carrinho Consolidado", ex.getMessage());
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}