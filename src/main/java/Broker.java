import com.rabbitmq.client.Delivery;

import java.io.IOException;

public class Broker {
    private static final String BROKER_QUEUE_NAME = "BROKER";

    private String compraResponseQueueName;
    private String vendaResponseQueueName;

    private MOM mom;

    public Broker(String rabbitMQServerAddress, String username, String password) {
        mom = new MOM( "gull.rmq.cloudamqp.com", "izamycsm", "X6H60yjeOeUKWBzJOxHzVYLGeBjPx0TO" );
        try {
            compraResponseQueueName = mom.createResponseQueue();
            mom.subscribeResponseQueue(compraResponseQueueName, this::handleResponseMessage);
            vendaResponseQueueName = mom.createResponseQueue();
            mom.subscribeResponseQueue(vendaResponseQueueName, this::handleVendaResponseMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendCompraOrder(String asset, int quantity, double price, String brokerCode) {
        String message = String.format("<ativo:%s,quant:%d,val:%.2f,corretora:%s,respQueue:%s>", asset, quantity, price, brokerCode, compraResponseQueueName);
        mom.publishCompra(message);
    }

    public void sendVendaOrder(String asset, int quantity, double price, String brokerCode) {
        String message = String.format("<ativo:%s,quant:%d,val:%.2f,corretora:%s,respQueue:%s>", asset, quantity, price, brokerCode, vendaResponseQueueName);
        mom.publishVenda(message);
    }

    private void handleResponseMessage(String consumerTag, Delivery delivery) throws IOException {
        String message = new String(delivery.getBody(), "UTF-8");
        receiveCompraResponse(message);
    }

    private void handleVendaResponseMessage(String consumerTag, Delivery delivery) throws IOException {
        String message = new String(delivery.getBody(), "UTF-8");
        receiveVendaResponse(message);
    }

    public void receiveCompraResponse(String message) {
        System.out.println("Resposta da ordem de compra: " + message);
    }

    public void receiveVendaResponse(String message) {
        System.out.println("Resposta da ordem de venda: " + message);
    }

    public void close() {
        // Não é necessário fechar a conexão aqui, pois a classe MOM já cuida disso
    }
}