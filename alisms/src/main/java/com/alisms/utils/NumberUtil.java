package com.alisms.utils;


import org.apache.commons.lang.StringUtils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class NumberUtil {
    private static String str1="1";
    public static boolean isNumber(String pStr) {
        char[] p = pStr.toCharArray();
        for (int i = 0; i < p.length; i++) {
            if (!Character.isDigit(p[i])) {
                return true;
            }
        }
        return false;
    }

    public static String getDate() {
        Calendar instance = Calendar.getInstance();
        Date time = instance.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String format1 = format.format(time);
        return format1;
    }

    public static synchronized String getSerielNo(String currentDate) {
        if(!StringUtils.isBlank(currentDate)) {
            String substring = currentDate.substring(3, 11);//数据库最新订单号
            System.out.println(substring + "================");
            Calendar instance2 = Calendar.getInstance();
            Date time1 = instance2.getTime();
            String format1 = new SimpleDateFormat("yyyyMMdd").format(time1);
            System.out.println(format1 + ">>>>>>>>>>>>>>>>>>");
            if (!substring.equals(format1)) {
                System.out.println("=============================");
                str1 = "1";
            }
            Calendar instance = Calendar.getInstance();
            Date time = instance.getTime();
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            DecimalFormat df = new DecimalFormat("00000");
            String str2 = df.format(Integer.parseInt(str1));
            String str = "KDH" + format.format(time) + str2;
            str1 = String.valueOf(Integer.valueOf(str2) + 1);
            System.out.println(str1);
            System.out.println(str);
            return str;
        }else{
            Calendar instance = Calendar.getInstance();
            Date time = instance.getTime();
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            DecimalFormat df = new DecimalFormat("00000");
            String str2 = df.format(Integer.parseInt(str1));
            String str = "KDH" + format.format(time) + str2;
            str1 = String.valueOf(Integer.valueOf(str2) + 1);
            System.out.println(str1);
            System.out.println(str);
            return str;
        }
    }


    public static void main(String[] args) {
    }

    public static boolean isDecimalNumber(String str) {
        String reg = "^[0-9]+(.[0-9]+)?$";
        return !str.matches(reg);
    }

    public static boolean isCurrentDate(String currentStr){
        String substring = currentStr.substring(3, 11);
        //KDH2019021200013
        String format = new SimpleDateFormat("yyyyMMdd").format(new Date());
        System.out.println(substring);
        if (format.equals(substring)){
            return true;
        }
        return false;
    }
}
