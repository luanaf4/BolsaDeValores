import java.util.HashMap;
import java.util.Map;

public class Transacoes {
    private static Map<String, TransacaoRecord> transacoes;

    public Transacoes() {
        transacoes = new HashMap<>();
    }

    public static void registerTransacao(String asset, int quantity, double price, String brokerCode) {
        TransacaoRecord record = transacoes.getOrDefault(asset, new TransacaoRecord());
        record.addTransacao(quantity, price, brokerCode);
        transacoes.put(asset, record);
    }


    public static TransacaoRecord getTransacoesForAsset(String asset) {
        return transacoes.get(asset);
    }

    static class TransacaoRecord {
        private int totalQuantity;
        private double totalValue;
        private Map<String, Integer> brokerTransacoes;

        public TransacaoRecord() {
            totalQuantity = 0;
            totalValue = 0.0;
            brokerTransacoes = new HashMap<>();
        }

        public void addTransacao(int quantity, double price, String brokerCode) {
            totalQuantity += quantity;
            totalValue += quantity * price;
            brokerTransacoes.merge(brokerCode, quantity, Integer::sum);
        }

        public int getTotalQuantity() {
            return totalQuantity;
        }

        public double getTotalValue() {
            return totalValue;
        }

        public Map<String, Integer> getBrokerTransacoes() {
            return brokerTransacoes;
        }
    }

    public static double getAveragePrice(String asset) {
        TransacaoRecord record = transacoes.get(asset);
        if (record != null && record.getTotalQuantity() > 0) {
            return record.getTotalValue() / record.getTotalQuantity();
        }
        return 0.0;
    }

    public static void imprimirTransacoes() {
        System.out.println("Transações:");
        if (transacoes != null && !transacoes.isEmpty()) {
            for (String asset : transacoes.keySet()) {
                TransacaoRecord record = transacoes.get(asset);
                System.out.println(asset + ": " + record);
            }
        } else {
            System.out.println("Nenhuma transação registrada.");
        }
    }
}