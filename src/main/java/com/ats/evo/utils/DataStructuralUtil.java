package com.ats.evo.utils;

import java.io.File;
import java.util.HashSet;

public class DataStructuralUtil {

    public static HashSet<String> cloneHashSet(HashSet<String> set){
        HashSet<String> res = new HashSet<>();

        for (String name : set){
            res.add(name);
        }
        return res;
    }
    public static HashSet<String> getFiles(String path){
        File file = new File(path);
        File[] array = file.listFiles();
        HashSet<String> list = new HashSet<>();
        if (array != null) {
            for (File value : array) {
                if (value.isFile()) {
                    list.add(value.getName());
                }
            }
        }
        return list;
    }
    public static HashSet<String> getDictionaries(String path){
        File file = new File(path);
        File[] array = file.listFiles();
        HashSet<String> list = new HashSet<>();
        assert array != null;
        for (File value : array) {
            if (value.isDirectory()) {
                list.add(value.getName());
            }
        }
        return list;
    }





}
