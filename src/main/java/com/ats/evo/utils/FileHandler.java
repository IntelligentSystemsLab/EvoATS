package com.ats.evo.utils;

import com.ats.evo.Globals;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

public class FileHandler {
    private HashMap<String, HashSet<String>> KGBase ;

    public void HandleKGBase(String workspace) { //列出文件夹下的所有知识图谱
        KGBase = new HashMap<>();
        HashSet<String> generations = DataStructuralUtil.getDictionaries(workspace);
        for (String generation : generations) {
            HashSet<String> KGs = DataStructuralUtil.getFiles(workspace + "/" + generation);
            KGBase.put(generation,KGs);
        }
    }
    public File parseOntologyPath(String workspace, String generation,String req){
        return new File(workspace + "/" + generation + "/" +req+".owl");
    }


    public HashMap<String, HashSet<String>> getKGBase() {
        return KGBase;
    }
}

