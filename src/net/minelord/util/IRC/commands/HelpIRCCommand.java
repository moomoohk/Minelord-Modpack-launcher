
package net.minelord.util.IRC.commands;

import net.minelord.gui.panes.IRCPane;
import net.minelord.util.IRC.IRCClient;
import net.minelord.util.IRC.IRCCommand;

public class HelpIRCCommand extends IRCCommand
{

	public HelpIRCCommand(String command, String message, String help, int minParams, int maxParams)
	{
		super(command, message, help, minParams, maxParams);
	}

	@Override
	public void execute(IRCClient client, String[] params)
	{
		this.color=IRCPane.errorColor;
		if(params.length==1)
		{
			if(getCommand(params[0].charAt(0)=='/'?params[0]:"/"+params[0])==null)
			{
				this.message="Unknown command!";
				return;
			}
			String temp = "<b>Commands:</b><br>";
			temp += "&nbsp;&nbsp;- - -<br>";
			temp += "&nbsp;&nbsp;"+getCommand(params[1].charAt(0)=='/'?params[1]:"/"+params[1]).getCommand() + "<br>";
			temp += "&nbsp;&nbsp;"+getCommand(params[1].charAt(0)=='/'?params[1]:"/"+params[1]).getHelp() + "<br>";
			temp += "&nbsp;- - -";
		}
		else
			this.message=getAllHelp();
		this.containsHTML=true;
	}
}

