
package net.minelord.util;

import java.util.ArrayList;

public class IRCCommand
{
	public static ArrayList<IRCCommand> commands=new ArrayList<IRCCommand>();
	private String command, message, help;
	private boolean needsParams;
	private Runnable execute;
	public IRCCommand(String command, String message, String help, boolean needsParams)
	{
		this.command=command;
		this.message=message;
		this.help=help;
		this.needsParams=needsParams;
		this.execute=null;
	}
	public static void add(IRCCommand command)
	{
		if(getCommand(command.getCommand(), command.needsParams)==null)
			commands.add(command);
		else
			throw new IllegalStateException(command.getCommand()+" already exists in the list!");
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
	public boolean needsParams()
	{
		return needsParams;
	}
	public Runnable getExecute()
	{
		return this.execute;
	}
	public void setExecute(Runnable execute)
	{
		this.execute=execute;
	}
	public static String getAllHelp()
	{
		if(commands.size()==0)
			return "No commands";
		String help="";
		for(int i=0; i<commands.size(); i++)
		{
			help+=commands.get(i).getCommand()+"\n";
			help+=commands.get(i).getHelp()+"\n";
			help+=commands.get(i).needsParams()+"\n";
		}
		return help;
	}
	public static String parseCommand(String command)
	{
		if(command.trim().contains(" "))
			return command.trim().substring(command.indexOf("/"), command.indexOf(" "));
		return command;
	}
	public static String[] parseParams(String command)
	{
		command=command.trim();
		int count=0;
		for(int i=0; i<command.length(); i++)
			if(command.charAt(i)==' ')
			{
				count++;
				while(i<command.length()&&command.charAt(i)==' ')
					i++;
			}
		if(count==0)
			return new String[0];
		command+=" ";
		String[] params=new String[0];
		String temp=null;
		for(int i=0; i<command.length(); i++)
		{
			if(command.charAt(i)==' ')
			{
				if(temp!=null)
				{
					String[] temp2=new String[params.length+1];
					for(int j=0; j<params.length; j++)
						temp2[j]=params[j];
					temp2[temp2.length-1]=temp;
					params=temp2;
				}
				temp=null;
				continue;
			}
			if(temp==null)
				temp="";
			temp+=command.charAt(i);
		}
		String[] temp2=new String[params.length-1];
		for(int i=1; i<params.length; i++)
			temp2[i-1]=params[i];
		params=temp2;
		return params;
	}
	public static IRCCommand getCommand(String command, boolean withParams)
	{
		for(IRCCommand temp: commands)
			if(temp.getCommand().equals(command)&&withParams==temp.needsParams())
				return temp;
		return null;
	}
}

