
package net.minelord.util.IRC;


public interface IRCMessageListener
{
	public void receiveMessage(String message);
	public void quit();
	public void disconnect();
	public void connect();
	public void connected();
	public void updateTopic();
	public void updateUserList(int sort);
	public void kicked();
	public void clearChat();
}

