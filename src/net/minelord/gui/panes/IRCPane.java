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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import net.minelord.util.IRCBot;
import net.minelord.util.IRCMessageListener;

public class IRCPane extends JPanel implements IRCMessageListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static IRCBot bot;
	public static String nick;
	public static JTextArea text;
	public static JScrollPane scroller;
	public static JTextField input;

	public IRCPane()
	{
		super();
		this.setBorder(new EmptyBorder(5, 5, 5, 5));
		this.setLayout(null);
		final JPanel nickSelectPane = new JPanel();
		nickSelectPane.setLayout(null);
		nickSelectPane.setBounds(325, 70, 200, 200);
		TitledBorder title= BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Pick a nick");
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
				if(arg0.getKeyCode()==10)
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

	public void recieveMessage(String message, Color col)
	{
		TitledBorder title= BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Connected");
		title.setTitleJustification(TitledBorder.RIGHT);
		scroller.setBorder(title);
		input.setEnabled(true);
		text.setText(text.getText()+"\n"+message);
	}

	public void startBot(String nick)
	{
		bot=new IRCBot("irc.liberty-unleashed.co.uk", "#minelord", nick, this);
		JPanel chatPane=new JPanel();
		chatPane.setBounds(0, 0, 850, 480);
		TitledBorder title= BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Connecting...");
		title.setTitleJustification(TitledBorder.RIGHT);
		text=new JTextArea();
		scroller=new JScrollPane(text);
		text.setBackground(Color.gray);
		text.setForeground(Color.black);
		text.setEditable(false);
		scroller.setBorder(title);
		scroller.setBounds(20, 20, 810, 250);
		add(scroller);
		input=new JTextField();
		input.setBounds(20, 270, 810, 30);
		input.setBackground(Color.gray);
		input.setEnabled(false);
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
				if(arg0.getKeyCode()==10)
				{
					bot.send(input.getText());
					if(input.getText().charAt(0)=='/')
						text.setText(text.getText()+"\n"+bot.parseCommand(input.getText()));
					else
						text.setText(text.getText()+"\n"+bot.getNick()+": "+input.getText());
					input.setText("");
				}
			}

			@Override
			public void keyPressed(KeyEvent arg0)
			{
				// TODO Auto-generated method stub

			}
		});
		//	add(chatPane);
	}
}