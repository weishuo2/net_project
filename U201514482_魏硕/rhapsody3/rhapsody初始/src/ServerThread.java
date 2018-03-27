import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JList;

public class ServerThread extends Thread {//�̳�

	DataInputStream dis;//������
	DataOutputStream dos;

	Socket remoteClient;//Ҫ������׽���
	Server server;//Server��

	String nickname;//����
	String[] threadNames;//����������
	String[] userNames;//ȫ������
	JList clientList;
	JList userList;

	String ext_pass = ".pass";//�û��������룬�����Ϣ�ĺ�׺
	String ext_msg = ".msg";//������Ϣ
	String ext_ans = ".ans";//�ܱ�����

	ArrayList<ServerThread> connectedClients; // �������ӵ��������Ŀͻ����б�

	public ServerThread(Socket remoteClient, Server server, ArrayList<ServerThread> connectedClients,
			String[] userNames, String[] threadNames, JList userList, JList clientList) {
		this.remoteClient = remoteClient;
		this.connectedClients = connectedClients;
		this.userNames = userNames;
		this.threadNames = threadNames;
		this.userList = userList;
		this.clientList = clientList;
		try {

			this.dis = new DataInputStream(remoteClient.getInputStream());//�ӵ�ǰ���ӵĿͻ��˵õ��䷢�͵���Ϣ
			this.dos = new DataOutputStream(remoteClient.getOutputStream());
			this.server = server;

			initUser();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void initUser() {//������ע�����б��б�
		File dir = new File(System.getProperty("user.dir") + "/");//����ڿͻ��˵ĵ�ǰ·����
		File[] files = dir.listFiles();//��ǰĿ¼�µ������ļ���Ŀ¼�ľ���·��
		if (files != null) {
			for (File file : files) {//����
				if (file.isDirectory()) {//�ļ��Ͳ�����
				} else if (file.isFile()) {
					if (file.getName().endsWith(ext_pass)) {//�����������β
						String line = file.getName();
						String[] line_array = line.split("\\.");//���������־���и
						int i = 0;
						for (; i < server.userNum; i++) {//�۲��б����Ƿ��������Ԫ��
							if (userNames[i].equals(line_array[0])) {
								break;
							}
						}
						if (i == server.userNum) {//û�о����
							userNames[server.userNum++] = line_array[0];
						}
					}
				}
			}
		}
		userList.repaint();
	}

	public void run() {
		while (true) // ��Э�����ѭ��
		{
			try {
				int mesgType = dis.readInt(); // �ȴ��������ж�һ����������
				System.err.println(mesgType);//��ӡ�����Ǻ�ɫ��

				// ���ݿͻ��˷��͵���Ϣ���ͽ��н���
				switch (mesgType) {
				case ServerConstants.USER_EXIT://�û��˳�

					for (ServerThread otherClient : connectedClients) {
						if (!otherClient.equals(this)) // ��Ҫ����Ϣ���͸�������Ϣ����
						{//���뿪����Ϣд�뽫Ҫ���͵ı���֮��
							otherClient.getDos().writeInt(ServerConstants.USER_EXIT);
							otherClient.getDos().writeUTF(this.nickname + ": " + "�뿪");
						}
					}
					//�����û����������б���ɾ��
					for (int i = 0; i < threadNames.length; i++) {
						if (threadNames[i].equals(nickname)) {
							threadNames[i] = " ";
							for (int j = i; j < server.threadNum - 1; j++) {
								threadNames[j] = threadNames[j + 1];
							}
							server.threadNum--;
							break;
						}
					}
					connectedClients.remove(this);//�����б����Ƴ�
					clientList.repaint();//������ʾ
					return;
				case ServerConstants.CHAT_MESSAGE://������Ϣ
					String data = dis.readUTF();//����������Ϣ
					System.err.println(data);
					// server.getSystemLog().append(remoteClient.getInetAddress()+":"+remoteClient.getPort()+">"+data+"\n");
					for (ServerThread client : connectedClients) {//û��Ҫ��һ��ѭ��
						if (client.equals(this)) 
						{//�ӵ���־
							server.getSystemLog().append(this.nickname + ": " + data + "\n");
						}
					}
					//����Ϣд�뷢�͵ı���֮��
					for (ServerThread otherClient : connectedClients) {
						if (!otherClient.equals(this))//���͸�������
						{
							otherClient.getDos().writeInt(ServerConstants.CHAT_BROADCAST);
							otherClient.getDos().writeUTF(this.nickname + ": " + data);
						}
					}

					break;
				case ServerConstants.REGISTER_CLIENT://�д��ڴ�
					nickname = dis.readUTF();
					server.threadNames[server.threadNum++] = nickname;
					server.clientPane.setViewportView(server.clientList);
					server.getSystemLog().append("Welcome " + nickname +"come in" + "\n");
					// ��ע����Ϣ�㲥��ȥ
					for (ServerThread otherClient : connectedClients) {
						if (!otherClient.equals(this)) // ���͸�������
						{
							otherClient.getDos().writeInt(ServerConstants.CHAT_BROADCAST);
							otherClient.getDos().writeUTF("Welcome " + nickname + "\n");
							otherClient.getDos().writeInt(ServerConstants.REGISTER_CLIENT);
							otherClient.getDos().writeUTF(nickname);
						}
					}
//					for (int i = 0; i < server.threadNum - 1; i++) {//��ע�����������б���ȥ
//						dos.writeInt(ServerConstants.REGISTER_CLIENT);
//						dos.writeUTF(server.threadNames[i]);
//					}
					break;
				case ServerConstants.PRIVATE_MESSAGE:
					//˽����Ϣ
					privateMsg();
					break;
				case ServerConstants.PRIVATE_FILE:
					//˽���ļ�
					privateFile();
					break;
				case ServerConstants.USER_REGISTER:
					// ע��
					userRegister();
					break;
				case ServerConstants.USER_LOGIN:
					// ��¼
					userLogin();
					break;
				case ServerConstants.USER_PASSWORD_GET_BACK:
					// �����һ�
					passGetBackLogin();
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
	}
//��̫��-------------------------------------
	void privateMsg() {
		try {
			String privateData = dis.readUTF();
			String[] privateDatas = privateData.split(":");//��˽����Ϣ�ã��ָ�
			boolean found = false;//��������
			for (ServerThread client : connectedClients) {//Ѱ�ҶԵ���
				if (privateDatas[0].equals("/" + client.nickname)) 
				{
					client.getDos().writeInt(ServerConstants.PRIVATE_MESSAGE);
					client.getDos().writeUTF(this.nickname + ":" + privateDatas[1]);
					found = true;
					break;
				}
			}
			if (!found) {//û���ҵ���д��������Ϣ
				String nickname = privateDatas[0].substring(1);//�����ֵĵ�һ���ַ���ʼ��ȡ����ʡ��"/"���ռ��˵�����
				String name = System.getProperty("user.dir") + "/" + nickname + ext_msg;//������Ӧ��������Ϣ�ļ�
				RandomAccessFile randomFile = new RandomAccessFile(name, "rw");//������Ӧ���ļ�
				// ��д�ļ�ָ���Ƶ��ļ�β��
				randomFile.seek(randomFile.length());
				randomFile.writeUTF(this.nickname + ":" + privateDatas[1] + "\n");
				randomFile.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void privateFile() {
		try {
			String privateData = dis.readUTF();
			String[] privateDatas = privateData.split(":");
			server.udp_port_num++;//��һ���ļ���һ���˿�
			for (ServerThread client : connectedClients) {
				if (privateDatas[0].equals("/" + client.nickname)) {// ���ܷ�
					client.getDos().writeInt(ServerConstants.PRIVATE_MESSAGE);
					client.getDos().writeUTF(this.nickname + "���͵��ļ�:" );
					client.getDos().writeInt(ServerConstants.PRIVATE_FILE);
					client.getDos()
							.writeUTF(client.nickname + ":" + privateDatas[1] + ":" + server.udp_port_num + ":recv");
					Thread.sleep(5000);
				}
				if (client.equals(this)) {// ���ͷ�
					client.getDos().writeInt(ServerConstants.PRIVATE_FILE);
					client.getDos()
							.writeUTF(client.nickname + ":" + privateDatas[1] + ":" + server.udp_port_num + ":send");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void userLogin() {//-----------���벿����Ҫ��д
		try {
			String read = dis.readUTF();
			String[] read_array = read.split(":");
			if (read_array.length == 2) {
				String name = System.getProperty("user.dir") + "/" + read_array[0] + ext_pass;//����һ��������ws.pass���ļ�
				if (new File(name).exists()) {//����ļ���������
					BufferedReader reader = new BufferedReader(new FileReader(new File(name)));//��ȡ�ļ�����
					String line = reader.readLine();
					if (line != null) {
//						String pass=new String();
//						java.util.StringTokenizer st=new java.util.StringTokenizer(read_array[1],"%");
//						while(st.hasMoreElements()) { 
//							int asc =Integer.parseInt((String)st.nextElement())-27;
//							pass = pass + (char) asc;
//						}
						BigInteger passInt = new BigInteger(line, 36);// �������
						String pass = new BigInteger(read_array[1].getBytes()).toString(36);
						if (line.equals(pass)) {//������ȷ
							//�������û��㲥
							for (ServerThread otherClient : connectedClients) {//�൱��������Ϣ
								otherClient.getDos().writeInt(ServerConstants.NICKNAME_CHANGE);
								otherClient.getDos().writeUTF(nickname + ":" + read_array[0]);
								otherClient.getDos().writeInt(ServerConstants.CHAT_BROADCAST);
								otherClient.getDos().writeUTF("�û���¼ : " + read_array[0] + "\n");
							}
							//��ոյ�¼���û����б�
							for (ServerThread otherClient : connectedClients) {
								if (otherClient.equals(this)) {
									otherClient.getDos().writeInt(ServerConstants.USER_LIST);
									String namelist = "";
									for (int i = 0; i < server.userNum; i++) {
										namelist += userNames[i] + ":";
									}
									namelist += ":";
									otherClient.getDos().writeUTF(namelist);

									
									String msg_name = System.getProperty("user.dir") + "/" + read_array[0] + ext_msg;
									if (new File(msg_name).exists()) {//����ո�����������Ϣ�ʹ���
										readFileMsg(msg_name, otherClient);
										(new File(msg_name)).delete();//ɾ���Ѿ����͹���������Ϣ
									}
								}
							}
							for (int i = 0; i < threadNames.length; i++) {//����Ÿ�Ϊ�û���
								if (threadNames[i].equals(nickname)) {
									nickname = read_array[0];
									threadNames[i] = read_array[0];
									break;
								}
							}
							for (int i = 0; i < server.threadNum - 1; i++) {//����¼����������б���ȥ
								dos.writeInt(ServerConstants.REGISTER_CLIENT);
								dos.writeUTF(server.threadNames[i]);
							}
							clientList.repaint();
						}
					}
					reader.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void readFileMsg(String msg_name, ServerThread otherClient) {//����ڶ�ȡ�����ļ��������ļ�ɾ��
		try {
			RandomAccessFile randomFile = new RandomAccessFile(msg_name, "rw");
			String line = randomFile.readUTF();
			while (line != null && !line.isEmpty()) {
				otherClient.getDos().writeInt(ServerConstants.PRIVATE_MESSAGE);
				otherClient.getDos().writeUTF(line);
				line = randomFile.readUTF();
			}
			randomFile.close();
		} catch (Exception e) {
		} finally {
		}
	}

	void userRegister() {
		try {
			String line = dis.readUTF();
			String[] line_array = line.split(":");
			if (line_array.length == 3) {
				String name = System.getProperty("user.dir") + "/" + line_array[0] + ext_pass;
				String answer = System.getProperty("user.dir") + "/" + line_array[0] + ext_ans;
				if (!new File(name).exists()) {//���ļ�������
					userNames[server.userNum++] = line_array[0];//���б�������û���
					userList.repaint();

					OutputStream output = new FileOutputStream(name);
					OutputStream outputanswer = new FileOutputStream(answer);

//					String newPassword=new String();
//					java.util.StringTokenizer st=new java.util.StringTokenizer(line_array[1],"%");
//					while(st.hasMoreElements()) { 
//						int asc =Integer.parseInt((String)st.nextElement())-27;
//						newPassword = newPassword + (char) asc;
//					}
//					output.write(Integer.parseInt(newPassword));// �����밴�ֽڱ�Ϊ36����
					output.write(new BigInteger(line_array[1].getBytes()).toString(36).getBytes());// �������
					outputanswer.write(new BigInteger(line_array[2].getBytes()).toString(36).getBytes());// �������
					output.close();
					outputanswer.close();

					//���û��б�����
					for (ServerThread otherClient : connectedClients) {
//						if (otherClient.equals(this)) {
//							otherClient.getDos().writeInt(ServerConstants.USER_LIST);
//							String namelist = "";
//							for (int i = 0; i < server.userNum; i++) {
//								namelist += userNames[i] + ":";
//							}
//							namelist += ":";
//							otherClient.getDos().writeUTF(namelist);
//						}
//						otherClient.getDos().writeInt(ServerConstants.NICKNAME_CHANGE);
//						otherClient.getDos().writeUTF(nickname + ":" + line_array[0]);
						otherClient.getDos().writeInt(ServerConstants.CHAT_BROADCAST);
						otherClient.getDos().writeUTF("�û�ע��: " + line_array[0] + "\n");
					}
					for (int i = 0; i < userNames.length; i++) {
						if (userNames[i].equals(nickname)) {//������
							nickname = line_array[0];
							userNames[i] = line_array[0];
							break;
						}
					}
				}
				else {
					for (ServerThread otherClient : connectedClients) {
						if (otherClient.equals(this)) {					
							otherClient.getDos().writeInt(ServerConstants.CHAT_BROADCAST);
							otherClient.getDos().writeUTF("�û����Ѵ��ڣ�\n");
						}
					}
				}
				clientList.repaint();
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void passGetBackLogin() {
		try {
			String read = dis.readUTF();
			String[] read_array = read.split(":");
			if (read_array.length == 3) {
				String name = System.getProperty("user.dir") + "/" + read_array[0] + ext_pass;//�����ļ�
				String answer = System.getProperty("user.dir") + "/" + read_array[0] + ext_ans;//�ܱ������ļ�
				if(new File(answer).exists()) {//�ܱ��ļ�����
					String passanswer = new BigInteger(read_array[2].getBytes()).toString(36);//�ܱ������
					BufferedReader answerreader = new BufferedReader(new FileReader(new File(answer)));//��ȡ�ļ�����
					String answerline = answerreader.readLine();
					if (answerline.equals(passanswer)) {//������ȷ
						if (new File(name).exists()) {//���ִ��ھʹ��������ڲ�����
							OutputStream output = new FileOutputStream(name);
							output.write(new BigInteger(read_array[1].getBytes()).toString(36).getBytes());// �������
							output.close();
							for (ServerThread otherClient : connectedClients) {
								if (otherClient.equals(this)) {
//								otherClient.getDos().writeInt(ServerConstants.CHAT_BROADCAST);
//								byte[] _line = line.getBytes("ISO-8859-1");
//								String truePassword=new String();
//								for (int i=0;i<_line.length;i++) {
//									int as2=_line[i];
//									_line[i]=(byte)(as2+27);
//									truePassword = truePassword +(as2 + 27)+ "%";
//								}
									otherClient.getDos().writeInt(ServerConstants.CHAT_BROADCAST);
									otherClient.getDos().writeUTF("�����һسɹ������޸�\n");
								}
							}
							answerreader.close();
							clientList.repaint();
						}
					}
					else {//�ܱ�����𰸴���
						for (ServerThread otherClient : connectedClients) {
							if (otherClient.equals(this)) {					
								otherClient.getDos().writeInt(ServerConstants.CHAT_BROADCAST);
								otherClient.getDos().writeUTF("�����һ�ʧ�ܣ�\n");
							}
						}
						answerreader.close();
						clientList.repaint();	
					}
				}
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

	public DataOutputStream getDos() {
		return dos;
	}
}
