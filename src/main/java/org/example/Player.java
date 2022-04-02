package org.example;

import net.mamoe.mirai.contact.Member;

public class Player {
    Member id;
    boolean is_wd=false;
    String subject;
    boolean is_survival=true;
    String describe="";
    int votes;
    boolean voting_rights=true;

    public Player(){}

    public Player(Member _id){
        id=_id;
    }

}
