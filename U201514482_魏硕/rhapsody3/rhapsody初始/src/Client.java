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

	private static final long serialVersionUID = 980389841528802556L;//序列化版本号,不加就报错

	// 定义用户界面组件
	JTextField chatInput = new JTextField();//文本框,默认一行
	JTextArea chatHistory = new JTextArea();//文本区
	JButton chatMessage = new JButton("发送");//按钮
	JButton sendFile = new JButton("文件");
	JButton loginButton = new JButton("登录");
	JButton registerButton = new JButton("注册");
	JButton findPasswordButton = new JButton("找回密码");//按钮
	String[] threadNames = { "在线用户"," ", " ", " ", " ", " ", " ", " ", " ", " ", " " };//初始化为空
	String[] userNames = { "好友列表"," ", " ", " ", " ", " ", " ", " ", " ", " ", " " };
	JList clientList = new JList(threadNames);//用户列表
	JList userList = new JList(userNames);
	
	//各个窗口
	JPanel panel = new JPanel(new BorderLayout());//主窗口
	JPanel chat = new JPanel(new BorderLayout());//轻量级面板容器,聊天窗口
	JScrollPane clientPane = new JScrollPane();//滚动视图
	JScrollPane userPane = new JScrollPane();
	JPanel pass = new JPanel(new BorderLayout());//布局器，默认组件没有间距

	int threadNum = 1;
	String privateUser;

	// 定义套接字和子窗口
	Socket client;//客户端套接字
	DataInputStream dis;//输入数据流
	DataOutputStream dos;//输出数据流，可以写到文件里面，看定向到哪里

	loginJDialog loginDialog;//窗口
	registerJDialog registerDialog;
	findpassJDialog findpassDialog;

	JPanel registerPanel = new JPanel(new BorderLayout());//注册窗口
	JPanel findPasswordPanel = new JPanel(new BorderLayout());//找回密码窗口
	//设置登录界面的参数
	private JLabel userLabel = new JLabel("用户名: ", JLabel.LEFT);
	JTextField userInput = new JTextField(30);//指定列数
	private JLabel passLabel = new JLabel("密码: ", JLabel.LEFT);
	JPasswordField passInput = new JPasswordField(30);
	private JLabel passLabel2 = new JLabel("新密码: ", JLabel.LEFT);
	JPasswordField passInput2 = new JPasswordField(30);
	private JLabel qustionLabel = new JLabel("密保问题: ", JLabel.LEFT);
	private JLabel qustionText = new JLabel("你的学号是？", JLabel.LEFT);
	private JLabel answerLabel = new JLabel("密保答案: ", JLabel.LEFT);
	JPasswordField answerInput = new JPasswordField(30);
