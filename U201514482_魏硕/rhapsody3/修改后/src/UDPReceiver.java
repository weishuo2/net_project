import java.net.*;
import java.io.*;
import java.util.*;

public class UDPReceiver 
{
	DatagramSocket s;
	String filename, initString;
	byte[] buffer;
	DatagramPacket initPacket, receivedPacket;
	FileOutputStream fileWriter;
	int bytesReceived, bytesToReceive;

	public UDPReceiver(int port) throws IOException 
	{
		// �����˿�
		s = new DatagramSocket(port);
		buffer = new byte[8192];

		System.out.println(" ���ܶ˿�: " + port);

		// �ȴ��ļ���
		initPacket = receivePacket();

		initString = "Recieved-" + new String(initPacket.getData(), 0, initPacket.getLength());
		StringTokenizer t = new StringTokenizer(initString, "::");//���ļ����ԣ����и
		filename = t.nextToken();
		bytesToReceive = new Integer(t.nextToken()).intValue();

		System.out.println("  �ļ��洢����: " + filename);
		System.out.println("  ׼������ " + bytesToReceive + " bytes");

		// �ȴ��ظ�OK
		send(initPacket.getAddress(), initPacket.getPort(), (new String("OK")).getBytes());
		System.out.println("���Ͷ˿� "+initPacket.getPort());

		//�����ļ��ľ�������
		fileWriter = new FileOutputStream(filename);

		while (bytesReceived < bytesToReceive) 
		{
			initPacket = receivePacket();
			send(initPacket.getAddress(), initPacket.getPort(), (new String("OK")).getBytes());
			receivedPacket = receivePacket();
			fileWriter.write(receivedPacket.getData(), 0, receivedPacket.getLength());
			bytesReceived = bytesReceived + receivedPacket.getLength();
		}
		
		fileWriter.close();
		s.close();
		
		System.out.println("UDPReceiver -- ��ɽ���");
	}

	public DatagramPacket receivePacket() throws IOException 
	{

		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		s.receive(packet);

		return packet;
	}

	public byte[] receiveData() throws IOException 
	{

		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		s.receive(packet);

		return packet.getData();
	}

	public void send(InetAddress recv, int port, byte[] message) throws IOException 
	{

		// InetAddress recv = InetAddress.getByName(host);
		DatagramPacket packet = new DatagramPacket(message, message.length, recv, port);
		s.send(packet);
	}

}