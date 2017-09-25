package com.zls.util;

public class StringUtil {
    public static boolean isNotEmpty(String str){
        if (str!=null&&!str.equals("")){
            return true;
        }else{
            return false;
        }
    }

    public static boolean isEmpty(String str){
        if (str==null||str.equals("")){
            return true;
        }else{
            return false;
        }
    }
    
    public static boolean exisStrArr(String str,String[] strArr){
    	for (int i = 0; i < strArr.length; i++) {
			if (str.equals(strArr[i])) {
				return true;
			}
		}
    	return false;
    }
}
