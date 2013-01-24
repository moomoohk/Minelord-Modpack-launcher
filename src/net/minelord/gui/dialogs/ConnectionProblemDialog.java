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
package net.minelord.gui.dialogs;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;

import net.minelord.gui.LaunchFrame;

public class ConnectionProblemDialog extends JDialog
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JButton btnQuit = new JButton("Quit");
	private JButton btnContinue = new JButton("Continue");
	private JEditorPane editorPane = new JEditorPane();

	public ConnectionProblemDialog()
	{
		super(LaunchFrame.getInstance(), true);
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_minelord.png")));
		getContentPane().setLayout(null);
		setBounds(300, 200, 300, 200);
		setTitle("Connection problem");
		setResizable(false);

		btnQuit.setBounds(10, 128, 82, 23);
		btnQuit.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				System.exit(0);
			}		
		});
		add(btnQuit);
		getRootPane().setDefaultButton(btnQuit);

		btnContinue.setBounds(192, 128, 82, 23);
		btnContinue.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setVisible(false);
			}
		});
		add(btnContinue);

		editorPane.setBounds(10, 11, 264, 115);
		editorPane.setEditable(false);
		editorPane.setHighlighter(null);
		editorPane.setContentType("text/html");
		editorPane.setText("It would appear that the launcher cannot connect to the internet. If you haven't launched the launcher before, you should wait until you have a working internet connection.");
		add(editorPane);
		setLocationRelativeTo(null);
	}
}
