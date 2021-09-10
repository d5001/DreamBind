package me.mcdcs.dreambind;

import java.util.ArrayList;

public class Config {
    public static ArrayList<String> getStringList(String s){
        return (ArrayList<String>) DreamBind.config.getStringList(s);
    }

    public static String getString(String s,String z){
        return DreamBind.config.getString(s,z);
    }
}
