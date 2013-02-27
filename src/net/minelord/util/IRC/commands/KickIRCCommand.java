
package net.minelord.util.IRC.commands;

import net.minelord.gui.panes.IRCPane;
import net.minelord.util.IRC.IRCClient;
import net.minelord.util.IRC.IRCCommand;

public class KickIRCCommand extends IRCCommand
{

	public KickIRCCommand(String command, String message, String help)
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
		client.getChannel().kick(params[0], params.length==1?"":params[1]);
	}

}

