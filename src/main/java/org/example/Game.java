package org.example;

import java.util.Random;
import java.util.Vector;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.User;

public class Game {

    public Vector<Player> player_list = new Vector<>();
    public Group crowd;//游戏所在群
    public Member author;//出题人id，没有出题人则为null
    public Topic topic;//题目
    public boolean is_game_over=false;
    public boolean status_code=false;//状态码



    public Game(){}

    public Game(Group _crowd){
        crowd=_crowd;
    }


    public void add_player(Player x){
        player_list.add(x);
        String msg="";
        for(Player p:player_list){
            msg=msg+p.id.getNick()+'\n';
        }
        crowd.sendMessage("加入成功,目前加入游戏的人员有:"+"\n"+msg);
        x.id.sendMessage("你已经成功加入游戏");
    }

    /**
     * 游戏开始前的获得题目分发题目等操作
     */
    public void begin(){
        crowd.sendMessage("游戏开始！");
        //获取题目
        if (author!=null){
            crowd.sendMessage("等待出题人出题中");
            //通知出题人出题
            author.sendMessage(
                    "您是群"+crowd.getId()+"此次的谁是卧底出题人\n"+
                            "请发送[&"+crowd.getId()+" 【平民词】 【卧底词】]指令进行出题操作\n"+
                            "如发送[&"+crowd.getId()+" 猕猴桃 水蜜桃]指令进行出题操作\n"
            );
        }else{
            crowd.sendMessage("此次游戏无出题人,正在准备从题库中获取题目");
            //题库待实现
        }

    }

    /**
     * 随机分配平民和卧底
     * 将题目分发给玩家
     */
    public void send_topic(){
        crowd.sendMessage("题目获取成功,准备分发题目");
        int player_sum=player_list.size();
        int player_wd=player_sum/5+1;
        crowd.sendMessage("此次游戏共"+player_sum+"人，系统将分配卧底"+player_wd+"人");

        //随机选取玩家为卧底
        Random random = new Random();
        while(player_wd--!=0){
            while(true){
                int x=random.nextInt(player_sum);
                if(!player_list.get(x).is_wd){
                    player_list.get(x).is_wd=true;
                    break;
                }
            }
        }

        for(Player x:player_list){
            if(x.is_wd){
                x.subject=topic.back_topic;
            }else{
                x.subject=topic.square_topic;
            }

            try{
                x.id.sendMessage("你此次游戏的题目是:"+x.subject);
            }catch (Exception e) {
                crowd.sendMessage("出错辣!游戏结束！\n" + e.toString());
                is_game_over=true;
                return;
            }
        }
        crowd.sendMessage("所有玩家题目分发完毕！\n"+"若未接受到题目，请私聊机器人并发送指令[&"+crowd.getId()+"]获得题目");
        crowd.sendMessage("进入描述阶段,指令[&描述【描述语】]");
    }

    /**
     * 游戏的群指令处理中心函数
     * @param text 消息
     * @param sender 发送者id
     */
    public void Processing(String  text,Member sender){
        /*
            流程：描述，投票，判定，循环
         */
        //非游戏内玩家指令，剔出
        if(!is_in_game(sender)) return;

        if(text.startsWith("描述")){
            if(status_code){
                crowd.sendMessage("现在不是描述的时候哦");
                return;
            }

            Player player=find_in_game(sender);
            if(!player.is_survival){
                crowd.sendMessage("您已出局");
                return;
            }
            player.describe=text.substring("描述".length());

            String text1="以下为玩家描述\n";
            for(int i=0;i<player_list.size();i++){
                Player player1=player_list.get(i);
                if(!player1.is_survival) continue;
                text1=text1+(i+1)+player1.id.getNick()+':'+player1.describe+'\n';
            }
            crowd.sendMessage(text1);
        }

        if(text.startsWith("投票")){
            if(!status_code){
                crowd.sendMessage("现在不是投票的时候哦");
                return;
            }
            Player player=find_in_game(sender);
            if(!player.voting_rights){
                crowd.sendMessage("您此回合已经投票");
                return;
            }
            if(!player.is_survival){
                crowd.sendMessage("您已出局");
                return;
            }

            text=text.substring("投票".length());
            try {
                int i = Integer.parseInt(text);
                player_list.get(i-1).votes++;
                crowd.sendMessage(player_list.get(i-1).id.getNick()+"被投了一票!");
                player.voting_rights=false;
            }catch (Exception e){
                crowd.sendMessage("错误的指令！");
            }
        }

        check_status();
    }

