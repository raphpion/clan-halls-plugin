package net.clanhalls.plugin;

import net.runelite.api.ChatMessageType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;

import javax.inject.Inject;
import java.awt.*;

public class ClanHallsChatMessenger {
    @Inject
    private ChatMessageManager chatMessageManager;

    public static final Color SUCCESS = new Color(34, 197, 94);
    public static final Color ERROR = new Color(239, 68, 68);
    public static final Color INFO = new Color(14, 165, 233);

    public void send(String message, Color color) {
        ChatMessageBuilder cmb = new ChatMessageBuilder();
        cmb.append("[ClanHalls] ");
        cmb.append(color, message);

        chatMessageManager.queue(QueuedMessage.builder()
                .type(ChatMessageType.CONSOLE)
                .runeLiteFormattedMessage(cmb.build())
                .build());
    }
}
