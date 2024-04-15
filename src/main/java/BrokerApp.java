import java.util.Scanner;

public class BrokerApp {
    private static final String RABBIT_MQ_SERVER_ADDRESS = "INSIRA O ENDEREÇO DO SERVIDOR";
    private static final String RABBIT_MQ_USERNAME = "INSIRA O USUARIO";
    private static final String RABBIT_MQ_PASSWORD = "INSIRA A SENHA";

    public static void main(String[] args) {
        Broker broker = new Broker(RABBIT_MQ_SERVER_ADDRESS, RABBIT_MQ_USERNAME, RABBIT_MQ_PASSWORD);

        Scanner scanner = new Scanner(System.in);
        String brokerCode = "BROKER_CODE";

        while (true) {
            System.out.println("Escolha uma opção:");
            System.out.println("1. Enviar ordem de compra");
            System.out.println("2. Enviar ordem de venda");
            System.out.println("3. Sair");

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