
package net.minelord.util;

public interface IRCAlertListener
{
	public void alert();
	public void disconnected();
	public void connected();
	public void topicChange();
}

