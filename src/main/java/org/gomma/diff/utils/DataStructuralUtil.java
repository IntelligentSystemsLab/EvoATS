package org.gomma.diff.utils;

import java.util.HashSet;

public class DataStructuralUtil {

    public HashSet<String> cloneHashSet(HashSet<String> set){
        HashSet<String> res = new HashSet<>();

        for (String name : set){
            res.add(name);
        }
        return res;
    }

}
