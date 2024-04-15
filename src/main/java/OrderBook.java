import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrderBook {
    private List<CompraOrder> compraOrders;
    private List<VendaOrder> vendaOrders;
    private int transacaoQuantity;
    private double transacaoPrice;
    private String transacaoBrokerCode;

    public OrderBook() {
        compraOrders = new ArrayList<>();
        vendaOrders = new ArrayList<>();
        transacaoQuantity = 0;
        transacaoPrice = 0.0;
        transacaoBrokerCode = "";

        // Carrega os ativos disponíveis para venda da lista de AssetList
        Map<String, AssetList.AssetInfo> assets = AssetList.getAssets();
        for (Map.Entry<String, AssetList.AssetInfo> entry : assets.entrySet()) {
            String asset = entry.getKey();
            AssetList.AssetInfo assetInfo = entry.getValue();
            int initialQuantity = assetInfo.getQuantity();
            double initialPrice = assetInfo.getPrice();
            vendaOrders.add(new VendaOrder(initialQuantity, initialPrice, "BROKER_CODE"));
        }
    }

    public boolean containsBrokerCode(String brokerCode) {
        return compraOrders.stream().anyMatch(order -> order.getBrokerCode().equals(brokerCode))
                || vendaOrders.stream().anyMatch(order -> order.getBrokerCode().equals(brokerCode));
    }

    public boolean processCompraOrder(String asset, int quantity, double price, String brokerCode) {
        if (AssetList.getAssets().containsKey(asset)) {
            VendaOrder vendaOrder = vendaOrders.stream()
                    .filter(order -> order.getPrice() <= price)
                    .findFirst()
                    .orElse(null);

            if (vendaOrder != null && vendaOrder.getQuantity() >= quantity) {
                int transacaoQty = quantity;
                double transacaoPrice = (price + vendaOrder.getPrice()) / 2;

                vendaOrder.decreaseQuantity(transacaoQty);
                if (vendaOrder.getQuantity() == 0) {
                    vendaOrders.remove(vendaOrder);
                }

                Transacoes.registerTransacao(asset, transacaoQty, transacaoPrice, brokerCode);

                // Atualiza a quantidade do ativo na lista AssetList
                AssetList.AssetInfo assetInfo = AssetList.getAssets().get(asset);
                assetInfo.setQuantity(assetInfo.getQuantity() - transacaoQty);

                return true;
            }
        }
        return false;
    }

    public boolean processVendaOrder(String asset, int quantity, double price, String brokerCode) {
        Transacoes.TransacaoRecord record = Transacoes.getTransacoesForAsset(asset);
        if (record != null) {
            double averagePrice = Transacoes.getAveragePrice(asset);
            if (quantity <= record.getTotalQuantity() && price <= averagePrice) {
                double transacaoPrice = price;
                Transacoes.registerTransacao(asset, quantity, transacaoPrice, brokerCode);

                // Atualiza a quantidade do ativo na lista AssetList
                AssetList.AssetInfo assetInfo = AssetList.getAssets().get(asset);
                assetInfo.setQuantity(assetInfo.getQuantity() + quantity);

                return true;
            }
        }
        return false;
    }

    static class CompraOrder {
        private int quantity;
        private double price;
        private String brokerCode;

        public CompraOrder(int quantity, double price, String brokerCode) {
            this.quantity = quantity;
            this.price = price;
            this.brokerCode = brokerCode;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getPrice() {
            return price;
        }

        public String getBrokerCode() {
            return brokerCode;
        }

        public void decreaseQuantity(int amount) {
            quantity -= amount;
        }
    }
    static class VendaOrder {
        private int quantity;
        private double price;
        private String brokerCode;

        public VendaOrder(int quantity, double price, String brokerCode) {
            this.quantity = quantity;
            this.price = price;
            this.brokerCode = brokerCode;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getPrice() {
            return price;
        }

        public String getBrokerCode() {
            return brokerCode;
        }

        public void decreaseQuantity(int amount) {
            quantity -= amount;
        }
    }
}