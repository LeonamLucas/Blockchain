package noobchain;

import java.security.PublicKey;

public class SaidaTransacao {
	public String id;
	public PublicKey destinatario; 
	public float valor;
	public String idTransacaoPai;
	

	public SaidaTransacao(PublicKey destinatario, float valor, String idTransacaoPai) {
		this.destinatario = destinatario;
		this.valor = valor;
		this.idTransacaoPai = idTransacaoPai;
		this.id = StringUtil.applySha256(StringUtil.getStringDaChave(destinatario)+Float.toString(valor)+idTransacaoPai);
	}
	
	//checa o dono das moedas
	public boolean checaDono(PublicKey publicKey) {
		return (publicKey == destinatario);
	}
	
}
