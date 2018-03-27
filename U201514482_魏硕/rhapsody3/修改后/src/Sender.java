import java.io.*;

/**
 * 网络文件发件人接口
 *
 */
public interface Sender 
{

	/**
	 * 读取文件内容发送到指定的主机和端口
	 *
	 */
	public void sendFile(File theFile) throws IOException;
	
}