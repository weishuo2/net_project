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
	private static final long serialVersionUID = -2291453973624020582L;//�����������л��汾��
	ServerSocket serverSocket;//�׽��ֽӿ�
	JTextArea systemLog = new JTextArea();//�ı���
	static JScrollPane clientPane = new JScrollPane();//������ͼ
	static JScrollPane userPane = new JScrollPane();
	ArrayList<ServerThread> connectedClients = new ArrayList<ServerThread>();//�������߳�
	static String[] threadNames = { " ", " ", " ", " ", " ", " ", " ", " ", " ", " " };//�����û�
	static String[] userNames = { " ", " ", " ", " ", " ", " ", " ", " ", " ", " " };//�����û�
	int threadNum = 0;
	int userNum = 0;
	int udp_port_num = 4800;//udp����˿ں�
	static JList clientList = new JList(threadNames);//�����û��б�
	static JList userList = new JList(userNames);//�����û��б�

	public Server() {

		// ������������׽���
		try {
			serverSocket = new ServerSocket(5000);//�˿ں�����Ϊ5000��������Ҫ��---------------------------------------
		} catch (IOException e) {
			e.printStackTrace();
		}

		// �򵥵Ľ���
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		clientPane.setPreferredSize(new Dimension(130, 90));//�û��б�
		clientPane.setViewportView(clientList);

		userPane.setPreferredSize(new Dimension(130, 90));//�������б�
		userPane.setViewportView(userList);

		// ��ӵ��û�����
		contentPane.add(userPane, BorderLayout.WEST);
		contentPane.add(new JScrollPane(systemLog), BorderLayout.CENTER);//ϵͳ��־
		contentPane.add(clientPane, BorderLayout.EAST);
		setTitle("Server");//���ñ���
		//����Ļ����
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screenSize = kit.getScreenSize();
		int width = (int) screenSize.getWidth();
		int height = (int) screenSize.getHeight();
		int w = 550;
		int h = 300;
		setSize(w, h);
		setLocation((width - w) / 2, (height - h) / 2);
		// pack();
		setVisible(true);//����Ϊ�ɼ�
	}

	public void start() {//����һ���߳�
		try {
			while (true) //���Ͻ����µĿͻ�
			{
				Socket remoteClient = serverSocket.accept(); //������������ȡ����һ��

				// ����һ���µķ������߳�������ÿһ���ͻ��˵��׽���
				ServerThread st = new ServerThread(remoteClient, this, connectedClients, userNames, threadNames,userList, clientList);
				st.start();

				connectedClients.add(st);//�������б��м�һ���������ӵĿͻ���
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {//����һ����������Ȼ��ʼ����
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
