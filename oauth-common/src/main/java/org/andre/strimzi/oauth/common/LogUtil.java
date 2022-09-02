package org.andre.strimzi.oauth.common;

public class LogUtil {
    public static String mask(String input){
        if(input==null){
            return null;
        }
        int len = input.length();
        if(len<8){
            return "********";
        }
        if(len<20){
            return ""+input.charAt(0)+"**********";
        }
        return input.substring(0, 4) + "**" + input.substring(len-4, len);
    }
}
