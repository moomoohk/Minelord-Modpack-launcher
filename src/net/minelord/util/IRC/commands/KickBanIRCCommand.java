
package net.minelord.util.IRC.commands;

import net.minelord.gui.panes.IRCPane;
import net.minelord.util.IRC.IRCClient;
import net.minelord.util.IRC.IRCCommand;

public class KickBanIRCCommand extends IRCCommand
{
	public KickBanIRCCommand(String command, String message, String help)
	{
		super(command, message, help);
	}

	@Override
	public void execute(IRCClient client, String[] params)
	{
		if(params.length==0)
		{
			this.color=IRCPane.errorColor;
			this.message="Missing parameters!";
		}
		getCommand("/ban").execute(client, params);
		getCommand("/kick").execute(client, params);
	}

}

