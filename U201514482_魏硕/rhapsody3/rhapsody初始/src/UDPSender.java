import java.net.*;
import java.io.*;

/**
 * UDPSender is an implementation of the Sender interface, using UDP as the
 * transport protocol. The object is bound to a specified receiver host and port
 * when created, and is able to send the contents of a file to this receiver.
 *
 * @author Alex Andersen (alex@daimi.au.dk)
 */
public class UDPSender implements Sender {
	private File theFile;
	private FileInputStream fileReader;
	private DatagramSocket s;
	private int fileLength, currentPos, bytesRead, toPort;
	private byte[] msg, buffer;
	private String toHost, initReply;
	private InetAddress toAddress;

	/**
	 * Class constructor. Creates a new UDPSender object capable of sending a file
	 * to the specified address and port.
	 *
	 * @param address
	 *            the address of the receiving host
	 * @param port
	 *            the listening port on the receiving host
	 */
	public UDPSender(InetAddress address, int port) throws IOException {
		toPort = port;
		toAddress = address;
		msg = new byte[32768];
		buffer = new byte[32768];
		s = new DatagramSocket();
		s.connect(toAddress , toPort);
		
	}

	/**
	 * Sends a file to the bound host. Reads the contents of the specified file, and
	 * sends it via UDP to the host and port specified at when the object was
	 * created.
	 *
	 * @param theFile
	 *            the file to send
	 */
	public void sendFile(File theFile) throws IOException {
		fileReader = new FileInputStream(theFile);
		fileLength = fileReader.available();

		System.out.println(" -- �ļ���: " + theFile.getName());
		System.out.println(" -- �ļ�����: " + fileLength);

		//�������߷����ļ���
		send((theFile.getName() + "::" + fileLength).getBytes());

		//�ȴ���������Ӧ
		DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
		s.receive(reply);

		//�����ļ�������
		if (new String(reply.getData(), 0, reply.getLength()).equals("OK")) {//���ACK
			System.out.println(" -- Got OK from receiver - sending the file ");
		int flag = 0;
			while (currentPos < fileLength) {//�ѷ�����<�ܳ�
				if(flag == 0)
					bytesRead = fileReader.read(msg);//����msg�У����ش�С
				if (bytesRead != -1) {
					DatagramPacket reply_ = new DatagramPacket(buffer, buffer.length);//�õ��ı��Ĵ���buf
					send(msg);
					s.setSoTimeout(50);
					try {
						s.receive(reply_);//�ձ���
					}catch (SocketTimeoutException e) {
						flag=1;
						continue;
					}
					flag=0;
					if (new String(reply_.getData(), 0, reply_.getLength()).equals("OK")) {//�õ������е�ACK				
						currentPos = currentPos + bytesRead;//��С����
					}
				} else {
					break;
				}
			}
			System.out.println("UDPSender -- UDP�ļ��������.");
		} else {
			System.out.println("Recieved something other than OK... exiting");
		}

		fileReader.close();
		s.close();
	}

	private void send(byte[] message) throws IOException {
		DatagramPacket packet = new DatagramPacket(message, message.length);
		s.send(packet);
	}
}
