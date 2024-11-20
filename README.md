# Clan Halls

This plugin helps you manage your clan at a glimpse by tracking your clan members by their activity. It integrates with the [Clan Halls web app](https://app.clanhalls.net) so you can easily consult your clan's activity.

It can also be used with a custom webhook URL so you can own and manage your data.

[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/nordveil)

## Features

### Automated tracking

If the option is enabled in the plugin configuration, it will automatically send your clan's members activity to the web app when you join your clan channel.

### Manual tracking

You can manually send data by right-clicking the Clan Icon.

![Clan Icon](/img/clan_icon.png)

![Clan Menu](/img/clan_menu.png)

#### Settings

Sends your clan name and list of titles to the API. You must sync your clan using this report once to be able to fully utilize the web app.

#### Members List

Sends the full list of your clan members to the API, with their rank. This will not add new members to the web app, only update the existing ones, and remove the ones that are not in the list.

#### Member Activity

Sends the list of currently connected clan members to the API, with their rank. This will create new members in the web app if they are not already there. If there is [name change data](https://wiseoldman.net/names) available in Wise Old Man, it will also try to update an existing member's name instead of creating a new one.

## Configuration

![Plugin Configuration](/img/plugin_config.png)

If you want to use the plugin with the Clan Halls web application, set the API Base URL to [https://app.clanhalls.net/api](https://app.clanhalls.net/api). Otherwise, you can set it to your custom webhook URL.

**We will not provide support for custom webhook URLs!**

1. The app uses credentials to authenticate the requests. To get your credentials, you must [sign in to the web application](https://app.clanhalls.net/sign-in) and create a new clan.

![Sign In](/img/onboarding_1.png)

2. Enter an original username and click "Set Username"

![Set Username](/img/onboarding_2.png)

3. Enter your clan name and click "Create Clan"

![Create Clan](/img/onboarding_3.png)

4. If it's your first time creating a clan, some credentials will be generated for you. Click the copy buttons to copy the Client ID and Client Secret and add them to the plugin's configuration.

![Credentials](/img/onboarding_4.png)

If you ever lose your credentials, you can always create new ones by going into your settings, in the Credentials tab.

![Settings](/img/settings_1.png)

![Credentials tab](/img/settings_2.png)
