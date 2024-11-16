package net.clanhalls.plugin;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ClanHallsTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(ClanHallsPlugin.class);
		RuneLite.main(args);
	}
}