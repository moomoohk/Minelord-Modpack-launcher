
package net.minelord.util.IRC.commands;

import net.minelord.util.IRC.IRCClient;
import net.minelord.util.IRC.IRCCommand;

public class QuitIRCCommand extends IRCCommand
{

	public QuitIRCCommand(String command, String message, String help)
	{
		super(command, message, help);
	}

	@Override
	public void execute(IRCClient client, String[] params)
	{
		client.closeChat();
	}
}

