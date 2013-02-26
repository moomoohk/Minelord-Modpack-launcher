
package net.minelord.util.IRC.commands;

import net.minelord.gui.panes.IRCPane;
import net.minelord.util.IRC.IRCClient;
import net.minelord.util.IRC.IRCCommand;

public class WhoisIRCCommand extends IRCCommand
{

	public WhoisIRCCommand(String command, String message, String help)
	{
		super(command, message, help);
	}

	@Override
	public void execute(IRCClient client, String[] params)
	{
		if(params.length==0)
		{
			this.message="Missing parameters!";
			this.color=IRCPane.errorColor;
		}
		if(params.length>1)
		{
			this.message="Too many parameters!";
			this.color=IRCPane.errorColor;
		}
		client.getSession().whois(params[0]);
	}

}

