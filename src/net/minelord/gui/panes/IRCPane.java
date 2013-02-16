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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIDefaults;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import net.minelord.log.Logger;
import net.minelord.util.IRCClient;
import net.minelord.util.IRCMessageListener;
import net.minelord.util.OSUtils;

public class IRCPane extends JPanel implements IRCMessageListener, ILauncherPane
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static IRCClient client = new IRCClient();
	public static String nick;
	public static JEditorPane text;
	public static JScrollPane scroller, userScroller;
	public static JTextField input;
	public static JPanel nickSelectPane, chatPane;
	public static JList userList;
	public static HTMLDocument doc;
	public static ArrayList<String> IRCLog;
	public static HTMLEditorKit kit;
	public static JLabel topic;
	public static JPopupMenu popup;

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
		final JTextField nickSelect = new JTextField();
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
	}

	public void disconnect()
	{
		TitledBorder title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Disconnecting...");
		title.setTitleJustification(TitledBorder.RIGHT);
		userScroller.setBorder(title);
		input.setText("");
		input.setEnabled(false);
	}

	public void connect()
	{
		text.setText("");
		TitledBorder title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Connecting...");
		title.setTitleJustification(TitledBorder.RIGHT);
		scroller.setBorder(title);
	}

	public void quit()
	{
		TitledBorder title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Disconnected");
		title.setTitleJustification(TitledBorder.RIGHT);
		userScroller.setBorder(title);
		input.setText("");
		input.setEnabled(false);
		remove(scroller);
		remove(input);
		remove(topic);
		remove(userScroller);
		add(nickSelectPane);
		revalidate();
		repaint();
	}

	public void updateTopic()
	{
		topic.setText(client.getTopic());
	}

	public void updateUserList()
	{
		userList.setListData(client.getUserList().toArray());
	}

	public void connected()
	{
		scroller.setBounds(20, 20, 600, 250);
		scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		TitledBorder title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Connected");
		title.setTitleJustification(TitledBorder.RIGHT);
		userList = new JList(client.getUserList().toArray());
		userScroller = new JScrollPane(userList);
		userScroller.setBounds(620, 20, 210, 250);
		userList.setBounds(0, 0, 210, 250);
		userList.setBackground(Color.gray);
		userList.setForeground(Color.gray.darker().darker().darker());
		userScroller.setBorder(title);
		scroller.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), ""));
		if (client.getTopic().trim().length() > 0)
		{
			topic = new JLabel(client.getTopic());
			scroller.setBounds(20, 45, 600, 225);
			userScroller.setBounds(620, 45, 210, 225);
			userList.setBounds(0, 0, 210, 225);
			title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Topic set by " + client.getTopicSetter());
			title.setTitleJustification(TitledBorder.LEFT);
			topic.setBorder(title);
			topic.setBounds(20, 5, 810, 40);
			add(topic);
		}
		input.setEnabled(true);
		final JPopupMenu popup = new JPopupMenu();
		JLabel help = new JLabel("Politely ask for help");
		help.addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				popup.setVisible(false);
				sendMessage("/me kicks " + userList.getModel().getElementAt(userList.getSelectedIndex())+" in the shins");
				sendMessage("I need help you pleb");
			}

			public void mouseReleased(MouseEvent e)
			{
			}
		});
		popup.add(help);
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
				if (e.isPopupTrigger())
				{
					userList.setSelectedIndex(userList.locationToIndex(e.getPoint()));
					popup.show(userList, e.getX(), e.getY());
				}
			}
		});
		add(userScroller);
		repaint();
	}

	public void receiveMessage(String message)
	{
		if (message.toLowerCase().contains("changed their nick to " + client.getNick()))
			return;
		String color = "#9E9E9E";
		if (message.charAt(0) == '*')
			color = "#ED6DC5";
		if (client.containsNick(message))
		{
			color = "#F74848";
			client.alertAlertListener();
		}
		IRCLog.add("<font color=\"" + color + "\">" + escapeHtml3(message).replaceAll("\"<\"", "<") + "</font><br>");
		refreshLogs();
	}

	public void startClient(String nick)
	{
		IRCLog = new ArrayList<String>();

		text = new JEditorPane("text/html", "<HTML>");
		text.setEditable(false);
		kit = new HTMLEditorKit();
		text.setEditorKit(kit);
		client.connect("irc.liberty-unleashed.co.uk", "#Minelord", nick, this);
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
					sendMessage(input.getText());
					input.setText("");
				}
				if (arg0.getKeyCode() == 27)
					input.setText("");
			}

			@Override
			public void keyPressed(KeyEvent arg0)
			{
				// TODO Auto-generated method stub

			}
		});
	}

	public static void sendMessage(String message)
	{
		if(message.trim().length()==0)
			return;
		client.send(message);
		if (message.length() > 0 && message.charAt(0) == '/')
		{
			if (client.parseCommand(message) == null)
			{
				input.setText("");
				IRCLog.add("<font color=\"red\">Unknown command!</font><br>");
				refreshLogs();
				return;
			}
			if (client.parseCommand(message).length() == 0)
			{
				input.setText("");
				return;
			}
			if (client.parseCommand(message) != null)
				IRCLog.add("<font color=\"#ED6DC5\">" + escapeHtml3(client.parseCommand(message)).replaceAll("\"<\"", "<") + "</font><br>");
			refreshLogs();
		}
		else
		{
			IRCLog.add("<font color=\"white\">" + escapeHtml3(client.getNick() + ": " + message).replaceAll("\"<\"", "<") + "</font><br>");
			refreshLogs();
		}
	}

	synchronized public static void refreshLogs()
	{
		doc = new HTMLDocument();
		text.setDocument(doc);
		StringBuilder logHTML = new StringBuilder();
		for (String message : IRCLog)
		{
			logHTML.append(message);
		}
		addHTML(logHTML.toString());
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

	@Override
	public void onVisible()
	{
	}
}