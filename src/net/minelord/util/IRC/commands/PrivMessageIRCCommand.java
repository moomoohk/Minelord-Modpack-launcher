package net.minelord.util.IRC.commands;

import net.minelord.gui.panes.IRCPane;
import net.minelord.util.IRC.IRCClient;
import net.minelord.util.IRC.IRCCommand;

public class PrivMessageIRCCommand extends IRCCommand
{

	public PrivMessageIRCCommand(String command, String message, String help)
	{
		super(command, message, help);
	}

	@Override
	public void execute(IRCClient client, String[] params)
	{
		System.out.println(params.length);
		if (params.length <=1)
		{
			this.message = "Missing parameters!";
			this.color = IRCPane.errorColor;
			return;
		}
		String pm = "";
		for (int i = 1; i < params.length; i++)
			pm += " " + params[i];
		this.message="[You -> " + params[0] + "]: " + pm;
		this.color=IRCPane.pmColor;
		client.message(params[0], pm);
	}
}
