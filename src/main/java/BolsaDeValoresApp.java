import java.util.Map;
import java.util.Scanner;

public class BolsaDeValoresApp {
    private static final String RABBIT_MQ_SERVER_ADDRESS = "INSIRA O ENDEREÇO DO SERVIDOR";
    private static final String RABBIT_MQ_USERNAME = "INSIRA O USUARIO";
    private static final String RABBIT_MQ_PASSWORD = "INSIRA A SENHA";

    private static BolsaDeValores bolsaDeValores;

    public static void main(String[] args) {
        bolsaDeValores = new BolsaDeValores(RABBIT_MQ_SERVER_ADDRESS, RABBIT_MQ_USERNAME, RABBIT_MQ_PASSWORD);

        if (bolsaDeValores.isConnectionSuccessful()) {
            System.out.println("Conexão com o RabbitMQ estabelecida com sucesso pela Bolsa de Valores.");
        } else {
            System.out.println("Falha ao estabelecer conexão com o RabbitMQ pela Bolsa de Valores.");
            return;
        }

        bolsaDeValores.subscribeTransacoes();

        while (true) {
            System.out.println("Escolha uma opção:");
            System.out.println("1. Imprimir transações");
            System.out.println("2. Visualizar lista de ativos disponíveis");
            System.out.println("3. Sair");

            Scanner scanner = new Scanner(System.in);
            int option = scanner.nextInt();

            switch (option) {
                case 1:
                    Transacoes.imprimirTransacoes();
                    break;
                case 2:
                    Map<String, AssetList.AssetInfo> ativos = bolsaDeValores.getAssetList().getAssets();
                    System.out.println("Lista de ativos disponíveis:");
                    for (Map.Entry<String, AssetList.AssetInfo> entry : ativos.entrySet()) {
                        String ativo = entry.getKey();
                        AssetList.AssetInfo assetInfo = entry.getValue();
                        System.out.println(ativo + " - Quantidade: " + assetInfo.getQuantity() + ", Preço: " + assetInfo.getPrice());
                    }
                    break;
                case 3:
                    bolsaDeValores.close();
                    scanner.close();
                    System.exit(0);
                    break;
                default:
                    System.out.println("Opção inválida!");
            }
        }
    }
}