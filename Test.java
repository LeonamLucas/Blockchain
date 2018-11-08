package noobchain;
import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;

public class Test {
	
	public static ArrayList<Bloco> blockchain = new ArrayList<Bloco>();
	public static HashMap<String,SaidaTransacao> UTXOs = new HashMap<String,SaidaTransacao>();
	
	public static int dificuldade = 3;
	public static float transacaoMinima = 0.1f;
	public static Carteira carteiraA;
	public static Carteira carteiraB;
	public static Transacao primeiraTransacao;

	public static void main(String[] args) {	

		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		
		//Cria as carteiras:
		carteiraA = new Carteira();
		carteiraB = new Carteira();		
		Carteira coinbase = new Carteira();
		
		//Cria a primeira transação, que envia 100 moedas para a carteira A: 
		primeiraTransacao = new Transacao(coinbase.chavePublica, carteiraA.chavePublica, 100f, null);
		primeiraTransacao.gerarAssinatura(coinbase.chavePrivada);	
		primeiraTransacao.idTransacao = "0";
		primeiraTransacao.saidas.add(new SaidaTransacao(primeiraTransacao.destinatario, primeiraTransacao.valor, primeiraTransacao.idTransacao)); 
		UTXOs.put(primeiraTransacao.saidas.get(0).id, primeiraTransacao.saidas.get(0));
		
		System.out.println("Criando e minerando o primeiro bloco");
		Bloco genesis = new Bloco("0");
		genesis.addTransacao(primeiraTransacao);
		addBlock(genesis);
		
		//testing
		Bloco bloco1 = new Bloco(genesis.hash);
		System.out.println("\nSaldo na carteira A: " + carteiraA.getSaldo());
		System.out.println("\nCarteira A está tentando enviar 40 moedas para a carteira B...");
		bloco1.addTransacao(carteiraA.enviaMoedas(carteiraB.chavePublica, 40f));
		addBlock(bloco1);
		System.out.println("\nSaldo na carteira A: " + carteiraA.getSaldo());
		System.out.println("Saldo na carteira B: " + carteiraB.getSaldo());
		
		Bloco bloco2 = new Bloco(bloco1.hash);
		System.out.println("\nCarteira A está tentando enviar mais moedas do que seu saldo (1000)...");
		bloco2.addTransacao(carteiraA.enviaMoedas(carteiraB.chavePublica, 1000f));
		addBlock(bloco2);
		System.out.println("\nSaldo na carteira A: " + carteiraA.getSaldo());
		System.out.println("Saldo na carteira B: " + carteiraB.getSaldo());
		
		Bloco bloco3 = new Bloco(bloco2.hash);
		System.out.println("\nCarteira B está tentando enviar 20 moedas para carteira A...");
		bloco3.addTransacao(carteiraB.enviaMoedas(carteiraA.chavePublica, 20));
		System.out.println("\nSaldo na carteira A: " + carteiraA.getSaldo());
		System.out.println("Saldo na carteira B: " + carteiraB.getSaldo());
		
		isChainValid();
		
	}
	
	public static Boolean isChainValid() {
		Bloco blocoAtual; 
		Bloco blocoAnterior;
		String hashTarget = new String(new char[dificuldade]).replace('\0', '0');
		HashMap<String,SaidaTransacao> tempUTXOs = new HashMap<String,SaidaTransacao>();
		tempUTXOs.put(primeiraTransacao.saidas.get(0).id, primeiraTransacao.saidas.get(0));
		
		//loop de checagem de hashes:
		for(int i=1; i < blockchain.size(); i++) {
			
			blocoAtual = blockchain.get(i);
			blocoAnterior = blockchain.get(i-1);
			//compara o hash registrado e o calculado
			if(!blocoAtual.hash.equals(blocoAtual.calculaHash()) ){
				System.out.println("#Os hashes não são iguais");
				return false;
			}
			//compara os hashes anteriores
			if(!blocoAnterior.hash.equals(blocoAtual.hashAnterior) ) {
				System.out.println("#Os hashes anteriores não são iguais");
				return false;
			}
			//checa se o hash foi resolvido
			if(!blocoAtual.hash.substring(0, dificuldade).equals(hashTarget)) {
				System.out.println("#Esse bloco não foi minerado");
				return false;
			}
			
			//loop das transações realizadas
			SaidaTransacao tempOutput;
			for(int t=0; t <blocoAtual.transacoes.size(); t++) {
				Transacao transacaoAtual = blocoAtual.transacoes.get(t);
				
				if(!transacaoAtual.verificarAssinatura()) {
					System.out.println("#Assinatura da transação (" + t + ") não é valida");
					return false; 
				}
				if(transacaoAtual.getValorEntradas() != transacaoAtual.getValorSaida()) {
					System.out.println("#As entradas não correspondem as saídas(" + t + ")");
					return false; 
				}
				
				for(EntradaTransacao entrada: transacaoAtual.entradas) {	
					tempOutput = tempUTXOs.get(entrada.transactionOutputId);
					
					if(tempOutput == null) {
						System.out.println("#Entrada referenciada (" + t + ") está faltando");
						return false;
					}
					
					if(entrada.UTXO.valor != tempOutput.valor) {
						System.out.println("#O valor da  transação (" + t + ") é inválido");
						return false;
					}
					
					tempUTXOs.remove(entrada.transactionOutputId);
				}
				
				for(SaidaTransacao saida: transacaoAtual.saidas) {
					tempUTXOs.put(saida.id, saida);
				}
				
				if( transacaoAtual.saidas.get(0).destinatario != transacaoAtual.destinatario) {
					System.out.println("#A saida da transação (" + t + ") não é o que deveria ser");
					return false;
				}
				if( transacaoAtual.saidas.get(1).destinatario != transacaoAtual.remetente) {
					System.out.println("#A saida da transação (" + t + ") não é o remetente.");
					return false;
				}
				
			}
			
		}
		System.out.println("Blockchain válido");
		return true;
	}
	
	public static void addBlock(Bloco novoBloco) {
		novoBloco.minerarBloco(dificuldade);
		blockchain.add(novoBloco);
	}
}