import com.rabbitmq.client.Channel;

public class Main {
    public static void main(String[] args) {
        // Iniciar o Broker e enviar mensagens de compra e venda para a Bolsa de Valores
        Thread brokerThread = new Thread(() -> {
            Broker broker = new Broker();
            broker.startBroker();

            // Enviar mensagens de compra e venda para a Bolsa de Valores
        });
        brokerThread.start();

// Iniciar a Bolsa de Valores
        Thread stockExchangeThread = new Thread(() -> {
            StockExchange stockExchange = new StockExchange();
            stockExchange.run();
        });
        stockExchangeThread.start();
        // Aguardar a interrupção dos threads
        try {
            stockExchangeThread.join();
            brokerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
