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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import net.minelord.log.Logger;
import net.minelord.util.IRCBot;
import net.minelord.util.IRCMessageListener;
import net.minelord.util.OSUtils;

public class IRCPane extends JPanel implements IRCMessageListener, ILauncherPane
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static IRCBot bot = new IRCBot();
	public static String nick;
	public static JEditorPane text;
	public static JScrollPane scroller;
	public static JTextField input;
	public static JPanel nickSelectPane, chatPane;
	public HTMLDocument doc;
	public static ArrayList<String> IRCLog;
	public static HTMLEditorKit kit;
	public static JLabel topic;

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
				startBot(nickSelect.getText());
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
		scroller.setBorder(title);
		input.setText("");
		input.setEnabled(false);
		try
		{
			Thread.sleep(1000);
		}
		catch (Exception e)
		{
			Logger.logError("Something broke", e);
		}
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
		scroller.setBorder(title);
		input.setText("");
		input.setEnabled(false);
		try
		{
			Thread.sleep(1000);
		}
		catch (Exception e)
		{
			Logger.logError("Something broke", e);
		}
		remove(scroller);
		remove(input);
		remove(topic);
		add(nickSelectPane);
		revalidate();
		repaint();
	}

	public void updateTopic()
	{
		topic.setText(bot.getTopic());
	}

	public void connected()
	{
		TitledBorder title = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Connected");
		title.setTitleJustification(TitledBorder.RIGHT);
		if (bot.getTopic().trim().length() > 0)
		{
			topic = new JLabel(bot.getTopic());
			scroller.setBounds(20, 45, 810, 225);
			TitledBorder title2 = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Topic set by " + bot.getTopicSetter());
			title2.setTitleJustification(TitledBorder.LEFT);
			topic.setBorder(title2);
			topic.setBounds(20, 5, 810, 40);
			add(topic);
		}
		scroller.setBorder(title);
		input.setEnabled(true);
		repaint();
	}

	public void receiveMessage(String message)
	{
		if (message.toLowerCase().contains("changed their nick to " + bot.getNick()))
			return;
		String color = "#B0B0B0";
		if (message.charAt(0) == '*')
			color = "purple";
		if (bot.containsNick(message))
		{
			color = "red";
			bot.alertAlertListener();
			// LaunchFrame.tabbedPane.setIconAt(3, new
			// ImageIcon(this.getClass().getResource("/image/tabs/chat_alert.png")));
		}
		IRCLog.add("<font color=\"" + color + "\">" + message.replaceAll("<", "\"<\"") + "</font><br>");
		refreshLogs();
	}

	public void startBot(String nick)
	{
		IRCLog = new ArrayList<String>();
		text = new JEditorPane("text/html", "<HTML>");
		text.setEditable(false);
		kit = new HTMLEditorKit();
		text.setEditorKit(kit);

		bot.connect("irc.liberty-unleashed.co.uk", "#Minelord", nick, this);
		scroller = new JScrollPane(text);
		scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		scroller.setForeground(Color.gray.darker());
		text.setForeground(Color.black);
		text.setEditable(false);
		connect();
		scroller.setBounds(20, 20, 810, 250);
		add(scroller);
		input = new JTextField();
		input.setBounds(20, 270, 810, 30);
		input.setBackground(Color.gray);
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
					bot.send(input.getText());
					if (input.getText().length() > 0 && input.getText().charAt(0) == '/')
					{
						if(bot.parseCommand(input.getText())==null)
						{
							input.setText("");
							IRCLog.add("<font color=\"red\">Unknown command!</font><br>");
							refreshLogs();
							return;
						}
						if(bot.parseCommand(input.getText()).length()==0)
						{
							input.setText("");
							return;
						}
						if (bot.parseCommand(input.getText()) != null)
							IRCLog.add("<font color=\"purple\">" + bot.parseCommand(input.getText()).replace("<", "\"<\"") + "</font><br>");
						refreshLogs();
					}
					else
					{
						IRCLog.add("<font color=\"gray\">" + bot.getNick() + ": " + input.getText().replace("<", "\"<\"") + "</font><br>");
						refreshLogs();
					}
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

	synchronized private void refreshLogs()
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

	private void addHTML(String html)
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