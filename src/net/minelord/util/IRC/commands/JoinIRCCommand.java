
package net.minelord.util.IRC.commands;

import net.minelord.gui.panes.IRCPane;
import net.minelord.util.IRC.IRCClient;
import net.minelord.util.IRC.IRCCommand;

public class JoinIRCCommand extends IRCCommand
{

	public JoinIRCCommand(String command, String message, String help)
	{
		super(command, message, help);
	}

	@Override
	public void execute(IRCClient client, String[] params)
	{
		if(params.length==0)
		{
			this.message="Too few parameters!";
			this.color=IRCPane.errorColor;
			return;
		}
		if(params.length>1)
		{
			this.message="Too many parameters!";
			this.color=IRCPane.errorColor;
			return;
		}
		if(IRCPane.instance!=null)
		{
			if(params[0].charAt(0)!='#')
				params[0]="#"+params[0];
			this.color=IRCPane.actionColor;
			this.message="Switching to channel "+params[0]+"...";
			client.quit();
			client.clearChat();
			IRCPane.instance.connect();
			client.connect(client.getNetwork(), params[0], client.getNick(), IRCPane.instance);
		}
		else
		{
			this.message="Unknown error! (IRCPane instance is null)";
			this.color=IRCPane.errorColor;
			return;
		}
	}

}

