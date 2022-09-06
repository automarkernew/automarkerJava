package com.tagging.utils;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataUtils {
    // 电话正则表达式
    public static String regex = "0\\d{2,3}[-]?\\d{7,8}|0\\d{2,3}\\s?\\d{7,8}|1[3-9]\\d{9}";

    public static String getSysTimeByFormat(String format) {
        SimpleDateFormat df = new SimpleDateFormat(format);//设置日期格式
        return df.format(new Date());
    }

    // 获取格式时间，形如：20210703183022
    public static String getSysTimeByFormat() {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");//设置日期格式
        return df.format(new Date());
    }


    public static String timeFormat(String args) {
        StringBuilder stringBuilder = new StringBuilder(args);
        if (args.length() == 6) {
            stringBuilder.insert(2,":");
            stringBuilder.insert(5,":");
        } else if (args.length() == 8){
            stringBuilder.insert(4,"-");
            stringBuilder.insert(7,"-");
        } else if (args.length() ==12) {
            stringBuilder.insert(4,"-");
            stringBuilder.insert(7,"-");
            stringBuilder.insert(10," ");
            stringBuilder.insert(13,":");
        }
        else if(args.length() == 14) {
            stringBuilder.insert(4,"-");
            stringBuilder.insert(7,"-");
            stringBuilder.insert(10," ");
            stringBuilder.insert(13,":");
            stringBuilder.insert(16,":");
        }
        return stringBuilder.toString();
    }

    public static String antiTimeFormat(String timeStr) {
        if (null == timeStr) return null;
        if (timeStr.length() == 8) {
            timeStr = timeStr.substring(0, 2) + timeStr.substring(3, 5)
                    + timeStr.substring(6);
        } else if (timeStr.length() == 10){
            timeStr = timeStr.substring(0, 4) + timeStr.substring(5, 7)
                    + timeStr.substring(8);
        }
        else if(timeStr.length() == 19) {
            timeStr = timeStr.substring(0, 4) + timeStr.substring(5, 7)
                    + timeStr.substring(8, 10) + timeStr.substring(11, 13)
                    + timeStr.substring(14, 16) + timeStr.substring(17);
        }
        return timeStr;
    }


    public static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<String>();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) emptyNames.add(pd.getName());
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

    //封装同名称属性复制，但是空属性不复制过去
    public static void copyPropertiesIgnoreNull(Object src, Object target) {
        BeanUtils.copyProperties(src, target, getNullPropertyNames(src));
    }

    /**
     * 电话号码正则判断
     * @param phoneNumber 电话号码
     * @return true 合格; false 非法电话号码
     */
    public static boolean isPhoneNumber(String phoneNumber) {
        if (null == phoneNumber) return false;
        Pattern pattern = Pattern.compile(regex);//编译正则表达式
        Matcher matcher = pattern.matcher(phoneNumber);  //创建给定输入模式的匹配器
        return matcher.matches();
    }

    // 对象字段非空处理
    public static Object notNullOperate(Object obj) throws IllegalAccessException {
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            String type = field.getGenericType().toString();
            field.setAccessible(true);
            if (null == field.get(obj)) {
                switch (type) {
                    case "class java.lang.String":
                        field.set(obj, "");

                        break;
                    case "class java.lang.Integer":
                    case "class java.lang.Short":
                    case "class java.lang.Double":
                    case "int":
                    case "long":
                    case "short":
                    case "float":
                    case "double":
                        field.set(obj, 0);

                        break;
                    case "class java.lang.Boolean":
                        field.set(obj, false);
                        break;
                }
            }
        }
        return obj;
    }

    /**
     * rainMinutes -> rain-minutes
     * @param str 如：rainMinutes
     * @return 如：rain-minutes
     */
    public static String shortMinusStr(String str) {
        StringBuilder result = new StringBuilder();
        if (str != null && str.length() > 0) {
            // 循环处理字符
            for (int i = 0; i < str.length(); i++) {
                String s = str.substring(i, i + 1);
                // 在大写字母前添加短杆
                if (s.equals(s.toUpperCase()) && !Character.isDigit(s.charAt(0))) {
                    result.append("-");
                }
                // 其他字符直接转成小写
                result.append(s.toLowerCase());
            }
        }
        return result.toString();
    }

    /**
     * 拼接字符串
     * @param strs 待拼接的字符串List
     * @param separator 分隔符
     * @return 拼接后的字符串
     */
    public static String concatStr(List<String> strs, String separator) {
        if (null == strs || strs.size() == 0) return null;
        StringBuilder s = new StringBuilder(strs.get(0));
        if (strs.size() == 1) return s.toString();

        for (int i = 1; i < strs.size(); i++) {
            s.append(separator).append(strs.get(i));
        }

        return s.toString();
    }
}

