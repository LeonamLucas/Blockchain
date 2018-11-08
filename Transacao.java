package noobchain;
import java.security.*;
import java.util.ArrayList;

public class Transacao {
	
	public String idTransacao; //Contains a hash of transaction*
	public PublicKey remetente; //Senders address/public key.
	public PublicKey destinatario; //Recipients address/public key.
	public float valor; //Contains the amount we wish to send to the recipient.
	public byte[] assinatura; //This is to prevent anybody else from spending funds in our wallet.
	
	public ArrayList<EntradaTransacao> entradas = new ArrayList<EntradaTransacao>();
	public ArrayList<SaidaTransacao> saidas = new ArrayList<SaidaTransacao>();
	
	private static int contador = 0;
	
	// Constructor: 
	public Transacao(PublicKey de, PublicKey para, float valor,  ArrayList<EntradaTransacao> entradas) {
		this.remetente = de;
		this.destinatario = para;
		this.valor = valor;
		this.entradas = entradas;
	}
	
	public boolean processarTransacao() {
		
		if(verificarAssinatura() == false) {
			System.out.println("#A verificação da assinatura falhou");
			return false;
		}
				
		for(EntradaTransacao i : entradas) {
			i.UTXO = Test.UTXOs.get(i.transactionOutputId);
		}

		//Checa se a transação é válida:
		if(getValorEntradas() < Test.transacaoMinima) {
			System.out.println("Entrada da transação muito pequena: " + getValorEntradas());
			System.out.println("Valor deve ser maior que " + Test.transacaoMinima);
			return false;
		}
		
		//Gera a saida das transações:
		float sobra = getValorEntradas() - valor;
		idTransacao = calculaHash();
		saidas.add(new SaidaTransacao( this.destinatario, valor,idTransacao)); //envia o valor
		saidas.add(new SaidaTransacao( this.remetente, sobra,idTransacao)); //envia a sobra de volta ao remetente	
				
		for(SaidaTransacao o : saidas) {
			Test.UTXOs.put(o.id , o);
		}
		for(EntradaTransacao i : entradas) {
			if(i.UTXO == null) continue; 
			Test.UTXOs.remove(i.UTXO.id);
		}
		
		return true;
	}
	
	public float getValorEntradas() {
		float total = 0;
		for(EntradaTransacao i : entradas) {
			if(i.UTXO == null) continue;
			total += i.UTXO.valor;
		}
		return total;
	}
	
	public void gerarAssinatura(PrivateKey chavePrivada) {
		String data = StringUtil.getStringDaChave(remetente) + StringUtil.getStringDaChave(destinatario) + Float.toString(valor)	;
		assinatura = StringUtil.applyECDSASig(chavePrivada,data);		
	}
	
	public boolean verificarAssinatura() {
		String dados = StringUtil.getStringDaChave(remetente) + StringUtil.getStringDaChave(destinatario) + Float.toString(valor)	;
		return StringUtil.verifyECDSASig(remetente, dados, assinatura);
	}
	
	public float getValorSaida() {
		float total = 0;
		for(SaidaTransacao o : saidas) {
			total += o.valor;
		}
		return total;
	}
	
	private String calculaHash() {
		contador++; 
		return StringUtil.applySha256(StringUtil.getStringDaChave(remetente) +
				StringUtil.getStringDaChave(destinatario) +
				Float.toString(valor) + contador
				);
	}
}
