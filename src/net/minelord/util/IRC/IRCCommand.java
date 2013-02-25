package net.minelord.util.IRC;

import java.util.ArrayList;
public abstract class IRCCommand
{
	public static ArrayList<IRCCommand> commands = new ArrayList<IRCCommand>();
	protected String command, message, help, color=null;
	protected boolean containsHTML;

	public IRCCommand(String command, String message, String help)
	{
		this.command = command;
		this.message = message;
		this.help = help;
		this.containsHTML=false;
	}

	public static void add(IRCCommand command)
	{
		if (getCommand(command.getCommand()) == null)
			commands.add(command);
		else
			throw new IllegalStateException(command.getCommand() + " already exists in the list!");
	}

	public String getMessage()
	{
		return message;
	}

	public String getHelp()
	{
		return help;
	}

	public String getCommand()
	{
		return command;
	}

	public String getColor()
	{
		return this.color;
	}
	public boolean containsHTML()
	{
		return this.containsHTML;
	}
	public static String getAllHelp()
	{
		if (commands.size() == 0)
			return "No commands";
		String help = "<b>Commands:</b><br>";
		for (int i = 0; i < commands.size(); i++)
		{
			help += "&nbsp;- - -<br>";
			help += "&nbsp;"+commands.get(i).getCommand() + "<br>";
			help += "&nbsp;"+commands.get(i).getHelp() + "<br>";
		}
		help += "&nbsp;- - -";
		return help;
	}

	public static String parseCommand(String command)
	{
		if (command.trim().contains(" "))
			return command.trim().substring(command.indexOf("/"), command.indexOf(" "));
		return command;
	}

	public static String[] parseParams(String command)
	{
		command = command.trim();
		int count = 0;
		for (int i = 0; i < command.length(); i++)
			if (command.charAt(i) == ' ')
			{
				count++;
				while (i < command.length() && command.charAt(i) == ' ')
					i++;
			}
		if (count == 0)
			return new String[0];
		command += " ";
		String[] params = new String[0];
		String temp = null;
		for (int i = 0; i < command.length(); i++)
		{
			if (command.charAt(i) == ' ')
			{
				if (temp != null)
				{
					String[] temp2 = new String[params.length + 1];
					for (int j = 0; j < params.length; j++)
						temp2[j] = params[j];
					temp2[temp2.length - 1] = temp;
					params = temp2;
				}
				temp = null;
				continue;
			}
			if (temp == null)
				temp = "";
			temp += command.charAt(i);
		}
		String[] temp2 = new String[params.length - 1];
		for (int i = 1; i < params.length; i++)
			temp2[i - 1] = params[i];
		params = temp2;
		return params;
	}

	public static IRCCommand getCommand(String command)
	{
		for (IRCCommand temp : commands)
			if (temp.getCommand().equals(command))
				return temp;
		return null;
	}

	public String toString()
	{
		return "Command: " + this.command + "\nMessage: " + this.message + "\nHelp: " + this.help;
	}

	public void checkAndExecute(IRCClient client, String[] params)
	{
		execute(client, params);
	}

	public abstract void execute(IRCClient client, String[] params);
}
