import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class Broker {
    private static final String BROKER_QUEUE_NAME = "BROKER";
    private static final String BOLSA_EXCHANGE_NAME = "BOLSA_DE_VALORES";
    private static final String COMPRA_ROUTING_KEY = "compra";
    private static final String VENDA_ROUTING_KEY = "venda";

    private Connection connection;
    private Channel channel;

    private Map<String, OrderBook> orderBooks;

    private BolsaDeValores bolsaDeValores;
    private boolean connectionSuccessful;

    public Broker(String rabbitMQServerAddress, BolsaDeValores bolsaDeValores) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("gull.rmq.cloudamqp.com");
            factory.setPort(5672);
            factory.setUsername("izamycsm");
            factory.setPassword("X6H60yjeOeUKWBzJOxHzVYLGeBjPx0TO");
            factory.setVirtualHost("izamycsm");
            connection = factory.newConnection();
            channel = connection.createChannel();

            // Declara a exchange da Bolsa de Valores
            channel.exchangeDeclare(BOLSA_EXCHANGE_NAME, "topic");

            // Declara a fila do Broker
            String queueName = channel.queueDeclare().getQueue();

            // Faz o binding das filas do Broker com a exchange da Bolsa de Valores
            channel.queueBind(queueName, BOLSA_EXCHANGE_NAME, COMPRA_ROUTING_KEY);
            channel.queueBind(queueName, BOLSA_EXCHANGE_NAME, VENDA_ROUTING_KEY);

            connectionSuccessful = true;

        }catch (IOException | TimeoutException e) {
            connectionSuccessful = false;
            e.printStackTrace();
        }
        this.bolsaDeValores = new BolsaDeValores(rabbitMQServerAddress);

    }

    public boolean isConnectionSuccessful() {
        return connectionSuccessful;
    }

    public void sendCompraOrder(String asset, int quantity, double price, String brokerCode) {
        String message = String.format("<ativo:%s,quant:%d,val:%.2f,corretora:%s>", asset, quantity, price, brokerCode);
        try {
            System.out.println("Enviando ordem de compra: " + message);
            boolean transacaoProcessada = bolsaDeValores.getOrderBook(asset).processCompraOrder(asset, quantity, price, brokerCode);
            if (transacaoProcessada) {
                System.out.println("Ordem de compra processada com sucesso.");
            } else {
                System.out.println("Não foi possível processar a ordem de compra.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendVendaOrder(String asset, int quantity, double price, String brokerCode) {
        String message = String.format("<ativo:%s,quant:%d,val:%.2f,corretora:%s>", asset, quantity, price, brokerCode);
        try {
            System.out.println("Enviando ordem de venda: " + message);
            channel.basicPublish(BOLSA_EXCHANGE_NAME, VENDA_ROUTING_KEY, null, message.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            channel.close();
            connection.close();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

}
