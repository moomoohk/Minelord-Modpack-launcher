package net.minelord.util.IRC;

import java.nio.channels.NotYetConnectedException;
import java.util.List;

import jerklib.Channel;
import jerklib.ConnectionManager;
import jerklib.EventToken;
import jerklib.Profile;
import jerklib.Session;
import jerklib.events.IRCEvent;
import jerklib.events.IRCEvent.Type;
import jerklib.events.JoinCompleteEvent;
import jerklib.listeners.IRCEventListener;
import net.minelord.gui.panes.IRCPane;
import net.minelord.log.Logger;

public class IRCClient implements IRCEventListener
{
	private Channel channel;
	private String network, room, nick;
	private ConnectionManager conman;
	private Profile p;
	private IRCMessageListener messageListener;
	private IRCAlertListener alertListener;
	private Session s;

	public void receiveEvent(IRCEvent e)
	{
		try
		{
			EventToken token = new EventToken(e.getRawEventData());
			if (token.prefix().contains("Lemming!stats@liberty-unleashed.co.uk"))
				return;
		}
		catch (NullPointerException ex)
		{
			IRCEvent event = new IRCEvent()
			{
				@Override
				public Type getType()
				{
					return Type.EXCEPTION;
				}

				@Override
				public Session getSession()
				{
					return s;
				}

				@Override
				public String getRawEventData()
				{
					return "No Internet connection!";
				}
			};
			receiveEvent(event);
			return;
		}
		catch (StringIndexOutOfBoundsException ex)
		{
			IRCEvent event = new IRCEvent()
			{
				@Override
				public Type getType()
				{
					return Type.EXCEPTION;
				}

				@Override
				public Session getSession()
				{
					return s;
				}

				@Override
				public String getRawEventData()
				{
					return "Lost connection!";
				}
			};
			receiveEvent(event);
			return;
		}
	//	Logger.logInfo(e.getType() + " : " + e.getRawEventData());
		if (e.getType() == Type.CONNECT_COMPLETE)
		{
			this.messageListener.clearChat();
			e.getSession().join(this.room);
			this.s = e.getSession();
			this.alertListener.connected();
		}
		else
		{
			if (e.getType() == Type.AWAY_EVENT || e.getType() == Type.NICK_IN_USE || e.getType() == Type.SERVER_INFORMATION || (e.getType() == Type.DEFAULT && !e.getRawEventData().contains("KICK")) || e.getType() == Type.SERVER_VERSION_EVENT || e.getType() == Type.MOTD
					|| e.getType() == Type.NICK_LIST_EVENT)
			{
				//Logger.logInfo(e.getRawEventData());
				return;
			}
			if (e.getType() == Type.MODE_EVENT && e.getRawEventData().contains("+nt"))
				return;
			if (e.getType() == Type.TOPIC)
			{
				this.messageListener.updateTopic();
				this.alertListener.topicChange();
				return;
			}
			if (e.getType() == Type.JOIN_COMPLETE)
			{
				alertAlertListener();
				JoinCompleteEvent jce = (JoinCompleteEvent) e;
				this.channel = jce.getChannel();
				this.messageListener.connected();
				return;
			}
			try
			{
				String raw = e.getRawEventData();
				EventToken token=new EventToken(raw);
				if(e.getType()==Type.WHOIS_EVENT)
				{
					printToken(token);
					System.out.println("username: "+token.args().get(2));
					System.out.println("host: "+token.arg(3));
					for(int i=0; i<token.args().size(); i++)
						System.out.println(i+" "+token.arg(i));
				}
				String sender = raw.substring(1, raw.indexOf('!'));
				String message = "";
				if(e.getType() == Type.NOTICE)
				{
					printToken(new EventToken(raw));
					message="-[NOTICE] "+sender+": "+raw.substring(raw.indexOf("NOTICE "+getNick())+getNick().length()+9);
					alertAlertListener();
				}
				if (e.getType() == Type.ERROR)
				{
					message = "-" + raw;
					alertAlertListener();
				}
				if (e.getType() == Type.CONNECTION_LOST)
				{
					message = "-" + raw;
					alertAlertListener();
					this.messageListener.kicked();
					this.alertListener.disconnected();
				}
				if (e.getType() == Type.EXCEPTION)
				{
					message = "-" + raw;
					alertAlertListener();
					this.messageListener.kicked();
					this.alertListener.disconnected();
				}
				if (e.getType() == Type.CHANNEL_MESSAGE)
					message = sender + ": " + raw.substring(raw.indexOf(this.channel.getName()) + this.channel.getName().length() + 2);
				if (e.getType() == Type.NICK_CHANGE)
				{
					boolean you=false;
					if(raw.substring(raw.indexOf("NICK") + 5).equals(getNick()))
						you=true;
					message = "*" + (you?"You":sender) + " changed "+(you?"your":"their")+" nick to " + raw.substring(raw.indexOf("NICK") + 5);
					this.messageListener.updateUserList(IRCPane.sortType);
				}
				if (e.getType() == Type.CTCP_EVENT)
					message = "*" + sender + raw.substring(raw.indexOf("ACTION") + 6);
				if (e.getType() == Type.JOIN)
				{
					//IRCPane.sendMessage(sender+": TRACING YOUR IP...");
					message = "*" + sender + " joined the room";
					this.messageListener.updateUserList(IRCPane.sortType);
				}
				if (e.getType() == Type.QUIT)
				{
					message = "*" + sender + " quit" + (raw.substring(raw.indexOf("QUIT :") + 6).trim().length() > 0 ? " (" + raw.substring(raw.indexOf("QUIT :") + 6).trim() + ")" : "");
					this.messageListener.updateUserList(IRCPane.sortType);
				}
				if (e.getType() == Type.PART)
				{
					message = "*" + sender + " parted";
					this.messageListener.updateUserList(IRCPane.sortType);
				}
				if (e.getType() == Type.DEFAULT && raw.contains("KICK"))
				{
					String kick = raw.substring(raw.indexOf("KICK")), reason = kick.substring(kick.indexOf(":") + 1);
					String kicked = kick.substring(6 + this.room.length(), kick.indexOf(":") - 1);
					boolean you = false;
					if (kicked.equals(getNick()))
					{
						kicked = "You were";
						you = true;
					}
					else
						kicked = kicked + " was";
					message = "*" + kicked + " kicked by " + sender;
					if (reason.length() > 0)
						message += " (" + reason + ")";
					this.messageListener.updateUserList(IRCPane.sortType);
					if (you)
					{
						if (this.messageListener != null)
							this.messageListener.kicked();
						if (this.alertListener != null)
							this.alertListener.disconnected();
					}
				}
				if (e.getType().toString().equals("MODE_EVENT"))
				{
					try
					{
						String mode = raw.substring(raw.indexOf(this.channel.getName()) + this.channel.getName().length() + 1, raw.indexOf(this.channel.getName()) + this.channel.getName().length() + 3);
						String modeChanged = raw.substring(raw.indexOf(this.channel.getName()) + this.channel.getName().length() + 4);
						if (modeChanged.equals(getNick()))
							modeChanged = "you";
						message = "TELL MOOMOOHK HE MUST CATCH " + mode;
						if (mode.equals("+b"))
							message = sender + " banned " + modeChanged;
						if (mode.equals("-b"))
							message = sender + " unbanned " + modeChanged;
						if (mode.equals("+ho") || mode.equals("+h"))
							message = sender + " promoted " + modeChanged + " to half operator";
						if (mode.equals("-ho") || mode.equals("-h"))
							message = sender + " demoted " + modeChanged + " from half operator";
						if (mode.equals("+o"))
							message = sender + " promoted " + modeChanged + " to operator";
						if (mode.equals("-o"))
							message = sender + " demoted " + modeChanged + " from operator";
						if (mode.equals("+v"))
							message = modeChanged.equals("you") ? "You were granted voice by " + sender : modeChanged + " was granted voice by " + sender;
						if (mode.equals("-v"))
							message = modeChanged.equals("you") ? "You had your voice removed by " + sender : modeChanged + " had their voice removed by " + sender;
						message = "*" + message;
					}
					catch (Exception ex)
					{
						return;
					}
				}
				if (e.getType() == Type.PRIVATE_MESSAGE)
				{
					this.messageListener.updateUserList(1);
					alertAlertListener();
					message = "[" + sender + " -> You]: " + raw.substring(raw.indexOf(getNick() + " :") + getNick().length() + 2);
				}
				if (message.contains("€¦"))
					message = message.replace("€¦", "...");
				if (messageListener != null)
					messageListener.receiveMessage(message);
			}
			catch (Exception ex)
			{
				if (messageListener != null)
					messageListener.receiveMessage("-ERROR DISPLAYING MESSAGE (Check console)");
				System.err.println("broke: " + e.getType() + " : " + e.getRawEventData());
				ex.printStackTrace();
				printToken(new EventToken(e.getRawEventData()));
			}
		}
	}

