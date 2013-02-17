
package net.minelord.util;


public interface IRCMessageListener
{
	public void receiveMessage(String message);
	public void quit();
	public void disconnect();
	public void connect();
	public void connected();
	public void updateTopic();
	public void updateUserList();
	public void kicked();
}

