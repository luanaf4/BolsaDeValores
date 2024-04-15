public class Broker {
    private static final String BROKER_QUEUE_NAME = "BROKER";

    private MOM mom;

    public Broker(String rabbitMQServerAddress, String username, String password) {
        mom = new MOM( "gull.rmq.cloudamqp.com", "izamycsm", "X6H60yjeOeUKWBzJOxHzVYLGeBjPx0TO" );
    }

    public void sendCompraOrder(String asset, int quantity, double price, String brokerCode) {
        String message = String.format("<ativo:%s,quant:%d,val:%.2f,corretora:%s>", asset, quantity, price, brokerCode);
        mom.publishCompra(message);
    }

    public void sendVendaOrder(String asset, int quantity, double price, String brokerCode) {
        String message = String.format("<ativo:%s,quant:%d,val:%.2f,corretora:%s>", asset, quantity, price, brokerCode);
        mom.publishVenda(message);
    }

    public void close() {
        // Não é necessário fechar a conexão aqui, pois a classe MOM já cuida disso
    }
}