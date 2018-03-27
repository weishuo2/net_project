import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Server extends JFrame {
	private static final long serialVersionUID = -2291453973624020582L;//服务器的序列化版本号
	ServerSocket serverSocket;//套接字接口
	JTextArea systemLog = new JTextArea();//文本区
	static JScrollPane clientPane = new JScrollPane();//滚动视图
	static JScrollPane userPane = new JScrollPane();
	ArrayList<ServerThread> connectedClients = new ArrayList<ServerThread>();//连接者线程
	static String[] threadNames = { " ", " ", " ", " ", " ", " ", " ", " ", " ", " " };//在线用户
	static String[] userNames = { " ", " ", " ", " ", " ", " ", " ", " ", " ", " " };//所有用户
	int threadNum = 0;
	int userNum = 0;
	int udp_port_num = 4800;//udp传输端口号
	static JList clientList = new JList(threadNames);//在线用户列表
	static JList userList = new JList(userNames);//所有用户列表

	public Server() {

		// 构造服务器的套接字
		try {
			serverSocket = new ServerSocket(5000);//端口号设置为5000，换电脑要改---------------------------------------
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 简单的界面
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		clientPane.setPreferredSize(new Dimension(130, 90));//用户列表
		clientPane.setViewportView(clientList);

		userPane.setPreferredSize(new Dimension(130, 90));//所有人列表
		userPane.setViewportView(userList);

		// 添加到用户界面
		contentPane.add(userPane, BorderLayout.WEST);
		contentPane.add(new JScrollPane(systemLog), BorderLayout.CENTER);//系统日志
		contentPane.add(clientPane, BorderLayout.EAST);
		setTitle("Server");//设置标题
		//将屏幕居中
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screenSize = kit.getScreenSize();
		int width = (int) screenSize.getWidth();
		int height = (int) screenSize.getHeight();
		int w = 550;
		int h = 300;
		setSize(w, h);
		setLocation((width - w) / 2, (height - h) / 2);
		// pack();
		setVisible(true);//设置为可见
	}

	public void start() {//启用一个线程
		try {
			while (true) //不断接受新的客户
			{
				Socket remoteClient = serverSocket.accept(); //从连接请求中取出来一个

				// 构造一个新的服务器线程来处理每一个客户端的套接字
				ServerThread st = new ServerThread(remoteClient, this, connectedClients, userNames, threadNames,userList, clientList);
				st.start();

				connectedClients.add(st);//在连接列表中加一个请求连接的客户端
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {//创建一个服务器，然后开始工作
		Server server = new Server();
		server.start();
	}

	public JTextArea getSystemLog() {
		return systemLog;
	}

	public void setSystemLog(JTextArea systemLog) {
		this.systemLog = systemLog;
	}
}
