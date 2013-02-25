package net.minelord.util.IRC.commands;

import net.minelord.gui.panes.IRCPane;
import net.minelord.util.IRC.IRCClient;
import net.minelord.util.IRC.IRCCommand;

public class NickIRCCommand extends IRCCommand
{

	public NickIRCCommand(String command, String message, String help)
	{
		super(command, message, help);
	}

	@Override
	public void execute(IRCClient client, String[] params)
	{
		if (params.length == 0)
		{
			if(client.getNick().equals(IRCPane.nick))
			{
				this.message=null;
				return;
			}
			this.color=IRCPane.actionColor;
			client.revertNick();
		}
		else
		{
			this.color = IRCPane.actionColor;
			client.setNick(params[0]);
		}
	}
}
