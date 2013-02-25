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
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.minelord.data.ModPack;
import net.minelord.data.Settings;
import net.minelord.gui.ChooseDir;
import net.minelord.gui.LaunchFrame;
import net.minelord.locale.I18N;
import net.minelord.util.OSUtils;

public class EditModPackDialog extends JDialog
{
	private final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);

	private JPanel modsFolderPane = new JPanel();
	private JPanel coreModsFolderPane = new JPanel();
	private JPanel jarModsFolderPane = new JPanel();

	private JLabel enabledLabel = new JLabel("<html><body><h1>" + I18N.getLocaleString("MODS_EDIT_ENABLED_LABEL") + "</h1></html></body>");
	private JLabel disabledLabel = new JLabel("<html><body><h1>" + I18N.getLocaleString("MODS_EDIT_DISABLED_LABEL") + "</h1></html></body>");

	private JButton openFolderButton = new JButton(I18N.getLocaleString("MODS_EDIT_OPEN_FOLDER"));
	private JButton addModButton = new JButton(I18N.getLocaleString("MODS_EDIT_ADD_MOD"));
	private JButton disableMod = new JButton(I18N.getLocaleString("MODS_EDIT_DISABLE_MOD"));
	private JButton enableMod = new JButton(I18N.getLocaleString("MODS_EDIT_ENABLE_MOD"));

	private JList enabled = new JList();
	private JList disabled = new JList();

	private List<String> enabledList_ = new ArrayList<String>();
	private List<String> disabledList_ = new ArrayList<String>();

	private JScrollPane enabledScroll = new JScrollPane(enabled);
	private JScrollPane disabledScroll = new JScrollPane(disabled);

	private final File modsFolder = new File(Settings.getSettings().getInstallPath(), ModPack.getSelectedPack().getDir() + File.separator + "minecraft" + File.separator + "mods");
	private final File coreModsFolder = new File(Settings.getSettings().getInstallPath(), ModPack.getSelectedPack().getDir() + File.separator + "minecraft" + File.separator + "coremods");
	private final File jarModsFolder = new File(Settings.getSettings().getInstallPath(), ModPack.getSelectedPack().getDir() + File.separator + "instMods");
	public File folder = modsFolder;

	private Tab currentTab = Tab.MODS;

	public enum Tab
	{
		MODS, JARMODS, COREMODS, OLD_VERSIONS
	}

	public EditModPackDialog(LaunchFrame instance)
	{
		super(instance, true);

		modsFolder.mkdirs();
		coreModsFolder.mkdirs();
		jarModsFolder.mkdirs();

		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_ftb.png")));
		setTitle(I18N.getLocaleString("MODS_EDIT_TITLE"));
		setBounds(300, 300, 635, 525);
		setResizable(false);
		getContentPane().setLayout(null);

		tabbedPane.setLocation(0, 0);
		tabbedPane.setSize(getSize());

		modsFolderPane.setLayout(null);
		coreModsFolderPane.setLayout(null);
		jarModsFolderPane.setLayout(null);

		getContentPane().add(tabbedPane);
		tabbedPane.addTab("<html><body leftMargin=15 topmargin=8 marginwidth=15 marginheight=5>Mods</body></html>", modsFolderPane);
		tabbedPane.addTab("<html><body leftMargin=15 topmargin=8 marginwidth=15 marginheight=5>JarMods</body></html>", jarModsFolderPane);
		tabbedPane.addTab("<html><body leftMargin=15 topmargin=8 marginwidth=15 marginheight=5>CoreMods</body></html>", coreModsFolderPane);

		tabbedPane.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent arg0)
			{
				currentTab = Tab.values()[tabbedPane.getSelectedIndex()];
				JPanel temp;
				switch (currentTab)
				{
				case MODS:
					temp = modsFolderPane;
					folder = modsFolder;
					break;
				case COREMODS:
					temp = coreModsFolderPane;
					folder = coreModsFolder;
					break;
				case JARMODS:
					temp = jarModsFolderPane;
					folder = jarModsFolder;
					break;
				default:
					return;
				}
				temp.add(enabledScroll);
				temp.add(disabledScroll);
				temp.add(enabledLabel);
				temp.add(disabledLabel);
				temp.add(openFolderButton);
				temp.add(addModButton);
				temp.add(enableMod);
				temp.add(disableMod);
				updateLists();
			}
		});
		tabbedPane.setSelectedIndex(0);

		addModButton.setBounds(380, 410, 240, 35);
		addModButton.addActionListener(new ChooseDir(this));
		modsFolderPane.add(addModButton);

		openFolderButton.setBounds(10, 410, 240, 35);
		openFolderButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				OSUtils.open(folder);
			}
		});
		modsFolderPane.add(openFolderButton);

		enabledLabel.setBounds(10, 10, 240, 30);
		enabledLabel.setHorizontalAlignment(SwingConstants.CENTER);
		modsFolderPane.add(enabledLabel);

		disabledLabel.setBounds(380, 10, 240, 30);
		disabledLabel.setHorizontalAlignment(SwingConstants.CENTER);
		modsFolderPane.add(disabledLabel);

		enabled.setListData(getEnabled());
		enabled.setBackground(UIManager.getColor("control").darker().darker());
		enabledScroll.setViewportView(enabled);
		enabledScroll.setBounds(10, 40, 240, 360);
		modsFolderPane.add(enabledScroll);

		disabled.setListData(getDisabled());
		disabled.setBackground(UIManager.getColor("control").darker().darker());
		disabledScroll.setViewportView(disabled);
		disabledScroll.setBounds(380, 40, 240, 360);
		modsFolderPane.add(disabledScroll);

		disableMod.setBounds(255, 80, 115, 30);
		disableMod.setVisible(true);
		disableMod.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				if (enabled.getSelectedIndices().length > 1)
				{
					for (int i = 0; i < enabled.getSelectedIndices().length; i++)
					{
						String name = enabledList_.get(enabled.getSelectedIndices()[i]);
						new File(folder, name).renameTo(new File(folder, name + ".disabled"));
					}
					updateLists();
				}
				else
				{
					if (enabled.getSelectedIndex() >= 0)
					{
						String name = enabledList_.get(enabled.getSelectedIndex());
						new File(folder, name).renameTo(new File(folder, name + ".disabled"));
					}
					updateLists();
				}
			}
		});
		modsFolderPane.add(disableMod);

		enableMod.setBounds(255, 120, 115, 30);
		enableMod.setVisible(true);
		enableMod.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				if (disabled.getSelectedIndices().length > 1)
				{
					for (int i = 0; i < disabled.getSelectedIndices().length; i++)
					{
						String name = disabledList_.get(disabled.getSelectedIndices()[i]);
						new File(folder, name).renameTo(new File(folder, name.replace(".disabled", "")));
					}
					updateLists();
				}
				else
				{
					if (disabled.getSelectedIndex() >= 0)
					{
						String name = disabledList_.get(disabled.getSelectedIndex());
						new File(folder, name).renameTo(new File(folder, name.replace(".disabled", "")));
					}
					updateLists();
				}
			}
		});
		modsFolderPane.add(enableMod);
		setLocationRelativeTo(null);
	}

	private String[] getEnabled()
	{
		enabledList_.clear();
		if (folder.exists())
		{
			for (String name : folder.list())
			{
				if (name.toLowerCase().endsWith(".zip"))
				{
					enabledList_.add(name);
				}
				else
					if (name.toLowerCase().endsWith(".jar"))
					{
						enabledList_.add(name);
					}
					else
						if (name.toLowerCase().endsWith(".litemod"))
						{
							enabledList_.add(name);
						}
			}
		}
		String[] enabledList = new String[enabledList_.size()];
		for (int i = 0; i < enabledList_.size(); i++)
		{
			enabledList[i] = enabledList_.get(i).replace(".zip", "").replace(".jar", "").replace(".litemod", "");
		}
		return enabledList;
	}

	private String[] getDisabled()
	{
		disabledList_.clear();
		if (folder.exists())
		{
			for (String name : folder.list())
			{
				if (name.toLowerCase().endsWith(".zip.disabled"))
				{
					disabledList_.add(name);
				}
				else
					if (name.toLowerCase().endsWith(".jar.disabled"))
					{
						disabledList_.add(name);
					}
					else
						if (name.toLowerCase().endsWith(".litemod.disabled"))
						{
							disabledList_.add(name);
						}
			}
		}
		String[] enabledList = new String[disabledList_.size()];
		for (int i = 0; i < disabledList_.size(); i++)
		{
			enabledList[i] = disabledList_.get(i).replace(".zip.disabled", "").replace(".jar.disabled", "").replace(".litemod.disabled", "");
		}
		return enabledList;
	}

	public void updateLists()
	{
		enabled.setListData(getEnabled());
		disabled.setListData(getDisabled());
	}
}