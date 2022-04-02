package org.example;

import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import org.jetbrains.annotations.NotNull;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;


public class group {
    boolean is_prepare=false;//游戏是否创建
    boolean is_gaming=false;//游戏是否开始
    Game game;//游戏类
    Group crowd;

    public group(){}

    public group(Group _crowd){
        crowd = _crowd;
    }

    public static boolean check_pro(String text){
        //System.out.println("判断: "+text);
        return text.startsWith("&");
    }

    public void Processing(String  text, Member sender)
    {
        text=text.substring(1);//去掉"&"

        if( text.startsWith("创建游戏") ){
            if(is_gaming){
                crowd.sendMessage("游戏已经开始");
            }
            else if(is_prepare){
                crowd.sendMessage("游戏已经创建");
            }
            else{
                is_prepare=true;
                game=new Game(crowd);
                crowd.sendMessage("创建游戏成功");
            }
        }

        if( text.startsWith("加入游戏") ){
            if(!is_prepare){
                crowd.sendMessage("游戏未创建");
            }
            else if(is_gaming){
                crowd.sendMessage("游戏正在运行");
            }
            else if(game.is_in_game(sender)){
                crowd.sendMessage("你已经在游戏中");
            }
            else if(sender.getId()==game.author.getId()){
                crowd.sendMessage("你已经是出题人，无法加入游戏");
            }
            else if (game.player_list.size()>=10){
                crowd.sendMessage("游戏人数达到上限");
            }
            else{
                game.add_player(new Player(sender));
            }

        }

        if ( text.startsWith("结束游戏") ){
            if(!is_prepare){
                crowd.sendMessage("游戏未创建");
            }
            else if(is_gaming){
                crowd.sendMessage("游戏正在运行，如执意结束，请输入指令[&强制结束游戏]");
            }else{
                game=null;//释放内存
                is_prepare=false;
                crowd.sendMessage("游戏成功结束");
            }

        }

        if ( text.startsWith("强制结束游戏") ){

            //管理者权限判断，等待加入

            if(!is_prepare){
                crowd.sendMessage("游戏未创建");
            }else{
                game=null;//释放内存
                is_prepare=false;
                is_gaming=false;
                crowd.sendMessage("游戏成功结束");
            }

        }

        if ( text.startsWith("开始游戏") ){
            if(!is_prepare){
                crowd.sendMessage("游戏未创建");
            }
            else if(is_gaming){
                crowd.sendMessage("游戏已开始");
            }
            else if (game.player_list.size()<3){
                crowd.sendMessage("游戏人数不足,至少需要3人");
            }
            else if (game.author==null){
                crowd.sendMessage("游戏启动失败，此次游戏缺少一名出题人，输入指令[&我来出题]成为出题人");
            }else{
                is_gaming=true;
                crowd.sendMessage("游戏准备开始");
                game.begin();
            }
        }

        if( text.startsWith("我来出题") ){
            if(!is_prepare){
                crowd.sendMessage("游戏未创建");
            }
            else if(is_gaming){
                crowd.sendMessage("游戏已开始");
            }
            else if( game.author!=null){
                crowd.sendMessage("已有出题人");
            }
            else if( game.is_in_game(sender) ){
                crowd.sendMessage("你已经加入游戏，无法担任出题人");
            }else{
                game.author=sender;
                crowd.sendMessage("你已经成为此次游戏的出题人");
            }

        }

        //若游戏已经开始，将指令传给游戏处理
        if(is_gaming){
            game.Processing(text,sender);
        }

        if(game!=null && game.is_game_over){
            //游戏结束，重置
            is_gaming=false;
            is_prepare=false;
            game=null;
        }
    }

    /**
     * 关于此群的私人消息处理
     * @param text 消息字符串
     * @param sender 发送者
     */
    public void Personal_message(String text , User sender){
        if(is_gaming) game.Personal_message(text , sender);
        else sender.sendMessage("此群游戏尚未开始");

        if(game!=null && game.is_game_over){
            //游戏结束，重置
            is_gaming=false;
            is_prepare=false;
            game=null;
        }
    }

}