package com.ats.evo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class leetcode {
    public int romanToInt(String s) {
        Map<Character, Character> precedenceMap = new HashMap<>();
        precedenceMap.put('V','I');
        precedenceMap.put('X','I');
        precedenceMap.put('L','X');
        precedenceMap.put('C','X');
        precedenceMap.put('D','C');
        precedenceMap.put('M','C');

        Map<Character,Integer> valueMap = new HashMap<>();
        valueMap.put('V', 5);
        valueMap.put('L', 50);
        valueMap.put('C', 100);
        valueMap.put('D', 500);
        valueMap.put('M', 1000);
        valueMap.put('X', 10);
        valueMap.put('I', 1);

        char[] c = s.toCharArray();
        int res =0;
        for(int i=0;i< c.length;i++){
            if(i+1<c.length && precedenceMap.containsKey(c[i+1])
                    && precedenceMap.get(c[i+1]) == c[i]){
                res += valueMap.get(c[i+1]) - valueMap.get(c[i]);
                i++;
            }else{
                res += valueMap.get(c[i]);
            }
        }
        return res;
    }

    public static void main(String[] args) {
        leetcode lc = new leetcode();
        System.out.println(lc.romanToInt("MCMXCIV"));
    }
}
