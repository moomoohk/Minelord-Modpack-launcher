
package net.minelord.util.IRC.commands;

import net.minelord.gui.panes.IRCPane;
import net.minelord.util.IRC.IRCClient;
import net.minelord.util.IRC.IRCCommand;

public class HelpIRCCommand extends IRCCommand
{

	public HelpIRCCommand(String command, String message, String help)
	{
		super(command, message, help);
	}

	@Override
	public void execute(IRCClient client, String[] params)
	{
		this.message=getAllHelp();
		this.color=IRCPane.errorColor;
		this.containsHTML=true;
	}
}

