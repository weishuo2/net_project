import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
//import java.util.Arrays;
import java.util.Base64;
//import java.util.Date;
import java.util.UUID;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
//import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
//import javax.swing.WindowConstants;

public class Client extends JFrame implements ActionListener, Runnable {

	private static final long serialVersionUID = 980389841528802556L;//���л��汾��,���Ӿͱ���

	// �����û��������
	JTextField chatInput = new JTextField();//�ı���,Ĭ��һ��
	JTextArea chatHistory = new JTextArea();//�ı���
	JButton chatMessage = new JButton("����");//��ť
	JButton sendFile = new JButton("�ļ�");
	JButton loginButton = new JButton("��¼");
	JButton registerButton = new JButton("ע��");
	JButton findPasswordButton = new JButton("�һ�����");//��ť
	String[] threadNames = { "�����û�"," ", " ", " ", " ", " ", " ", " ", " ", " ", " " };//��ʼ��Ϊ��
	String[] userNames = { "�����б�"," ", " ", " ", " ", " ", " ", " ", " ", " ", " " };
	JList clientList = new JList(threadNames);//�û��б�
	JList userList = new JList(userNames);
	
	//��������
	JPanel panel = new JPanel(new BorderLayout());//������
	JPanel chat = new JPanel(new BorderLayout());//�������������,���촰��
	JScrollPane clientPane = new JScrollPane();//������ͼ
	JScrollPane userPane = new JScrollPane();
	JPanel pass = new JPanel(new BorderLayout());//��������Ĭ�����û�м��

	int threadNum = 1;
	String privateUser;

	// �����׽��ֺ��Ӵ���
	Socket client;//�ͻ����׽���
	DataInputStream dis;//����������
	DataOutputStream dos;//���������������д���ļ����棬����������

	loginJDialog loginDialog;//����
	registerJDialog registerDialog;
	findpassJDialog findpassDialog;

	JPanel registerPanel = new JPanel(new BorderLayout());//ע�ᴰ��
	JPanel findPasswordPanel = new JPanel(new BorderLayout());//�һ����봰��
	//���õ�¼����Ĳ���
	private JLabel userLabel = new JLabel("�û���: ", JLabel.LEFT);
	JTextField userInput = new JTextField(30);//ָ������
	private JLabel passLabel = new JLabel("����: ", JLabel.LEFT);
	JPasswordField passInput = new JPasswordField(30);
	private JLabel passLabel2 = new JLabel("������: ", JLabel.LEFT);
	JPasswordField passInput2 = new JPasswordField(30);
	private JLabel qustionLabel = new JLabel("�ܱ�����: ", JLabel.LEFT);
	private JLabel qustionText = new JLabel("���ѧ���ǣ�", JLabel.LEFT);
	private JLabel answerLabel = new JLabel("�ܱ���: ", JLabel.LEFT);
	JPasswordField answerInput = new JPasswordField(30);
//�ӽ���İ�ť
	JButton okLoginButton = new JButton("��¼");
	JButton cancelLoginButton = new JButton("ȡ��");
	JButton okRegisterButton = new JButton("ע��");
	JButton cancelRegisterButton = new JButton("ȡ��");
	JButton okFindpassButton = new JButton("�����һ�");
	JButton cancelFindpassButton = new JButton("ȡ��");

	class findpassJDialog extends JDialog {//�һ��������
		JPanel panel = new JPanel(new GridLayout(5, 2));//�и�
		public findpassJDialog(JFrame frame) {
			super(frame, "�����һ�", true);// ʵ����һ��JDialog�����ָ���Ի���ĸ����塢���⡢����
			Container container = getContentPane();// ����һ������
			panel.add(userLabel);
			panel.add(userInput);
			
			panel.add(passLabel2);
			panel.add(passInput2);
			
			panel.add(qustionLabel);
			panel.add(qustionText);
			
			panel.add(answerLabel);
			panel.add(answerInput);

			panel.add(okFindpassButton);
			panel.add(cancelFindpassButton);

			okFindpassButton.addActionListener(new ButtonListener());//���Ӽ�����
			cancelFindpassButton.addActionListener(new ButtonListener());

			container.add(panel, BorderLayout.CENTER);//����ӵ�������ж���

			//������ʾ���ھ���
			Toolkit kit = Toolkit.getDefaultToolkit();//��ȡ���߰�
			Dimension screenSize = kit.getScreenSize();//�����Ļ��С
			int width = (int) screenSize.getWidth();
			int height = (int) screenSize.getHeight();
			int w = 230;
			int h = 120;
			setSize(w, h);
			setLocation((width - w) / 2, (height - h) / 2);
			// pack();
		}
	}

