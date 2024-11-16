package net.clanhalls.plugin.beans;

import lombok.Value;

import java.util.List;

@Value
public class MemberActivityReport {
    String clientId;
    String clientSecret;
    List<MemberActivity> members;
}
