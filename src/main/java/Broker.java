import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class Broker {
    private static final String BROKER_QUEUE_NAME = "BROKER";

    public static void main(String[] args) {
        try {
            // Configurar a conexão com o RabbitMQ de acordo com as credenciais do cloudAMPQ
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

            System.out.println("Broker conectado ao RabbitMQ e aguardando mensagens na fila " + BROKER_QUEUE_NAME);

            // Enviar mensagens de compra e venda para a fila
            sendBuyOrder(channel, 100, 50.0, "ABCD");
            sendSellOrder(channel, 50, 60.0, "EFGH");

        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    private static void sendBuyOrder(Channel channel, int quantity, double value, String broker) {
        String message = String.format("compra<quant:%d,val:%.2f,corretora:%s>", quantity, value, broker);
        try {
            channel.basicPublish("", BROKER_QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println("Mensagem de compra enviada: " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendSellOrder(Channel channel, int quantity, double value, String broker) {
        String message = String.format("venda<quant:%d,val:%.2f,corretora:%s>", quantity, value, broker);
        try {
            channel.basicPublish("", BROKER_QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println("Mensagem de venda enviada: " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}