package com.webdifftool.server;

import com.ats.evo.utils.DataStructuralUtil;

import java.util.*;

import static com.ats.evo.utils.DataStructuralUtil.cloneHashSet;

public class JointGraphOWLManager extends com.webdifftool.server.OWLManager {
    public HashSet<String> getChildConcepts(HashSet<String> concepts ) { //返回层级关系上的子概念
        HashSet<String> childConcepts = new HashSet<>();
        Set<String> conceptClone = DataStructuralUtil.cloneHashSet(concepts);

        for (String[] relation : relationshipsImport) {

            if (conceptClone.contains(relation[1])) {
                if (relation[2].equals("is_a")) {
                    childConcepts.add(relation[0]);
                }
            }
        }
        return childConcepts;
    }
    public HashSet<String> getFatherConcepts(HashSet<String> concepts ){
        HashSet<String> fatherConcepts = new HashSet<>();
        Set<String> conceptClone = DataStructuralUtil.cloneHashSet(concepts);

        for (String child : conceptClone) {
            if (relationships.get(child) != null) {
                //获取所有概念 一一找其父类
                for (String[] strings : relationships.get(child)) {
                    if(strings[2].equalsIgnoreCase("Thing")){
                        continue;
                    }
                    if (strings[1].equals("is_a")) {
                        String potentialFather = strings[2];
                        fatherConcepts.add(potentialFather);
                    }
                }
            }
        }
        return fatherConcepts;
    }

    public Map<String, List<String[]>> getFatherHierarchicalRelations(HashSet<String> concepts ){
        Set<String> conceptClone = cloneHashSet(concepts);
        Map<String, List<String[]>> relations = new HashMap<>();
        for (String child : conceptClone) {
            if (relationships.get(child) != null) {
                //获取所有概念 一一找其父类
                for (String[] strings : relationships.get(child)) {
                    if(strings[2].equalsIgnoreCase("Thing")){
                        continue;
                    }
                    if (strings[1].equals("is_a")) {
                        String potentialFather = strings[2];
                        List<String[]> rels = relations.get(child);
                        if (rels == null) {
                            rels = new Vector<>();
                        }
                        rels.add(new String[]{child, "is_a", potentialFather});
                        relations.put(child, rels);
                    }
                }
            }
        }
        return relations;
    }

    public void getComponentAttributes(com.webdifftool.server.OWLManager owl) {

        for (String concept : concepts) {
            if (owl.getAttributes().get(concept) != null) {
                owl.getAttributes().put(concept, attributes.get(concept));
            }
        }

    }


    public void removeDuplicateRelations(com.webdifftool.server.OWLManager owl) {

        for (String key : owl.getRelationships().keySet()) {

            HashSet<String[]> set = new HashSet(owl.getRelationships().get(key));
            owl.getRelationships().get(key).clear();
            owl.getRelationships().get(key).addAll(set);
        }
    }
    public boolean findRelevantHierarchicalRelation(com.webdifftool.server.OWLManager groupedOWL, com.webdifftool.server.OWLManager owl) { //找到相关关系 则融合子图，并返回true
        boolean isCrossed = false;
        HashSet<String> concepts1 = groupedOWL.getConcepts();
        HashSet<String> fathers1 = this.getFatherConcepts(concepts1);
        HashSet<String> concepts2 = owl.getConcepts();
        HashSet<String> fathers2 = this.getFatherConcepts(concepts2);
        fathers1.retainAll(concepts2);
        fathers2.retainAll(concepts1);
        if(fathers1.size()>0||fathers2.size()>0) {
            isCrossed = true;
            groupedOWL.getConcepts().addAll(concepts2);
            if (fathers1.size() > 0) { //2 is father of 1
                for(String concept : fathers1){
                    for(String[] strings : relationshipsImport){
                        if(strings[1].equals(concept)){
                            if(strings[2].equals("is_a")&&(concepts1.contains(strings[0]))) {
                                List<String[]> rels = groupedOWL.getRelationships().get(strings[0]);
                                if (rels == null) {
                                    rels = new Vector<>();
                                }
                                rels.add(new String[]{strings[0], "is_a", strings[1]});
                                groupedOWL.getRelationships().put(strings[0],rels);
                            }
                        }
                    }
                }
            }
            if (fathers2.size() > 0) {
                for(String concept : fathers2){
                    for(String[] strings : relationshipsImport){
                        if(strings[1].equals(concept)){
                            if(strings[2].equals("is_a")&&(concepts2.contains(strings[0]))) {
                                List<String[]> rels = groupedOWL.getRelationships().get(strings[0]);
                                if (rels == null) {
                                    rels = new Vector<>();
                                }
                                rels.add(new String[]{strings[0], "is_a", strings[1]});
                                groupedOWL.getRelationships().put(strings[0],rels);
                            }
                        }
                    }
                }
            }
        }
        return isCrossed;
    }
    public boolean findRelevantHierarchicalRelation(com.webdifftool.server.OWLManager groupedOWL, com.webdifftool.server.OWLManager owl, Boolean externalFusion) { //找到相关关系 则融合子图，并返回true
        boolean isCrossed = false;
        if(externalFusion) {

            HashSet<String> concepts1 = groupedOWL.getConcepts();
            HashSet<String> fathers1 = this.getFatherConcepts(groupedOWL.getTempConcepts());
            HashSet<String> concepts2 = owl.getConcepts();
            HashSet<String> fathers2 = this.getFatherConcepts(owl.getTempConcepts());
            fathers1.retainAll(concepts2);
            fathers2.retainAll(concepts1);
            if (fathers1.size() > 0 || fathers2.size() > 0) {
                isCrossed = true;
                groupedOWL.getConcepts().addAll(concepts2);
                injectRelations(groupedOWL, concepts1, fathers1);
                injectRelations(groupedOWL, concepts2, fathers2);
            }
        } else {
            isCrossed = findRelevantHierarchicalRelation(groupedOWL,owl);
        }
        return isCrossed;
    }

