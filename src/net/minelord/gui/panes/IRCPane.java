/*
 * This file is part of FTB Launcher.
 *
 * Copyright © 2012-2013, FTB Launcher Contributors <https://github.com/Slowpoke101/FTBLaunch/>
 * FTB Launcher is licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.minelord.gui.panes;

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml3;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import net.minelord.log.Logger;
import net.minelord.util.OSUtils;
import net.minelord.util.IRC.IRCClient;
import net.minelord.util.IRC.IRCCommand;
import net.minelord.util.IRC.IRCMessageListener;
import net.minelord.util.IRC.commands.ActionIRCCommand;
import net.minelord.util.IRC.commands.AlertIRCCommand;
import net.minelord.util.IRC.commands.ClearChatIRCCommand;
import net.minelord.util.IRC.commands.DebugIRCCommand;
import net.minelord.util.IRC.commands.HelpIRCCommand;
import net.minelord.util.IRC.commands.JoinIRCCommand;
import net.minelord.util.IRC.commands.NickIRCCommand;
import net.minelord.util.IRC.commands.PrivMessageIRCCommand;
import net.minelord.util.IRC.commands.QuitIRCCommand;
import net.minelord.util.IRC.commands.ReplyIRCCommand;
import net.minelord.util.IRC.commands.WhoisIRCCommand;

public class IRCPane extends JPanel implements IRCMessageListener, ILauncherPane
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static IRCClient client = new IRCClient();
	public static String nick, status = "Disconnected", lastNick = null;
	public static JEditorPane text;
	public static JScrollPane scroller, userScroller;
	public static JTextField input, nickSelect;
	public static JPanel nickSelectPane, chatPane;
	public static JList userList;
	public static HTMLDocument doc;
	public static ArrayList<String> IRCLog;
	public static HTMLEditorKit kit;
	public static JLabel topic;
	public static JPopupMenu popup;
	public static Rectangle scrollerWithoutTopicWithUserlist = new Rectangle(22, 10, 598, 258), scrollerWithTopicWithUserlist = new Rectangle(22, 45, 598, 223), userScrollWithTopic = new Rectangle(620, 45, 210, 225), userScrollerWithoutTopic = new Rectangle(620, 10, 210, 260),
	topicBounds = new Rectangle(20, 5, 810, 40), scrollerWithoutTopicWithoutUserlist = new Rectangle(20, 10, 810, 260), scrollerWithTopicWithoutUserlist = new Rectangle(20, 45, 810, 225);
	public static String actionColor = "#5194ED", receiveColor = "#858585", sendColor = "#A1A1A1", nickalertColor = "#F74848", errorColor = "red", pmColor = "#B225CF";
	public static boolean quit = false, showTopic = true, showUserList = true;
	public static ArrayList<String> lastCommands = new ArrayList<String>();
	public static int lastCommandSelector = 0, sortType = 0;
	public static HashMap<Character, String> colorMap = new HashMap<Character, String>();
	public static IRCPane instance = null;

	static
	{
		loadColors();
		loadCommands();
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
		{
			public void run()
			{
				if (text != null)
				{
					UIDefaults defaults = new UIDefaults();
					Object painter = UIManager.get("EditorPane[Enabled].backgroundPainter");
					defaults.put("EditorPane[Enabled].backgroundPainter", painter);
					text.putClientProperty("Nimbus.Overrides", defaults);
					text.putClientProperty("Nimbus.Overrides.InheritDefaults", true);
					text.setBackground(null);
				}
			}
		}));
	}

	public IRCPane()
	{
		super();
		this.setBorder(new EmptyBorder(5, 5, 5, 5));
		this.setLayout(null);
		nickSelectPane = new JPanel();
		nickSelectPane.setLayout(null);
		nickSelectPane.setBounds(325, 70, 200, 200);
		TitledBorder title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Pick a nick");
		title.setTitleJustification(TitledBorder.RIGHT);
		nickSelectPane.setBorder(title);
		nickSelect = new JTextField();
		final JButton done = new JButton("Done");
		nickSelect.addKeyListener(new KeyListener()
		{
			@Override
			public void keyTyped(KeyEvent arg0)
			{
			}

			@Override
			public void keyReleased(KeyEvent arg0)
			{
				String nick = nickSelect.getText();
				boolean success = true;
				if (nick.length() > 0)
				{
					if (nick.substring(0, 1).matches("[0-9]") || nick.charAt(0) == '-' || nick.contains(" "))
						success = false;
				}
				else
					success = false;
				if (success)
					IRCPane.nick = nick;
				done.setEnabled(success);
			}

			@Override
			public void keyPressed(KeyEvent arg0)
			{
				if (arg0.getKeyCode() == 10)
					done.doClick();
			}
		});
		nickSelect.setBounds(50, 65, 100, 30);
		done.setBounds(50, 125, 100, 30);
		done.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				remove(nickSelectPane);
				repaint();
				startClient(nickSelect.getText());
			}
		});

		done.setEnabled(false);
		nickSelectPane.add(nickSelect);
		nickSelectPane.add(done);
		add(nickSelectPane);
		addFocusListener(new FocusListener()
		{
			@Override
			public void focusLost(FocusEvent paramFocusEvent)
			{
			}

			@Override
			public void focusGained(FocusEvent paramFocusEvent)
			{
				if (nickSelectPane.getParent() != null)
					nickSelect.requestFocus();
				if (input != null && input.getParent() != null)
					input.requestFocus();
			}
		});
		instance = this;
	}

	public void disconnect()
	{
		status = "Disconnecting...";
		TitledBorder title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), status);
		title.setTitleJustification(TitledBorder.RIGHT);
		if (userScroller != null)
			userScroller.setBorder(title);
		else
			scroller.setBorder(title);
		input.setEnabled(false);
	}

	public void connect()
	{
		text.setText("");
		status = "Connecting...";
		TitledBorder title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), status);
		title.setTitleJustification(TitledBorder.RIGHT);
		scroller.setBorder(title);
		if (topic != null && topic.getParent() == this)
		{
			showTopic(false);
			remove(topic);
		}
		if (userScroller != null && userScroller.getParent() == this)
		{
			showUserList(false);
			remove(userScroller);
		}
	}

	public void quit()
	{
		status = "Disconnected";
		lastNick = null;
		TitledBorder title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), status);
		title.setTitleJustification(TitledBorder.RIGHT);
		if (userScroller != null)
			userScroller.setBorder(title);
		else
			scroller.setBorder(title);
		input.setText("");
		input.setEnabled(false);
		remove(scroller);
		remove(input);
		if (topic != null && topic.getParent() == this)
			remove(topic);
		if (userScroller != null && userScroller.getParent() == this)
			remove(userScroller);
		add(nickSelectPane);
		quit = false;
		repaint();
	}

	public void updateTopic()
	{
		if (client.getTopic().trim().length() > 0)
		{
			if (topic == null)
				topic = new JLabel();
			topic.setText(client.getTopic());
			TitledBorder title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Topic set by " + client.getTopicSetter());
			title.setTitleJustification(TitledBorder.LEFT);
			topic.setBorder(title);
			// add(topic);

		}
		else
		{
			showTopic(false);
		}
		repaint();
	}

	public void updateUserList(int type)
	{
		sortType = type;
		switch (sortType)
		{
		case 0:
			userList.setListData(client.getUserList().toArray());
			break;
		case 1:
			Object[] temp = client.getUserList().toArray();
			Arrays.sort(temp);
			userList.setListData(temp);
			break;
		case 2:
			ArrayList<String> nicks = new ArrayList<String>();
			for (String nick : client.getOps())
				nicks.add(nick);
			for (String nick : client.getHops())
				nicks.add(nick);
			for (String nick : client.getUserList())
				if (!nicks.contains(nick))
					nicks.add(nick);
			userList.setListData(nicks.toArray());
			break;
		case 3:
			userList.setListData(new Object[0]);
			break;
		}
	}

	public void connected()
	{
		scroller.setBounds(scrollerWithoutTopicWithUserlist);
		scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		status = "Connected";
		client.connectAlertListener();
		TitledBorder title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Connected");
		title.setTitleJustification(TitledBorder.RIGHT);
		userList = new JList(client.getUserList().toArray());
		userScroller = new JScrollPane(userList);
		userScroller.setBounds(userScrollerWithoutTopic);
		userList.setBounds(0, 0, 210, 250);
		userList.setBackground(Color.gray);
		userList.setForeground(Color.gray.darker().darker().darker());
		userScroller.setBorder(title);
		userScroller.getVerticalScrollBar().setUnitIncrement(5);
		scroller.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), ""));
		if (client.getTopic().trim().length() > 0)
		{
			topic = new JLabel(client.getTopic());
			scroller.setBounds(scrollerWithTopicWithUserlist);
			userScroller.setBounds(userScrollWithTopic);
			userList.setBounds(0, 0, 210, 225);
			title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Topic set by " + client.getTopicSetter());
			title.setTitleJustification(TitledBorder.LEFT);
			topic.setBorder(title);
			topic.setBounds(topicBounds);
			add(topic);
		}
		else
			topic = new JLabel("");
		input.setEnabled(true);
		input.requestFocus();
		final JPopupMenu userPopup = new JPopupMenu();
		JLabel help = new JLabel("Politely ask for help");
		JLabel message = new JLabel("Message");
		JLabel sortNormal = new JLabel("Normal");
		JLabel sortAlphabetical = new JLabel("Alphabetical");
		JLabel sortRoles = new JLabel("Roles");

		help.addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				userPopup.setVisible(false);
				sendMessage("/me kicks " + userList.getModel().getElementAt(userList.getSelectedIndex()) + " in the shins");
				sendMessage("I need help you pleb");
			}

			public void mouseReleased(MouseEvent e)
			{
			}
		});
		message.addMouseListener(new MouseAdapter()
		{

			@Override
			public void mouseReleased(MouseEvent paramMouseEvent)
			{
				userPopup.setVisible(false);
				input.setText("/msg " + userList.getModel().getElementAt(userList.getSelectedIndex()) + (input.getText().length() > 0 && input.getText().charAt(0) == ' ' ? input.getText() : " " + input.getText()));
				input.select(0, ("/msg " + userList.getModel().getElementAt(userList.getSelectedIndex()) + (input.getText().length() > 0 && input.getText().charAt(0) == ' ' ? "" : " ")).length());
				input.requestFocus();
			}
		});
		sortNormal.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseReleased(MouseEvent paramMouseEvent)
			{
				userPopup.setVisible(false);
				updateUserList(0);
			}
		});
		sortAlphabetical.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseReleased(MouseEvent paramMouseEvent)
			{
				userPopup.setVisible(false);
				updateUserList(1);
			}
		});
		sortRoles.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseReleased(MouseEvent paramMouseEvent)
			{
				userPopup.setVisible(false);
				updateUserList(2);
			}
		});
		userPopup.add(help);
		userPopup.add(message);
		userPopup.add(sortNormal);
		userPopup.add(sortAlphabetical);
		userPopup.add(sortRoles);

		userList.addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				check(e);
			}

			public void mouseReleased(MouseEvent e)
			{
				check(e);
			}

			public void check(MouseEvent e)
			{
				userList.setSelectedIndex(userList.locationToIndex(e.getPoint()));
				userPopup.show(userList, e.getX(), e.getY());
			}
		});
		add(userScroller);

		final JPopupMenu textPopup = new JPopupMenu();
		JLabel copy = new JLabel("Copy");
		copy.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseReleased(MouseEvent paramMouseEvent)
			{
				textPopup.setVisible(false);
				if (text.getSelectedText() != null && text.getSelectedText().length() != 0)
				{
					StringSelection selection = new StringSelection(text.getSelectedText());
					Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
					clipboard.setContents(selection, selection);
				}
			}
		});
		textPopup.add(copy);
		text.addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
			}

			public void mouseReleased(MouseEvent e)
			{
				if (SwingUtilities.isRightMouseButton(e))
					textPopup.show(text, e.getX(), e.getY());
			}
		});
		add(userScroller);
		repaint();
	}

	public static void sendMessage(String message)
	{
		/*
		 * for (int i = 0; i < message.length(); i++) for (Entry<Character,
		 * String> entry : colorMap.entrySet()) if (("" +
		 * topic.getText().charAt(i)).equals(entry.getKey()))
		 * System.out.println(entry.getValue());
		 */
		if (message.trim().length() > 0)
		{
			if (message.charAt(0) == '/')
			{
				message = message.trim();
				IRCCommand command = IRCCommand.getCommand(IRCCommand.parseCommand(message));
				if (command == null)
				{
					IRCLog.add("<font color=\"" + errorColor + "\">" + escapeHtml3("Unknown command!").replaceAll("\"<\"", "<") + "</font><br>");
					refreshLogs();
					return;
				}
				command.checkAndExecute(client, IRCCommand.parseParams(message));
				if (command.getMessage() != null)
				{
					String color = command.getColor();
					if (color == null)
						color = sendColor;
					IRCLog.add("<font color=\"" + color + "\">" + (!command.containsHTML() ? escapeHtml3(command.getMessage()) : command.getMessage()) + "</font><br>");
					refreshLogs();
				}
				/*
				 * if (client.parseCommand(message) == null) {
				 * IRCLog.add("<font color=\"" + errorColor + "\">" +
				 * escapeHtml3("Unknown command!").replaceAll("\"<\"", "<") +
				 * "</font><br>"); refreshLogs(); return; } else { if
				 * (client.parseCommand(message).length() == 0) return; String
				 * color = sendColor, actualMessage =
				 * client.parseCommand(message); input.setText(""); if
				 * (client.parseCommand(message).charAt(0) == '*') color =
				 * actionColor; if (client.parseCommand(message).charAt(0) ==
				 * '-') { color = errorColor; actualMessage =
				 * actualMessage.substring(1); } if
				 * (client.parseCommand(message).charAt(0) == '[') color =
				 * pmColor; if (client.parseCommand(message) != null &&
				 * client.parseCommand(message).length() == 0) {
				 * input.setText(""); return; } IRCLog.add("<font color=\"" +
				 * color + "\">" +
				 * escapeHtml3(actualMessage).replaceAll("\"<\"", "<") +
				 * "</font><br>"); refreshLogs(); }
				 */
			}
			else
			{
				client.send(message);
				message=parseLinks(message, sendColor);
				IRCLog.add("<font color=\"" + sendColor + "\">" +client.getNick() + ": " + message + "</font><br>");
				//IRCLog.add("<font color=\"" + sendColor + "\">" + escapeHtml3(client.getNick() + ": " + message).replaceAll("\"<\"", "<") + "</font><br>");
				refreshLogs();
			}
		}

	}

	public static String parseColors(String message)
	{
		for (int i = 0; i < message.length(); i++)
			for (Entry<Character, String> entry : colorMap.entrySet())
				if (("" + message.charAt(i)).equals(entry.getKey()))
					System.out.println(entry.getValue());
		return null;
	}

	public void receiveMessage(String message)
	{
		if (message.charAt(0) == '-')
		{
			message = message.substring(1);
			IRCLog.add("<font color=\"" + errorColor + "\">" + escapeHtml3(message).replaceAll("\"<\"", "<") + "</font><br>");
			refreshLogs();
			return;
		}
		else
		{
			String color = receiveColor;
			/*
			 * if (message.toLowerCase().contains("changed their nick to " +
			 * client.getNick())) { nick = client.getNick(); return; }
			 */
			if (message.charAt(0) == '[')
				color = pmColor;
			if (message.charAt(0) == '*')
				color = actionColor;
			if (client.containsNick(message))
			{
				color = nickalertColor;
				client.alertAlertListener();
			}
			if (message.charAt(0) == '[')
				lastNick = message.substring(1, message.indexOf(" "));
			message=parseLinks(message, color);
			IRCLog.add("<font color=\"" + color + "\">" + message + "</font><br>");
			refreshLogs();
		}
	}
	public static String parseLinks(String message, String color)
	{
		String regex = "\\b(((ht|f)tp(s?)\\:\\/\\/|~\\/|\\/)|www.)(\\w+:\\w+@)?(([-\\w]+\\.)+(com|org|net|gov|mil|biz|info|mobi|name|aero|jobs|museum|travel|[a-z]{2}))(:[\\d]{1,5})?" + "(((\\/([-\\w~!$+|.,=]|%[a-f\\d]{2})+)+|\\/)+|\\?|#)?((\\?([-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)(&(?:[-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?" + "([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)*)*(#([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)?\\b";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(message);
		message=escapeHtml3(message).replaceAll("\"<\"", "<");
		String temp=message, temp2="";
		boolean found=false;
		while (m.find())
		{
			found=true;
			String url = m.group();
			if (url.startsWith("(") && url.endsWith(")"))
				url = url.substring(1, url.length() - 1);
			temp2+=temp.substring(0, temp.indexOf(url));
			temp=temp.substring(temp.indexOf(url));
			temp2+="</font><font color=\"#1C6FF\"><u><a href="+temp.substring(0, url.length()).trim()+">"+temp.substring(0, url.length())+"</a></u><font color=\""+color+"\">";
			temp=temp.substring(url.length());
			message=temp2;
		}
		if(found)
			message+=temp;
		return message;
	}
	public void startClient(String nick)
	{
		IRCLog = new ArrayList<String>();

		text = new JEditorPane("text/html", "<HTML>");
		text.setEditable(false);
		kit = new HTMLEditorKit();
		text.setEditorKit(kit);
		client.connect("irc.liberty-unleashed.co.uk", "#minelord-modpack", nick, this);
		scroller = new JScrollPane(text);
		text.setEditable(false);
		connect();
		scroller.setBounds(20, 20, 810, 250);
		add(scroller);
		input = new JTextField();
		input.setBounds(20, 270, 810, 30);
		Color bgColor = Color.gray.darker().darker();
		UIDefaults defaults = new UIDefaults();
		defaults.put("EditorPane[Enabled].backgroundPainter", bgColor);
		text.putClientProperty("Nimbus.Overrides", defaults);
		text.putClientProperty("Nimbus.Overrides.InheritDefaults", true);
		text.setBackground(bgColor);
		input.setBackground(Color.gray);
		input.setForeground(Color.gray.darker().darker().darker());
		input.setEnabled(false);
		text.addHyperlinkListener(new HyperlinkListener()
		{
			@Override
			public void hyperlinkUpdate(HyperlinkEvent event)
			{
				if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
				{
					OSUtils.browse(event.getURL().toString());
				}
			}
		});
		text.addFocusListener(new FocusListener()
		{

			@Override
			public void focusLost(FocusEvent paramFocusEvent)
			{
			}

			@Override
			public void focusGained(FocusEvent paramFocusEvent)
			{
				input.requestFocus();
			}
		});
		scroller.setViewportView(text);
		add(input);
		input.addKeyListener(new KeyListener()
		{

			@Override
			public void keyTyped(KeyEvent arg0)
			{
			}

			@Override
			public void keyReleased(KeyEvent arg0)
			{
				if (arg0.getKeyCode() == 10)
				{
					if (input.getText().length() > 0)
					{
						lastCommandSelector = lastCommands.size();
						lastCommands.add(input.getText());
					}
					sendMessage(input.getText());
					input.setText("");
				}
				if (arg0.getKeyCode() == 27)
					input.setText("");
				if (arg0.getKeyCode() == 17)
				{
					int before = input.getText().length();
					input.setText(complete(input.getText()));
					input.select(input.getText().length() - complete(input.getText()).length() + before, input.getText().length());
				}
				if (arg0.getKeyCode() == 38)
					if (lastCommandSelector > 0)
					{
						lastCommandSelector--;
						input.setText(lastCommands.get(lastCommandSelector));
					}
				if (arg0.getKeyCode() == 40)
					if (lastCommandSelector < lastCommands.size())
					{
						lastCommandSelector++;
						if (lastCommandSelector == lastCommands.size())
							input.setText("");
						if (lastCommandSelector < lastCommands.size())
							input.setText(lastCommands.get(lastCommandSelector));
						return;
					}
			}

			@Override
			public void keyPressed(KeyEvent arg0)
			{
			}
		});
	}

	synchronized public static void refreshLogs()
	{
		doc = new HTMLDocument();
		if (doc != null && text != null && !text.getDocument().equals(doc))
			text.setDocument(doc);
		StringBuilder logHTML = new StringBuilder();
		for (String message : IRCLog)
		{
			logHTML.append(message);
		}
		addHTML(logHTML.toString());
	}

	@Override
	public void clearChat()
	{
		IRCLog = new ArrayList<String>();
		refreshLogs();
	}

	public static void addHTML(String html)
	{
		synchronized (kit)
		{
			try
			{
				kit.insertHTML(doc, doc.getLength(), html, 0, 0, null);
			}
			catch (BadLocationException ignored)
			{
				Logger.logError(ignored.getMessage(), ignored);
			}
			catch (IOException ignored)
			{
				Logger.logError(ignored.getMessage(), ignored);
			}
			text.setCaretPosition(text.getDocument().getLength());
		}
	}

	public static String complete(String incomplete)
	{
		String temp = incomplete.toLowerCase().trim();
		if (temp.length() == 0)
			return incomplete;
		String before = "";
		if (temp.contains(" "))
		{
			before = incomplete.substring(0, incomplete.lastIndexOf(" ")) + " ";
			temp = temp.substring(temp.lastIndexOf(' ') + 1);
		}
		for (int i = 0; i < userList.getModel().getSize(); i++)
		{
			if (userList.getModel().getElementAt(i).toString().toLowerCase().startsWith(temp))
				return before + userList.getModel().getElementAt(i).toString();
		}
		return incomplete;
	}

	@Override
	public void onVisible()
	{
		if (nickSelectPane.getParent() != null)
			nickSelect.requestFocus();
		if (input != null && input.getParent() != null)
			input.requestFocus();
	}

	@Override
	public void kicked()
	{
		client.quit();
		status = "Disconnected";
		TitledBorder title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), status);
		title.setTitleJustification(TitledBorder.RIGHT);
		if (userScroller != null)
		{
			userScroller.setBorder(title);
			userList.setListData(new Object[0]);
		}
		else
			scroller.setBorder(title);
		input.setText("");
		input.setEnabled(false);
		quit = true;
	}

	public static void showTopic(boolean f)
	{
		showTopic = f;
		updateBounds();
	}

	public static void showUserList(boolean f)
	{
		showUserList = f;
		updateBounds();
	}

	public static void updateBounds()
	{
		if (topic.getText().length() == 0)
			showTopic = false;
		topic.setVisible(showTopic);
		userScroller.setVisible(showUserList);
		if (topic != null && !showTopic && !showUserList)
			scroller.setBounds(scrollerWithoutTopicWithoutUserlist);
		if (topic != null && !showTopic && showUserList)
		{
			scroller.setBounds(scrollerWithoutTopicWithUserlist);
			userScroller.setBounds(userScrollerWithoutTopic);
		}
		if (topic != null && showTopic && showUserList)
		{
			scroller.setBounds(scrollerWithTopicWithUserlist);
			userScroller.setBounds(userScrollWithTopic);
		}
		if (topic != null && showTopic && !showUserList)
			scroller.setBounds(scrollerWithTopicWithoutUserlist);
		if (topic != null && !showUserList)
		{
			TitledBorder title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), status);
			title.setTitleJustification(TitledBorder.RIGHT);
			scroller.setBorder(title);
		}
		else
			scroller.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), ""));
		userList.repaint();
		scroller.repaint();
	}

	public static void loadColors()
	{
		colorMap.put('\300', "white");
		colorMap.put('\301', "black");
		colorMap.put('\302', "dark_blue");
		colorMap.put('\303', "dark_green");
		colorMap.put('\304', "red");
		colorMap.put('\305', "brown");
		colorMap.put('\306', "purple");
		colorMap.put('\307', "olive");
		// colorMap.put('\308', "yellow");
		// colorMap.put('\309', "green");
		colorMap.put('\310', "'teal");
		colorMap.put('\311', "cyan");
		colorMap.put('\312', "blue");
		colorMap.put('\313', "magenta");
		colorMap.put('\314', "dark_gray");
		colorMap.put('\315', "light_gray");
	}

	public static void loadCommands()
	{
		ArrayList<IRCCommand> commands = new ArrayList<IRCCommand>();
		commands.add(new HelpIRCCommand("/help", null, "I think we know what this does."));
		commands.add(new ActionIRCCommand("/me", null, "Performs and action."));
		commands.add(new NickIRCCommand("/nick", null, "Changes your nickname. Don't include parameters to revert your nick."));
		commands.add(new ClearChatIRCCommand("/clear", null, "Clears the chat area."));
		commands.add(new PrivMessageIRCCommand("/msg", null, "Sends a private message."));
		commands.add(new ReplyIRCCommand("/r", null, "Replies by private message to the last person that PM'd you."));
		commands.add(new QuitIRCCommand("/quit", "Quitting...", "Disconnects you from chat"));
		commands.add(new WhoisIRCCommand("/whois", null, "Submits a whois request for a provided nick."));
		commands.add(new JoinIRCCommand("/join", null, "Switches IRC channels."));
		commands.add(new DebugIRCCommand("/break", null, "I don't know what you're talking about."));
		commands.add(new AlertIRCCommand("/alert", null, "I still don't know what you're talking about."));
		for (int i = 0; i < commands.size(); i++)
			IRCCommand.add(commands.get(i));
	}
}