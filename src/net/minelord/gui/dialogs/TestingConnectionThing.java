package net.minelord.gui.dialogs;

import java.awt.Toolkit;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;

import net.minelord.gui.LaunchFrame;
import net.minelord.log.Logger;

public class TestingConnectionThing extends JDialog
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static boolean offline=true;
	
	public TestingConnectionThing()
	{
		super(LaunchFrame.getInstance(), true);
		setResizable(false);
		setUndecorated(true);
		setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/image/logo_minelord.png")));
		getContentPane().setLayout(null);
		setBounds(0, 0, 70, 70);
		JLabel loadingGIF = new JLabel(new ImageIcon(this.getClass().getResource("/image/gif_loading.gif")));
		loadingGIF.setBounds(2, 2, 66, 66);
		add(loadingGIF);
		setLocationRelativeTo(null);
		Thread suppressor=new Thread(new Runnable()
		{
			
			@Override
			public void run()
			{
				testConnection();
			}
		});
		suppressor.start();
		setVisible(true);
	}
	
	public void testConnection()
	{
		System.out.println("starting");
		Thread connect=new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				URL minelord;
				try
				{
					minelord = new URL("http://minelord.com");
					HttpURLConnection connection=(HttpURLConnection)minelord.openConnection();
					if(connection.getResponseCode()==200)
						offline=false;
				}
				catch (Exception e)
				{
					Logger.logInfo("Couldn't connect to the internet.");
				}
			}
		});
		connect.start();
		try
		{
			Thread.sleep(5000);
		}
		catch (InterruptedException e1)
		{
			e1.printStackTrace();
		}
		connect.interrupt();
		System.out.println("done");
		setVisible(false);
		
	}
}