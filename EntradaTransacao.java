package noobchain;

public class EntradaTransacao {
	public String transactionOutputId;
	public SaidaTransacao UTXO;
	
	public EntradaTransacao(String transactionOutputId) {
		this.transactionOutputId = transactionOutputId;
	}
}
