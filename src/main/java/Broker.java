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

    public static void main(String[] args) {
        try {
            // Configurar a conexão com o RabbitMQ
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("gull.rmq.cloudamqp.com");
            factory.setPort(1883);
            factory.setUsername("izamycsm");
            factory.setPassword("X6H60yjeOeUKWBzJOxHzVYLGeBjPx0TO");

            // Criar a conexão
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

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
                    sendOrderToStockExchange(channel, COMPRA_ROUTING_KEY, message);
                } else if (message.startsWith("venda")) {
                    sendOrderToStockExchange(channel, VENDA_ROUTING_KEY, message);
                }
            };
            channel.basicConsume(BROKER_QUEUE_NAME, true, deliverCallback, consumerTag -> {
            });

        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    private static void sendOrderToStockExchange(Channel channel, String routingKey, String message) {
        try {
            channel.basicPublish(BOLSA_DE_VALORES_EXCHANGE_NAME, routingKey, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println("Mensagem enviada para a Bolsa de Valores: " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}