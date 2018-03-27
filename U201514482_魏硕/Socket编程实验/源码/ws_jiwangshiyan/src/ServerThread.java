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
{//�̳�

	DataInputStream dis;//������
	DataOutputStream dos;
	Socket remoteClient;//Ҫ������׽���
	Server server;//Server��
	String nickname;//����
	String[] onlineNames;//����������
	String[] userNames;//ȫ������
	JList onlineList;
	JList allList;
	ArrayList<ServerThread> connectedClients; // �������ӵ��������Ŀͻ����б�

	public ServerThread(Socket remoteClient, Server server, ArrayList<ServerThread> connectedClients,
			String[] userNames, String[] onlineNames, JList allList, JList onlineList) 
	{
		JButton okLoginButton = new JButton("��¼");
		JButton cancelLoginButton = new JButton("ȡ��");
		JButton okRegisterButton = new JButton("ע��");
		JButton cancelRegisterButton = new JButton("ȡ��");
		JButton okFindpassButton = new JButton("�����һ�");
		JButton cancelFindpassButton = new JButton("ȡ��");
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

			this.dis = new DataInputStream(remoteClient.getInputStream());//�ӵ�ǰ���ӵĿͻ��˵õ��䷢�͵���Ϣ
			this.dos = new DataOutputStream(remoteClient.getOutputStream());
			this.server = server;

			File dir = new File(System.getProperty("user.dir") + "/");//����ڿͻ��˵ĵ�ǰ·����
			File[] files = dir.listFiles();//��ǰĿ¼�µ������ļ���Ŀ¼�ľ���·��
			if (files != null) 
			{
				for (File file : files) 
				{//����
					if (file.isDirectory()) 
					{//�ļ��Ͳ�����
					} 
					else if (file.isFile()) 
					{
						if (file.getName().endsWith(".mima")) 
						{//�����������β
							String line = file.getName();
							String[] line_array = line.split("\\.");//���������־���и
							int i = 0;
							for (; i < server.allNum; i++) 
							{//�۲��б����Ƿ��������Ԫ��
								if (userNames[i].equals(line_array[0])) 
									break;
							}
							if (i == server.allNum) //û�о����
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
		while (true) // ��Э�����ѭ��
		{
			try 
			{
				int mesgType = dis.readInt(); // �ȴ��������ж�һ����������
				// ���ݿͻ��˷��͵���Ϣ���ͽ��н���
				switch (mesgType) 
				{
					case 0://������Ϣ
						String data = dis.readUTF();//����������Ϣ
						for (ServerThread client : connectedClients) 
						{//û��Ҫ��һ��ѭ��
							if (client.equals(this)) //�ӵ���־
								server.systemLog.append(this.nickname + ": " + data + "\n");
						}
						//����Ϣд�뷢�͵ı���֮��
						for (ServerThread otherClient : connectedClients) 
						{
							if (!otherClient.equals(this))//���͸�������
							{
								otherClient.dos.writeInt(2);
								otherClient.dos.writeUTF(this.nickname + ": " + data);
							}
						}
						break;
					case 3:
						//˽����Ϣ
						try 
						{
							String privateData = dis.readUTF();
							String[] privateDatas = privateData.split(":");//��˽����Ϣ�ã��ָ�
							boolean found = false;//��������
							for (ServerThread client : connectedClients) 
							{//Ѱ�ҶԵ���
								if (privateDatas[0].equals("/" + client.nickname)) 
								{
									client.dos.writeInt(3);
									client.dos.writeUTF(this.nickname + ":" + privateDatas[1]);
									found = true;
									break;
								}
							}
							if (!found) 
							{//û���ҵ���д��������Ϣ
								String nickname = privateDatas[0].substring(1);//�����ֵĵ�һ���ַ���ʼ��ȡ����ʡ��"/"���ռ��˵�����
								String name = System.getProperty("user.dir") + "/" + nickname + ".lixian";//������Ӧ��������Ϣ�ļ�
								RandomAccessFile randomFile = new RandomAccessFile(name, "rw");//������Ӧ���ļ�
								// ��д�ļ�ָ���Ƶ��ļ�β��
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
					case 4://�д��ڴ�
						nickname = dis.readUTF();
						server.onlineNames[server.onlineNum++] = nickname;
						server.clientPane.setViewportView(server.onlineList);
						server.systemLog.append("Welcome " + nickname +"come in" + "\n");
						// ��ע����Ϣ�㲥��ȥ
						for (ServerThread otherClient : connectedClients) 
						{
							if (!otherClient.equals(this)) // ���͸�������
							{
								otherClient.dos.writeInt(2);
								otherClient.dos.writeUTF("Welcome " + nickname + "\n");
								otherClient.dos.writeInt(4);
								otherClient.dos.writeUTF(nickname);
							}
						}
						break;
					case 6://�û��˳�
						for (ServerThread otherClient : connectedClients) 
						{
							if (!otherClient.equals(this)) // ��Ҫ����Ϣ���͸�������Ϣ����
							{//���뿪����Ϣд�뽫Ҫ���͵ı���֮��
								otherClient.dos.writeInt(6);
								otherClient.dos.writeUTF(this.nickname + ": " + "�뿪");
							}
						}
						//�����û����������б���ɾ��
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
						connectedClients.remove(this);//�����б����Ƴ�
						onlineList.repaint();//������ʾ
						return;
					case 7:
						// ע��
						zhuce();
						break;
					case 8:
						// ��¼
						denglu();
						break;
					case 9:
						// �����һ�
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
	{//-----------���벿����Ҫ��д
		try 
		{
			String read = dis.readUTF();
			String[] read_array = read.split(":");
			if (read_array.length == 2) 
			{
				String name = System.getProperty("user.dir") + "/" + read_array[0] + ".mima";//����һ��������ws.pass���ļ�
				if (new File(name).exists()) 
				{//����ļ���������
					BufferedReader reader = new BufferedReader(new FileReader(new File(name)));//��ȡ�ļ�����
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
						{//������ȷ
							//�������û��㲥
							for (ServerThread otherClient : connectedClients) 
							{//�൱��������Ϣ
								otherClient.dos.writeInt(10);
								otherClient.dos.writeUTF(nickname + ":" + read_array[0]);
								otherClient.dos.writeInt(2);
								otherClient.dos.writeUTF("�û���¼ : " + read_array[0] + "\n");
							}
							//��ոյ�¼���û����б�
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
									{//����ո�����������Ϣ�ʹ���
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
										(new File(msg_name)).delete();//ɾ���Ѿ����͹���������Ϣ
									}
								}
							}
							for (int i = 0; i < onlineNames.length; i++) 
							{//����Ÿ�Ϊ�û���
								if (onlineNames[i].equals(nickname)) 
								{
									nickname = read_array[0];
									onlineNames[i] = read_array[0];
									break;
								}
							}
							for (int i = 0; i < server.onlineNum - 1; i++) 
							{//����¼����������б���ȥ
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
				{//���ļ�������
					userNames[server.allNum++] = massage_array[0];//���б�������û���
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
					output.write((new BigInteger(massage_array[1].getBytes()).add(mypass)).toString(48).getBytes());// �������
					outputanswer.write((new BigInteger(massage_array[2].getBytes()).add(mypass)).toString(48).getBytes());// �������
					output.close();
					outputanswer.close();
					//���û��б�����
					for (ServerThread otherClient : connectedClients) 
					{
						otherClient.dos.writeInt(2);
						otherClient.dos.writeUTF("�û�ע��: " + massage_array[0] + "\n");
					}
					for (int i = 0; i < userNames.length; i++) 
					{
						if (userNames[i].equals(nickname)) 
						{//������
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
							otherClient.dos.writeUTF("�û����Ѵ��ڣ�\n");
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
				String name = System.getProperty("user.dir") + "/" + massage_array[0] + ".mima";//�����ļ�
				String answer = System.getProperty("user.dir") + "/" + massage_array[0] + ".ans";//�ܱ������ļ�
				if(new File(answer).exists()) 
				{//�ܱ��ļ�����
					String passanswer = (new BigInteger(massage_array[2].getBytes()).add(mypass)).toString(48);//�ܱ������
					BufferedReader answerreader = new BufferedReader(new FileReader(new File(answer)));//��ȡ�ļ�����
					String answerline = answerreader.readLine();
					if (answerline.equals(passanswer)) 
					{//������ȷ
						if (new File(name).exists()) 
						{//���ִ��ھʹ��������ڲ�����
							OutputStream output = new FileOutputStream(name);
							output.write((new BigInteger(massage_array[1].getBytes()).add(mypass)).toString(48).getBytes());// �������
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
									otherClient.dos.writeUTF("�����һسɹ������޸�\n");
								}
							}
							answerreader.close();
							onlineList.repaint();
						}
					}
					else 
					{//�ܱ�����𰸴���
						for (ServerThread otherClient : connectedClients) 
						{
							if (otherClient.equals(this)) 
							{					
								otherClient.dos.writeInt(2);
								otherClient.dos.writeUTF("�����һ�ʧ�ܣ�\n");
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
