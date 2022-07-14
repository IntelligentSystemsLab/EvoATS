package com.ats.evo;

import com.ats.evo.utils.OntologyCreateUtil;
import com.webdifftool.server.BasicDiffManager;
import com.ats.evo.utils.FileHandler;
import com.io.OntologyReader;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import com.webdifftool.client.model.DiffEvolutionMapping;
import com.webdifftool.server.JointGraphOWLManager;
import com.webdifftool.server.OWLManager;

import java.io.IOException;
import java.util.*;

public class AdaptiveDesign {
    private List<OWLManager> requirementKGs;
    private JointGraphOWLManager jointKG;



    private String workspace = "/Users/godson/Documents/CStandard";
    private String KGBaseName = "KnowledgeBase";

    public List<OWLManager> getRequirementKGs() {
        return requirementKGs;
    }

    public void setRequirementKGs(List<OWLManager> requirementKGs) {
        this.requirementKGs = requirementKGs;
    }

    public JointGraphOWLManager getJointKG() {
        return jointKG;
    }

    public void setJointKG(JointGraphOWLManager jointKG) {
        this.jointKG = jointKG;
    }

    public static void main(String[] args) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
        int i = 0;
        AdaptiveDesign ad = new AdaptiveDesign();
//        ad.requirementCombine("C3", "Execute_DDT,Active_System");
        ad.parseKGs(ad.getWorkspace(), ad.KGBaseName, "C3zw", "Execute_DDT,Monitor");
//        outputCompletedRequirements(generation);
        List<OWLManager> managers = ad.planA(ad.getJointKG(), ad.getRequirementKGs());

            String name = managers.get(0).designOntology("/Users/godson/Documents/out","Execute_DDT,Monitor", "C3" + i);

    }


    public void parseKGs(String workspace,String KGBaseName, String generation, String require) throws OWLOntologyCreationException { //初始化数据格式，根据需求加载对应功能知识图谱
        FileHandler fh = new FileHandler();
        OntologyReader reader = new OntologyReader();
        requirementKGs = new ArrayList<>();
        setWorkspace(workspace);
        setKGBaseName(KGBaseName);
        fh.HandleKGBase(getWorkspace());
        String[] requires = require.split(",");
        for (String req : requires) {
            if (fh.getKGBase().get(generation).contains(req + ".owl")) {
                OWLOntology ont = reader.loadOntology(fh.parseOntologyPath(getWorkspace(),generation, req));
                OWLManager owl = OntologyCreateUtil.initOntologyManager(ont);
                owl.setKGName(req);
                requirementKGs.add(owl);
            }
        }
        jointKG = OntologyCreateUtil.initJointOntologyManager(reader.loadOntology(fh.parseOntologyPath(getWorkspace(),generation, getKGBaseName())));
        jointKG.classifyHierarchy();
    }

//    public void outputCompletedRequirements(String generation) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException { ////输出需求对应的功能图在层级上的完整表示
//        for (OWLManager requirementKG : requirementKGs) {
//            OntologyCreateUtil.completeOntology(jointKG, requirementKG);
//            requirementKG.designOntology(requirementKG.getKGName(), generation);
//        }
//
//    }

//    public void requirementCombine(String generation, String require) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {  //将各个需求代表的子图连接起来
//        parseKGs(generation, require);
////        outputCompletedRequirements(generation);
//        OWLManager owl = new OWLManager();
//        for (OWLManager requirementKG : requirementKGs) {
//            owl.getConcepts().addAll(requirementKG.getConcepts());
//            owl.getAttributes().putAll(requirementKG.getAttributes());
//            owl.getRelationships().putAll(requirementKG.getRelationships());
//        }
//        owl.designOntology(require, generation);
//
//    }

    public DiffEvolutionMapping computeEvolution(OWLManager oldManager, OWLManager newManager) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
        DiffComputation dc = new DiffComputation();
        DiffExecutor.getSingleton().setupRepository();
        BasicDiffManager basicDiffManager = new BasicDiffManager(oldManager, newManager);
