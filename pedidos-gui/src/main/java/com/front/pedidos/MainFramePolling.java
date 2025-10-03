package com.front.pedidos;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

public class MainFramePolling extends JFrame {

    // Constantes de configuração
    private static final String API_BASE_URL = "http://localhost:8080/api/pedidos";
    private static final int POLLING_INTERVAL_MS = 3000; // 3 segundos

    // Componentes da UI
    private final JTextField produtoField;
    private final JSpinner quantidadeSpinner;
    private final JButton enviarButton;
    private final JTable pedidosTable;
    private final DefaultTableModel tableModel;

    // Utilitários
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    // Estruturas de dados para controle
    private final Map<String, Integer> pedidoIdToRowMap = new ConcurrentHashMap<>();
    private final Map<String, String> pedidoIdToStatusMap = new ConcurrentHashMap<>();

    public MainFramePolling() {


        super("Sistema de Pedidos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout(10, 10));

        // Inicialização de utilitários
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();


        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Novo Pedido"));
        inputPanel.add(new JLabel("Produto:"));
        produtoField = new JTextField(20);
        inputPanel.add(produtoField);
        inputPanel.add(new JLabel("Quantidade:"));
        quantidadeSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        inputPanel.add(quantidadeSpinner);
        enviarButton = new JButton("Enviar Pedido");
        enviarButton.addActionListener(e -> enviarPedido());
        inputPanel.add(enviarButton);

        // Tabela de Status dos Pedidos
        String[] columnNames = {"ID do Pedido", "Produto", "Quantidade", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        pedidosTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(pedidosTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Status dos Pedidos"));

        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Inicia o mecanismo de polling
        iniciarPollingDeStatus();
    }

    private void enviarPedido() {
        final String produto = produtoField.getText();
        final int quantidade = (int) quantidadeSpinner.getValue();

        if (produto.isBlank()) {
            JOptionPane.showMessageDialog(this, "O nome do produto é obrigatório.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            return;
        }

        enviarButton.setEnabled(false);

        // Requisições de rede por fora para que a thread principal(interface) não congele
        new Thread(() -> {
            try {
                // sem ID, que será gerado no backend
                String jsonPayload = String.format("{\"produto\": \"%s\", \"quantidade\": %d}", produto, quantidade);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_BASE_URL))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 202) {
                    JsonNode responseJson = objectMapper.readTree(response.body());
                    String pedidoId = responseJson.get("id").asText();

                    // Adiciona na UI e nos mapas de controle
                    SwingUtilities.invokeLater(() -> {
                        String statusInicial = "ENVIADO, AGUARDANDO PROCESSO";
                        tableModel.addRow(new Object[]{pedidoId, produto, quantidade, statusInicial});
                        int rowIndex = tableModel.getRowCount() - 1;
                        pedidoIdToRowMap.put(pedidoId, rowIndex);
                        pedidoIdToStatusMap.put(pedidoId, statusInicial);
                        produtoField.setText("");
                        quantidadeSpinner.setValue(1);
                    });
                } else {
                    String errorMsg = String.format("Erro ao enviar pedido.\nStatus: %d\nResposta: %s", response.statusCode(), response.body());
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, errorMsg, "Erro no Backend", JOptionPane.ERROR_MESSAGE));
                }
            } catch (IOException | InterruptedException ex) {

                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Falha de comunicação com o servidor: " + ex.getMessage(), "Erro de Rede", JOptionPane.ERROR_MESSAGE));
                Thread.currentThread().interrupt();
            } finally {
                // Reabilita o botão
                SwingUtilities.invokeLater(() -> enviarButton.setEnabled(true));
            }
        }).start();
    }

    private void iniciarPollingDeStatus() {

        Timer timer = new Timer(POLLING_INTERVAL_MS, e -> {
            // Itera sobre os pedidos que ainda não estão em um estado final
            for (Map.Entry<String, String> entry : pedidoIdToStatusMap.entrySet()) {
                String pedidoId = entry.getKey();
                String statusAtual = entry.getValue();

                if (!statusAtual.equals("SUCESSO") && !statusAtual.equals("FALHA")) {
                    // Usa SwingWorker para cada consulta
                    criarWorkerDeStatus(pedidoId).execute();
                }
            }
        });
        timer.start();
    }

    private SwingWorker<String, Void> criarWorkerDeStatus(String pedidoId) {
        return new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                //Consulta do Status na API
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_BASE_URL + "/status/" + pedidoId))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    return objectMapper.readTree(response.body()).get("status").asText();
                }
                return null;
            }

            @Override
            protected void done() {

                try {
                    String novoStatus = get();
                    if (novoStatus != null) {
                        Integer rowIndex = pedidoIdToRowMap.get(pedidoId);
                        if (rowIndex != null) {

                            // Atualiza o status na tabela e no mapa de controle
                            tableModel.setValueAt(novoStatus, rowIndex, 3);
                            pedidoIdToStatusMap.put(pedidoId, novoStatus);
                        }
                    }
                } catch (InterruptedException | ExecutionException ex) {

                    System.err.println("Erro ao consultar status do pedido " + pedidoId + ": " + ex.getMessage());
                }
            }
        };
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            MainFramePolling frame = new MainFramePolling();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}