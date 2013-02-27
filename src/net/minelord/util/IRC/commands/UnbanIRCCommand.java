
package net.minelord.util.IRC.commands;

import net.minelord.gui.panes.IRCPane;
import net.minelord.util.IRC.IRCClient;
import net.minelord.util.IRC.IRCCommand;

public class UnbanIRCCommand extends IRCCommand
{

	public UnbanIRCCommand(String command, String message, String help)
	{
		super(command, message, help);
	}

	@Override
	public void execute(IRCClient client, String[] params)
	{
		if(params.length<1)
		{
			this.color=IRCPane.errorColor;
			this.message="Missing parameters!";
		}
		if(params.length>1)
		{
			this.color=IRCPane.errorColor;
			this.message="Too many parameters!";
		}
		String[] temp=new String[2];
		temp[0]=params[0];
		temp[1]="-b";
		getCommand("/mode").execute(client, temp);
		this.message=getCommand("/mode").getMessage();
		this.color=getCommand("/mode").getColor();
	}

}

