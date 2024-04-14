import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class MOM {
    private static final String BOLSA_EXCHANGE_NAME = "BOLSA_DE_VALORES";
    private static final String COMPRA_ROUTING_KEY = "compra";
    private static final String VENDA_ROUTING_KEY = "venda";
    private static final String TRANSACAO_ROUTING_KEY = "transacao.{ativo}";

    public Connection connection;
    public Channel channel;

    public MOM(String rabbitMQServerAddress, String username, String password) {
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
            channel.exchangeDeclare(BOLSA_EXCHANGE_NAME, "topic", false);
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    public void subscribeCompra(CompraCallback callback) {
        try {
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, BOLSA_EXCHANGE_NAME, COMPRA_ROUTING_KEY);

            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String message = new String(body, "UTF-8");
                    callback.onCompra(message);
                }
            };
            channel.basicConsume(queueName, true, consumer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void subscribeVenda(VendaCallback callback) {
        try {
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, BOLSA_EXCHANGE_NAME, VENDA_ROUTING_KEY);

            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String message = new String(body, "UTF-8");
                    callback.onVenda(message);
                }
            };
            channel.basicConsume(queueName, true, consumer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void subscribeTransacao(TransacaoCallback callback) {
        try {
            String queueName = channel.queueDeclare().getQueue();
            Map<String, AssetList.AssetInfo> assets = AssetList.getAssets();
            for (String asset : assets.keySet()) {
                channel.queueBind(queueName, BOLSA_EXCHANGE_NAME, TRANSACAO_ROUTING_KEY.replace("{ativo}", asset));
            }

            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String message = new String(body, "UTF-8");
                    callback.onTransacao(message);
                }
            };
            channel.basicConsume(queueName, true, consumer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface TransacaoCallback {
        void onTransacao(String message);
    }


    public void publishCompra(String message) {
        try {
            channel.basicPublish(BOLSA_EXCHANGE_NAME, COMPRA_ROUTING_KEY, null, message.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void publishVenda(String message) {
        try {
            channel.basicPublish(BOLSA_EXCHANGE_NAME, VENDA_ROUTING_KEY, null, message.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
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

    public interface CompraCallback {
        void onCompra(String message);
    }

    public interface VendaCallback {
        void onVenda(String message);
    }
}