	public String parseCommand(String message)
	{
		message = message.trim();
		if (message.trim().contains(" "))
			for (int i = 0; i < message.length(); i++)
			{
				if (message.charAt(i) == ' ')
				{
					if (message.toLowerCase().contains("/isonline"))
					{
						boolean isOnline = this.channel.getNicks().contains(message.substring(i).trim());
						return "*" + message.substring(i).trim() + " is " + (isOnline ? "" : "not") + " online";
					}
					if (message.toLowerCase().contains("/me"))
						return "*" + getNick() + message.substring(i);
					if (message.toLowerCase().contains("/quit"))
						return "Quitting...";
					if (message.toLowerCase().contains("/join"))
						return "Switching...";
					if (message.toLowerCase().contains("/nick"))
					{
						if (message.substring(i + 1).equals(getNick()))
							return "";
						else
							return "*You changed your nick to " + message.substring(i + 1);
					}
					if (message.contains("/msg"))
					{
						if (message.trim().indexOf(" ") != message.trim().lastIndexOf(" "))
						{
							String receipient = message.substring(i + 1);
							String pm = receipient.substring(receipient.indexOf(' '));
							receipient = receipient.substring(0, receipient.indexOf(' '));
							return "[You -> " + receipient + "]: " + pm;
						}
						else
							return "-Missing parameters!";
					}
					if (message.toLowerCase().contains("/r"))
						if (IRCPane.lastNick != null)
							return "[You -> " + IRCPane.lastNick + "]: " + message.substring(i).trim();
						else
							return "-Nobody to reply to!";
					break;
				}
			}
		else
		{
			if (message.toLowerCase().contains("/quit"))
				return "Quitting...";
			if (message.toLowerCase().contains("/nick"))
			{
				if (!this.nick.equals(getNick()))
					return "*You changed your nick to " + this.nick;
				else
					return "";
			}
			if (message.toLowerCase().contains("/break"))
				return "";
			if (message.toLowerCase().contains("/isonline"))
				return "-Missing parameters!";
		}
		return null;
	}

