package net.minelord.util.IRC;

import java.nio.channels.NotYetConnectedException;
import java.util.ArrayList;
import java.util.List;

import jerklib.Channel;
import jerklib.ConnectionManager;
import jerklib.EventToken;
import jerklib.Profile;
import jerklib.Session;
import jerklib.events.IRCEvent;
import jerklib.events.JoinCompleteEvent;
import jerklib.events.JoinEvent;
import jerklib.events.KickEvent;
import jerklib.events.MessageEvent;
import jerklib.events.NickChangeEvent;
import jerklib.events.NoticeEvent;
import jerklib.events.PartEvent;
import jerklib.events.QuitEvent;
import jerklib.events.WhoisEvent;
import jerklib.events.modes.ModeAdjustment;
import jerklib.events.modes.ModeEvent;
import jerklib.listeners.IRCEventListener;
import net.minelord.gui.panes.IRCPane;

public class IRCClient implements IRCEventListener
{
	private Channel channel;
	private String network, room, nick;
	private ConnectionManager conman;
	private Profile p;
	private IRCMessageListener messageListener;
	private IRCAlertListener alertListener;
	private Session s;

	public void receiveEvent(final IRCEvent e)
	{
		final String raw = e.getRawEventData();
		String message = "-UNCAUGHT EVENT " + e.getType();
		EventToken token = new EventToken(raw);
		switch (e.getType())
		{
		case EXCEPTION:
			message = "-" + raw;
			alertAlertListener();
			this.messageListener.kicked();
			this.alertListener.disconnected();
			break;
		case CONNECT_COMPLETE:
			this.messageListener.clearChat();
			e.getSession().join(this.room);
			this.s = e.getSession();
			this.alertListener.connected();
			return;
		case JOIN_COMPLETE:
			alertAlertListener();
			JoinCompleteEvent jce = (JoinCompleteEvent) e;
			this.channel = jce.getChannel();
			this.messageListener.connected();
			return;
		case NOTICE:
			NoticeEvent ne = (NoticeEvent) e;
			message = "-[NOTICE] " + ne.byWho() + ": " + ne.getNoticeMessage();
			alertAlertListener();
			break;
		case ERROR:
			message = "-[ERROR] " + token.arg(2);
			alertAlertListener();
			if (message.contains("€¦"))
				message = message.replace("€¦", "...");
			if (messageListener != null)
				messageListener.receiveMessage(message);
			return;
		case CONNECTION_LOST:
			message = "-" + raw;
			alertAlertListener();
			this.messageListener.kicked();
			this.alertListener.disconnected();
			break;
		case CTCP_EVENT:
			return;
		case AWAY_EVENT:
			return;
		case NICK_IN_USE:
			return;
		case SERVER_INFORMATION:
			return;
		case DEFAULT:
			if (raw.contains("KICK"))
			{
				System.out.println(e.getRawEventData());
				KickEvent ke = new KickEvent()
				{
					@Override
					public Type getType()
					{
						return Type.KICK_EVENT;
					}

					@Override
					public Session getSession()
					{
						return e.getSession();
					}

					@Override
					public String getRawEventData()
					{
						return e.getRawEventData();
					}

					@Override
					public String getWho()
					{
						return raw.substring(raw.indexOf("KICK")).substring(6 + room.length(), raw.substring(raw.indexOf("KICK")).indexOf(":") - 1);
					}

					@Override
					public String getUserName()
					{
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public String getMessage()
					{
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public String getHostName()
					{
						return null;
					}

					@Override
					public Channel getChannel()
					{
						return channel;
					}

					@Override
					public String byWho()
					{
						return raw.substring(raw.indexOf("KICK"));
					}
				};
				String sender = raw.substring(1, raw.indexOf('!'));
				String kick = raw.substring(raw.indexOf("KICK")), reason = kick.substring(kick.indexOf(":") + 1);
				String kicked = kick.substring(6 + this.room.length(), kick.indexOf(":") - 1);
				boolean youKickee = false;
				if (kicked.equals(getNick()))
				{
					kicked = "You were";
					youKickee = true;
				}
				else
					kicked = kicked + " was";
				message = "*" + kicked + " kicked by " + (sender.equals(getNick()) ? "you" : sender);
				if (kicked.equalsIgnoreCase(sender))
					if (sender.equalsIgnoreCase(getNick()))
						message = "*You kicked yourself lol";
					else
						message = "*" + kick + " kicked themselves lol";
				if (reason.length() > 0)
					message += " (" + reason + ")";
				this.messageListener.updateUserList(IRCPane.sortType);
				if (youKickee)
				{
					if (this.messageListener != null)
						this.messageListener.kicked();
					if (this.alertListener != null)
						this.alertListener.disconnected();
				}
				break;
			}
			return;
		case SERVER_VERSION_EVENT:
			return;
		case MOTD:
			return;
		case NICK_LIST_EVENT:
			return;
		case MODE_EVENT:
			if (raw.contains("+nt") || raw.contains("+x"))
				return;
			ModeEvent me = (ModeEvent) e;
			char mode = ((ArrayList<ModeAdjustment>) (me.getModeAdjustments())).get(0).getMode();
			String setBy = me.setBy(),
			modeChanged = ((ArrayList<ModeAdjustment>) (me.getModeAdjustments())).get(0).getArgument();
			if (modeChanged.equals(getNick()))
				modeChanged = "you";
			message = "-TELL MOOMOOHK HE MUST CATCH " + mode;
			if (((ArrayList<ModeAdjustment>) (me.getModeAdjustments())).get(0).getAction() == ModeAdjustment.Action.PLUS)
			{
				if (mode == 'b')
					message = setBy + " banned " + modeChanged;
				if (mode == 'h')
					message = setBy + " promoted " + modeChanged + " to half operator";
				if (mode == 'o')
					message = setBy + " promoted " + modeChanged + " to operator";
				if (mode == 'q')
					message = setBy + " promoted " + modeChanged + " to channel owner";
				if (mode == 'a')
					message = setBy + " promoted " + modeChanged + " to administrator";
				if (mode == 'v')
					message = modeChanged.equals("you") ? "You were granted voice by " + setBy : modeChanged + " was granted voice by " + setBy;
			}
			else
			{
				if (mode == 'b')
					message = setBy + " unbanned " + modeChanged;
				if (mode == 'h')
					message = setBy + " demoted " + modeChanged + " from half operator";
				if (mode == 'o')
					message = setBy + " demoted " + modeChanged + " from operator";
				if (mode == 'q')
					message = setBy + " demoted " + modeChanged + " from channel owner";
				if (mode == 'a')
					message = setBy + " demoted " + modeChanged + " from administrator";
				if (mode == 'v')
					message = modeChanged.equals("you") ? "You had your voice removed by " + setBy : modeChanged + " had their voice removed by " + setBy;
			}
			if (message.charAt(0) != '-')
				message = "*" + message;
			break;
		case TOPIC:
			this.messageListener.updateTopic();
			this.alertListener.topicChange();
			return;
		case CHANNEL_MESSAGE:
			MessageEvent me3 = (MessageEvent) e;
			message = me3.getNick() + ": " + me3.getMessage();
			break;
		case PRIVATE_MESSAGE:
			MessageEvent me2 = (MessageEvent) e;
			alertAlertListener();
			message = "[" + me2.getNick() + " -> You]: " + me2.getMessage();
			break;
		case JOIN:
			JoinEvent je = (JoinEvent) e;
			message = "*" + je.getNick() + " joined the room";
			this.messageListener.updateUserList(IRCPane.sortType);
			break;
		case QUIT:
			QuitEvent qe = (QuitEvent) e;
			message = "*" + qe.getNick() + " quit" + (raw.substring(raw.indexOf("QUIT :") + 6).trim().length() > 0 ? " (" + raw.substring(raw.indexOf("QUIT :") + 6).trim() + ")" : "");
			this.messageListener.updateUserList(IRCPane.sortType);
			break;
		case PART:
			PartEvent pe = (PartEvent) e;
			message = "*" + pe.getWho() + " parted";
			this.messageListener.updateUserList(IRCPane.sortType);
			break;
		case NICK_CHANGE:
			NickChangeEvent nce = (NickChangeEvent) e;
			boolean you = false;
			if (nce.getNewNick().equals(getNick()))
				you = true;
			message = "*" + (you ? "You" : nce.getOldNick()) + " changed " + (you ? "your" : "their") + " nick to " + nce.getNewNick();
			this.messageListener.updateUserList(IRCPane.sortType);
			break;
		case WHOIS_EVENT:
			/*
			 * WhoisEvent whois=(WhoisEvent)e;
			 * message="-<b><u>Whois "+token.args().get(2)+"</u></b><br>";
			 * message+="Channels: <br>"; if(whois.getChannelNames().size()==0)
			 * message+="&nbsp;&nbsp;(None)"; else {
			 * message+="&nbsp;&nbsp;"+whois
			 * .getChannelNames().get(0).substring(whois
			 * .getChannelNames().get(0).indexOf("#")); for(int i=0;
			 * i<whois.getChannelNames().size(); i++)
			 * message+=", "+whois.getChannelNames
			 * ().get(i).substring(whois.getChannelNames().get(i).indexOf("#"));
			 * } System.out.println(whois.getHost());
			 */
			WhoisEvent wie = (WhoisEvent) e;
			List<String> userChannels = wie.getChannelNames();
			message = "-<HR WIDTH=\"50%\" SIZE=\"3\" NOSHADE>";
			boolean idle = wie.isIdle();
			message+= " <b><u>WhoIs information about " + wie.getUser() + "</u></b><br>" + "&nbsp;&nbsp;&nbsp;Username: " + wie.getUser() + " (" + wie.getHost() + ")<br>" + "&nbsp;&nbsp;&nbsp;Connected at: " + wie.signOnTime() + "<br>" + "&nbsp;&nbsp;&nbsp;Channels: ";

			for (int i = 0; i < userChannels.size(); i++)
			{ 
				int size = userChannels.size();
				if (i == (size - 1))
					message += userChannels.get(i).substring(userChannels.get(i).indexOf("#")) + "<br>"; 
				else
					message += userChannels.get(i).substring(userChannels.get(i).indexOf("#")) + ", ";
			}

			if (idle)
				message+= " * " + wie.getNick() + " is currently idle<br>" + " * Idle for the past " + wie.secondsIdle() + " seconds<br>";
			message+= "<HR WIDTH=\"50%\" SIZE=\"3\" NOSHADE>";
			break;
		}
		if (message.contains("€¦"))
			message = message.replace("€¦", "...");
		if (messageListener != null)
			messageListener.receiveMessage(message);
	}

	public void send(String message)
	{
		if (message == null || message.length() == 0)
			return;
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
		this.alertListener.hideGUI();
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

	public Channel getChannel()
	{
		return this.channel;
	}

	public void closeChat()
	{
		try
		{
			if (conman != null)
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
