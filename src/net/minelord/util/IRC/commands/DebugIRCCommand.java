
package net.minelord.util.IRC.commands;

import jerklib.Session;
import jerklib.events.IRCEvent;
import net.minelord.util.IRC.IRCClient;
import net.minelord.util.IRC.IRCCommand;

public class DebugIRCCommand extends IRCCommand
{

	public DebugIRCCommand(String command, String message, String help)
	{
		super(command, message, help); 
	}

	@Override
	public void execute(final IRCClient client, String[] params)
	{
		IRCEvent event = new IRCEvent()
		{
			@Override
			public Type getType()
			{
				return Type.ERROR;
			}

			@Override
			public Session getSession()
			{
				return client.getSession();
			}

			@Override
			public String getRawEventData()
			{
				return "This is debug!";
			}
		};
		client.receiveEvent(event);
	}
}

