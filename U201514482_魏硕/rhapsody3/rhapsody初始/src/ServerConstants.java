public class ServerConstants {
	public static final int CHAT_MESSAGE = 0;//客户端群发消息
	public static final int EXIT_MESSAGE = 1;//退出信息
	public static final int CHAT_BROADCAST = 2;//服务器转发群发消息加,注册也用这个
	public static final int PRIVATE_MESSAGE = 3; // 客户端 单发消息
	public static final int REGISTER_CLIENT = 4; //  注册新的客户端，并将客户的名字发送给服务器；服务器群发注册消息也会加
	public static final int REGISTER_BROADCAST = 5; //  服务器转发群发消息加

	//表示自己当前的状态，会发送出去
	public static final int USER_EXIT = 6;//客户端发 用户退出
	public static final int USER_REGISTER = 7;//客户端发 用户注册
	public static final int USER_LOGIN = 8;//客户端发 用户登录
	public static final int USER_PASSWORD_GET_BACK = 9;//客户端发 用户密码找回
	public static final int NICKNAME_CHANGE = 10;//改名字
	public static final int PRIVATE_FILE = 11;//客户端发 私人文件
	public static final int USER_LIST = 12;//用户列表
	public static final int USER_MSG = 13;//用户信息
}
