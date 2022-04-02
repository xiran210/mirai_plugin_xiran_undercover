package org.example;

import kotlin.coroutines.CoroutineContext;
import kotlin.reflect.jvm.internal.impl.util.MemberKindCheck;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.command.MemberCommandSenderOnMessage;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.GroupTempMessageEvent;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import org.jetbrains.annotations.NotNull;

public final class Plugin extends JavaPlugin {
    public static final Plugin INSTANCE = new Plugin();

    private Plugin() {
        super(new JvmPluginDescriptionBuilder("org.example.plugin", "1.0-SNAPSHOT").build());
    }

    @Override
    public void onEnable() {
        getLogger().info("xiran_Plugin onEnable!");

        GlobalEventChannel.INSTANCE.registerListenerHost(new SimpleListenerHost() {

            @Override
            public void handleException(@NotNull CoroutineContext context, @NotNull Throwable exception) {
                super.handleException(context, exception);
            }

            @EventHandler
            public void handleMessage(GroupMessageEvent event) {
                String text = event.getMessage().get(1).toString();
                Distribution.Processing_instruction(text, event.getSender(), event.getGroup());
            }

            @EventHandler
            public void handleMessage(GroupTempMessageEvent event) {
                getLogger().info("收到群临时消息");
                String text = event.getMessage().get(1).toString();
                Distribution.Personal_message(text, (User) event.getSender());
            }

            @EventHandler
            public void handleMessage(FriendMessageEvent event) {
                getLogger().info("收到好友消息");
                String text = event.getMessage().get(1).toString();
                Distribution.Personal_message(text, (User) event.getSender());
            }

        });
    }
}