	class loginJDialog extends JDialog {//��¼����
		JPanel panel = new JPanel(new GridLayout(3, 2));

		public loginJDialog(JFrame frame) {
			super(frame, "�û���¼", true);// ʵ����һ��JDialog�����ָ���Ի���ĸ����塢���⡢����
			Container container = getContentPane();// ����һ������
			panel.add(userLabel);
			panel.add(userInput);
			panel.add(passLabel);
			panel.add(passInput);

			panel.add(okLoginButton);
			panel.add(cancelLoginButton);

			okLoginButton.addActionListener(new ButtonListener());
			cancelLoginButton.addActionListener(new ButtonListener());

			container.add(panel, BorderLayout.CENTER);

			Toolkit kit = Toolkit.getDefaultToolkit();
			Dimension screenSize = kit.getScreenSize();
			int width = (int) screenSize.getWidth();
			int height = (int) screenSize.getHeight();
			int w = 230;
			int h = 120;
			setSize(w, h);
			setLocation((width - w) / 2, (height - h) / 2);
			// pack();
		}
	}

	class registerJDialog extends JDialog {//ע�����
		JPanel panel = new JPanel(new GridLayout(5, 2));

		public registerJDialog(JFrame frame) {
			super(frame, "�û�ע��", true);// ʵ����һ��JDialog�����ָ���Ի���ĸ����塢���⡢����
			Container container = getContentPane();// ����һ������
			panel.add(userLabel);
			panel.add(userInput);
			panel.add(passLabel);
			panel.add(passInput);

			panel.add(qustionLabel);
			panel.add(qustionText);
			
			panel.add(answerLabel);
			panel.add(answerInput);
			
			panel.add(okRegisterButton);
			panel.add(cancelRegisterButton);

			okRegisterButton.addActionListener(new ButtonListener());
			cancelRegisterButton.addActionListener(new ButtonListener());

			container.add(panel, BorderLayout.CENTER);

			Toolkit kit = Toolkit.getDefaultToolkit();
			Dimension screenSize = kit.getScreenSize();
			int width = (int) screenSize.getWidth();
			int height = (int) screenSize.getHeight();
			int w = 270;
			int h = 150;
			setSize(w, h);
			setLocation((width - w) / 2, (height - h) / 2);
			// pack();
		}
	}