    /**
     * 判断并切换描述和投票状态
     * 并判断游戏是否结束
     */
    public void check_status(){
        if(status_code){
            /*
             * 判断投票是否结束
             * 判断是否有人出局
             * 平票不出局
             */
            for(Player player:player_list){
                if(player.is_survival&&player.voting_rights) return;
            }

            int max_votes=0;
            boolean is_tie=false;

            for(int i=1;i<player_list.size();i++){
                if(player_list.get(i).votes>player_list.get(max_votes).votes){
                    max_votes=i;
                    is_tie=false;
                }else if(player_list.get(i).votes==player_list.get(max_votes).votes){
                    is_tie=true;
                }
            }

            status_code=false;

            //清空票数+获得投票权
            for(Player player:player_list){
                player.votes=0;
                player.voting_rights=true;
            }

            if(is_tie){
                crowd.sendMessage("有平票产生,无人出局");
            }else{
                crowd.sendMessage(player_list.get(max_votes).id.getNick()+"出局!");
                player_list.get(max_votes).is_survival=false;

                //判断游戏是否结束
                //游戏结束条件:1.卧底全部出局 2.剩下2人且有卧底
                int play_sum=0,play_wd=0;
                for(Player player:player_list){
                    if(player.is_survival){
                        play_sum++;
                        if(player.is_wd)
                            play_wd++;
                    }
                }
                if(play_wd==0){
                    crowd.sendMessage("游戏结束,平民胜利！");
                    game_over();
                    return;
                }
                if(play_sum<=2){
                    crowd.sendMessage("游戏结束,卧底胜利!");
                    game_over();
                    return;
                }

            }
            crowd.sendMessage("进入描述阶段,指令[&描述【描述语】]");


        }else{
            /*
             * 判断描述是否结束
             */
            for(Player player:player_list){
                if(player.is_survival&&player.describe=="") return;
            }

            //清空所有人的描述
            for(Player player:player_list){
                player.describe="";
            }

            crowd.sendMessage("描述阶段结束,即将加入投票阶段\n投票指令[&投票【想要投的人的序号】],如[&投票1]");
            status_code=true;//状态转换
        }
    }

    public void game_over(){
        is_game_over=true;
        String text="";
        for(Player player:player_list){
            text=text+player.id.getNick()+":"+ (player.is_wd?"卧底\n":"平民\n");
        }
        crowd.sendMessage(topic+"\n"+text);
    }

    /**
     * 游戏私聊消息处理函数
     * @param text 消息字符串
     * @param sender 发送者
     */
    public void Personal_message(String text , User sender){

        //若发送者为游戏出题人
        if(sender.getId()==author.getId()){
            if(topic!=null){
                sender.sendMessage("此次游戏的出题已经完成");
                return;
            }

            //去除前导空格
            while(text.length()!=0 && text.charAt(0)==' ')
                text=text.substring(1);

            String[] com = new String[4];
            try{
                com=text.split("\\s+");
                System.out.println("获得平民词"+com[0]);
                System.out.println("获得卧底词"+com[1]);
            }catch(Exception e){
                sender.sendMessage("出错辣,错误详情如下");
                sender.sendMessage(e.toString());
            }

            topic = new Topic(com[0],com[1]);
            sender.sendMessage(
                    "出题成功\n"+
                            "平民词："+com[0]+'\n'+
                            "卧底词："+com[1]+'\n'
            );
            send_topic();


        }

        //若发送者为玩家
        if( is_in_game(sender) ){
            if(topic!=null){
                sender.sendMessage("您的题目为:"+find_in_game(sender).subject);
            }
            else{
                sender.sendMessage("此群还未加载题目");
            }

        }

    }

    public Player find_in_game(User sender){
        for(Player a:player_list){
            if(a.id.getId()==sender.getId()) return a;
        }
        return null;
    }

    public boolean is_in_game(User sender){
        for(Player a:player_list){
            if(a.id.getId()==sender.getId()) return true;
        }
        return false;
    }

}