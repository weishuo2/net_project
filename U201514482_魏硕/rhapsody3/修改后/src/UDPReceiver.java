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
		// 建立端口
		s = new DatagramSocket(port);
		buffer = new byte[8192];

		System.out.println(" 接受端口: " + port);

		// 等待文件名
		initPacket = receivePacket();

		initString = "Recieved-" + new String(initPacket.getData(), 0, initPacket.getLength());
		StringTokenizer t = new StringTokenizer(initString, "::");//将文件名以：：切割开
		filename = t.nextToken();
		bytesToReceive = new Integer(t.nextToken()).intValue();

		System.out.println("  文件存储名字: " + filename);
		System.out.println("  准备接受 " + bytesToReceive + " bytes");

		// 等待回复OK
		send(initPacket.getAddress(), initPacket.getPort(), (new String("OK")).getBytes());
		System.out.println("发送端口 "+initPacket.getPort());

		//接收文件的具体内容
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
		
		System.out.println("UDPReceiver -- 完成接受");
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