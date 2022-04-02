package org.example;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.User;

import java.util.*;

public class Distribution {
    /**
     * 群列表
     */
    private static Vector<group> group_list = new Vector<>();

    /**
     * 对指令进行分发
     * @param text 指令
     * @param sender 发送者id
     * @param crowd 发送者所在群id
     */
    public static void Processing_instruction(String text , Member sender, Group crowd){

        //判断是否为测试群
        //if(crowd.getId()!=1042224233) return;

        //crowd.sendMessage("测试中:程序成功收到消息");

        if(!group.check_pro(text)) return;

        for (group a : group_list) {
            if (a.crowd.getId() == crowd.getId()) {
                a.Processing(text, sender);
                return;
            }
        }

        //没有在已有群中找到群，新建群
        group k = new group(crowd);
        group_list.add(k);
        k.Processing(text,sender);
    }

    /**
     * 处理好友消息和群临时消息的分发
     * @param text 消息字符串
     * @param sender 发送者
     */
    public static void Personal_message(String text , User sender){
        if(!group.check_pro(text)) return;
        text=text.substring(1);

        //判断指令前缀
        for (group a : group_list) {
            if ( text.startsWith( String.valueOf(a.crowd.getId()) ) ) {
                a.Personal_message(text.substring( String.valueOf(a.crowd.getId()).length() ), sender);
                return;
            }
        }

        sender.sendMessage("无效的群号前缀");

    }


}