	public Client() {//�ͻ���
		addWindowListener(new WindowAdapter() {//��������
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);//���ڹر�
				try {//���ܳ���Ĵ���
					dos.writeInt(ServerConstants.USER_EXIT);//���������
					dos.writeUTF(getTitle() + ":" + "exit");//��UTF-8����
					dos.flush();//��������ǿ�������
					Thread.sleep(1000);//��ͣһ��
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});//�رմ��ڵĲ���

		// �����û����沢���ö������������ת
		// ��Ϣ��ť

		Container contentPane = this.getContentPane();
		contentPane.setLayout(new BorderLayout());//�ޱ߽����


		//δ��¼ǰ��������Ŵ���
		int hash = UUID.randomUUID().toString().hashCode();//ǰһ�������Զ�����һ��Ψһ����
		String nickName = "�������_" + (hash > 0 ? hash : -hash);//����û�����guest�������򸺵Ĺ�ϣֵ

		// ��Ӷ�������
		//�ͻ��˷���Ϣ������ʹ�� JList

		clientPane.setPreferredSize(new Dimension(130, 90));//ԭ����130,90
		clientPane.setViewportView(clientList);

		userPane.setPreferredSize(new Dimension(130, 90));//���ô�С
		userPane.setViewportView(userList);//������ͼ
//��ΪJlistû���ṩ���ĵ����Ӧ����
		clientList.addMouseListener(new MouseAdapter() {//����ͻ����б�ĵ��
			public void mouseClicked(MouseEvent evt) {
				JList list = (JList) evt.getSource();//���ز����仯�Ķ��󣬷ֱ����ĸ���ť
				if (evt.getClickCount() == 2) {//˫��
					int index = list.locationToIndex(evt.getPoint());//��ȡλ��
					privateUser = threadNames[index];//˽�������˵�����
					chatInput.setText("/" + privateUser + ": ");
				}
			}
		});

		userList.addMouseListener(new MouseAdapter() {//�����û��б�ĵ��
			public void mouseClicked(MouseEvent evt) {
				JList list = (JList) evt.getSource();
				if (evt.getClickCount() == 2) {//˫��
					int index = list.locationToIndex(evt.getPoint());
					privateUser = userNames[index];//��ȡ�����λ�õ��û���
					chatInput.setText("/" + privateUser + ": ");//����������ʾ��Ӧ���û���Ϣ
				}
			}
		});

		//��½��Ľ���Ĳ��ֹ���
		panel.add(userPane, BorderLayout.WEST);
		panel.add(new JScrollPane(chatHistory), BorderLayout.CENTER);
		panel.add(clientPane, BorderLayout.EAST);

		chat.add(sendFile, BorderLayout.WEST);
		chat.add(chatInput, BorderLayout.CENTER);
		chat.add(chatMessage, BorderLayout.EAST);

		pass.add(registerButton, BorderLayout.EAST);
		pass.add(loginButton, BorderLayout.CENTER);
		pass.add(findPasswordButton, BorderLayout.WEST);

		registerButton.addActionListener(new ButtonListener());
		loginButton.addActionListener(new ButtonListener());
		findPasswordButton.addActionListener(new ButtonListener());

		contentPane.add(pass, BorderLayout.NORTH);
		contentPane.add(panel, BorderLayout.CENTER);
		contentPane.add(chat, BorderLayout.SOUTH);

		setTitle(nickName);//�����ڵı�������Ϊ�Լ������֣��ʼΪһ�����
//����ʾ�Ľ������
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screenSize = kit.getScreenSize();
		int width = (int) screenSize.getWidth();
		int height = (int) screenSize.getHeight();
		int w = 550;
		int h = 300;
		setSize(w, h);
		setLocation((width - w) / 2, (height - h) / 2);

		// pack();
		setVisible(true);//��ʾ����

		chatMessage.addActionListener(this);
		sendFile.addActionListener(this);

		// ���ӵ�����
		try {
			//����������ʱҪ��-------------------------------------
			client = new Socket("202.114.212.191", 5000);//�򱾻�5000�Ŷ˿ڷ�����������localhost
			dis = new DataInputStream(client.getInputStream());//����������
			dos = new DataOutputStream(client.getOutputStream());//���������

			dos.writeInt(ServerConstants.REGISTER_CLIENT);
			dos.writeUTF(nickName);
			dos.flush();

			// ����һ���߳�������ӷ��������͵���Ϣ
			Thread clientThread = new Thread(this);
			clientThread.start();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();//��ӡ�쳣��Ϣ
		}
	}

//��Ӧ��ť���µķ�����������Ϣ���͵�������

	private class ButtonListener implements ActionListener {//ʹ�ýӿ�
		public void actionPerformed(ActionEvent event) {
			// ��Ҫ�
			JButton jb = (JButton) event.getSource();
			if (jb == loginButton) {
				System.out.println("loginButton");//��ӡ���
				loginDialog = new loginJDialog(Client.this);
				loginDialog.setVisible(true);//��ʾ��¼����
			} else if (jb == registerButton) {
				System.out.println("registerButton");
				registerDialog = new registerJDialog(Client.this);
				registerDialog.setVisible(true);
			} else if (jb == findPasswordButton) {
				System.out.println("findPasswordButton");
				findpassDialog = new findpassJDialog(Client.this);
				findpassDialog.setVisible(true);
			} else if (jb == okLoginButton) {
				if (loginDialog != null) {
					System.out.println("okLoginButton");
					callLogin(userInput.getText(), passInput.getText());
					userInput.setText("");
					passInput.setText("");
					loginDialog.dispose();
					loginDialog = null;
				}
			} else if (jb == cancelLoginButton) {
				if (loginDialog != null) {
					System.out.println("cancelLoginButton");
					userInput.setText("");
					passInput.setText("");
					loginDialog.dispose();
					loginDialog = null;
				}
			} else if (jb == okRegisterButton) {
				if (registerDialog != null) {
					System.out.println("okRegisterButton");
					callRegister(userInput.getText(), passInput.getText(),answerInput.getText());//����ע�ắ��
					userInput.setText("");
					passInput.setText("");
					answerInput.setText("");
					registerDialog.dispose();//�ر�
					registerDialog = null;
				}
			} else if (jb == cancelRegisterButton) {
				if (registerDialog != null) {
					System.out.println("cancelRegisterButton");
					userInput.setText("");
					passInput.setText("");
					answerInput.setText("");
					registerDialog.dispose();
					registerDialog = null;
				}
			} else if (jb == okFindpassButton) {
				if (findpassDialog != null) {
					System.out.println("okFindpassButton");
					callFindPass(userInput.getText(),passInput2.getText(),answerInput.getText());//�����ٻغ���
					userInput.setText("");
					answerInput.setText("");
					passInput2.setText("");
					findpassDialog.dispose();//�ر�
					findpassDialog = null;
				}
			} else if (jb == cancelFindpassButton) {
				if (findpassDialog != null) {
					System.out.println("cancelFindpassButton");
					userInput.setText("");
					answerInput.setText("");
					findpassDialog.dispose();
					findpassDialog = null;
				}
			}
		}
	}

