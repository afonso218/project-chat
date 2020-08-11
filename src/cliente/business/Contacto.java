package cliente.business;

import java.util.ArrayList;
import java.util.List;

import cliente.shared.Mensagem;

public class Contacto{

	private String nome;
	private List<Mensagem> mensagens;
	
	public Contacto(String nome) {
		this.nome = nome;
		this.mensagens = new ArrayList<Mensagem>();
	}
	
	public String getNome() {
		return nome;
	}
	
	public List<Mensagem> getMensagens() {
		return mensagens;
	}
	
	public void addMessage(Mensagem msg) {
		mensagens.add(msg);
	}
	
	@Override
	public String toString() {
		int msgPorLer = 0;
		for(Mensagem msg : mensagens){
			if(!msg.isLida()){
				msgPorLer++;
			}
		}
		if(msgPorLer == 0)
			return nome;
		else
			return nome + " (" + msgPorLer + ")";
	}

	public void setAllMessageRead() {
		for(Mensagem msg : mensagens){
			msg.lida();
		}
	}
	
}
