package net.clanhalls.plugin;

import net.clanhalls.plugin.beans.*;
import net.clanhalls.plugin.web.APIResponse;
import net.clanhalls.plugin.web.ClanHallsClient;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.clan.*;
import net.runelite.api.events.ClanChannelChanged;
import net.runelite.api.widgets.ComponentID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.menus.WidgetMenuOption;
import net.runelite.client.plugins.PluginDescriptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@PluginDescriptor(
	name = "Clan Halls Plugin",
	description = "Automatically send your clan online members to any webhook url."
)
public class ClanHallsPlugin extends net.runelite.client.plugins.Plugin
{
  static final String CONFIG_GROUP = "clanhalls";

	@Inject
	private Client client;

	@Inject
	private ClanHallsChatMessenger messenger;

	@Inject
	private MenuManager menuManager;

	@Inject
	private ClanHallsConfig config;

	@Inject
	private ClanHallsClient clanHallsClient;

	private final int RESIZABLE_VIEWPORT_BOTTOM_LINE_FRIEND_CHAT_TAB_ID = 10747942;
	private final int[] WIDGET_IDS = new int[] {
			RESIZABLE_VIEWPORT_BOTTOM_LINE_FRIEND_CHAT_TAB_ID,
			ComponentID.RESIZABLE_VIEWPORT_FRIENDS_CHAT_TAB,
			ComponentID.FIXED_VIEWPORT_FRIENDS_CHAT_TAB,
	};

    private final List<WidgetMenuOption> widgetMenuOptions = new ArrayList<>();

	@Override
	protected void startUp() throws Exception
	{
		for (var widgetId : WIDGET_IDS)
		{
            var sendSettingsOption = new WidgetMenuOption("Send", "Settings", widgetId);
            var sendMembersListOption = new WidgetMenuOption("Send", "Members List", widgetId);
            var sendMemberActivityOption = new WidgetMenuOption("Send", "Member Activity", widgetId);

			widgetMenuOptions.add(sendSettingsOption);
			widgetMenuOptions.add(sendMembersListOption);
			widgetMenuOptions.add(sendMemberActivityOption);

			menuManager.addManagedCustomMenu(sendSettingsOption, (entry) -> { sendSettingsReport(entry); });
			menuManager.addManagedCustomMenu(sendMembersListOption, (entry) -> { sendMembersListReport(entry); });
			menuManager.addManagedCustomMenu(sendMemberActivityOption, (entry) -> { sendMemberActivityReport(entry); });
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		for (WidgetMenuOption option : widgetMenuOptions) {
			menuManager.removeManagedCustomMenu(option);
		}
	}

	@Subscribe
	public void onClanChannelChanged(ClanChannelChanged event) throws IOException
	{
		if (event.getClanChannel() == null || event.getClanId() != 0) return;

		APIResponse<ClanInfo> clanResponse = clanHallsClient.getClan();
		if (clanResponse.getError() != null) {
			messenger.send("Unable to retrieve clan information.", ClanHallsChatMessenger.ERROR);
		} else {
			ClanInfo clanInfo = clanResponse.getData();
			if (clanInfo != null && clanInfo.getLastSyncedAt() == null) {
				sendSettingsReport(null);
			}
		}

		if (config.sendActivityOnJoined()) {
			sendMemberActivityReport(null);
		}
	}

	@Provides
	ClanHallsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ClanHallsConfig.class);
	}

	public void sendSettingsReport(MenuEntry entry) {
		if (!validateWebhookConfig()) return;

		ClanSettings clanSettings = client.getClanSettings();
		if (clanSettings == null) {
			messenger.send("Please join your clan's channel before sending a settings report.", ClanHallsChatMessenger.ERROR);
			return;
		}

		List<RankTitle> rankTitles = new ArrayList<>();
		ClanTitle firstTitle = null;
		for (int i = 0; i < 127; i++) {
			ClanTitle title = clanSettings.titleForRank(new ClanRank(i));
			if (title == null) continue;

			if (i == 0) firstTitle = title;
			else if (firstTitle != null && title.getName().equals(firstTitle.getName())) continue;

			RankTitle rankTitle = new RankTitle(i, title.getName());
			rankTitles.add(rankTitle);
		}

		SettingsReport report = new SettingsReport(clanSettings.getName(), rankTitles);

		APIResponse<ReportInfo> response = clanHallsClient.sendSettingsReport(report);
		if (response.getError() != null) {
			messenger.send("Unexpected error while sending settings report.", ClanHallsChatMessenger.ERROR);
			return;
		}

		messenger.send("Settings report sent successfully!", ClanHallsChatMessenger.SUCCESS);
	}

	public void sendMembersListReport(MenuEntry entry) {
		if (!validateWebhookConfig()) return;

		ClanSettings settings = client.getClanSettings();
		if (settings == null) {
			messenger.send("Please join your clan's channel before sending a members list report.", ClanHallsChatMessenger.ERROR);
			return;
		}

		List<ListMember> members = new ArrayList<>();
		for (ClanMember member : settings.getMembers()) {
			members.add(new ListMember(member.getName(), member.getRank().getRank()));
		}

		MembersListReport membersListReport = new MembersListReport(members);
		APIResponse<ReportInfo> response = clanHallsClient.sendMembersListReport(membersListReport);
		if (response.getError() != null) {
			messenger.send("Unexpected error while sending members list report.", ClanHallsChatMessenger.ERROR);
			return;
		}

		messenger.send("Members list report sent successfully!", ClanHallsChatMessenger.SUCCESS);
	}

	public void sendMemberActivityReport(MenuEntry entry) {
		if (!validateWebhookConfig()) return;

		ClanChannel channel = client.getClanChannel();
		if (channel == null) {
			messenger.send("Please join your clan's channel before sending a members list report.", ClanHallsChatMessenger.ERROR);
			return;
		}

		List<MemberActivity> members = new ArrayList<>();
		for (ClanChannelMember member : channel.getMembers()) {
			members.add(new MemberActivity(member.getName(), member.getRank().getRank()));
		}

		MemberActivityReport memberActivityReport = new MemberActivityReport(members);

		APIResponse<ReportInfo> response = clanHallsClient.sendMemberActivityReport(memberActivityReport);
		if (response.getError() != null) {
			messenger.send("Unexpected error while sending member activity report.", ClanHallsChatMessenger.ERROR);
			return;
		}

		messenger.send("Member activity report sent successfully!", ClanHallsChatMessenger.SUCCESS);
	}

	private boolean validateWebhookConfig()
	{
		var clientId = config.clientId();
		if (clientId == null || clientId.isEmpty())
		{
			messenger.send("Client ID is null or empty. Please update your config.", ClanHallsChatMessenger.ERROR);
			return false;
		}

		var clientSecret = config.clientSecret();
		if (clientSecret == null || clientSecret.isEmpty())
		{
			messenger.send("Client secret is null or empty. Please update your config.", ClanHallsChatMessenger.ERROR);
			return false;
		}

		var apiBaseUrl = config.apiBaseUrl();
		if (apiBaseUrl == null || apiBaseUrl.isEmpty())
		{
			messenger.send("API base URL is null or empty. Please update your config.", ClanHallsChatMessenger.ERROR);
			return false;
		}

		return true;
	}
}