	@Override//�Ӹ���̳еĺ���
	public void actionPerformed(ActionEvent event) {
		JButton jb = (JButton) event.getSource();
		if (jb == chatMessage) {//������Ͱ�ť
			sendMsg();
		} else if (jb == sendFile) {//�����ļ�
			JFileChooser fd = new JFileChooser();
			// fd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);//ѡ��Ŀ¼
			fd.showOpenDialog(null);
			File f = fd.getSelectedFile();
			if (f != null) {
				sendFile(f);//���÷����ļ�����
			}
		}
	}

	void sendFile(File file) {//�����ļ��ĺ���
		try {
			String txt = chatInput.getText();//��ȡ�û���
			if (txt != null && !txt.isEmpty()) {
				if (txt.charAt(0) != '/') {//������һ���ַ�
					System.out.println("����˫��ѡ���û�");
				} else {
					String name = Base64.getEncoder().encodeToString(file.getPath().getBytes("UTF-8"));
					//���ļ���BASE64��������
					dos.writeInt(ServerConstants.PRIVATE_FILE);
					dos.writeUTF("/" + privateUser + ": " + name);//���ļ����ͳ�ȥ
					chatInput.setText("/" + privateUser + ": ");
					dos.flush(); // ǿ�Ʒ�����Ϣ
				}
			} else {
				System.out.println("����˫��ѡ���û�");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void sendMsg() {//������Ϣ����
		try {
			String txt = chatInput.getText();
			if (txt != null && !txt.isEmpty()) {
				if (txt.charAt(0) != '/') {
					dos.writeInt(ServerConstants.CHAT_MESSAGE); // Ⱥ����Ϣ
					dos.writeUTF(txt); // ���������û�м���
					chatHistory.append( getTitle() + ": " + txt+ "\n");//��ʾ�������¼��
					chatInput.setText("");
					dos.flush(); //ǿ�������Ϣ
				} else {
					dos.writeInt(ServerConstants.PRIVATE_MESSAGE);//������Ϣ
					dos.writeUTF(txt);
					String[] line_array = txt.split(":");
					chatHistory.append( "�Լ�: " + line_array[1]+ "\n");//��ʾ�������¼��
					chatInput.setText("/" + privateUser + ": ");
					dos.flush(); //ǿ�Ʒ���
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void callRegister(String user, String pass, String answer) {//ע�ắ��
		try {
			dos.writeInt(ServerConstants.USER_REGISTER);
			dos.writeUTF(user + ":" + pass +":" + answer);//д�û���������
			dos.flush();//���������
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void callLogin(String user, String pass) {//��¼����
		try {
			dos.writeInt(ServerConstants.USER_LOGIN);
			dos.writeUTF(user + ":" + pass);
			dos.flush(); // �������
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void callFindPass(String user,String newpass,String answer) {//�һ�����
		try {
			dos.writeInt(ServerConstants.USER_PASSWORD_GET_BACK);
			dos.writeUTF(user + ":" + newpass + ":" + answer);
			dos.flush(); // ���������
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// �������Է���������Ϣ
	@Override
	public void run() {
		while (true) {//��ͣѭ��
			try {
				int messageType = dis.readInt(); // ������Ϣ�����������ж���Ϣ����

				//������Ϣ���ͽ��д���
				switch (messageType) {
				case ServerConstants.CHAT_BROADCAST://������Ϣ��Ⱥ����Ϣ
					chatHistory.append(dis.readUTF() + "\n");//��ʾ�������¼��
					break;
				case ServerConstants.REGISTER_CLIENT://����ע��
					String threadName = dis.readUTF();
					threadNames[threadNum++] = threadName;//�����ע�������
					clientPane.setViewportView(clientList);//������ʾע���
					break;
				case ServerConstants.PRIVATE_MESSAGE://˽����Ϣ
					chatHistory.append(dis.readUTF() + "\n");
					break;
				case ServerConstants.PRIVATE_FILE://�Լ��յ��ļ�
					String line = dis.readUTF();
					receiveFile(line);
					String[] line_array = line.split(":");//�ԣ�Ϊ��ǰ�ǰ���п�Ϊ�ַ�������
					String name = line_array[1].trim();//�Ƶ��հ�
					name = new String(Base64.getDecoder().decode(name), "UTF-8");//���룬�ļ���
					if (line_array[3].equals("send")) {//���ͷ�
						chatHistory.append(line_array[0]+"������ļ�"+name + "\n");
					} else if (line_array[3].equals("recv")) {//���շ�
						chatHistory.append(name + "\n");
					}
					break;
				case ServerConstants.USER_EXIT://�û��˳�
					userExit(dis);
					break;
				case ServerConstants.USER_LIST://�û�����
					userList(dis);
					break;
				case ServerConstants.NICKNAME_CHANGE://�û���¼�Ժ����ָı�
					nicChange(dis);
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
//�����������Ϣ�ĺ���
	void receiveFile(String line) {//�յ��ļ�
		try {
			// guest_1508278710: 20171103174500.jpg:3000:send
			// guest_1172299066: 20171103174500.jpg:3001:recv
			String[] line_array = line.split(":");//�ԣ�Ϊ��ǰ�ǰ���п�Ϊ�ַ�������
			if (line_array.length == 4) {
				//String user = line_array[0];
				String name = line_array[1].trim();//�Ƶ��հ�
				int port = Integer.parseInt(line_array[2]);//�˿ں�
				name = new String(Base64.getDecoder().decode(name), "UTF-8");//���룬�ļ���
				if (line_array[3].equals("send")) {//�����ļ���
					udpSendFile(name, port);//���ĸ���ʾ�˿ں�
				} else if (line_array[3].equals("recv")) {
					udpRecvFile(name + ".udp", port);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void udpRecvFile(String name, int thePort) {//udp���ļ�
		try {
			//InetAddress theAddress = InetAddress.getByName("localhost");//����IP��ַ
			new UDPReceiver(thePort);//���ú���UDPReceiver��֪ͨҪ���ĸ��˿�
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void udpSendFile(String name, int thePort) {//udp���ļ�
		try {
			InetAddress theAddress = InetAddress.getByName("202.144.212.191");//��ô�������IP��ַ-----localhost--------------------------------
			File theFile = new File(name);
			if (theFile.canRead()) {
				long start = System.currentTimeMillis();//��õ�ǰʱ�䣬��λ�Ǻ���
				Sender theSender = new UDPSender(theAddress, thePort);//���ú���UDPSender
				// Sender theSender = new TCPSender(theAddress, thePort);
				theSender.sendFile(theFile);
				long dlt = System.currentTimeMillis() - start;
				if (dlt > 0) {//�����ļ���������
					chatHistory.append("�ļ���������: " + (1000 * theFile.length() / dlt) + " b/s\n");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void userList(DataInputStream dis) {
		try {
			String line = dis.readUTF();
			String[] line_array = line.split(":");
			chatHistory.append("��ע���û���");
			if (line_array.length > 0) {
				for (int i = 0; i < line_array.length; i++) {
					if (!line_array[i].isEmpty()) {
						chatHistory.append(line_array[i] + "  ");
						int j = 1;
						for (; j < userNames.length; j++) {
							if (userNames[j].equals(line_array[i])) {
								break;
							}
						}
						if (j == userNames.length) {
							userNames[i+1] = line_array[i];
						}
					}
				}
				userList.repaint();
			}
			chatHistory.append("\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void userExit(DataInputStream dis) {
		try {
			String line = dis.readUTF();
			String[] line_array = line.split(":");
			if (line_array.length == 2) {
				for (int i = 0; i < threadNames.length; i++) {
					if (threadNames[i].equals(line_array[0])) {
						threadNames[i] = " ";
						for (int j = i; j < threadNames.length - 1; j++) {
							threadNames[j] = threadNames[j + 1];
						}
						break;
					}
				}
				clientList.repaint();
			}
			chatHistory.append(line + "\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void nicChange(DataInputStream dis) {
		try {
			String line = dis.readUTF();
			String[] line_array = line.split(":");
			if (line_array.length == 2) {
				for (int i = 0; i < threadNames.length; i++) {
					if (threadNames[i].equals(line_array[0])) {
						threadNames[i] = line_array[1];
						break;
					}
				}
				if (getTitle().equals(line_array[0])) {
					setTitle(line_array[1]);
				}
				clientList.repaint();
			}
			chatHistory.append(line + "\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Client client = new Client();//����һ���µĿͻ�����
		client.setVisible(true);
	}

}
