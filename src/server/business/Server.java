package server.business;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import server.shared.Mensagem;

public class Server {

	public static final int PORTO = 8080;
	
	private GestorLigacoes gestor;
	private ServerSocket serverSocket;
	private boolean usada;
	private boolean enviando;
	private List<Mensagem> waiting;
	private HashMap<String , Ligacao> users;
	
	public Server() {
		usada = false;
		enviando = false;
		users = new HashMap<String , Ligacao>();
		waiting = new ArrayList<Mensagem>();
		gestor = new GestorLigacoes();
		try {
			serverSocket = new ServerSocket(PORTO);
			System.out.println("IP: " + InetAddress.getLocalHost());
		} catch (IOException e) {
			System.out.println("> Warning: ServerSocket Port or Gestor de Ligacoes Fail");
		}
	}
	
	public void start(){
		gestor.start();
		Scanner s = new Scanner(System.in);
		String comando = s.nextLine(); 
		while (true) {
	        if (comando.equals("exit")) {
	            break;
	        }else if(comando.equals("number")){
	        	System.out.println("> Total number of connections: " + getSize());
	        }else if(comando.equals("list")){
	        	System.out.println("> List of users connected: " + getList().size());
	        	for(String name : getList()){
		        	System.out.println("> \t" + name);
	        	}
	        }else if(comando.equals("waiting")){
	        	System.out.println("> Waiting for users: " + getWaitingList().size());
	        	for(Mensagem msg : getWaitingList()){
		        	System.out.println("> \t" + msg);
	        	}
	        }else if(comando.equals("help")){
	        	System.out.println("> Comandos:");
	        	System.out.println("> number - \"Total number of connections\"");
	        	System.out.println("> list - \"List of all users connected\"");
	        	System.out.println("> waiting - \"List of Mensagens waiting for users\"");
	        	System.out.println("> exit - \"Stop Server\"");	
	        }else{
	        	System.out.println("> type -help to list comands");
	        }
	        comando = s.nextLine();
	    }
		s.close();
		System.out.println("> Exit Server");
		stop();
	}
	
	private void stop(){
		try{
			serverSocket.close();
			gestor.interrupt();
			for(Thread t : users.values()){
				t.interrupt();
			}
		}catch(Exception e){
			System.out.println("> Warning: InterruptedException - Stop");
		}
	}
	
	private synchronized void sendMesg(Mensagem msg){
		while(enviando){
			try {
				wait();
			} catch (InterruptedException e) {}
		}
		enviando = true;
		// RECURSO CRITICO
		try {
			users.get(msg.getReceptor()).getOut().writeObject(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
		enviando = false;
		notifyAll();
	}
	
	private synchronized void addWaitingList(Mensagem msg){
		while(usada){
			try {
				wait();
			} catch (InterruptedException e) {}
		}
		usada = true;
		// RECURSO CRITICO
		waiting.add(msg);
		usada = false;
		notifyAll();
	}
	
	private synchronized void extractWaitingMsg(ObjectOutputStream out, String user) throws IOException {
		while(usada){
			try {
				wait();
			} catch (InterruptedException e) {}
		}
		usada = true;
		// RECURSO CRITICO
		Iterator<Mensagem> iterator = waiting.iterator();
		while (iterator.hasNext()) {
		    Mensagem msg = iterator.next();
		    if(msg.getReceptor().equals(user)){
		    	sendMesg(msg);
				iterator.remove();
			}
		}
		usada = false;
		notifyAll();
	}
	
	private class GestorLigacoes extends Thread{
		
		public void run(){
				
			while(!interrupted()){
				try{
					Socket socket = serverSocket.accept();
					ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
					ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
					try{
						Mensagem intro = (Mensagem)in.readObject();
						// Verifica se a mensagem é de INICIO DE LIGACAO
						if(intro.getType() == Mensagem.INTRO){
							String user = intro.getEmissor();
							Ligacao ligacao = new Ligacao(user, socket, in, out);
							if(users.containsKey(user)){
								in.close();
								out.close();
								socket.close();
								System.out.println("> Blocked: " + user);
							}else{
								users.put(user, ligacao);
								ligacao.start();
								// verifica se existem msg a espera
								extractWaitingMsg(out, user);
								System.out.println("> Connected " + user);
							}
						}
					} catch (ClassNotFoundException e) {
						System.out.println("> Error (ClassNotFoundException)");
						socket.close();
					}
				}catch(IOException e){
					System.out.println("> Connection fail");
				}
			}
		}
	
	}
	
	private class Ligacao extends Thread{

		private final String user;
		private Socket socket;
		private ObjectInputStream in;
		private ObjectOutputStream out;
		
		public Ligacao(String user, Socket socket, ObjectInputStream in, ObjectOutputStream out) {
			this.user = user;
			this.socket=socket;
			this.in = in;
			this.out = out;
		}
			
		public void run(){
			try{
				while(!interrupted()){
					
					Mensagem msg = (Mensagem)in.readObject();
					System.out.print(user + "=" + msg.getEmissor() + ">" + msg.getReceptor() + ":" + msg.getTexto());
					
					if(users.containsKey(msg.getReceptor())){
						// Thread Output
						new Thread(){
							
							@Override
							public void run() {
								sendMesg(msg);
							};	
							
						}.start();
						
					}else{
						addWaitingList(msg);
						System.out.println(" - Waiting!" );
					}
				
				}
			} catch (Exception e) {
				System.out.println("> Disconnected: " + user);
			}finally{
				users.remove(user);
				try {
					in.close();
					out.close();
					socket.close();
				} catch (IOException e1) {}
			}
		}
		

		public ObjectOutputStream getOut() {
			return out;
		}
		
	}

	public int getSize() {
		return users.size();
	}
	
	public Collection<String> getList(){
		return users.keySet();
	}
	
	public Collection<Mensagem> getWaitingList() {
		return waiting;
	}
	
}
