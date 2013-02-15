package net.minelord.util;

import jerklib.Channel;
import jerklib.ConnectionManager;
import jerklib.Profile;
import jerklib.Session;
import jerklib.events.IRCEvent;
import jerklib.events.IRCEvent.Type;
import jerklib.events.JoinCompleteEvent;
import jerklib.listeners.IRCEventListener;
import net.minelord.log.Logger;

public class IRCBot implements IRCEventListener
{
	private Channel channel;
	private String network, room, nick;
	private ConnectionManager conman;
	private Profile p;
	private IRCMessageListener messageListener;
	private Session s;

	public IRCBot(String network, String room, String nick)
	{
		this(network, room, nick, null);
	}

	public IRCBot(String network, String room, String nick, IRCMessageListener messageListener)
	{
		this.channel = null;
		this.nick = nick;
		this.p = new Profile(nick);
		this.network = network;
		this.room = room.charAt(0) != '#' ? "#" + room : room;
		this.conman = new ConnectionManager(new Profile(nick));
		this.conman.requestConnection(network).addIRCEventListener(this);
		this.messageListener = messageListener;
		this.s=null;
	}

	public void receiveEvent(IRCEvent e)
	{
		Logger.logInfo(e.getType() + " : " + e.getRawEventData());
		if (e.getType() == Type.CONNECT_COMPLETE)
		{
			e.getSession().join(this.room);
			this.s=e.getSession();
		}
		else
		{
			if (e.getType()==Type.AWAY_EVENT||e.getType() == Type.NICK_IN_USE||e.getType() == Type.NOTICE || e.getType() == Type.SERVER_INFORMATION || (e.getType() == Type.DEFAULT && !e.getRawEventData().contains("KICK")) || e.getType() == Type.SERVER_VERSION_EVENT || e.getType() == Type.MOTD || e.getType() == Type.NICK_LIST_EVENT || e.getType() == Type.TOPIC)
			{
				Logger.logInfo(e.getRawEventData());
				return;
			}
			if (e.getType() == Type.MODE_EVENT && e.getRawEventData().contains("+nt"))
				return;
			if(e.getType()==Type.TOPIC)
			{
				this.messageListener.updateTopic();
				return;
			}
			if (e.getType() == Type.JOIN_COMPLETE)
			{
				JoinCompleteEvent jce = (JoinCompleteEvent) e;
				this.channel = jce.getChannel();
				this.messageListener.connected();
				return;
			}
			if (e.getType() == Type.ERROR)
			{
				ConnectionManager conman = new ConnectionManager(p);
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
					message = "*"+sender + " changed their nick to " + raw.substring(raw.indexOf("NICK") + 5);
				if (e.getType() == Type.CTCP_EVENT)
					message = "*" + sender + raw.substring(raw.indexOf("ACTION") + 6);
				if (e.getType() == Type.JOIN)
					message = "*"+sender + " joined the room";
				if (e.getType() == Type.QUIT)
					message = "*"+sender + " quit";
				if (e.getType() == Type.PART)
					message = "*"+sender + " parted";
				if (e.getType() == Type.DEFAULT && raw.contains("KICK"))
				{
					String kick = raw.substring(raw.indexOf("KICK")), reason = kick.substring(kick.indexOf(":") + 1);
					message = "*"+kick.substring(6 + this.room.length(), kick.indexOf(":") - 1) + " was kicked by " + sender;
					if (reason.length() > 0)
						message += " (" + reason + ")";
				}
				if (e.getType().toString().equals("MODE_EVENT"))
				{
					try
					{
						String mode = raw.substring(raw.indexOf(this.channel.getName()) + this.channel.getName().length() + 1, raw.indexOf(this.channel.getName()) + this.channel.getName().length() + 3);
						String modeChanged = raw.substring(raw.indexOf(this.channel.getName()) + this.channel.getName().length() + 4);
						if (modeChanged.equals(this.nick))
							modeChanged = "you";
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
						if (mode.equals("+v"))
							message = modeChanged.equals("you") ? "You were granted voice by " + sender : modeChanged + " was granted voice by " + sender;
						if (mode.equals("-v"))
							message = modeChanged.equals("you") ? "You had your voice removed by " + sender : modeChanged + " had their voice removed by " + sender;
						message="*"+message;
					}
					catch (Exception ex)
					{
						return;
					}
				}
				if (message.contains("��"))
					message = message.replace("��", "...");
				// System.err.println(e.getType() + " : " + raw);
				/*
				 * if (containsNick(message)) System.err.println(message); else
				 * System.out.println(message); // part
				 */
				if (messageListener != null)
					messageListener.receiveMessage(message);
				/*
				 * else if (containsNick(message)) System.err.println(message);
				 * else System.out.println(message);
				 */
			}
			catch (Exception ex)
			{
				System.err.println("broke: " + e.getType() + " : " + e.getRawEventData());
				ex.printStackTrace();
			}
		}
	}

	public String parseCommand(String message)
	{
		if (message.contains(" "))
			for (int i = 0; i < message.length(); i++)
			{
				if (message.charAt(i) == ' ')
				{
					if (message.toLowerCase().contains("/me"))
						return getNick()+""+message.substring(i);
					if (message.toLowerCase().contains("/quit"))
						return "Quitting...";
					if(message.toLowerCase().contains("/join"))
						return "Switching...";
					break;
				}
			}
		else
		{
			if (message.toLowerCase().contains("/quit"))
				return "Quitting...";
		}
		return "";
	}

	public void send(String message)
	{
		message=message.trim();
		if (message.charAt(0) == '/')
		{
			if (message.contains(" "))
				for (int i = 0; i < message.length(); i++)
				{
					if (message.charAt(i) == ' ')
					{
						if(message.toLowerCase().contains("/away"))
							this.s.setAway(message.substring(i));
						if (message.toLowerCase().contains("/me"))
							this.channel.action(message.substring(i));
						if (message.toLowerCase().contains("/quit"))
						{
							this.conman.quit(message.substring(i));
							this.messageListener.disconnect();
							this.messageListener.quit();
						}
						if(message.toLowerCase().contains("/join"))
						{
							this.messageListener.disconnect();
							this.conman.quit();
							this.s.join(message.substring(i).trim());
							this.room=message.substring(i);
							this.messageListener.connect();
						}
						break;
					}
				}
			else
			{
				if (message.toLowerCase().contains("/quit"))
				{
					this.conman.quit();
					this.messageListener.quit();
				}
				if(message.toLowerCase().contains("/back"))
					this.s.unsetAway();
				if(message.toLowerCase().contains("/away"))
					this.s.setAway("");
			}
		}
		else
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
		return this.s.getNick();
	}

	public String getNetwork()
	{
		return this.network;
	}
	public String getTopic()
	{
		return this.channel.getTopic();
	}
	public String getTopicSetter()
	{
		return this.channel.getTopicSetter();
	}
}
