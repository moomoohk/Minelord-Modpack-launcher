package net.minelord.util;

import jerklib.Channel;
import jerklib.ConnectionManager;
import jerklib.Profile;
import jerklib.events.IRCEvent;
import jerklib.events.IRCEvent.Type;
import jerklib.events.JoinCompleteEvent;
import jerklib.listeners.IRCEventListener;
import net.minelord.log.Logger;

public class IRCBot implements IRCEventListener
{
	private Channel channel;
	private String network, room, nick;

	public IRCBot(String network, String room, String nick)
	{
		ConnectionManager conman = new ConnectionManager(new Profile(nick));
		conman.requestConnection(network).addIRCEventListener(this);
		this.channel = null;
		this.nick = nick;
		this.network = network;
		this.room = room.charAt(0) != '#' ? "#" + room : room;
	}

	public void receiveEvent(IRCEvent e)
	{
		if (e.getType() == Type.CONNECT_COMPLETE)
		{
			e.getSession().join(this.room);
		}
		else
		{
			if (e.getType() == Type.NOTICE || e.getType() == Type.SERVER_INFORMATION)
			{
				Logger.logInfo(e.getRawEventData());
				return;
			}
			if (e.getType() == Type.JOIN_COMPLETE)
			{
				JoinCompleteEvent jce = (JoinCompleteEvent) e;
				this.channel = jce.getChannel();
				return;
			}
			if (e.getType() == Type.ERROR)
			{
				ConnectionManager conman = new ConnectionManager(new Profile(nick));
				conman.requestConnection(network).addIRCEventListener(this);
				return;
			}
			try
			{
				String raw = e.getRawEventData();
				String sender = raw.substring(1, raw.indexOf('!'));
				String message = "";
				if (e.getType() == Type.CHANNEL_MESSAGE)
					message = sender + ": " + raw.substring(raw.indexOf(this.channel.getName()) + this.channel.getName().length() + 2);
				if (e.getType() == Type.NICK_CHANGE)
					message = sender + " changed his nick to " + raw.substring(raw.indexOf("NICK") + 5);
				if (e.getType() == Type.CTCP_EVENT)
					message = "*" + sender + raw.substring(raw.indexOf("ACTION") + 6);
				if (e.getType() == Type.JOIN)
					message = sender + " joined the room";
				if (e.getType() == Type.QUIT)
					message = sender + " quit";
				if (e.getType() == Type.DEFAULT && raw.contains("KICK"))
				{
					String kick = raw.substring(raw.indexOf("KICK"));
					message = sender + " was kicked by " + kick.substring(6 + this.room.length(), kick.indexOf(":") - 1);
					// :moomoohk!moomoohk@lu-kema2a.minelord.com KICK #Minelord
					// moomoohk :
					// :moomoohk!moomoohk@lu-kema2a.minelord.com KICK #Minelord
					// moomoohk :test
				}
				if (e.getType().toString().equals("MODE_EVENT"))
				{
					String mode = raw.substring(raw.indexOf(this.channel.getName()) + this.channel.getName().length() + 1, raw.indexOf(this.channel.getName()) + this.channel.getName().length() + 3);
					String modeChanged = raw.substring(raw.indexOf(this.channel.getName()) + this.channel.getName().length() + 4);
					message = "gotta catch " + mode;
					if (mode.equals("+b"))
						message = sender + " banned " + modeChanged;
					if (mode.equals("-b"))
						message = sender + " unbanned " + modeChanged;
					if (mode.equals("+ho"))
						message = sender + " promoted " + modeChanged + " to half operator";
					if (mode.equals("-ho"))
						message = sender + " demoted " + modeChanged + " from half operator";
					if (mode.equals("+o"))
						message = sender + " promoted " + modeChanged + " to operator";
					if (mode.equals("-o"))
						message = sender + " demoted " + modeChanged + " from operator";
				}
				if (message.contains("€¦"))
					message = message.replace("€¦", "...");
				// System.err.println(e.getType() + " : " + raw);
				if (containsNick(message))
					System.err.println(message);
				else
					System.out.println(message);
				// part*/
			}
			catch (Exception ex)
			{
				System.err.println("broke: " + e.getType() + " : " + e.getRawEventData());
				ex.printStackTrace();
			}
		}
	}

	public void send(String message)
	{
		if (this.channel != null)
			this.channel.say(message);
		else
			System.out.println("No channel!");
	}

	public boolean containsNick(String msg)
	{
		if (msg.toLowerCase().contains(this.nick.toLowerCase()))
			return true;
		return false;
	}

	public String getRoom()
	{
		return this.room;
	}

	public String getNick()
	{
		return this.nick;
	}

	public String getNetwork()
	{
		return this.network;
	}
}
