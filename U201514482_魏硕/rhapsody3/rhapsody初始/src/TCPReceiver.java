import java.net.*;
import java.io.*;
import java.util.*;

public class TCPReceiver {
	int length;
	ServerSocket listener;
	Socket s;
	String filename, initString;
	byte[] buffer;
	FileOutputStream fileWriter;
	int bytesReceived, bytesToReceive;
	InputStream theInstream;
	OutputStream theOutstream;

	public TCPReceiver(int port) throws IOException {
		// Init stuff
		listener = new ServerSocket(port);
		buffer = new byte[8192];

		System.out.println(" -- Ready to receive file on port: " + port);

		s = listener.accept();
		theInstream = s.getInputStream();
		theOutstream = s.getOutputStream();

		// 1. Wait for a sender to transmit the filename

		length = theInstream.read(buffer);
		initString = "Recieved-" + new String(buffer, 0, length);
		StringTokenizer t = new StringTokenizer(initString, "::");
		filename = t.nextToken();
		bytesToReceive = new Integer(t.nextToken()).intValue();

		System.out.println("  -- The file will be saved as: " + filename);
		System.out.println("  -- Expecting to receive: " + bytesToReceive + " bytes");

		// 2. Send an reply containing OK to the sender
		theOutstream.write((new String("OK")).getBytes());
		System.out.println("send something");

		// 3. Receive the contents of the file
		fileWriter = new FileOutputStream(filename);

		while (bytesReceived < bytesToReceive) {
			length = theInstream.read(buffer);
			fileWriter.write(buffer, 0, length);
			bytesReceived = bytesReceived + length;
		}
		
		fileWriter.close();
		s.close();
		System.out.println("  -- File transfer complete.");
	}
}