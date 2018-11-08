package noobchain;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Carteira {
	
	public PrivateKey chavePrivada;
	public PublicKey chavePublica;
	
	public HashMap<String,SaidaTransacao> UTXOs = new HashMap<String,SaidaTransacao>();
	
	public Carteira() {
		gerarPar();
	}
		
	public void gerarPar() {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
			
			keyGen.initialize(ecSpec, random);
	        KeyPair keyPair = keyGen.generateKeyPair();
	        chavePrivada = keyPair.getPrivate();
	        chavePublica = keyPair.getPublic();
	        
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public float getSaldo() {
		float total = 0;	
        for (Map.Entry<String, SaidaTransacao> item: Test.UTXOs.entrySet()){
        	SaidaTransacao UTXO = item.getValue();
            if(UTXO.checaDono(chavePublica)) {
            	UTXOs.put(UTXO.id,UTXO);
            	total += UTXO.valor ; 
            }
        }  
		return total;
	}
	
	public Transacao enviaMoedas(PublicKey destinatario,float valor ) {
		if(getSaldo() < valor) {
			System.out.println("#Sem saldo suficiente.");
			return null;
		}
		ArrayList<EntradaTransacao> entradas = new ArrayList<EntradaTransacao>();
		
		float total = 0;
		for (Map.Entry<String, SaidaTransacao> item: UTXOs.entrySet()){
			SaidaTransacao UTXO = item.getValue();
			total += UTXO.valor;
			entradas.add(new EntradaTransacao(UTXO.id));
			if(total > valor) break;
		}
		
		Transacao novaTransacao = new Transacao(chavePublica, destinatario , valor, entradas);
		novaTransacao.gerarAssinatura(chavePrivada);
		
		for(EntradaTransacao input: entradas){
			UTXOs.remove(input.transactionOutputId);
		}
		
		return novaTransacao;
	}
	
}


