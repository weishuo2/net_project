import java.io.*;

/**
 * �����ļ������˽ӿ�
 *
 */
public interface Sender 
{

	/**
	 * ��ȡ�ļ����ݷ��͵�ָ���������Ͷ˿�
	 *
	 */
	public void sendFile(File theFile) throws IOException;
	
}