	public void send(String message)
	{
		if (message == null || message.length() == 0)
			return;
		/*if (message.charAt(0) == '/')
		{
			if (message.contains(" "))
				for (int i = 0; i < message.length(); i++)
				{
					if (message.charAt(i) == ' ')
					{
						if(message.toLowerCase().contains("/whois"))
							this.s.whois(message.substring(i));
						if (message.toLowerCase().contains("/away"))
							this.s.setAway(message.substring(i));
						if (message.toLowerCase().contains("/me"))
							this.channel.action(message.substring(i).trim());
						if (message.toLowerCase().contains("/nick"))
							this.s.changeNick(message.substring(i).trim());
						if (message.toLowerCase().contains("/quit"))
						{
							this.conman.quit(message.substring(i));
							this.messageListener.disconnect();
							this.messageListener.quit();
							this.alertListener.disconnected();
						}
						if (message.toLowerCase().contains("/join"))
						{
							this.messageListener.disconnect();
							this.conman.quit();
							this.s.join(message.substring(i).trim());
							this.room = message.substring(i);
							this.messageListener.connect();
						}
						if (message.toLowerCase().contains("/msg"))
						{
							if (message.trim().indexOf(" ") != message.trim().lastIndexOf(" "))
							{
								String receipient = message.substring(i + 1);
								String pm = receipient.substring(receipient.indexOf(' '));
								receipient = receipient.substring(0, receipient.indexOf(' '));
								this.s.sayPrivate(receipient, pm);
							}
							else
								return;
						}
						if (message.toLowerCase().contains("/topic"))
							this.channel.setTopic(message.substring(i).trim());
						if (message.toLowerCase().contains("/r"))
							if (IRCPane.lastNick != null)
								this.s.sayPrivate(IRCPane.lastNick, message.substring(i));
						break;
					}
				}
			else
			{
				if (message.toLowerCase().contains("/quit"))
				{
					try
					{
						this.conman.quit();
						this.messageListener.disconnect();
						this.messageListener.quit();
						this.alertListener.disconnected();
					}
					catch (NotYetConnectedException ex)
					{
						IRCEvent event = new IRCEvent()
						{
							@Override
							public Type getType()
							{
								return Type.CONNECTION_LOST;
							}

							@Override
							public Session getSession()
							{
								return s;
							}

							@Override
							public String getRawEventData()
							{
								return "No Internet connection!";
							}
						};
						receiveEvent(event);
						return;
					}
				}
				if (message.toLowerCase().contains("/back"))
					this.s.unsetAway();
				if (message.toLowerCase().contains("/away"))
					this.s.setAway("");
				if (message.toLowerCase().contains("/nick"))
					this.s.changeNick(nick);
				if (message.toLowerCase().contains("/cleartopic"))
					this.channel.setTopic("");
				if (message.toLowerCase().contains("/break"))
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
							return s;
						}

						@Override
						public String getRawEventData()
						{
							return "This is debug!";
						}
					};
					receiveEvent(event);
				}
			}
		}
		else*/
		if (this.channel != null)
			this.channel.say(message);
		else
			System.out.println("No channel!");
	}

