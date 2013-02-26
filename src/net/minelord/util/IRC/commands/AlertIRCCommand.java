
package net.minelord.util.IRC.commands;

import net.minelord.util.IRC.IRCClient;
import net.minelord.util.IRC.IRCCommand;

public class AlertIRCCommand extends IRCCommand
{

	public AlertIRCCommand(String command, String message, String help)
	{
		super(command, message, help);
	}

	@Override
	public void execute(IRCClient client, String[] params)
	{
		try
		{
			Thread.sleep(1000);
		}
		catch(Exception e)
		{
			
		}
		client.alertAlertListener();
	}

}

