import com.rabbitmq.client.Channel;

public class Main {
    public static void main(String[] args) {
        // Iniciar a Bolsa de Valores
        Thread stockExchangeThread = new Thread(() -> {
            StockExchange stockExchange = new StockExchange();
            stockExchange.run();



        });
        stockExchangeThread.start();

        // Iniciar o Broker
        Thread brokerThread = new Thread(() -> {
            Broker broker = new Broker();
            broker.startBroker();

            // Enviar mensagens de compra e venda para a Bolsa de Valores
            broker.sendOrderToStockExchange("compra.ativo", "compra<quant:100,val:50.0,corretora:ABCD>");
            broker.sendOrderToStockExchange("venda.ativo", "venda<quant:50,val:60.0,corretora:EFGH>");
        });
        brokerThread.start();

        // Aguardar a interrupção dos threads
        try {
            stockExchangeThread.join();
            brokerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}