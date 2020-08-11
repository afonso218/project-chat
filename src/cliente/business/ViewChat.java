package cliente.business;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import cliente.shared.Mensagem;


@SuppressWarnings("serial")
public class ViewChat extends JFrame {

	private final int x = 400;
	private final int y = 400;
	
	private Sender sender;
	private String user;
	private Contacto contacto;
	
	private ViewChat thisVista;
	
	private JPanel painel;
	private JPanel painel_bottom;
	private JTextArea texto;
	private JTextField input;
	
	public ViewChat(String user, Contacto contacto, Sender sender) {
		thisVista = this;
		this.user = user;
		this.sender = sender;
		this.contacto = contacto;
		setTitle("From: " + user + " To:" + contacto.toString());
		setResizable(false);
		setSize(400, 400);
		int width = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
		int height = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
		setLocation(width/2-x/2, height/2-y/2);
		
        painel = new JPanel();
		painel.setLayout(new BorderLayout());
		
		configTopPanel();
		configBottomPanel();
		this.getContentPane().add(painel);
	}

	private void configBottomPanel() {
		painel_bottom = new JPanel();
        painel_bottom.setLayout(new BorderLayout());
		input = new JTextField("Type there...");
		input.setPreferredSize(new Dimension(365, 30));
		// Este Mouse Listener apena remove o texto apresentado
		input.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {}
			
			@Override
			public void mousePressed(MouseEvent e) {
				if(input.getText().equals("Type there..."))
					input.setText("");
			}
			
			@Override
			public void mouseExited(MouseEvent e) {}
			
			@Override
			public void mouseEntered(MouseEvent e) {}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				if(input.getText().equals("Type there..."))
					input.setText("");
			}
		});
		input.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(input.getText().trim().length() != 0){
					Mensagem msg = new Mensagem(user, contacto.getNome(), input.getText());
					new Thread(){
						@Override
						public void run() {
							// turn off chat			
							sender.askToSend(msg);
							contacto.addMessage(msg);
							texto.setText(texto.getText() + msg + "\n");
							thisVista.repaint();
						}
					}.start();
					input.setText("");
				}
			}
		});

        painel_bottom.add(input, BorderLayout.WEST);
        
		try {
			JPanel painel_icons = new JPanel();
		
			BufferedImage pic = ImageIO.read(new File("images/photo.png"));
			JButton icon_photo = new JButton(new ImageIcon(pic));
			icon_photo.setPreferredSize(new Dimension(30, 30));
			icon_photo.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO: IMAGE SENDING
					System.out.println("Sending Image ... to do!");
				}
			});
			painel_icons.add(icon_photo, BorderLayout.EAST);
	        painel_bottom.add(painel_icons);
	        
		} catch (IOException e1) {
			System.out.println("Files missing in the directory: images/photo.png");
		}
        
		painel.add(painel_bottom, BorderLayout.SOUTH);
	}

	private void configTopPanel() {
		texto = new JTextArea();
		texto.setEditable(false);
		JScrollPane scroll = new JScrollPane(texto);
		for(Mensagem msg : contacto.getMensagens()){
			texto.setText(texto.getText() + msg + "\n");
		}
		painel.add(scroll, BorderLayout.CENTER);
	}
	
	public void updateConversa() {
		texto.setText("");
		for(Mensagem msg : contacto.getMensagens()){
			texto.setText(texto.getText() + msg + "\n");
		}
	}

	public void start(){
		updateConversa();
		this.setVisible(true);
	}

	public Contacto getContactoActivo(){
		return contacto;
	}

}
