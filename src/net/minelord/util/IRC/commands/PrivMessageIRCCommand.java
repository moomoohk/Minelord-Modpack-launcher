package net.minelord.util.IRC.commands;

import net.minelord.gui.panes.IRCPane;
import net.minelord.util.IRC.IRCClient;
import net.minelord.util.IRC.IRCCommand;

public class PrivMessageIRCCommand extends IRCCommand
{
	public PrivMessageIRCCommand(String command, String message, String help, int minParams, int maxParams)
	{
		super(command, message, help, minParams, maxParams);
	}

	@Override
	public void execute(IRCClient client, String[] params)
	{
		this.message="[You -> " + params[0] + "]: " + stringParams(params, 1);
		this.color=IRCPane.pmColor;
		client.message(params[0], stringParams(params, 1));
	}
	
	@Override
	public void missingParameters(IRCClient client, String[] params)
	{
		this.color=IRCPane.errorColor;
		this.message="Missing message/receipient!";
	}
}
