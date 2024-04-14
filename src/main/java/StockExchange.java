import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class StockExchange {
    private static final String BOLSA_DE_VALORES_EXCHANGE_NAME = "BOLSADEVALORES";
    private static final String COMPRA_ROUTING_KEY = "compra.#";
    private static final String VENDA_ROUTING_KEY = "venda.#";
    private static final String TRANSACAO_ROUTING_KEY = "transacao.#";

    private Map<String, Map<Double, Integer>> orderBook = new HashMap<>(); // Livro de ofertas

    public static void main(String[] args) {
        StockExchange stockExchange = new StockExchange();
        stockExchange.run();
    }


    public void run() {
        try {
            // Configurar a conexão com o RabbitMQ
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("gull.rmq.cloudamqp.com");
            factory.setPort(5672);
            factory.setUsername("izamycsm");
            factory.setPassword("X6H60yjeOeUKWBzJOxHzVYLGeBjPx0TO");
            factory.setVirtualHost("izamycsm");

            // Criar a conexão
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            // Declarar a exchange da Bolsa de Valores
            channel.exchangeDeclare(BOLSA_DE_VALORES_EXCHANGE_NAME, "topic");

            // Criar uma fila temporária para receber as mensagens
            String queueName = channel.queueDeclare().getQueue();

            // Vincular a fila à exchange usando as chaves de roteamento
            channel.queueBind(queueName, BOLSA_DE_VALORES_EXCHANGE_NAME, COMPRA_ROUTING_KEY);
            channel.queueBind(queueName, BOLSA_DE_VALORES_EXCHANGE_NAME, VENDA_ROUTING_KEY);

            System.out.println("Bolsa de Valores conectada ao RabbitMQ e aguardando mensagens.");

            // Consumir mensagens da fila
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                System.out.println("Mensagem recebida: " + message);

                // Processar a mensagem de acordo com as regras de negócio
                processOrder(channel,message);

                // Publicar atualizações do livro de ofertas e operações realizadas
                String[] parts = message.split("<");
                int quantity = Integer.parseInt(parts[1].split(":")[1]);
                double price = Double.parseDouble(parts[2].split(":")[1]);
                String broker = parts[3].split(":")[1].replace(">", "");

                publishOrderBookUpdate(channel, broker, price, quantity, true);
                publishTradeNotification(channel, broker, quantity, price, true);
            };
            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
            });

        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

     void processOrder(Channel channel, String message) {
        if (message.startsWith("compra")) {
            processBuyOrder(channel, message);
        } else if (message.startsWith("venda")) {
            processSellOrder(channel, message);
        }
    }

    private void processBuyOrder(Channel channel, String message) {
        String[] parts = message.split("<");
        int quantity = Integer.parseInt(parts[1].split(":")[1]);
        double value = Double.parseDouble(parts[2].split(":")[1]);
        String broker = parts[3].split(":")[1].replace(">", "");

        // Adicionar a ordem de compra no livro de ofertas
        orderBook.computeIfAbsent(broker, k -> new HashMap<>())
                .merge(value, quantity, Integer::sum);

        // Verificar se há uma ordem de venda correspondente
        Map<Double, Integer> sellOrders = orderBook.getOrDefault(broker, new HashMap<>());
        int executedQuantity = 0;
        for (Map.Entry<Double, Integer> entry : sellOrders.entrySet()) {
            double sellPrice = entry.getKey();
            int sellQuantity = entry.getValue();
            if (sellPrice <= value) {
                executedQuantity = Math.min(quantity, sellQuantity);
                quantity -= executedQuantity;
                sellQuantity -= executedQuantity;

                // Publicar a transação
                publishTransaction(channel, broker, executedQuantity, sellPrice);
                publishTradeNotification(channel, broker, executedQuantity, sellPrice, false);

                if (sellQuantity == 0) {
                    sellOrders.remove(sellPrice);
                } else {
                    sellOrders.put(sellPrice, sellQuantity);
                }

                if (quantity == 0) {
                    break;
                }
            }
        }

        // Atualizar o livro de ofertas
        if (quantity > 0) {
            orderBook.get(broker).put(value, quantity);
        }

        // Publicar a atualização do livro de ofertas
        publishOrderBookUpdate(channel, broker, value, quantity, true);
    }

    private void processSellOrder(Channel channel, String message) {
        String[] parts = message.split("<");
        int quantity = Integer.parseInt(parts[1].split(":")[1]);
        double value = Double.parseDouble(parts[2].split(":")[1]);
        String broker = parts[3].split(":")[1].replace(">", "");

        // Adicionar a ordem de venda no livro de ofertas
        orderBook.computeIfAbsent(broker, k -> new HashMap<>())
                .merge(value, quantity, Integer::sum);

        // Verificar se há uma ordem de compra correspondente
        Map<Double, Integer> buyOrders = orderBook.getOrDefault(broker, new HashMap<>());
        int executedQuantity = 0;
        for (Map.Entry<Double, Integer> entry : buyOrders.entrySet()) {
            double buyPrice = entry.getKey();
            int buyQuantity = entry.getValue();
            if (buyPrice >= value) {
                executedQuantity = Math.min(quantity, buyQuantity);
                quantity -= executedQuantity;
                buyQuantity -= executedQuantity;

                // Publicar a transação
                publishTransaction(channel, broker, executedQuantity, value);
                publishTradeNotification(channel, broker, executedQuantity, value, true);

                if (buyQuantity == 0) {
                    buyOrders.remove(buyPrice);
                } else {
                    buyOrders.put(buyPrice, buyQuantity);
                }

                if (quantity == 0) {
                    break;
                }
            }
        }

        // Atualizar o livro de ofertas
        if (quantity > 0) {
            orderBook.get(broker).put(value, quantity);
        }

        // Publicar a atualização do livro de ofertas
        publishOrderBookUpdate(channel, broker, value, quantity, false);
    }

    private void publishTransaction(Channel channel, String broker, int quantity, double price) {
        String message = String.format("transacao<quant:%d,val:%.2f,corretora:%s>", quantity, price, broker);
        try {
            channel.basicPublish(BOLSA_DE_VALORES_EXCHANGE_NAME, TRANSACAO_ROUTING_KEY, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println("Transação publicada: " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

     void publishOrderBookUpdate(Channel channel, String broker, double price, int quantity, boolean isBuy) {
        String routingKey = (isBuy ? "compra" : "venda") + "." + broker;
        String message = String.format("%s<quant:%d,val:%.2f,corretora:%s>", isBuy ? "compra" : "venda", quantity, price, broker);
        try {
            channel.basicPublish(BOLSA_DE_VALORES_EXCHANGE_NAME, routingKey, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println("Atualização do livro de ofertas publicada: " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

     void publishTradeNotification(Channel channel, String broker, int quantity, double price, boolean isBuy) {
        String routingKey = (isBuy ? "compra" : "venda") + "." + broker;
        String message = String.format("%s<quant:%d,val:%.2f,corretora:%s>", isBuy ? "compra" : "venda", quantity, price, broker);
        try {
            channel.basicPublish(BOLSA_DE_VALORES_EXCHANGE_NAME, routingKey, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println("Notificação de transação publicada: " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

