
package net.minelord.util.IRC.commands;

import net.minelord.util.IRC.IRCClient;
import net.minelord.util.IRC.IRCCommand;

public class BanIRCCommand extends IRCCommand
{

	public BanIRCCommand(String command, String message, String help, int minParams, int maxParams)
	{
		super(command, message, help, minParams, maxParams);
	}

	@Override
	public void execute(IRCClient client, String[] params)
	{
		String[] temp=new String[2];
		temp[0]=params[0];
		temp[1]="+b";
		getCommand("/mode").execute(client, temp);
		this.message=getCommand("/mode").getMessage();
		this.color=getCommand("/mode").getColor();
	}
}

