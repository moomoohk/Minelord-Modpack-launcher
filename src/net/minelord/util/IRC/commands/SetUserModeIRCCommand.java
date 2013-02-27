
package net.minelord.util.IRC.commands;

import net.minelord.gui.panes.IRCPane;
import net.minelord.util.IRC.IRCClient;
import net.minelord.util.IRC.IRCCommand;

public class SetUserModeIRCCommand extends IRCCommand
{

	public SetUserModeIRCCommand(String command, String message, String help)
	{
		super(command, message, help);
	}

	@Override
	public void execute(IRCClient client, String[] params)
	{
		if(params.length<2)
		{
			this.color=IRCPane.errorColor;
			this.message="Missing parameters!";
		}
		if(params.length>2)
		{
			this.color=IRCPane.errorColor;
			this.message="Too many parameters!";
		}
		client.getChannel().mode(params[1]+" "+params[0]);
	}

}

