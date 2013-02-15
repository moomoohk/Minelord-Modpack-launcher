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

import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.minelord.gui.LaunchFrame;
import net.minelord.gui.panes.IRCPane;
import net.minelord.gui.panes.ModpacksPane;
import net.minelord.gui.panes.TexturepackPane;

public class SearchDialog extends JDialog {
	public static String lastPackSearch = "", lastMapSearch = "", lastTextureSearch = "";
	public JTextField searchBar = new JTextField();

	public SearchDialog(final ModpacksPane instance) {
		super(LaunchFrame.getInstance(), true);
		setUpGui();
		searchBar.setText((lastPackSearch == null) ? "" : lastPackSearch);
		searchBar.getDocument().addDocumentListener(new DocumentListener() {
			@Override public void removeUpdate(DocumentEvent arg0) {
				lastPackSearch = searchBar.getText();
				instance.sortPacks();
			}
			@Override public void insertUpdate(DocumentEvent arg0) {
				lastPackSearch = searchBar.getText();
				instance.sortPacks();
			}
			@Override public void changedUpdate(DocumentEvent arg0) { }
		});
		searchBar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				lastPackSearch = searchBar.getText();
				instance.sortPacks();
				setVisible(false);
			}
		});
	}

	public SearchDialog(final IRCPane instance) {
		super(LaunchFrame.getInstance(), true);
		setUpGui();
		searchBar.setText((lastMapSearch == null) ? "" : lastMapSearch);
		searchBar.getDocument().addDocumentListener(new DocumentListener() {
			@Override public void removeUpdate(DocumentEvent arg0) {
				lastMapSearch = searchBar.getText();

			}
			@Override public void insertUpdate(DocumentEvent arg0) {
				lastMapSearch = searchBar.getText();
			}
			@Override public void changedUpdate(DocumentEvent arg0) { }
		});
		searchBar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				lastPackSearch = searchBar.getText();
			
				setVisible(false);
			}
		});
	}

	public SearchDialog(final TexturepackPane instance) {
		super(LaunchFrame.getInstance(), true);
		setUpGui();
		searchBar.setText((lastTextureSearch == null) ? "" : lastTextureSearch);
		searchBar.getDocument().addDocumentListener(new DocumentListener() {
			@Override public void removeUpdate(DocumentEvent arg0) {
				lastTextureSearch = searchBar.getText();
				instance.sortTexturePacks();
			}
			@Override public void insertUpdate(DocumentEvent arg0) {
				lastTextureSearch = searchBar.getText();
				instance.sortTexturePacks();
			}
			@Override public void changedUpdate(DocumentEvent arg0) { }
		});
		searchBar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				lastPackSearch = searchBar.getText();
				instance.sortTexturePacks();
				setVisible(false);
			}
		});
	}

	private void setUpGui() {
		setTitle("Text Search Filter");
		setBounds(300, 300, 220, 90);
		setResizable(false);
		getContentPane().setLayout(null);
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
		searchBar.setBounds(10, 10, 200, 30);
		getContentPane().add(searchBar);
	}
}
