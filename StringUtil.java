package noobchain;
import java.security.*;
import java.util.ArrayList;
import java.util.Base64;
import com.google.gson.GsonBuilder;
import java.util.List;

public class StringUtil {
	
	//Applies Sha256 to a string and returns the result. 
	public static String applySha256(String input){
		
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
	        
			//Applies sha256 to our input, 
			byte[] hash = digest.digest(input.getBytes("UTF-8"));
	        
			StringBuffer hexString = new StringBuffer(); // hash como hexadecimal
			for (int i = 0; i < hash.length; i++) {
				String hex = Integer.toHexString(0xff & hash[i]);
				if(hex.length() == 1) hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	//retorna o resutado da assinatura.
	public static byte[] applyECDSASig(PrivateKey chavePrivada, String entrada) {
		Signature dsa;
		byte[] saida = new byte[0];
		try {
			dsa = Signature.getInstance("ECDSA", "BC");
			dsa.initSign(chavePrivada);
			byte[] strByte = entrada.getBytes();
			dsa.update(strByte);
			byte[] realSig = dsa.sign();
			saida = realSig;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return saida;
	}
	
	//Verifica a assinatura
	public static boolean verifyECDSASig(PublicKey chavePublica, String dados, byte[] assinatura) {
		try {
			Signature ecdsaverifica = Signature.getInstance("ECDSA", "BC");
			ecdsaverifica.initVerify(chavePublica);
			ecdsaverifica.update(dados.getBytes());
			return ecdsaverifica.verify(assinatura);
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	//Transforma objeto em String json
	public static String getJson(Object o) {
		return new GsonBuilder().setPrettyPrinting().create().toJson(o);
	}
	
	//Retorna o alvo da dificuldade para comparar com o hash, dificuldade 5 vai ser "00000".
	public static String getDificuldade(int difficulty) {
		return new String(new char[difficulty]).replace('\0', '0');
	}
	
	public static String getStringDaChave(Key key) {
		return Base64.getEncoder().encodeToString(key.getEncoded());
	}
	
	public static String getMerkleRoot(ArrayList<Transacao> transacoes) {
		int count = transacoes.size();
		
		List<String> camadaAnterior = new ArrayList<String>();
		for(Transacao transaction : transacoes) {
			camadaAnterior.add(transaction.idTransacao);
		}
		List<String> camada = camadaAnterior;
		
		while(count > 1) {
			camada = new ArrayList<String>();
			for(int i=1; i < camadaAnterior.size(); i+=2) {
				camada.add(applySha256(camadaAnterior.get(i-1) + camadaAnterior.get(i)));
			}
			count = camada.size();
			camadaAnterior = camada;
		}
		
		String merkleRoot = (camada.size() == 1) ? camada.get(0) : "";
		return merkleRoot;
	}
}
