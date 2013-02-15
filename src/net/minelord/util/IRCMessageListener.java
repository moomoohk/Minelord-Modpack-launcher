
package net.minelord.util;

import java.awt.Color;

public interface IRCMessageListener
{
	public void recieveMessage(String message, Color col);
	public void quit();
}

