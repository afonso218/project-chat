package cliente.business;

import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import cliente.shared.Mensagem;
import cliente.util.Tools;

public class Cliente {

	public static final int PORTO = 8080;
	private final String dir = "data/";
	private final int x = 500;
	private final int y = 500;
	
	// Janela
	private JFrame window;
	private ViewMain vistaContactos;

	// Connection
	private Socket socket;
	private ObjectOutputStream out;
	private Thread connection;
	private Sender sender;
	
	// User Information
	private String user;
	private Map<String,Contacto> contactos;
	
	public Cliente() {
		
		autenticacao();
				
		window = new JFrame("Chat App (" + user + ")");
		window.setSize(x, y);
		window.setResizable(false);
		int width = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
		int height = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
		window.setLocation(width/2-x/2, height/2-y/2);
		window.addWindowListener(new WindowAdapter() {
		    @Override
		    public void windowClosing(WindowEvent e) {
				save();
		     	System.exit(0);
		    }
		});
		
		load();
		sender = new Sender();
		connection = new Thread(){
			@Override
			public void run() {
				while(!interrupted()){
					if(socket == null || socket.isClosed()){
						try {
							InetAddress address = InetAddress.getByName(null);
							socket = new Socket(address, PORTO);
							System.out.println("> Connecting to Server, " + address);
							out = new ObjectOutputStream(socket.getOutputStream());
							Mensagem intro = new Mensagem(user);
							out.writeObject(intro);
							System.out.println("> Connected to Server, " + address);
							update();
							new InputClienteListener(new ObjectInputStream(socket.getInputStream())).start();
							sender.setOut(out);
						} catch (Exception e) {
							if(socket != null){
								sender.setOff();
								try {
									socket.close();
								} catch (IOException e1) {}
							}
						}
					}
					try {
						sleep(2500);
					} catch (InterruptedException e) {
						System.out.println("> Error: Thread Fail to Sleep");
					}
				}
			}
		};

		vistaContactos = new ViewMain(user, contactos, sender);
		window.getContentPane().add(vistaContactos);
		
	}

	private void autenticacao() {
		do{
			String input = JOptionPane.showInputDialog(null, "Nome do utilizador: (Min 3 chars)");
			user = Tools.formatUserName(input);
		}while(user.length() <= 3);
	}
	
	@SuppressWarnings("resource")
	private void load(){
		contactos = new HashMap<String, Contacto>();
		try{
			File file = new File(dir + user + "/contactos.data");
			if (file.exists()) {
				System.out.println("> Loading Files...");
				Scanner scanner = new Scanner(file);
				while(scanner.hasNext()){
					String nome = scanner.next();
					Contacto c = new Contacto(nome);
					
					// Carregar Conversas do contacto
					File file_contacto = new File(dir + user + "/" + nome + ".data");
					if(file_contacto.exists()){
						Scanner scanner_msgs = new Scanner(file_contacto);
						while(scanner_msgs.hasNext()){
							String[] msg_raw = scanner_msgs.nextLine().split("::");
							if(msg_raw.length == 3){
								Mensagem msg = new Mensagem(msg_raw[0], msg_raw[1],msg_raw[2]);
								msg.lida();
								c.addMessage(msg);	
							}else{
								throw new Exception("Cliente (load): Mensagem com formato invalido, " + file_contacto);
							}
						}
						contactos.put(c.getNome(), c);
						scanner_msgs.close();
					}
				}
				scanner.close();
			}
		}catch(Exception e){
			System.out.println("> Error (load): Fail to load file (data broken)");
		}
	}
	
	private void save(){
		try{
			File file = new File(dir + user + "/contactos.data");
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				System.out.println("> Saving File...");
			}
			PrintWriter writer = new PrintWriter(file, "ISO-8859-1");
			for(Contacto c : contactos.values()){
				writer.write(c.getNome() + "\n");
				PrintWriter writer_msgs = new PrintWriter(dir + user+ "/" + c.getNome() + ".data", "ISO-8859-1");
				for(Mensagem m : c.getMensagens()){
					writer_msgs.write(m.toFormat()+"\n");
				}
				writer_msgs.flush();
				writer_msgs.close();
			}
			writer.flush();
			writer.close();
		}catch(IOException i){
			System.out.println("> Error (save): Fail to save file.");
		}
	}
	
	private class InputClienteListener extends Thread {
		
		private ObjectInputStream  in;

		public InputClienteListener(ObjectInputStream in) {
			this.in = in;
		}

		public void run() {
			try {
				while (!interrupted()){
					try {
						Mensagem m = (Mensagem)in.readObject();
						if(contactos.containsKey(m.getEmissor())){
							contactos.get(m.getEmissor()).addMessage(m);
							System.out.println("New Msg: " + m);
						}else{
							Contacto novo = new Contacto(m.getEmissor());
							novo.addMessage(m);
							contactos.put(novo.getNome(), novo);
							System.out.println("New Msg/ New Contact: " + m);	
						}
						update();
					} catch (ClassNotFoundException e) {
						sender.setOff();
						System.out.println("> Error: InputClienteListener > ClassNotFoundException");
					}
				}
			} catch (IOException e) {
			} finally {
				sender.setOff();
				System.out.println("> Connection Fail");
				try {
					in.close();
					socket.close();
				} catch (IOException e1) {}
			}
		}
	}
	
	private void update(){
		vistaContactos.updateContactos();
		vistaContactos.updateChat();
		window.validate();
		window.repaint();
	}
	
	public void start() {
		connection.start();
		window.setVisible(true);
	}

}
