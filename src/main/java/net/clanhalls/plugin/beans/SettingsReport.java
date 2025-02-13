package net.clanhalls.plugin.beans;

import lombok.Value;

import java.util.List;

@Value
public class SettingsReport {
    String name;
    List<RankTitle> ranks;
}
