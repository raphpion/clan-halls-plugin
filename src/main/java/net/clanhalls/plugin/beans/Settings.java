package net.clanhalls.plugin.beans;

import lombok.Value;

import java.util.List;

@Value
public class Settings {
    String name;
    List<RankTitle> ranks;
}
