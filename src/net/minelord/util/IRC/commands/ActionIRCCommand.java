
package net.minelord.util.IRC.commands;

import net.minelord.gui.panes.IRCPane;
import net.minelord.util.IRC.IRCClient;
import net.minelord.util.IRC.IRCCommand;

public class ActionIRCCommand extends IRCCommand
{

	public ActionIRCCommand(String command, String message, String help)
	{
		super(command, message, help);
	}

	@Override
	public void execute(IRCClient client, String[] params)
	{
		String action="";
		for(int i=0; i<params.length; i++)
			action+=params[i]+(i!=params.length-1?" ":"");
		this.message="*"+client.getNick()+" "+action;
		this.color=IRCPane.actionColor;
		client.action(action);
	}
}

