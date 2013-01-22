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
package net.minelord.data;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import net.minelord.data.events.ModPackListener;
import net.minelord.gui.panes.ModpacksPane;
import net.minelord.workers.ModpackLoader;

public class ModPack
{
	private String name, author, version, url, dir, mcVersion, serverUrl, logoName, imageName, info, animation, xml;
	private String[] mods, oldVersions;
	private int index;
	private boolean updated = false;
	private final static ArrayList<ModPack> packs = new ArrayList<ModPack>();
	private static List<ModPackListener> listeners = new ArrayList<ModPackListener>();
	private boolean privatePack;

	/**
	 * Loads the modpack.xml and adds it to the modpack array in this class
	 */
	public static void loadXml(ArrayList<String> xmlFile)
	{
		ModpackLoader loader = new ModpackLoader(xmlFile);
		loader.start();
	}

	public static void loadXml(String xmlFile)
	{
		ArrayList<String> temp = new ArrayList<String>();
		temp.add(xmlFile);
		ModpackLoader loader = new ModpackLoader(temp);
		loader.start();
	}

	/**
	 * Adds a listener to the listeners array
	 * 
	 * @param listener
	 *            - the ModPackListener to add
	 */
	public static void addListener(ModPackListener listener)
	{
		listeners.add(listener);
	}

	/**
	 * Adds modpack to the modpacks array
	 * 
	 * @param pack
	 *            - a ModPack instance
	 */
	public static void addPack(ModPack pack)
	{
		synchronized (packs)
		{
			packs.add(pack);
		}
		for (ModPackListener listener : listeners)
		{
			listener.onModPackAdded(pack);
		}
	}

	public static void removePacks(String xml)
	{
		ArrayList<ModPack> remove = new ArrayList<ModPack>();
		for (ModPack pack : packs)
		{
			if (pack.getParentXml().equalsIgnoreCase(xml))
			{
				remove.add(pack);
			}
		}
		for (ModPack pack : remove)
		{
			packs.remove(pack);
		}
	}

	/**
	 * Used to get the List of modpacks
	 * 
	 * @return - the array containing all the modpacks
	 */
	public static ArrayList<ModPack> getPackArray()
	{
		return packs;
	}

	/**
	 * Gets the ModPack form the array and the given index
	 * 
	 * @param i
	 *            - the value in the array
	 * @return - the ModPack based on the i value
	 */
	public static ModPack getPack(int i)
	{
		return packs.get(i);
	}

	public static ModPack getPack(String dir)
	{
		for (ModPack pack : packs)
		{
			if (pack.getDir().equalsIgnoreCase(dir))
			{
				return pack;
			}
		}
		return null;
	}

	/**
	 * Used to grab the currently selected ModPack based off the selected index
	 * from ModPacksPane
	 * 
	 * @return ModPack - the currently selected ModPack
	 */
	public static ModPack getSelectedPack()
	{
		return getPack(ModpacksPane.getSelectedModIndex());
	}