//        System.out.println("Loading ontology versions");
//        oldManager.computeRequirementElements(scene,0);
//        newManager.computeRequirementElements(scene,0);
//
//        oldManager.designOntology(scene,1);
//        newManager.designOntology(scene,2);

        basicDiffManager.analysisBasicChange();
//		owl.parseAndIntegrateChanges(owl.getOBOContentFromFile(oldVersion), owl.getOBOContentFromFile(newVersion));
        return null;

    }


    public static int findNextDisjointGraphIndicator(int i, List<Boolean> isCrossed) {
        if (!isCrossed.contains(false)) {
            return -1;
        }
        int j = -1;
        for (j = i + 1; j < isCrossed.size(); j++) {
            if (!isCrossed.get(j)) {
                break;
            }
        }
        return j;
    }

    public static List<OWLManager> CombineByHierarchicalRelation(JointGraphOWLManager joint, List<OWLManager> owlManagers) {
        List<Boolean> isCrossed = new ArrayList<>();
        List<OWLManager> res = new ArrayList<>();
        for (int i = 0; i < owlManagers.size(); i++) {
            isCrossed.add(false);
        }
        while (isCrossed.contains(false)) {
            OWLManager groupedOWL = initGroupedOWL(owlManagers, isCrossed);

            while (true) {
                int added = 0;
                for (int i = 0; (i < owlManagers.size()) && (i >= 0); i = findNextDisjointGraphIndicator(i, isCrossed)) {
                    if (!(isCrossed.get(i))) {
                        isCrossed.set(i, joint.findRelevantHierarchicalRelation(groupedOWL, owlManagers.get(i)));
                        if(isCrossed.get(i)){
                            OWLManager.addElementsForOWLManager(groupedOWL,owlManagers.get(i));
                        }
                        added++;
                    }
                }
                if (added == 0) {
                    break;
                }
            }
            res.add(groupedOWL);
        }
        return res;
    }

    public static List<OWLManager> CombineByExtendSubgraph(JointGraphOWLManager joint, List<OWLManager> owlManagers) {
        List<Boolean> isCrossed = new ArrayList<>();
        List<OWLManager> res = new ArrayList<>();
        for (int i = 0; i < owlManagers.size(); i++) {
            isCrossed.add(false);
        }
        while (isCrossed.contains(false)) {
            OWLManager groupedOWL = initGroupedOWL(owlManagers, isCrossed);

            while (true) {
                int added = 0;
                for (int i = 0; (i < owlManagers.size()) && (i >= 0); i = findNextDisjointGraphIndicator(i, isCrossed)) {
                    if (!(isCrossed.get(i))) {
                        isCrossed.set(i, joint.findRelevantHierarchicalRelation(groupedOWL, owlManagers.get(i),true));
                        if(isCrossed.get(i)){
                            OWLManager.addElementsForOWLManager(groupedOWL,owlManagers.get(i));
                        }
                        added++;
                    }
                }
                if (added == 0) {
                    break;
                }
            }
            res.add(groupedOWL);
        }
        return res;
    }

    private static OWLManager initGroupedOWL(List<OWLManager> owlManagers, List<Boolean> isCrossed) {
        OWLManager groupedOWL = new OWLManager();
        int j;
        for (j = 0; j < isCrossed.size(); j++) {
            if (!isCrossed.get(j)) {
                break;
            }
        }
        isCrossed.set(j, true);
        OWLManager.addElementsForOWLManager(groupedOWL, owlManagers.get(j));
        return groupedOWL;
    }

    public List<OWLManager> updateHierarchicalRelation(JointGraphOWLManager joint, List<OWLManager> owlManagers) {
        List<OWLManager> step3DisjointGraphs = new ArrayList<>();
        for (OWLManager before : owlManagers) {
            OWLManager after = new OWLManager();
            OWLManager.addElementsForOWLManager(after, before);
            HashSet<String> addedConcept = joint.getFatherConcepts(before.getConcepts());
            Map<String, List<String[]>> addedRelations = joint.getFatherHierarchicalRelations(before.getConcepts());
            after.addTempConcepts(addedConcept);  //添加节点和关系到暂存列表
            after.addTempRelationships(addedRelations);
//            after.getConcepts().addAll(joint.getFatherConcepts(before.getConcepts()));
//            after.getRelationships().putAll(addedRelations);
            step3DisjointGraphs.add(after);
        }
        return step3DisjointGraphs;

    }

    public List<OWLManager> planA(JointGraphOWLManager joint, List<OWLManager> owlManagers) { //处理几个不连通图的方案A
        //step 1:处理可连接的子图
        List<Boolean> isCrossed = new ArrayList<>();  //用来鉴别第一步中未连接的子图
        List<OWLManager> step1DisjointGraphs = new ArrayList<>();
        List<OWLManager> step2DisjointGraphs;
        List<OWLManager> step3DisjointGraphs;
        List<OWLManager> step4DisjointGraphs;

        for (int i = 0; i < owlManagers.size(); i++) {
            isCrossed.add(false);
        }
        while (isCrossed.contains(false)) {
            OWLManager groupedOWL = initGroupedOWL(owlManagers, isCrossed);
            while (true) {
                int added = 0;
                for (int i = 0; (i < owlManagers.size()) && (i >= 0); i = findNextDisjointGraphIndicator(i, isCrossed)) {
                    if ((!isCrossed.get(i))) {
                        if (!(joint.isDisjoint(groupedOWL, owlManagers.get(i)))) { //如果可以融合
                            OWLManager.addElementsForOWLManager(groupedOWL, owlManagers.get(i));
                            isCrossed.set(i, true);
                            added++;
                        }
                    }
                }
                if (added == 0) {
                    break;
                }
            }
            step1DisjointGraphs.add(groupedOWL);

        }

        //step 2:处理完成聚合后 仍孤立的子图
        //Case1:两个图存在父/子类关联
        List<OWLManager> midResult= step1DisjointGraphs;

        while (midResult.size() > 1) {
            step2DisjointGraphs = CombineByHierarchicalRelation(joint, midResult);

            if (step2DisjointGraphs.size()>1) {

                //Case2:无法通过父子类关系将功能关联起来，继续向父类寻找可相连的共同实体(两个方案：1.按层级划分向上推演关系。2.子图分别找父类关联)
                //1.两两比较，能连接上：连接 忽略多余信息
                step3DisjointGraphs = updateHierarchicalRelation(joint, midResult);
                step4DisjointGraphs = CombineByExtendSubgraph(joint,step3DisjointGraphs);

//                2.没有能连接的，再向上走一层，循环
                while(step4DisjointGraphs.size() == step3DisjointGraphs.size()){
                    for(OWLManager owl : step3DisjointGraphs){
                        owl.addTempRelationships(joint.getFatherHierarchicalRelations(owl.getTempConcepts()));
                        owl.addTempConcepts(joint.getFatherConcepts(owl.getTempConcepts()));
                    }
                    step4DisjointGraphs = CombineByExtendSubgraph(joint,step3DisjointGraphs);
                }
                midResult = step4DisjointGraphs;


            } else if (step2DisjointGraphs.size() == 1) {
                midResult = step2DisjointGraphs;
                break;
            }
        }

        //最终处理结果
        for (OWLManager graph : midResult) {
            joint.getComponentAttributes(graph);
            joint.removeDuplicateRelations(graph);
        }
        return midResult;
    }
    public String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    public String getKGBaseName() {
        return KGBaseName;
    }

    public void setKGBaseName(String KGBaseName) {
        this.KGBaseName = KGBaseName;
    }

}
