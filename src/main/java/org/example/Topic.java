package org.example;

public class Topic {
    public String square_topic;
    public String back_topic;

    public Topic(){}
    public Topic(String a,String b){
        square_topic=a;
        back_topic=b;
    }

    public String toString(){
        return "平民词:"+square_topic+"  卧底词:"+back_topic;
    }

}
