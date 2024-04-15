import com.rabbitmq.client.Delivery;

import java.io.IOException;

public class Broker {
    private static final String BROKER_QUEUE_NAME = "BROKER";

    private String responseQueueName;

    private MOM mom;

    public Broker(String rabbitMQServerAddress, String username, String password) {
        mom = new MOM( "gull.rmq.cloudamqp.com", "izamycsm", "X6H60yjeOeUKWBzJOxHzVYLGeBjPx0TO" );
        try {
            responseQueueName = mom.createResponseQueue();
            mom.subscribeResponseQueue(responseQueueName, this::handleResponseMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendCompraOrder(String asset, int quantity, double price, String brokerCode) {
        String message = String.format("<ativo:%s,quant:%d,val:%.2f,corretora:%s,respQueue:%s>", asset, quantity, price, brokerCode, responseQueueName);
        mom.publishCompra(message);
    }

    public void sendVendaOrder(String asset, int quantity, double price, String brokerCode) {
        String message = String.format("<ativo:%s,quant:%d,val:%.2f,corretora:%s>", asset, quantity, price, brokerCode);
        mom.publishVenda(message);
    }

    private void handleResponseMessage(String consumerTag, Delivery delivery) throws IOException {
        String message = new String(delivery.getBody(), "UTF-8");
        receiveCompraResponse(message);
    }

    public void receiveCompraResponse(String message) {
        System.out.println("Resposta da ordem de compra: " + message);
    }

    public void close() {
        // Não é necessário fechar a conexão aqui, pois a classe MOM já cuida disso
    }
}