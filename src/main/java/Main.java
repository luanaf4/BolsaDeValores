import java.util.Map;
import java.util.Scanner;

public class Main {
    private static final String RABBIT_MQ_SERVER_ADDRESS = "gull.rmq.cloudamqp.com";
    private static final String RABBIT_MQ_USERNAME = "izamycsm";
    private static final String RABBIT_MQ_PASSWORD = "X6H60yjeOeUKWBzJOxHzVYLGeBjPx0TO";

    private static BolsaDeValores bolsaDeValores;
    private static Broker broker;

    public static void main(String[] args) {
        bolsaDeValores = new BolsaDeValores(RABBIT_MQ_SERVER_ADDRESS);
        broker = new Broker(RABBIT_MQ_SERVER_ADDRESS, bolsaDeValores);

        if (bolsaDeValores.isConnectionSuccessful()) {
            System.out.println("Conexão com o RabbitMQ estabelecida com sucesso pela Bolsa de Valores.");
        } else {
            System.out.println("Falha ao estabelecer conexão com o RabbitMQ pela Bolsa de Valores.");
        }

        if (broker.isConnectionSuccessful()) {
            System.out.println("Conexão com o RabbitMQ estabelecida com sucesso pelo Broker.");
        } else {
            System.out.println("Falha ao estabelecer conexão com o RabbitMQ pelo Broker.");
        }

            bolsaDeValores.subscribeTransacoes();

            Scanner scanner = new Scanner(System.in);
            String brokerCode = "BROKER_CODE";

            while (true) {
                System.out.println("Escolha uma opção:");
                System.out.println("1. Enviar ordem de compra");
                System.out.println("2. Enviar ordem de venda");
                System.out.println("3. Imprimir transações");
                System.out.println("4. Visualizar lista de ativos disponíveis");
                System.out.println("5. Sair");

                int option = scanner.nextInt();

                switch (option) {
                    case 1:
                        System.out.print("Digite o nome do ativo: ");
                        String compraAsset = scanner.next();
                        System.out.print("Digite a quantidade: ");
                        int compraQuantity = scanner.nextInt();
                        System.out.print("Digite o preço: ");
                        double compraPrice = scanner.nextDouble();
                        broker.sendCompraOrder(compraAsset, compraQuantity, compraPrice, brokerCode);
                        break;
                    case 2:
                        System.out.print("Digite o nome do ativo: ");
                        String vendaAsset = scanner.next();
                        System.out.print("Digite a quantidade: ");
                        int vendaQuantity = scanner.nextInt();
                        System.out.print("Digite o preço: ");
                        double vendaPrice = scanner.nextDouble();
                        broker.sendVendaOrder(vendaAsset, vendaQuantity, vendaPrice, brokerCode);
                        break;
                    case 3:
                        Transacoes.imprimirTransacoes();
                        break;
                    case 4:
                        Map<String, AssetList.AssetInfo> ativos = bolsaDeValores.getAssetList().getAssets();
                        System.out.println("Lista de ativos disponíveis:");
                        for (Map.Entry<String, AssetList.AssetInfo> entry : ativos.entrySet()) {
                            String ativo = entry.getKey();
                            AssetList.AssetInfo assetInfo = entry.getValue();
                            System.out.println(ativo + " - Quantidade: " + assetInfo.getQuantity() + ", Preço: " + assetInfo.getPrice());
                        }
                        break;
                    case 5:
                        bolsaDeValores.close();
                        broker.close();
                        scanner.close();
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Opção inválida!");
                }
            }
        }
    }