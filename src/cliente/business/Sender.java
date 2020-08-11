package cliente.business;

import java.io.IOException;
import java.io.ObjectOutputStream;

import cliente.shared.Mensagem;

public class Sender {
	
	private ObjectOutputStream out;
	
	public synchronized void setOut(ObjectOutputStream out) {
		this.out = out;
		notifyAll();
	}
	
	public synchronized void askToSend(Mensagem msg){
		while(out == null){
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.out.println("Waiting for outputstream");
			}
		}
		try {
			out.writeObject(msg);
			out.flush();
			System.out.println("Sended: " + msg);
		} catch (IOException e) {
			System.out.println("Not Sended: " + msg);
			e.printStackTrace();
		}
	}

	public boolean isOn(){
		return out != null;
	}
	
	public void setOff(){
		try {
			if(out != null){
				out.close();
			}
		} catch (IOException e) {
			System.out.println("Set off Sender fail!");
			e.printStackTrace();
		}
		out = null;
	}
}