	public boolean containsNick(String msg)
	{
		if (msg.toLowerCase().contains(getNick().toLowerCase()))
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
	public String getActualNick()
	{
		return this.nick;
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

	public void setIRCAlertListener(IRCAlertListener listener)
	{
		this.alertListener = listener;
	}

	public void alertAlertListener()
	{
		if (this.alertListener != null)
			this.alertListener.alert();
	}

	public void connect(String network, String room, String nick, IRCMessageListener messageListener)
	{
		this.channel = null;
		this.nick = nick;
		this.p = new Profile(nick);
		this.network = network;
		this.room = room.charAt(0) != '#' ? "#" + room : room;
		this.messageListener = messageListener;
		this.s = null;
		this.conman = new ConnectionManager(p);
		this.conman.requestConnection(network).addIRCEventListener(this);
	}

	public List<String> getUserList()
	{
		return this.channel.getNicks();
	}

	public void quitMessageListener()
	{
		this.messageListener.quit();
	}

	public void quit()
	{
		this.messageListener.disconnect();
		this.conman.quit();
	}

	public void connectAlertListener()
	{
		this.alertListener.connected();
	}
	public List<String> getOps()
	{
		return this.channel.getNicksForMode(null, 'o');
	}
	public List<String> getHops()
	{
		return this.channel.getNicksForMode(null, 'h');
	}
	public void setNick(String nick)
	{
		this.s.changeNick(nick);
	}
	public void revertNick()
	{
		this.s.changeNick(nick);
	}
	public void action(String action)
	{
		this.channel.action(action);
	}
	@SuppressWarnings("unused")
	private void printToken(EventToken token)
	{
		System.out.println("command " + token.command() + " --");
		System.out.println("data " + token.data() + " --");
		System.out.println("hostname " + token.hostName() + " --");
		System.out.println("nick " + token.nick() + " --");
		System.out.println("numeric " + token.numeric() + " --");
		System.out.println("prefix " + token.prefix() + " --");
		System.out.println("username " + token.userName() + " --");
		System.out.println("tostring " + token.args().toString() + " --");
		System.out.println();
	}
	public void clearChat()
	{
		this.messageListener.clearChat();
	}
	public void message(String nick, String message)
	{
		this.s.sayPrivate(nick, message);
	}
	public Session getSession()
	{
		return this.s;
	}
	public void closeChat()
	{
		try
		{
			this.conman.quit();
			this.messageListener.disconnect();
			this.messageListener.quit();
			this.alertListener.disconnected();
		}
		catch (NotYetConnectedException ex)
		{
			IRCEvent event = new IRCEvent()
			{
				@Override
				public Type getType()
				{
					return Type.CONNECTION_LOST;
				}

				@Override
				public Session getSession()
				{
					return s;
				}

				@Override
				public String getRawEventData()
				{
					return "No Internet connection!";
				}
			};
			receiveEvent(event);
			return;
		}
	}
}
