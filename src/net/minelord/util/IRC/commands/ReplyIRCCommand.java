
package net.minelord.util.IRC.commands;

import net.minelord.gui.panes.IRCPane;
import net.minelord.util.IRC.IRCClient;
import net.minelord.util.IRC.IRCCommand;

public class ReplyIRCCommand extends IRCCommand
{
	public ReplyIRCCommand(String command, String message, String help, int minParams, int maxParams)
	{
		super(command, message, help, minParams, maxParams);
	}

	@Override
	public void execute(IRCClient client, String[] params)
	{
		if (IRCPane.lastNick != null)
		{
			String[] newParams=new String[params.length+1];
			newParams[0]=IRCPane.lastNick;
			for(int i=0; i<params.length; i++)
				newParams[i+1]=params[i];
			IRCCommand.getCommand("/msg").execute(client, newParams);
			this.message=IRCCommand.getCommand("/msg").getMessage();
			this.color=IRCCommand.getCommand("/msg").getColor();
		}
		else
		{
			this.message="Nobody to reply to!";
			this.color=IRCPane.errorColor;
		}
	}

}

