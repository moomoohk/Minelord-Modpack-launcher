
package net.minelord.util.IRC;

public interface IRCAlertListener
{
	public void alert();
	public void disconnected();
	public void connected();
	public void topicChange();
}

