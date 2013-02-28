
package net.minelord.util.IRC.commands;

import net.minelord.util.IRC.IRCClient;
import net.minelord.util.IRC.IRCCommand;

public class KickBanIRCCommand extends IRCCommand
{
	public KickBanIRCCommand(String command, String message, String help, int minParams, int maxParams)
	{
		super(command, message, help, minParams, maxParams);
	}

	@Override
	public void execute(IRCClient client, String[] params)
	{
		getCommand("/ban").execute(client, params);
		getCommand("/kick").execute(client, params);
	}

}

