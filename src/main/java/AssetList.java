import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssetList {
    private static final String ATIVOS_BOVESPA_FILE = "src/main/java/ativos_bovespa.csv";
    private static Map<String, AssetInfo> assets;

    public AssetList() {
        assets = new HashMap<>();
        loadAssets();
    }

    private void loadAssets() {
        try (BufferedReader br = new BufferedReader(new FileReader(ATIVOS_BOVESPA_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 3) {
                    String asset = parts[0].trim();
                    int quantity = Integer.parseInt(parts[1].trim());
                    double price = Double.parseDouble(parts[2].trim());
                    assets.put(asset, new AssetInfo(quantity, price));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, AssetInfo> getAssets() {
        return assets;
    }

    public static class AssetInfo {
        private int quantity;
        private double price;

        public AssetInfo(int quantity, double price) {
            this.quantity = quantity;
            this.price = price;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getPrice() {
            return price;
        }
    }
}