    private void injectRelations(com.webdifftool.server.OWLManager groupedOWL, HashSet<String> concepts1, HashSet<String> fathers1) {
        if (fathers1.size() > 0) { //2 is father of 1
            for (String concept : fathers1) {
                for (String[] strings : relationshipsImport) {
                    if (strings[1].equals(concept)) {
                        if (strings[2].equals("is_a") && (concepts1.contains(strings[0]))) {
                            insertRelations(groupedOWL.getRelationships(), strings[0], "is_a", strings[1]);
                        }
                    }
                }
                groupedOWL.getConcepts().add(concept);
                for (String head : getHeadEntities(groupedOWL.getTempRelationships(), "is_a", concept)) {
                    insertRelations(groupedOWL.getRelationships(), head, "is_a", concept);
                }

            }
        }
    }

    public boolean isDisjoint(com.webdifftool.server.OWLManager owl1, com.webdifftool.server.OWLManager owl2){ //判断两个子图是否没有交集
        return Collections.disjoint(owl1.getConcepts(),owl2.getConcepts());
    }
    public void classifyHierarchy(){ //对知识库中概念的层级进行划分
        HashSet<String> children = new HashSet<>();
        children.add("Thing");
        int layer = 0;
        while (children.size()>0){
            for (String child : children){
                conceptLayer.put(child, layer);
            }
            children = this.getChildConcepts(children);
            layer++;
            for (String child : children){
                conceptLayer.put(child, layer);
            }
        }

    }

    public HashSet<String> getFatherLayerConceptsAndRelations(HashSet<String> concepts, com.webdifftool.server.OWLManager owl) {  //找到概念集的父类
        HashSet<String> fatherConcepts = new HashSet<>();
        Set<String> conceptClone = cloneHashSet(concepts);

        for (String child : conceptClone) {
            if (relationships.get(child) != null) {
                //获取所有概念 一一找其父类
                for (String[] strings : relationships.get(child)) {
                    if(strings[2].equalsIgnoreCase("Thing")){
                        continue;
                    }
                    if (strings[1].equals("is_a")) {
                        String potentialFather = strings[2];
                        concepts.add(potentialFather);
                        fatherConcepts.add(potentialFather);
                        List<String[]> rels = owl.getRelationships().get(child);
                        if (rels == null) {
                            rels = new Vector<>();
                        }
                        rels.add(new String[]{child, "is_a", potentialFather});
                        owl.getRelationships().put(child, rels);
                    }
                }
            }
        }
        return fatherConcepts;
    }


    public void computeFatherGraph(){

    }

    /**
     *根据知识图谱，完成完整需求子图的构建，形成需求子图的在层级关系上的完整形式
     * @param requirementOWL
     */
    public void computeRequirementFullGraph(com.webdifftool.server.OWLManager requirementOWL) {
        //要求：直接关联场景要素的 父类 相关的概念及其父类 这些概念的所有关系和属性
        com.webdifftool.server.OWLManager output = new OWLManager();
        HashSet<String> assertedConcepts = requirementOWL.getConcepts();
        output.getConcepts().addAll(assertedConcepts);
        int i = 0;
        //关联场景的所有要素

        HashSet<String> fathers = getFatherLayerConceptsAndRelations(assertedConcepts, requirementOWL);
        requirementOWL.getConcepts().addAll(fathers);


        while (fathers.size() > 0) {

            fathers = getFatherLayerConceptsAndRelations(fathers,requirementOWL);   //循环寻找父类
            requirementOWL.getConcepts().addAll(fathers);


            i++;    //TODO：判断退出条件
            if (i > 10) {
                break;
            }

        }
        //关系去重
        removeDuplicateRelations(requirementOWL);

        //找到了所有有关概念
        getComponentAttributes(requirementOWL);

    }

//    public void findIsARelation(OWLManager owl, HashSet<String> fathers, HashSet<String> children){ //找层级关系
//        for(String child : children){
//            owl.getRelationships().get(child).
//        }
//
//    }
}
