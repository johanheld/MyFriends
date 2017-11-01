package com.example.johan.myfriends.Modules;

/**
 * Created by johan on 2017-11-01.
 */

public class TextMessage
{
    private String group;
    private String member;
    private String message;

    public TextMessage(String group, String member, String message)
    {
        this.group = group;
        this.member = member;
        this.message = message;
    }

    public String getGroup()
    {
        return group;
    }

    public String getMember()
    {
        return member;
    }

    public String getMessage()
    {
        return message;
    }
}