//子界面的按钮
	JButton okLoginButton = new JButton("登录");
	JButton cancelLoginButton = new JButton("取消");
	JButton okRegisterButton = new JButton("注册");
	JButton cancelRegisterButton = new JButton("取消");
	JButton okFindpassButton = new JButton("密码找回");
	JButton cancelFindpassButton = new JButton("取消");

	class findpassJDialog extends JDialog {//找回密码界面
		JPanel panel = new JPanel(new GridLayout(5, 2));//切割
		public findpassJDialog(JFrame frame) {
			super(frame, "密码找回", true);// 实例化一个JDialog类对象，指定对话框的父窗体、标题、类型
			Container container = getContentPane();// 创建一个容器
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

			okFindpassButton.addActionListener(new ButtonListener());//增加监听者
			cancelFindpassButton.addActionListener(new ButtonListener());

			container.add(panel, BorderLayout.CENTER);//将添加的组件居中对齐

			//控制显示窗口居中
			Toolkit kit = Toolkit.getDefaultToolkit();//获取工具包
			Dimension screenSize = kit.getScreenSize();//获得屏幕大小
			int width = (int) screenSize.getWidth();
			int height = (int) screenSize.getHeight();
			int w = 230;
			int h = 120;
			setSize(w, h);
			setLocation((width - w) / 2, (height - h) / 2);
			// pack();
		}
	}

	class loginJDialog extends JDialog {//登录界面
		JPanel panel = new JPanel(new GridLayout(3, 2));

		public loginJDialog(JFrame frame) {
			super(frame, "用户登录", true);// 实例化一个JDialog类对象，指定对话框的父窗体、标题、类型
			Container container = getContentPane();// 创建一个容器
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

	class registerJDialog extends JDialog {//注册界面
		JPanel panel = new JPanel(new GridLayout(5, 2));

		public registerJDialog(JFrame frame) {
			super(frame, "用户注册", true);// 实例化一个JDialog类对象，指定对话框的父窗体、标题、类型
			Container container = getContentPane();// 创建一个容器
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

	public Client() {//客户端
		addWindowListener(new WindowAdapter() {//监听窗口
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);//窗口关闭
				try {//可能出错的代码
					dos.writeInt(ServerConstants.USER_EXIT);//输出数据流
					dos.writeUTF(getTitle() + ":" + "exit");//用UTF-8编码
					dos.flush();//将流数据强制性清空
					Thread.sleep(1000);//暂停一秒
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});//关闭窗口的操作

		// 创建用户界面并设置动作侦查器的跳转
		// 信息按钮

		Container contentPane = this.getContentPane();
		contentPane.setLayout(new BorderLayout());//无边界界面


		//未登录前用主机编号代替
		int hash = UUID.randomUUID().toString().hashCode();//前一部分是自动生成一个唯一主键
		String nickName = "主机编号_" + (hash > 0 ? hash : -hash);//存的用户名用guest加上正或负的哈希值

		// 添加额外的组件
		//客户端发消息，建议使用 JList

		clientPane.setPreferredSize(new Dimension(130, 90));//原来是130,90
		clientPane.setViewportView(clientList);

		userPane.setPreferredSize(new Dimension(130, 90));//设置大小
		userPane.setViewportView(userList);//创建视图
//因为Jlist没有提供鼠标的点击响应函数
		clientList.addMouseListener(new MouseAdapter() {//处理客户端列表的点击
			public void mouseClicked(MouseEvent evt) {
				JList list = (JList) evt.getSource();//返回产生变化的对象，分辨是哪个按钮
				if (evt.getClickCount() == 2) {//双击
					int index = list.locationToIndex(evt.getPoint());//获取位置
					privateUser = threadNames[index];//私人聊天人的名字
					chatInput.setText("/" + privateUser + ": ");
				}
			}
		});

		userList.addMouseListener(new MouseAdapter() {//处理用户列表的点击
			public void mouseClicked(MouseEvent evt) {
				JList list = (JList) evt.getSource();
				if (evt.getClickCount() == 2) {//双击
					int index = list.locationToIndex(evt.getPoint());
					privateUser = userNames[index];//获取被点击位置的用户名
					chatInput.setText("/" + privateUser + ": ");//在输入栏显示相应的用户信息
				}
			}
		});

		//登陆后的界面的布局构架
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

		setTitle(nickName);//将窗口的标题设置为自己的名字，最开始为一个编号
//将显示的界面居中
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screenSize = kit.getScreenSize();
		int width = (int) screenSize.getWidth();
		int height = (int) screenSize.getHeight();
		int w = 550;
		int h = 300;
		setSize(w, h);
		setLocation((width - w) / 2, (height - h) / 2);

		// pack();
		setVisible(true);//显示出来

		chatMessage.addActionListener(this);
		sendFile.addActionListener(this);

		// 连接到主机
		try {
			//换其他电脑时要改-------------------------------------
			client = new Socket("202.114.212.191", 5000);//向本机5000号端口发出连接请求localhost
			dis = new DataInputStream(client.getInputStream());//输入数据流
			dos = new DataOutputStream(client.getOutputStream());//输出数据流

			dos.writeInt(ServerConstants.REGISTER_CLIENT);
			dos.writeUTF(nickName);
			dos.flush();

			// 定义一个线程来处理从服务器发送的消息
			Thread clientThread = new Thread(this);
			clientThread.start();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();//打印异常信息
		}
	}

//相应按钮按下的方法，并将消息发送到服务器

	private class ButtonListener implements ActionListener {//使用接口
		public void actionPerformed(ActionEvent event) {
			// 需要填补
			JButton jb = (JButton) event.getSource();
			if (jb == loginButton) {
				System.out.println("loginButton");//打印这个
				loginDialog = new loginJDialog(Client.this);
				loginDialog.setVisible(true);//显示登录界面
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
					callRegister(userInput.getText(), passInput.getText(),answerInput.getText());//调用注册函数
					userInput.setText("");
					passInput.setText("");
					answerInput.setText("");
					registerDialog.dispose();//关闭
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
					callFindPass(userInput.getText(),passInput2.getText(),answerInput.getText());//调用召回函数
					userInput.setText("");
					answerInput.setText("");
					passInput2.setText("");
					findpassDialog.dispose();//关闭
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

	@Override//从父类继承的函数
	public void actionPerformed(ActionEvent event) {
		JButton jb = (JButton) event.getSource();
		if (jb == chatMessage) {//点击发送按钮
			sendMsg();
		} else if (jb == sendFile) {//发送文件
			JFileChooser fd = new JFileChooser();
			// fd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);//选择目录
			fd.showOpenDialog(null);
			File f = fd.getSelectedFile();
			if (f != null) {
				sendFile(f);//调用发送文件函数
			}
		}
	}

	void sendFile(File file) {//发送文件的函数
		try {
			String txt = chatInput.getText();//获取用户名
			if (txt != null && !txt.isEmpty()) {
				if (txt.charAt(0) != '/') {//检索第一个字符
					System.out.println("请先双击选择用户");
				} else {
					String name = Base64.getEncoder().encodeToString(file.getPath().getBytes("UTF-8"));
					//将文件用BASE64方法加密
					dos.writeInt(ServerConstants.PRIVATE_FILE);
					dos.writeUTF("/" + privateUser + ": " + name);//将文件发送出去
					chatInput.setText("/" + privateUser + ": ");
					dos.flush(); // 强制发送消息
				}
			} else {
				System.out.println("请先双击选择用户");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void sendMsg() {//发送信息函数
		try {
			String txt = chatInput.getText();
			if (txt != null && !txt.isEmpty()) {
				if (txt.charAt(0) != '/') {
					dos.writeInt(ServerConstants.CHAT_MESSAGE); // 群发消息
					dos.writeUTF(txt); // 聊天的内容没有加密
					chatHistory.append( getTitle() + ": " + txt+ "\n");//显示在聊天记录中
					chatInput.setText("");
					dos.flush(); //强制清空信息
				} else {
					dos.writeInt(ServerConstants.PRIVATE_MESSAGE);//单独消息
					dos.writeUTF(txt);
					String[] line_array = txt.split(":");
					chatHistory.append( "自己: " + line_array[1]+ "\n");//显示在聊天记录中
					chatInput.setText("/" + privateUser + ": ");
					dos.flush(); //强制发送
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void callRegister(String user, String pass, String answer) {//注册函数
		try {
			dos.writeInt(ServerConstants.USER_REGISTER);
			dos.writeUTF(user + ":" + pass +":" + answer);//写用户名和密码
			dos.flush();//清空数据流
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void callLogin(String user, String pass) {//登录函数
		try {
			dos.writeInt(ServerConstants.USER_LOGIN);
			dos.writeUTF(user + ":" + pass);
			dos.flush(); // 清空数据
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void callFindPass(String user,String newpass,String answer) {//找回密码
		try {
			dos.writeInt(ServerConstants.USER_PASSWORD_GET_BACK);
			dos.writeUTF(user + ":" + newpass + ":" + answer);
			dos.flush(); // 清空数据流
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 处理来自服务器的消息
	@Override
	public void run() {
		while (true) {//不停循环
			try {
				int messageType = dis.readInt(); // 接受消息，依据整数判断消息类型

				//依据消息类型进行处理
				switch (messageType) {
				case ServerConstants.CHAT_BROADCAST://离线消息或群发消息
					chatHistory.append(dis.readUTF() + "\n");//显示在聊天记录中
					break;
				case ServerConstants.REGISTER_CLIENT://有人注册
					String threadName = dis.readUTF();
					threadNames[threadNum++] = threadName;//添加新注册的名字
					clientPane.setViewportView(clientList);//重新显示注册表
					break;
				case ServerConstants.PRIVATE_MESSAGE://私人消息
					chatHistory.append(dis.readUTF() + "\n");
					break;
				case ServerConstants.PRIVATE_FILE://自己收到文件
					String line = dis.readUTF();
					receiveFile(line);
					String[] line_array = line.split(":");//以：为标记把前后切开为字符串数组
					String name = line_array[1].trim();//移掉空白
					name = new String(Base64.getDecoder().decode(name), "UTF-8");//解码，文件名
					if (line_array[3].equals("send")) {//发送方
						chatHistory.append(line_array[0]+"传输的文件"+name + "\n");
					} else if (line_array[3].equals("recv")) {//接收方
						chatHistory.append(name + "\n");
					}
					break;
				case ServerConstants.USER_EXIT://用户退出
					userExit(dis);
					break;
				case ServerConstants.USER_LIST://用户加入
					userList(dis);
					break;
				case ServerConstants.NICKNAME_CHANGE://用户登录以后，名字改变
					nicChange(dis);
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
//处理服务器消息的函数
	void receiveFile(String line) {//收到文件
		try {
			// guest_1508278710: 20171103174500.jpg:3000:send
			// guest_1172299066: 20171103174500.jpg:3001:recv
			String[] line_array = line.split(":");//以：为标记把前后切开为字符串数组
			if (line_array.length == 4) {
				//String user = line_array[0];
				String name = line_array[1].trim();//移掉空白
				int port = Integer.parseInt(line_array[2]);//端口号
				name = new String(Base64.getDecoder().decode(name), "UTF-8");//解码，文件名
				if (line_array[3].equals("send")) {//接收文件方
					udpSendFile(name, port);//第四个表示端口号
				} else if (line_array[3].equals("recv")) {
					udpRecvFile(name + ".udp", port);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void udpRecvFile(String name, int thePort) {//udp收文件
		try {
			//InetAddress theAddress = InetAddress.getByName("localhost");//主机IP地址
			new UDPReceiver(thePort);//调用函数UDPReceiver，通知要打开哪个端口
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void udpSendFile(String name, int thePort) {//udp发文件
		try {
			InetAddress theAddress = InetAddress.getByName("202.144.212.191");//获得传输对象的IP地址-----localhost--------------------------------
			File theFile = new File(name);
			if (theFile.canRead()) {
				long start = System.currentTimeMillis();//获得当前时间，单位是毫秒
				Sender theSender = new UDPSender(theAddress, thePort);//调用函数UDPSender
				// Sender theSender = new TCPSender(theAddress, thePort);
				theSender.sendFile(theFile);
				long dlt = System.currentTimeMillis() - start;
				if (dlt > 0) {//计算文件传输速率
					chatHistory.append("文件传输速率: " + (1000 * theFile.length() / dlt) + " b/s\n");
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
			chatHistory.append("已注册用户：");
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
		Client client = new Client();//创建一个新的客户端类
		client.setVisible(true);
	}

}
