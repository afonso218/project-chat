package cliente.business;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import cliente.util.Tools;


@SuppressWarnings("serial")
public class ViewMain extends JPanel{

	private String user;
	private Sender sender;
	private Map<String, Contacto> contactos;

	private ViewChat vistaChat;
	private JPanel painelBotoes;
	private JLabel connection_status;
	private JList<Contacto> lista;
	private DefaultListModel<Contacto> model;
	private JTabbedPane tab;
	
	public ViewMain(String user, Map<String, Contacto> contactos, Sender sender) {
		this.sender = sender;
		this.user = user;
		this.contactos = contactos;
		this.setLayout(new BorderLayout());
		tab = new JTabbedPane();
		
		// TAB CONTACTO
		ImageIcon icon = null;
		try {
			icon = new ImageIcon(ImageIO.read(new File("images/contactos.gif")));
		} catch (IOException e) {
			System.err.println("Falhou a carregar imagem: images/contactos.gif");
		}
		tab.addTab("Contactos", icon, createViewContactos(),"Todos os seus contactos estão aqui!");
		tab.addTab("Grupos", null, new JPanel(),"Todos os seus grupos estão aqui!");
		this.add(tab);
	}
	
	private JPanel createViewContactos() {
		JPanel painel = new JPanel();
		painel.setLayout(new BorderLayout());
		
		// TODO: POP UP GROUPS
		JPopupMenu pop = new JPopupMenu();
		JMenuItem chatOption = new JMenuItem("Open Chat");
		pop.add(chatOption);
		JMenu groupOption = new JMenu("Add to Group");
		JMenuItem group_default = new JMenuItem("Default Group");
		groupOption.add(group_default);
		pop.add(groupOption);
		
		model = new DefaultListModel<Contacto>();
		for(Contacto c : contactos.values()){
			model.addElement(c);
		}
		lista = new JList<Contacto>(model);
		lista.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {}
			
			@Override
			public void mousePressed(MouseEvent e) {}
			
			@Override
			public void mouseExited(MouseEvent e) {}
			
			@Override
			public void mouseEntered(MouseEvent e) {}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				pop.setVisible(false);
				if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
					openChatContact();
				}
				if(SwingUtilities.isRightMouseButton(e)){	
					pop.setLocation((int)e.getLocationOnScreen().getX(), (int)e.getLocationOnScreen().getY());
					pop.setVisible(true);
				}
			}
		});
		JScrollPane scroll = new JScrollPane(lista);
		painel.add(scroll, BorderLayout.CENTER);
		
		painelBotoes = new JPanel();
		JButton botaoAdd = new JButton("Adiciona Contacto");
		botaoAdd.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String input = JOptionPane.showInputDialog("Insira novo contacto:");
				if(input != null && input.length() >=3){
					input = Tools.formatUserName(input);
					Contacto novoContacto = new Contacto(input);
					contactos.put(novoContacto.getNome(), novoContacto);
					updateContactos();
				}
			}
		});
		
		JButton botaoRemove = new JButton("Remove Contacto");
		botaoRemove.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Contacto contacto = (Contacto)lista.getSelectedValue();
				int input = JOptionPane.showConfirmDialog(null, "Tem a certeza que pretende remover \"" + contacto.getNome() + "\"?");
				if(input == 0){
					contactos.remove(contacto.getNome());
					updateContactos();
				}
			}
		});

		painelBotoes.add(botaoAdd);
		painelBotoes.add(botaoRemove);
		
		try {
			BufferedImage pic = ImageIO.read(new File("images/alert.png"));
			connection_status = new JLabel(new ImageIcon(pic));
			connection_status.setPreferredSize(new Dimension(30, 30));
		} catch (IOException e1) {
			System.out.println("Files missing in the directory: images/alert.png");
		}
		
		painel.add(painelBotoes, BorderLayout.SOUTH);
		return painel;
	}
	
	public void setOnline(boolean connection){
		if(connection){
			painelBotoes.add(connection_status);
		}else{
			painelBotoes.remove(connection_status);
		}
	}
	
	private void openChatContact() {
		Contacto c = (Contacto) lista.getSelectedValue();
		c.setAllMessageRead();
		if(c != null){
			if(vistaChat != null){
				if(!vistaChat.getContactoActivo().getNome().equals(c.getNome())){
					vistaChat.dispose();
					vistaChat = new ViewChat(user, c, sender);
				}
			}else{
				vistaChat = new ViewChat(user, c, sender);
			}
			vistaChat.start();
		}
	}

	public void updateContactos() {
		model.removeAllElements();
		for(Contacto c : contactos.values()){
			model.addElement(c);
		}
		lista.setModel(model);
		
		if(vistaChat != null){
			vistaChat.validate();
			vistaChat.repaint();
		}
		this.validate();
		this.repaint();
	}
	
	public void updateChat(){
		if(vistaChat != null)
			vistaChat.updateConversa();
	}
	
}
