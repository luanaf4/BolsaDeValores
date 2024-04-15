# Bolsa de Valores

## Descrição
Este projeto implementa um sistema de bolsa de valores utilizando RabbitMQ para comunicação entre corretoras (brokers) e a bolsa de valores. As corretoras podem enviar ordens de compra e venda de ativos, e a bolsa de valores processa essas ordens, registrando as transações e atualizando as quantidades dos ativos.

## Arquivos
- `BolsaDeValores.java`: Classe principal que representa a bolsa de valores. Ela gerencia ordens de compra e venda, e transações.
- `Broker.java`: Classe que representa uma corretora. Ela envia ordens de compra e venda para a bolsa de valores.
- `MOM.java`: Classe que lida com a comunicação via RabbitMQ.
- `OrderBook.java`: Classe que gerencia as ordens de compra e venda para cada ativo.
- `Transacoes.java`: Classe que registra as transações realizadas.
- `AssetList.java`: Classe que contém a lista de ativos disponíveis na bolsa de valores.
- `BrokerApp.java`: Aplicação de console para simular uma corretora enviando ordens de compra e venda.
- `BolsaDeValoresApp.java`: Aplicação de console para iniciar a bolsa de valores e processar ordens de compra e venda. 

## Requisitos
- Java Development Kit (JDK) instalado
- RabbitMQ instalado e em execução OU Instância do RabbitMQ criada no CloudAMQP


## Instruções de execução
1. Execute a aplicação da bolsa de valores:
   
Com o botâo direito, clique em BolsaDeValores.App e selecione a opção 'Run BolsaDeValores...main()'


3. Execute a aplicação da corretora:

Com o botâo direito, clique em Broker.Appp e selecione a opção 'Run BrokerApp.main()'

4. Na aplicação da corretora, você poderá enviar ordens de compra e venda de ativos.
5. As ordens serão processadas pela bolsa de valores, e as transações serão registradas.

## Observações
- Certifique-se de que o RabbitMQ esteja em execução antes de iniciar as aplicações (No caso de uso local).
- As credenciais de acesso ao RabbitMQ estão codificadas diretamente nas classes. Você pode modificá-las conforme necessário.
- A lista de ativos é carregada por meio do arquivo 'ativos_bovespa.csv' e processada pela classe AssetList.
- O código foi desenvolvido para fins educacionais e pode conter simplificações ou limitações.
