
package net.minelord.util.IRC.commands;

import net.minelord.gui.panes.IRCPane;
import net.minelord.util.IRC.IRCClient;
import net.minelord.util.IRC.IRCCommand;

public class TopicCommand extends IRCCommand
{

	public TopicCommand(String command, String message, String help, int minParams, int maxParams)
	{
		super(command, message, help, minParams, maxParams);
	}

	@Override
	public void execute(IRCClient client, String[] params)
	{
		this.color=IRCPane.actionColor;
		if(params.length==0)
			this.message=client.getTopic();
		else
			client.getChannel().setTopic(stringParams(params, 0));
	}
}

