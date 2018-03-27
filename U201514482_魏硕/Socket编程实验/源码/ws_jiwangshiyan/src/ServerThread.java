import java.awt.GridLayout;
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

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;

public class ServerThread extends Thread 
{//继承

	DataInputStream dis;//数据流
	DataOutputStream dos;
	Socket remoteClient;//要处理的套接字
	Server server;//Server类
	String nickname;//名字
	String[] onlineNames;//在线者名字
	String[] userNames;//全部名字
	JList onlineList;
	JList allList;
	ArrayList<ServerThread> connectedClients; // 所有连接到服务器的客户端列表

	public ServerThread(Socket remoteClient, Server server, ArrayList<ServerThread> connectedClients,
			String[] userNames, String[] onlineNames, JList allList, JList onlineList) 
	{
		JButton okLoginButton = new JButton("登录");
		JButton cancelLoginButton = new JButton("取消");
		JButton okRegisterButton = new JButton("注册");
		JButton cancelRegisterButton = new JButton("取消");
		JButton okFindpassButton = new JButton("密码找回");
		JButton cancelFindpassButton = new JButton("取消");
		JPanel panel = new JPanel(new GridLayout(5, 2));
		panel.add(okLoginButton);
		panel.add(cancelLoginButton);
		panel.add(okFindpassButton);
		panel.add(cancelFindpassButton);	
		panel.add(okRegisterButton);
		panel.add(cancelRegisterButton);
		this.remoteClient = remoteClient;
		this.connectedClients = connectedClients;
		this.userNames = userNames;
		this.onlineNames = onlineNames;
		this.allList = allList;
		this.onlineList = onlineList;
		try 
		{

			this.dis = new DataInputStream(remoteClient.getInputStream());//从当前连接的客户端得到其发送的消息
			this.dos = new DataOutputStream(remoteClient.getOutputStream());
			this.server = server;

			File dir = new File(System.getProperty("user.dir") + "/");//存放在客户端的当前路径下
			File[] files = dir.listFiles();//当前目录下的所有文件和目录的绝对路径
			if (files != null) 
			{
				for (File file : files) 
				{//遍历
					if (file.isDirectory()) 
					{//文件就不处理
					} 
					else if (file.isFile()) 
					{
						if (file.getName().endsWith(".mima")) 
						{//以这个东西结尾
							String line = file.getName();
							String[] line_array = line.split("\\.");//遇到这个标志就切割开
							int i = 0;
							for (; i < server.allNum; i++) 
							{//观察列表中是否已有这个元素
								if (userNames[i].equals(line_array[0])) 
									break;
							}
							if (i == server.allNum) //没有就添加
								userNames[server.allNum++] = line_array[0];
						}
					}
				}
			}
			allList.repaint();

		} catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	public void run() 
	{
		while (true) // 主协议解码循环
		{
			try 
			{
				int mesgType = dis.readInt(); // 先从数据流中读一个整数出来
				// 根据客户端发送的消息类型进行解码
				switch (mesgType) 
				{
					case 0://聊天信息
						String data = dis.readUTF();//读入聊天信息
						for (ServerThread client : connectedClients) 
						{//没必要加一个循环
							if (client.equals(this)) //加到日志
								server.systemLog.append(this.nickname + ": " + data + "\n");
						}
						//将消息写入发送的报文之中
						for (ServerThread otherClient : connectedClients) 
						{
							if (!otherClient.equals(this))//别发送给发送者
							{
								otherClient.dos.writeInt(2);
								otherClient.dos.writeUTF(this.nickname + ": " + data);
							}
						}
						break;
					case 3:
						//私人消息
						try 
						{
							String privateData = dis.readUTF();
							String[] privateDatas = privateData.split(":");//将私人消息用：分割
							boolean found = false;//布尔变量
							for (ServerThread client : connectedClients) 
							{//寻找对的人
								if (privateDatas[0].equals("/" + client.nickname)) 
								{
									client.dos.writeInt(3);
									client.dos.writeUTF(this.nickname + ":" + privateDatas[1]);
									found = true;
									break;
								}
							}
							if (!found) 
							{//没有找到，写成离线消息
								String nickname = privateDatas[0].substring(1);//从名字的第一个字符开始提取，即省略"/"，收件人的名字
								String name = System.getProperty("user.dir") + "/" + nickname + ".lixian";//创建对应的离线消息文件
								RandomAccessFile randomFile = new RandomAccessFile(name, "rw");//创建对应的文件
								// 将写文件指针移到文件尾。
								randomFile.seek(randomFile.length());
								randomFile.writeUTF(this.nickname + ":" + privateDatas[1] + "\n");
								randomFile.close();
							}
						} 
						catch (Exception e) 
						{
							e.printStackTrace();
						}
						break;
					case 4://有窗口打开
						nickname = dis.readUTF();
						server.onlineNames[server.onlineNum++] = nickname;
						server.clientPane.setViewportView(server.onlineList);
						server.systemLog.append("Welcome " + nickname +"come in" + "\n");
						// 将注册消息广播出去
						for (ServerThread otherClient : connectedClients) 
						{
							if (!otherClient.equals(this)) // 别发送给发送者
							{
								otherClient.dos.writeInt(2);
								otherClient.dos.writeUTF("Welcome " + nickname + "\n");
								otherClient.dos.writeInt(4);
								otherClient.dos.writeUTF(nickname);
							}
						}
						break;
					case 6://用户退出
						for (ServerThread otherClient : connectedClients) 
						{
							if (!otherClient.equals(this)) // 不要将消息发送给发送消息的人
							{//将离开的消息写入将要发送的报文之中
								otherClient.dos.writeInt(6);
								otherClient.dos.writeUTF(this.nickname + ": " + "离开");
							}
						}
						//将该用户从在线者列表中删除
						for (int i = 0; i < onlineNames.length; i++) 
						{
							if (onlineNames[i].equals(nickname)) 
							{
								onlineNames[i] = " ";
								for (int j = i; j < server.onlineNum - 1; j++) 
									onlineNames[j] = onlineNames[j + 1];
								server.onlineNum--;
								break;
							}
						}
						connectedClients.remove(this);//连接列表中移除
						onlineList.repaint();//重新显示
						return;
					case 7:
						// 注册
						zhuce();
						break;
					case 8:
						// 登录
						denglu();
						break;
					case 9:
						// 密码找回
						zhaohuimima();
						break;
					}
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
					return;
				}
			}
		}
	
	void denglu() 
	{//-----------密码部分需要重写
		try 
		{
			String read = dis.readUTF();
			String[] read_array = read.split(":");
			if (read_array.length == 2) 
			{
				String name = System.getProperty("user.dir") + "/" + read_array[0] + ".mima";//创建一个类似于ws.pass的文件
				if (new File(name).exists()) 
				{//如果文件名存在了
					BufferedReader reader = new BufferedReader(new FileReader(new File(name)));//读取文件内容
					String line = reader.readLine();
					if (line != null) 
					{
						String pass1=new String();
						java.util.StringTokenizer st=new java.util.StringTokenizer(read_array[1],"%");
						while(st.hasMoreElements()) 
						{ 
							int asc =Integer.parseInt((String)st.nextElement())-27;
							pass1 = pass1 + (char) asc;
						}
						BigInteger mypass = new BigInteger("19970826");
						String pass = (new BigInteger(read_array[1].getBytes()).add(mypass)).toString(48);
						if (line.equals(pass)) 
						{//密码正确
							//向所有用户广播
							for (ServerThread otherClient : connectedClients) 
							{//相当于两段消息
								otherClient.dos.writeInt(10);
								otherClient.dos.writeUTF(nickname + ":" + read_array[0]);
								otherClient.dos.writeInt(2);
								otherClient.dos.writeUTF("用户登录 : " + read_array[0] + "\n");
							}
							//向刚刚登录的用户发列表
							for (ServerThread otherClient : connectedClients) 
							{
								if (otherClient.equals(this)) 
								{
									otherClient.dos.writeInt(12);
									String namelist = "";
									for (int i = 0; i < server.allNum; i++) 
									{
										namelist += userNames[i] + ":";
									}
									namelist += ":";
									otherClient.dos.writeUTF(namelist);

									
									String msg_name = System.getProperty("user.dir") + "/" + read_array[0] + ".lixian";
									if (new File(msg_name).exists()) 
									{//如果刚刚上线离线消息就存在
										try 
										{
											RandomAccessFile randomFile = new RandomAccessFile(msg_name, "rw");
											String linex = randomFile.readUTF();
											while (linex != null && !linex.isEmpty()) 
											{
												otherClient.dos.writeInt(3);
												otherClient.dos.writeUTF(linex);
												linex = randomFile.readUTF();
											}
											randomFile.close();
										} 
										catch (Exception e) 
										{
										} finally 
										{
										}
										(new File(msg_name)).delete();//删除已经发送过的离线消息
									}
								}
							}
							for (int i = 0; i < onlineNames.length; i++) 
							{//将编号改为用户名
								if (onlineNames[i].equals(nickname)) 
								{
									nickname = read_array[0];
									onlineNames[i] = read_array[0];
									break;
								}
							}
							for (int i = 0; i < server.onlineNum - 1; i++) 
							{//将登录后的在线者列表发出去
								dos.writeInt(4);
								dos.writeUTF(server.onlineNames[i]);
							}
							onlineList.repaint();
						}
					}
					reader.close();
				}
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	void zhuce() 
	{
		try 
		{
			String massage = dis.readUTF();
			String[] massage_array = massage.split(":");
			BigInteger mypass=new BigInteger("19970826");
			if (massage_array.length == 3) 
			{
				String filename = System.getProperty("user.dir") + "/" + massage_array[0] + ".mima";
				String answer = System.getProperty("user.dir") + "/" + massage_array[0] + ".ans";
				if (!new File(filename).exists()) 
				{//该文件不存在
					userNames[server.allNum++] = massage_array[0];//在列表中添加用户名
					allList.repaint();
					OutputStream output = new FileOutputStream(filename);
					OutputStream outputanswer = new FileOutputStream(answer);
					String newPassword=new String();
					java.util.StringTokenizer st=new java.util.StringTokenizer(massage_array[1],"%");
					while(st.hasMoreElements()) 
					{ 
						int asc =Integer.parseInt((String)st.nextElement())-27;
						newPassword = newPassword + (char) asc;
					}
					output.write((new BigInteger(massage_array[1].getBytes()).add(mypass)).toString(48).getBytes());// 密码加密
					outputanswer.write((new BigInteger(massage_array[2].getBytes()).add(mypass)).toString(48).getBytes());// 密码加密
					output.close();
					outputanswer.close();
					//将用户列表发给他
					for (ServerThread otherClient : connectedClients) 
					{
						otherClient.dos.writeInt(2);
						otherClient.dos.writeUTF("用户注册: " + massage_array[0] + "\n");
					}
					for (int i = 0; i < userNames.length; i++) 
					{
						if (userNames[i].equals(nickname)) 
						{//改名字
							nickname = massage_array[0];
							userNames[i] = massage_array[0];
							break;
						}
					}
				}
				else {
					for (ServerThread otherClient : connectedClients) 
					{
						if (otherClient.equals(this)) 
						{					
							otherClient.dos.writeInt(2);
							otherClient.dos.writeUTF("用户名已存在！\n");
						}
					}
				}
				onlineList.repaint();
				
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	void zhaohuimima() 
	{
		try 
		{
			String massage = dis.readUTF();
			String[] massage_array = massage.split(":");
			BigInteger mypass=new BigInteger("19970826");
			if (massage_array.length == 3) 
			{
				String name = System.getProperty("user.dir") + "/" + massage_array[0] + ".mima";//密码文件
				String answer = System.getProperty("user.dir") + "/" + massage_array[0] + ".ans";//密保问题文件
				if(new File(answer).exists()) 
				{//密保文件存在
					String passanswer = (new BigInteger(massage_array[2].getBytes()).add(mypass)).toString(48);//密保问题答案
					BufferedReader answerreader = new BufferedReader(new FileReader(new File(answer)));//读取文件内容
					String answerline = answerreader.readLine();
					if (answerline.equals(passanswer)) 
					{//密码正确
						if (new File(name).exists()) 
						{//名字存在就处理，不存在不处理
							OutputStream output = new FileOutputStream(name);
							output.write((new BigInteger(massage_array[1].getBytes()).add(mypass)).toString(48).getBytes());// 密码加密
							output.close();
							for (ServerThread otherClient : connectedClients) 
							{
								if (otherClient.equals(this)) 
								{
									String line = "19970826";
									byte[] _line = line.getBytes("ISO-8859-1");
									String truePassword=new String();
									for (int i=0;i<_line.length;i++) 
									{
										int as2=_line[i];
										_line[i]=(byte)(as2+27);
										truePassword = truePassword +(as2 + 27)+ "%";
									}
									otherClient.dos.writeInt(2);
									otherClient.dos.writeUTF("密码找回成功，已修改\n");
								}
							}
							answerreader.close();
							onlineList.repaint();
						}
					}
					else 
					{//密保问题答案错误
						for (ServerThread otherClient : connectedClients) 
						{
							if (otherClient.equals(this)) 
							{					
								otherClient.dos.writeInt(2);
								otherClient.dos.writeUTF("密码找回失败！\n");
							}
						}
						answerreader.close();
						onlineList.repaint();	
					}
				}
			}
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
}
