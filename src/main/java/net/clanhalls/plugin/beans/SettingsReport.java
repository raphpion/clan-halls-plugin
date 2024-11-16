package net.clanhalls.plugin.beans;

import lombok.Value;

@Value
public class SettingsReport {
    String clientId;
    String clientSecret;
    Settings settings;
}
