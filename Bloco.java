package noobchain;

import java.util.ArrayList;
import java.util.Date;

public class Bloco {
	
	public String hash;
	public String hashAnterior; 
	public String merkleRoot;
	public ArrayList<Transacao> transacoes = new ArrayList<Transacao>();
	public long timeStamp;
	public int count;
	
	//Block Constructor.  
	public Bloco(String hashAnterior ) {
		this.hashAnterior = hashAnterior;
		this.timeStamp = new Date().getTime();
		
		this.hash = calculaHash(); //Making sure we do this after we set the other values.
	}
	
	//Calculate new hash based on blocks contents
	public String calculaHash() {
		String hashCalculado = StringUtil.applySha256(hashAnterior +
				Long.toString(timeStamp) +
				Integer.toString(count) + 
				merkleRoot
				);
		return hashCalculado;
	}
	
	//Increases nonce value until hash target is reached.
	public void minerarBloco(int dificuldade) {
		merkleRoot = StringUtil.getMerkleRoot(transacoes);
		String target = StringUtil.getDificuldade(dificuldade); //Create a string with difficulty * "0" 
		while(!hash.substring( 0, dificuldade).equals(target)) {
			count ++;
			hash = calculaHash();
		}
		System.out.println("Bloco minerado : " + hash);
	}
	
	//Add transactions to this block
	public boolean addTransacao(Transacao transacao) {
		//process transaction and check if valid, unless block is genesis block then ignore.
		if(transacao == null) return false;		
		if((!"0".equals(hashAnterior))) {
			if((transacao.processarTransacao() != true)) {
				System.out.println("Falha no processamento da transação");
				return false;
			}
		}

		transacoes.add(transacao);
		System.out.println("Transação adicionada ao bloco");
		return true;
	}
	
}
