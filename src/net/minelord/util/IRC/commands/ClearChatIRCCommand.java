
package net.minelord.util.IRC.commands;

import net.minelord.util.IRC.IRCClient;
import net.minelord.util.IRC.IRCCommand;

public class ClearChatIRCCommand extends IRCCommand
{

	public ClearChatIRCCommand(String command, String message, String help)
	{
		super(command, message, help);
	}

	@Override
	public void execute(IRCClient client, String[] params)
	{
		client.clearChat();
	}
}

