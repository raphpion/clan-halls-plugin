package net.clanhalls.plugin;

import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(ClanHallsPlugin.CONFIG_GROUP)
public interface ClanHallsConfig extends net.runelite.client.config.Config
{
	@ConfigItem(
		keyName = "clientID",
		name = "Client ID",
		description = "The ID of the client that will be used to authenticate to the webhook."
	)
	default String clientId() { return ""; }

	@ConfigItem(
		keyName = "clientSecret",
		name = "Client Secret",
		description = "The secret of the client that will be used to authenticate to the webhook."
	)
	default String clientSecret() { return ""; }

	@ConfigItem(
		keyName = "apiBaseUrl",
		name = "API Base URL",
		description = "The base URL of the webhook that will be used to send the data."
	)
	default String apiBaseUrl() { return "https://app.clanhalls.net/api"; }

	@ConfigItem(
		keyName = "sendActivityOnJoined",
		name = "Send member activity on join",
		description = "Automatically send the list of currently connected members when you join your clan's channel."
	)
	default boolean sendActivityOnJoined() { return false; }
}
