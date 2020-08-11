package cliente.shared;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Mensagem implements Serializable{

	public static final int INTRO = 0;
	public static final int NORMAL = 1;
	
	private int type;
	private String texto;
	private String emissor;
	private String receptor;
	private Estado estado;
	
	public Mensagem(String user) {
		type = INTRO;
		this.emissor = user;
	}
	
	public Mensagem(String emissor, String receptor, String texto) {
		type = NORMAL;
		this.emissor = emissor;
		this.receptor = receptor;
		this.texto = texto;
		this.estado = Estado.ESPERA;
	}
	
	public Mensagem(String emissor, String receptor, String texto, Estado estado) {
		type = NORMAL;
		this.emissor = emissor;
		this.receptor = receptor;
		this.texto = texto;
		this.estado = estado;
	}
	
	public void lida(){
		estado = Estado.ENTREGUE;
	}

	public boolean isLida() {
		return estado == Estado.ENTREGUE;
	}

	public int getType() {
		return type;
	}
	
	public String getEmissor() {
		return emissor;
	}
	
	public String getReceptor() {
		return receptor;
	}
	
	public String getTexto() {
		return texto;
	}
	
	public String toFormat(){
		return emissor + "::" + receptor + "::" + texto; 
	}
	
	@Override
	public String toString() {
		return emissor + ": " + texto;
	}
	
}