	/**
	 * Constructor for ModPack class
	 * 
	 * @param name
	 *            - the name of the ModPack
	 * @param author
	 *            - the author of the ModPack
	 * @param version
	 *            - the version of the ModPack
	 * @param logo
	 *            - the logo file name for the ModPack
	 * @param url
	 *            - the ModPack file name
	 * @param image
	 *            - the splash image file name for the ModPack
	 * @param dir
	 *            - the directory for the ModPack
	 * @param mcVersion
	 *            - the minecraft version required for the ModPack
	 * @param serverUrl
	 *            - the server file name of the ModPack
	 * @param info
	 *            - the description for the ModPack
	 * @param mods
	 *            - string containing a list of mods included in the ModPack by
	 *            default
	 * @param oldVersions
	 *            - string containing all available old versions of the ModPack
	 * @param animation
	 *            - the animation to display before minecraft launches
	 * @param idx
	 *            - the actual position of the modpack in the index
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public ModPack(String name, String author, String version, String logo, String url, String image, String dir, String mcVersion, String serverUrl, String info, String mods, String oldVersions, String animation, int idx, boolean privatePack, String xml) throws IOException,
	NoSuchAlgorithmException
	{
		index = idx;
		this.name = name;
		this.author = author;
		this.version = version;
		this.dir = dir;
		this.mcVersion = mcVersion;
		this.url = url;
		this.serverUrl = serverUrl;
		this.privatePack = privatePack;
		this.xml = xml;
		if (!animation.equalsIgnoreCase(""))
		{
			this.animation = animation;
		}
		else
		{
			this.animation = "empty";
		}
		logoName = logo;
		imageName = image;
		this.info = info;
		if (mods.isEmpty())
		{
			this.mods = null;
		}
		else
		{
			this.mods = mods.split("; ");
		}
		if (oldVersions.isEmpty())
		{
			this.oldVersions = null;
		}
		else
		{
			this.oldVersions = oldVersions.split(";");
		}
	}

	/**
	 * Used to get index of modpack
	 * 
	 * @return - the index of the modpack in the GUI
	 */
	public int getIndex()
	{
		return index;
	}

	/**
	 * Used to get name of modpack
	 * 
	 * @return - the name of the modpack
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Used to get Author of modpack
	 * 
	 * @return - the modpack's author
	 */
	public String getAuthor()
	{
		return author;
	}

	/**
	 * Used to get the version of the modpack
	 * 
	 * @return - the modpacks version
	 */
	public String getVersion()
	{
		return version;
	}

	/**
	 * Used to get the URL or File name of the modpack
	 * 
	 * @return - the modpacks URL
	 */
	public String getUrl()
	{
		return url;
	}

	/**
	 * Used to get the directory of the modpack
	 * 
	 * @return - the directory for the modpack
	 */
	public String getDir()
	{
		return dir;
	}

	/**
	 * Used to get the minecraft version required for the modpack
	 * 
	 * @return - the minecraft version
	 */
	public String getMcVersion()
	{
		return mcVersion;
	}

	/**
	 * Used to get the info or description of the modpack
	 * 
	 * @return - the info for the modpack
	 */
	public String getInfo()
	{
		return info;
	}

	/**
	 * Used to get an array of mods inside the modpack
	 * 
	 * @return - string array of all mods contained
	 */
	public String[] getMods()
	{
		return mods;
	}

	/**
	 * Used to get the name of the server file for the modpack
	 * 
	 * @return - string representing server file name
	 */
	public String getServerUrl()
	{
		return serverUrl;
	}

	/**
	 * Used to get the logo file name
	 * 
	 * @return - the logo name as saved on the repo
	 */
	public String getLogoName()
	{
		return logoName;
	}

	/**
	 * Used to get the splash file name
	 * 
	 * @return - the splash image name as saved on the repo
	 */
	public String getImageName()
	{
		return imageName;
	}

	/**
	 * Used to set whether the modpack has been updated
	 * 
	 * @param result
	 *            - the status of whether the modpack has been updated or not
	 */
	public void setUpdated(boolean result)
	{
		updated = result;
	}

	/**
	 * Used to check if the modpack has been updated
	 * 
	 * @return - the boolean representing whether the modpack has been updated
	 */
	public boolean isUpdated()
	{
		return updated;
	}

	/**
	 * Used to get all available old versions of the modpack
	 * 
	 * @return - string array containing all available old version of the
	 *         modpack
	 */
	public String[] getOldVersions()
	{
		return oldVersions;
	}

	/**
	 * Used to set the minecraft version required of the pack to a custom
	 * version
	 * 
	 * @param version
	 *            - the version of minecraft for the pack
	 */
	public void setMcVersion(String version)
	{
		mcVersion = version;
	}

	/**
	 * @return the filename of the gif animation to display before minecraft
	 *         loads
	 */
	public String getAnimation()
	{
		return animation;
	}

	public boolean isPrivatePack()
	{
		return privatePack;
	}

	public String getParentXml()
	{
		return xml;
	}
}
