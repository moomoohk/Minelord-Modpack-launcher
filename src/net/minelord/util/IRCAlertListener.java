
package net.minelord.util;

public interface IRCAlertListener
{
	public void alert();
	public void kicked();
	public void connected();
	public void topicChange();
}

