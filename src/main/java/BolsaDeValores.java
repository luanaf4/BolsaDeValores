import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class BolsaDeValores {
    private static final String BOLSA_EXCHANGE_NAME = "BOLSA_DE_VALORES";
    private static final String TRANSACAO_ROUTING_KEY = "transacao.{ativo}";
    private Channel channel;

    private Connection connection;

    public MOM getMOM() {
        return mom;
    }

    public Map<String, OrderBook> getOrderBooks() {
        return orderBooks;
    }

    public AssetList getAssetList() {
        return assetList;
    }

    private MOM mom;
    private Map<String, OrderBook> orderBooks;
    private AssetList assetList;
    private Transacoes transacoes;
    private boolean connectionSuccessful;

    public BolsaDeValores(String rabbitMQServerAddress, String username, String password) {
        try {
            mom = new MOM(rabbitMQServerAddress, username, password);
            orderBooks = new HashMap<>();
            assetList = new AssetList();
            transacoes = new Transacoes();

            // Carrega a lista de ativos da Bovespa
            loadAssets();

            // Subscreve os tópicos de compra e venda
            mom.subscribeCompra(this::handleCompra);
            mom.subscribeVenda(this::handleVenda);

            connectionSuccessful = true;
        } catch (Exception e) {
            connectionSuccessful = false;
            e.printStackTrace();
        }
    }



    public boolean isConnectionSuccessful() {
        return connectionSuccessful;
    }

    private void loadAssets() {
        for (Map.Entry<String, AssetList.AssetInfo> entry : assetList.getAssets().entrySet()) {
            String asset = entry.getKey();
            AssetList.AssetInfo assetInfo = entry.getValue();
        }
    }

    private void handleCompra(String message) {
        System.out.println("Recebendo ordem de compra: " + message);
        String[] parts = message.split(",");
        String asset = parts[0].split(":")[1];
        int quantity = Integer.parseInt(parts[1].split(":")[1]);
        double price = Double.parseDouble(parts[2].split(":")[1]);
        String brokerCode = parts[3].split(":")[1].replace(">", "");

        if (assetList.getAssets().containsKey(asset)) {
            OrderBook orderBook = getOrderBook(asset);
            orderBook.addCompraOrder(quantity, price, brokerCode);
            processarTransacoes(asset, orderBook);
        } else {
            System.out.println("Ativo não encontrado na lista de ativos disponíveis.");
        }
    }

    private void handleVenda(String message) {
        System.out.println("Recebendo ordem de venda: " + message);
        String[] parts = message.split(",");
        String asset = parts[0].split(":")[1];
        int quantity = Integer.parseInt(parts[1].split(":")[1]);
        double price = Double.parseDouble(parts[2].split(":")[1]);
        String brokerCode = parts[3].split(":")[1].replace(">", "");

        if (assetList.getAssets().containsKey(asset)) {
            OrderBook orderBook = getOrderBook(asset);
            orderBook.addVendaOrder(quantity, price, brokerCode);
            processarTransacoes(asset, orderBook);
        } else {
            System.out.println("Ativo não encontrado na lista de ativos disponíveis.");
        }
    }

    public void publishTransacao(String ativo, int quantity, double price, String brokerCode) {
        String message = String.format("<quant:%d,val:%.2f,corretora:%s>", quantity, price, brokerCode);
        try {
            channel.basicPublish(BOLSA_EXCHANGE_NAME, TRANSACAO_ROUTING_KEY.replace("{ativo}", ativo), null, message.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processarTransacoes(String asset, OrderBook orderBook) {
        int quantity = orderBook.checkTransacao();
        while (quantity > 0) {
            double price = orderBook.getTransacaoPrice();
            String brokerCode = orderBook.getTransacaoBrokerCode();
            mom.publishTransacao(asset, quantity, price, brokerCode);
            Transacoes.registerTransacao(asset, quantity, price, brokerCode);
            quantity = orderBook.checkTransacao();
        }
    }

    private void checkTransacao(String asset, OrderBook orderBook) {
        // Verifica se há uma transação
        int quantity = orderBook.checkTransacao();
        if (quantity > 0) {
            double price = orderBook.getTransacaoPrice();
            String brokerCode = orderBook.getTransacaoBrokerCode();
            mom.publishTransacao(asset, quantity, price, brokerCode);
        }
    }

    private String findAssetByBrokerCode(String brokerCode) {
        // Encontra o ativo correspondente ao código do broker
        for (Map.Entry<String, OrderBook> entry : orderBooks.entrySet()) {
            if (entry.getValue().containsBrokerCode(brokerCode)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void subscribeTransacoes() {
        mom.subscribeTransacao(this::handleTransacao);
    }

    private void handleTransacao(String message) {
        System.out.println("Recebendo transação: " + message);
        // Extrai os dados da mensagem de transação
        String[] parts = message.split(",");
        int quantity = Integer.parseInt(parts[0].split(":")[1]);
        double price = Double.parseDouble(parts[1].split(":")[1]);
        String brokerCode = parts[2].split(":")[1].replace(">", "");

        // Encontra o ativo correspondente ao código do broker
        String asset = findAssetByBrokerCode(brokerCode);
        if (asset != null) {
            Transacoes.registerTransacao(asset, quantity, price, brokerCode);
        }
    }

    public OrderBook getOrderBook(String asset) {
        if (orderBooks.containsKey(asset)) {
            return orderBooks.get(asset);
        } else {
            OrderBook newOrderBook = new OrderBook();
            orderBooks.put(asset, newOrderBook);
            return newOrderBook;
        }
    }

    public void close() {
        try {
            mom.channel.close();
            mom.connection.close();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }
}