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

public class ServerThread extends Thread {//继承

	DataInputStream dis;//数据流
	DataOutputStream dos;

	Socket remoteClient;//要处理的套接字
	Server server;//Server类

	String nickname;//名字
	String[] threadNames;//在线者名字
	String[] userNames;//全部名字
	JList clientList;
	JList userList;

	String ext_pass = ".pass";//用户名和密码，存放信息的后缀
	String ext_msg = ".msg";//离线消息
	String ext_ans = ".ans";//密保问题

	ArrayList<ServerThread> connectedClients; // 所有连接到服务器的客户端列表

	public ServerThread(Socket remoteClient, Server server, ArrayList<ServerThread> connectedClients,
			String[] userNames, String[] threadNames, JList userList, JList clientList) {
		this.remoteClient = remoteClient;
		this.connectedClients = connectedClients;
		this.userNames = userNames;
		this.threadNames = threadNames;
		this.userList = userList;
		this.clientList = clientList;
		try {

			this.dis = new DataInputStream(remoteClient.getInputStream());//从当前连接的客户端得到其发送的消息
			this.dos = new DataOutputStream(remoteClient.getOutputStream());
			this.server = server;

			initUser();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void initUser() {//建立已注册者列表列表
		File dir = new File(System.getProperty("user.dir") + "/");//存放在客户端的当前路径下
		File[] files = dir.listFiles();//当前目录下的所有文件和目录的绝对路径
		if (files != null) {
			for (File file : files) {//遍历
				if (file.isDirectory()) {//文件就不处理
				} else if (file.isFile()) {
					if (file.getName().endsWith(ext_pass)) {//以这个东西结尾
						String line = file.getName();
						String[] line_array = line.split("\\.");//遇到这个标志就切割开
						int i = 0;
						for (; i < server.userNum; i++) {//观察列表中是否已有这个元素
							if (userNames[i].equals(line_array[0])) {
								break;
							}
						}
						if (i == server.userNum) {//没有就添加
							userNames[server.userNum++] = line_array[0];
						}
					}
				}
			}
		}
		userList.repaint();
	}

	public void run() {
		while (true) // 主协议解码循环
		{
			try {
				int mesgType = dis.readInt(); // 先从数据流中读一个整数出来
				System.err.println(mesgType);//打印出来是红色的

				// 根据客户端发送的消息类型进行解码
				switch (mesgType) {
				case ServerConstants.USER_EXIT://用户退出

					for (ServerThread otherClient : connectedClients) {
						if (!otherClient.equals(this)) // 不要将消息发送给发送消息的人
						{//将离开的消息写入将要发送的报文之中
							otherClient.getDos().writeInt(ServerConstants.USER_EXIT);
							otherClient.getDos().writeUTF(this.nickname + ": " + "离开");
						}
					}
					//将该用户从在线者列表中删除
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
					connectedClients.remove(this);//连接列表中移除
					clientList.repaint();//重新显示
					return;
				case ServerConstants.CHAT_MESSAGE://聊天信息
					String data = dis.readUTF();//读入聊天信息
					System.err.println(data);
					// server.getSystemLog().append(remoteClient.getInetAddress()+":"+remoteClient.getPort()+">"+data+"\n");
					for (ServerThread client : connectedClients) {//没必要加一个循环
						if (client.equals(this)) 
						{//加到日志
							server.getSystemLog().append(this.nickname + ": " + data + "\n");
						}
					}
					//将消息写入发送的报文之中
					for (ServerThread otherClient : connectedClients) {
						if (!otherClient.equals(this))//别发送给发送者
						{
							otherClient.getDos().writeInt(ServerConstants.CHAT_BROADCAST);
							otherClient.getDos().writeUTF(this.nickname + ": " + data);
						}
					}

					break;
				case ServerConstants.REGISTER_CLIENT://有窗口打开
					nickname = dis.readUTF();
					server.threadNames[server.threadNum++] = nickname;
					server.clientPane.setViewportView(server.clientList);
					server.getSystemLog().append("Welcome " + nickname +"come in" + "\n");
					// 将注册消息广播出去
					for (ServerThread otherClient : connectedClients) {
						if (!otherClient.equals(this)) // 别发送给发送者
						{
							otherClient.getDos().writeInt(ServerConstants.CHAT_BROADCAST);
							otherClient.getDos().writeUTF("Welcome " + nickname + "\n");
							otherClient.getDos().writeInt(ServerConstants.REGISTER_CLIENT);
							otherClient.getDos().writeUTF(nickname);
						}
					}
//					for (int i = 0; i < server.threadNum - 1; i++) {//将注册后的在线者列表发出去
//						dos.writeInt(ServerConstants.REGISTER_CLIENT);
//						dos.writeUTF(server.threadNames[i]);
//					}
					break;
				case ServerConstants.PRIVATE_MESSAGE:
					//私人消息
					privateMsg();
					break;
				case ServerConstants.PRIVATE_FILE:
					//私人文件
					privateFile();
					break;
				case ServerConstants.USER_REGISTER:
					// 注册
					userRegister();
					break;
				case ServerConstants.USER_LOGIN:
					// 登录
					userLogin();
					break;
				case ServerConstants.USER_PASSWORD_GET_BACK:
					// 密码找回
					passGetBackLogin();
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
	}
//不太懂-------------------------------------
	void privateMsg() {
		try {
			String privateData = dis.readUTF();
			String[] privateDatas = privateData.split(":");//将私人消息用：分割
			boolean found = false;//布尔变量
			for (ServerThread client : connectedClients) {//寻找对的人
				if (privateDatas[0].equals("/" + client.nickname)) 
				{
					client.getDos().writeInt(ServerConstants.PRIVATE_MESSAGE);
					client.getDos().writeUTF(this.nickname + ":" + privateDatas[1]);
					found = true;
					break;
				}
			}
			if (!found) {//没有找到，写成离线消息
				String nickname = privateDatas[0].substring(1);//从名字的第一个字符开始提取，即省略"/"，收件人的名字
				String name = System.getProperty("user.dir") + "/" + nickname + ext_msg;//创建对应的离线消息文件
				RandomAccessFile randomFile = new RandomAccessFile(name, "rw");//创建对应的文件
				// 将写文件指针移到文件尾。
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
			server.udp_port_num++;//发一个文件换一个端口
			for (ServerThread client : connectedClients) {
				if (privateDatas[0].equals("/" + client.nickname)) {// 接受方
					client.getDos().writeInt(ServerConstants.PRIVATE_MESSAGE);
					client.getDos().writeUTF(this.nickname + "发送的文件:" );
					client.getDos().writeInt(ServerConstants.PRIVATE_FILE);
					client.getDos()
							.writeUTF(client.nickname + ":" + privateDatas[1] + ":" + server.udp_port_num + ":recv");
					Thread.sleep(5000);
				}
				if (client.equals(this)) {// 发送方
					client.getDos().writeInt(ServerConstants.PRIVATE_FILE);
					client.getDos()
							.writeUTF(client.nickname + ":" + privateDatas[1] + ":" + server.udp_port_num + ":send");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void userLogin() {//-----------密码部分需要重写
		try {
			String read = dis.readUTF();
			String[] read_array = read.split(":");
			if (read_array.length == 2) {
				String name = System.getProperty("user.dir") + "/" + read_array[0] + ext_pass;//创建一个类似于ws.pass的文件
				if (new File(name).exists()) {//如果文件名存在了
					BufferedReader reader = new BufferedReader(new FileReader(new File(name)));//读取文件内容
					String line = reader.readLine();
					if (line != null) {
//						String pass=new String();
//						java.util.StringTokenizer st=new java.util.StringTokenizer(read_array[1],"%");
//						while(st.hasMoreElements()) { 
//							int asc =Integer.parseInt((String)st.nextElement())-27;
//							pass = pass + (char) asc;
//						}
						BigInteger passInt = new BigInteger(line, 36);// 密码加密
						String pass = new BigInteger(read_array[1].getBytes()).toString(36);
						if (line.equals(pass)) {//密码正确
							//向所有用户广播
							for (ServerThread otherClient : connectedClients) {//相当于两段消息
								otherClient.getDos().writeInt(ServerConstants.NICKNAME_CHANGE);
								otherClient.getDos().writeUTF(nickname + ":" + read_array[0]);
								otherClient.getDos().writeInt(ServerConstants.CHAT_BROADCAST);
								otherClient.getDos().writeUTF("用户登录 : " + read_array[0] + "\n");
							}
							//向刚刚登录的用户发列表
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
									if (new File(msg_name).exists()) {//如果刚刚上线离线消息就存在
										readFileMsg(msg_name, otherClient);
										(new File(msg_name)).delete();//删除已经发送过的离线消息
									}
								}
							}
							for (int i = 0; i < threadNames.length; i++) {//将编号改为用户名
								if (threadNames[i].equals(nickname)) {
									nickname = read_array[0];
									threadNames[i] = read_array[0];
									break;
								}
							}
							for (int i = 0; i < server.threadNum - 1; i++) {//将登录后的在线者列表发出去
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

	void readFileMsg(String msg_name, ServerThread otherClient) {//最好在读取离线文件后将离线文件删除
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
				if (!new File(name).exists()) {//该文件不存在
					userNames[server.userNum++] = line_array[0];//在列表中添加用户名
					userList.repaint();

					OutputStream output = new FileOutputStream(name);
					OutputStream outputanswer = new FileOutputStream(answer);

//					String newPassword=new String();
//					java.util.StringTokenizer st=new java.util.StringTokenizer(line_array[1],"%");
//					while(st.hasMoreElements()) { 
//						int asc =Integer.parseInt((String)st.nextElement())-27;
//						newPassword = newPassword + (char) asc;
//					}
//					output.write(Integer.parseInt(newPassword));// 将密码按字节变为36进制
					output.write(new BigInteger(line_array[1].getBytes()).toString(36).getBytes());// 密码加密
					outputanswer.write(new BigInteger(line_array[2].getBytes()).toString(36).getBytes());// 密码加密
					output.close();
					outputanswer.close();

					//将用户列表发给他
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
						otherClient.getDos().writeUTF("用户注册: " + line_array[0] + "\n");
					}
					for (int i = 0; i < userNames.length; i++) {
						if (userNames[i].equals(nickname)) {//改名字
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
							otherClient.getDos().writeUTF("用户名已存在！\n");
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
				String name = System.getProperty("user.dir") + "/" + read_array[0] + ext_pass;//密码文件
				String answer = System.getProperty("user.dir") + "/" + read_array[0] + ext_ans;//密保问题文件
				if(new File(answer).exists()) {//密保文件存在
					String passanswer = new BigInteger(read_array[2].getBytes()).toString(36);//密保问题答案
					BufferedReader answerreader = new BufferedReader(new FileReader(new File(answer)));//读取文件内容
					String answerline = answerreader.readLine();
					if (answerline.equals(passanswer)) {//密码正确
						if (new File(name).exists()) {//名字存在就处理，不存在不处理
							OutputStream output = new FileOutputStream(name);
							output.write(new BigInteger(read_array[1].getBytes()).toString(36).getBytes());// 密码加密
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
									otherClient.getDos().writeUTF("密码找回成功，已修改\n");
								}
							}
							answerreader.close();
							clientList.repaint();
						}
					}
					else {//密保问题答案错误
						for (ServerThread otherClient : connectedClients) {
							if (otherClient.equals(this)) {					
								otherClient.getDos().writeInt(ServerConstants.CHAT_BROADCAST);
								otherClient.getDos().writeUTF("密码找回失败！\n");
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
