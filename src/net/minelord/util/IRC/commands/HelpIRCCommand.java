
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
			this.message = "&nbsp;&nbsp;- - -<br>";
			this.message += "&nbsp;&nbsp;"+getCommand(params[0].charAt(0)=='/'?params[0]:"/"+params[0]).getCommand() + "<br>";
			this.message += "&nbsp;&nbsp;"+getCommand(params[0].charAt(0)=='/'?params[0]:"/"+params[0]).getHelp() + "<br>";
			this.message += "&nbsp;&nbsp;- - -";
		}
		else
			this.message=getAllHelp();
		this.containsHTML=true;
	}
}

