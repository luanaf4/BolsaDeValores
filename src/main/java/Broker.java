import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class Broker {
    private static final String BROKER_QUEUE_NAME = "BROKER";
    private static final String BOLSA_DE_VALORES_EXCHANGE_NAME = "BOLSA_DE_VALORES";
    private static final String COMPRA_ROUTING_KEY = "compra";
    private static final String VENDA_ROUTING_KEY = "venda";

    private Connection connection;
    private Channel channel;

    public static void main(String[] args) {
        Broker broker = new Broker();
        broker.startBroker();
    }

    public void startBroker() {
        try {
            // Configurar a conexão com o RabbitMQ
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("gull.rmq.cloudamqp.com");
            factory.setPort(5672);
            factory.setUsername("izamycsm");
            factory.setPassword("X6H60yjeOeUKWBzJOxHzVYLGeBjPx0TO");
            factory.setVirtualHost("izamycsm");

            // Criar a conexão
            connection = factory.newConnection();
            channel = connection.createChannel();

            // Declarar a fila do Broker
            channel.queueDeclare(BROKER_QUEUE_NAME, false, false, false, null);

            // Declarar o exchange da Bolsa de Valores
            channel.exchangeDeclare(BOLSA_DE_VALORES_EXCHANGE_NAME, "topic");

            System.out.println("Broker conectado ao RabbitMQ e aguardando mensagens na fila " + BROKER_QUEUE_NAME);

            // Consumir mensagens da fila do Broker
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                System.out.println("Mensagem recebida: " + message);

                // Processar a mensagem e enviar para a Bolsa de Valores
                if (message.startsWith("compra")) {
                    sendOrderToStockExchange(COMPRA_ROUTING_KEY, message);
                } else if (message.startsWith("venda")) {
                    sendOrderToStockExchange(VENDA_ROUTING_KEY, message);
                }
            };
            channel.basicConsume(BROKER_QUEUE_NAME, true, deliverCallback, consumerTag -> {
            });
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    void sendOrderToStockExchange(String routingKey, String message) {
        try {
            channel.basicPublish(BOLSA_DE_VALORES_EXCHANGE_NAME, routingKey, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println("Mensagem enviada para a Bolsa de Valores: " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopBroker() {
        try {
            channel.close();
            connection.close();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }
}