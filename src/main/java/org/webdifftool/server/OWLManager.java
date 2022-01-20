/*
 *
 *  * Copyright © 2014 - 2021 Leipzig University (Database Research Group)
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, version 3.
 *  *
 *  * This program is distributed in the hope that it will be useful, but
 *  * WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.webdifftool.server;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import org.gomma.diff.Globals;
import org.gomma.diff.utils.DataBaseHandler;
import org.gomma.diff.utils.DataStructuralUtil;
import org.gomma.diff.utils.DiffUtil;
import org.gomma.io.importer.models.ImportObj;
import org.gomma.io.importer.models.ImportObj.ImportObjAttribute;
import org.gomma.io.importer.models.ImportSourceStructure;
import org.gomma.io.importer.models.ImportSourceStructure.ImportObjRelationship;
import org.gomma.model.DataTypes;
import org.io.OntologyCreateUtil;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.search.EntitySearcher;

public class OWLManager {

    List<String[]> matchedConcepts;
    List<String[]> sceneConcepts;

    List<String> matchedConceptsList = new ArrayList<>();
    List<String> sceneConceptsList = new ArrayList<>();

    List<String[]> objectsImport;
    List<String[]> attributesImport;
    List<String[]> relationshipsImport;
    List<String> relationsName;

    public int getVersionConceptSize() {
        return versionConceptSize;
    }

    public void setVersionConceptSize(int versionConceptSize) {
        this.versionConceptSize = versionConceptSize;
    }

    public int getVersionRelationshipSize() {
        return versionRelationshipSize;
    }

    public void setVersionRelationshipSize(int versionRelationshipSize) {
        this.versionRelationshipSize = versionRelationshipSize;
    }

    public int getVersionAttributeSize() {
        return versionAttributeSize;
    }

    public void setVersionAttributeSize(int versionAttributeSize) {
        this.versionAttributeSize = versionAttributeSize;
    }

    public int getVersionATSConceptSize() {
        return versionATSConceptSize;
    }

    public void setVersionATSConceptSize(int versionATSConceptSize) {
        this.versionATSConceptSize = versionATSConceptSize;
    }

    public int getVersionATSRelationshipSize() {
        return versionATSRelationshipSize;
    }

    public void setVersionATSRelationshipSize(int versionATSRelationshipSize) {
        this.versionATSRelationshipSize = versionATSRelationshipSize;
    }

    public int getVersionATSAttributeSize() {
        return versionATSAttributeSize;
    }

    public void setVersionATSAttributeSize(int versionATSAttributeSize) {
        this.versionATSAttributeSize = versionATSAttributeSize;
    }

    public int getVersionMatchedConceptSize() {
        return versionMatchedConceptSize;
    }

    public void setVersionMatchedConceptSize(int versionMatchedConceptSize) {
        this.versionMatchedConceptSize = versionMatchedConceptSize;
    }

    public int getVersionMatchedRelationshipSize() {
        return versionMatchedRelationshipSize;
    }

    public void setVersionMatchedRelationshipSize(int versionMatchedRelationshipSize) {
        this.versionMatchedRelationshipSize = versionMatchedRelationshipSize;
    }

    public int getVersionMatchedAttributeSize() {
        return versionMatchedAttributeSize;
    }

    public void setVersionMatchedAttributeSize(int versionMatchedAttributeSize) {
        this.versionMatchedAttributeSize = versionMatchedAttributeSize;
    }

    public int getVersionSceneConceptSize() {
        return versionSceneConceptSize;
    }

    public void setVersionSceneConceptSize(int versionSceneConceptSize) {
        this.versionSceneConceptSize = versionSceneConceptSize;
    }

    public int getVersionSceneRelationshipSize() {
        return versionSceneRelationshipSize;
    }

    public void setVersionSceneRelationshipSize(int versionSceneRelationshipSize) {
        this.versionSceneRelationshipSize = versionSceneRelationshipSize;
    }

    public int getVersionSceneAttributeSize() {
        return versionSceneAttributeSize;
    }

    public void setVersionSceneAttributeSize(int versionSceneAttributeSize) {
        this.versionSceneAttributeSize = versionSceneAttributeSize;
    }

    private int versionConceptSize = 0;
    private int versionRelationshipSize = 0;
    private int versionAttributeSize = 0;

    private int versionATSConceptSize = 0;
    private int versionATSRelationshipSize = 0;
    private int versionATSAttributeSize = 0;

    private int versionMatchedConceptSize = 0;
    private int versionMatchedRelationshipSize = 0;
    private int versionMatchedAttributeSize = 0;

    private int versionSceneConceptSize = 0;
    private int versionSceneRelationshipSize = 0;
    private int versionSceneAttributeSize = 0;



    public HashSet<String> getConcepts() {
        return Concepts;
    }

    public void setConcepts(HashSet<String> concepts) {
        Concepts = concepts;
    }

    public Map<String, List<String[]>> getAttributes() {
        return Attributes;
    }

    public void setAttributes(Map<String, List<String[]>> attributes) {
        Attributes = attributes;
    }

    public Map<String, List<String[]>> getRelationships() {
        return Relationships;
    }

    public void setRelationships(Map<String, List<String[]>> relationships) {
        Relationships = relationships;
    }

    public Set<String> getATSConcepts() {
        return ATSConcepts;
    }

    public void setATSConcepts(Set<String> ATSConcepts) {
        this.ATSConcepts = ATSConcepts;
    }

    public Map<String, List<String[]>> getATSAttributes() {
        return ATSAttributes;
    }

    public void setATSAttributes(Map<String, List<String[]>> ATSAttributes) {
        this.ATSAttributes = ATSAttributes;
    }

    public Map<String, List<String[]>> getATSRelationships() {
        return ATSRelationships;
    }

    public void setATSRelationships(Map<String, List<String[]>> ATSRelationships) {
        this.ATSRelationships = ATSRelationships;
    }

    public void setSceneConcepts(Set<String> sceneConcepts) {
        SceneConcepts = sceneConcepts;
    }

    public Map<String, List<String[]>> getStructuralSceneAttributes() {
        return StructuralSceneAttributes;
    }

    public void setStructuralSceneAttributes(Map<String, List<String[]>> structuralSceneAttributes) {
        StructuralSceneAttributes = structuralSceneAttributes;
    }

    public Map<String, List<String[]>> getStructuralSceneRelationships() {
        return StructuralSceneRelationships;
    }

    public void setStructuralSceneRelationships(Map<String, List<String[]>> structuralSceneRelationships) {
        StructuralSceneRelationships = structuralSceneRelationships;
    }

    public Map<String, List<String[]>> getSceneAttributes() {
        return SceneAttributes;
    }

    public void setSceneAttributes(Map<String, List<String[]>> sceneAttributes) {
        SceneAttributes = sceneAttributes;
    }

    public Map<String, List<String[]>> getSceneRelationships() {
        return SceneRelationships;
    }

    public void setSceneRelationships(Map<String, List<String[]>> sceneRelationships) {
        SceneRelationships = sceneRelationships;
    }



    public Map<String, List<String[]>> getStructuralMatchedAttributes() {
        return StructuralMatchedAttributes;
    }

    public void setStructuralMatchedAttributes(Map<String, List<String[]>> structuralMatchedAttributes) {
        StructuralMatchedAttributes = structuralMatchedAttributes;
    }

    public Map<String, List<String[]>> getStructuralMatchedRelationships() {
        return StructuralMatchedRelationships;
    }

    public void setStructuralMatchedRelationships(Map<String, List<String[]>> structuralMatchedRelationships) {
        StructuralMatchedRelationships = structuralMatchedRelationships;
    }

    public Map<String, List<String[]>> getMatchedAttributes() {
        return MatchedAttributes;
    }

    public void setMatchedAttributes(Map<String, List<String[]>> matchedAttributes) {
        MatchedAttributes = matchedAttributes;
    }

    public Map<String, List<String[]>> getMatchedRelationships() {
        return MatchedRelationships;
    }

    public void setMatchedRelationships(Map<String, List<String[]>> matchedRelationships) {
        MatchedRelationships = matchedRelationships;
    }

    HashSet<String> Concepts = new HashSet<>();
    Map<String, List<String[]>> Attributes = new HashMap<>();
    Map<String, List<String[]>> Relationships = new HashMap<>();

    Set<String> ATSConcepts = new HashSet<>();
    Map<String, List<String[]>> ATSAttributes = new HashMap<>();
    Map<String, List<String[]>> ATSRelationships = new HashMap<>();

    Set<String> SceneConcepts = new HashSet<>();
    Map<String, List<String[]>> StructuralSceneAttributes = new HashMap<>();
    Map<String, List<String[]>> StructuralSceneRelationships = new HashMap<>();

    Map<String, List<String[]>> SceneAttributes = new HashMap<>();
    Map<String, List<String[]>> SceneRelationships = new HashMap<>();

//    Set<String> MatchedConcepts = new HashSet<>();
    Map<String, List<String[]>> StructuralMatchedAttributes = new HashMap<>();
    Map<String, List<String[]>> StructuralMatchedRelationships = new HashMap<>();

    Map<String, List<String[]>> MatchedAttributes = new HashMap<>();
    Map<String, List<String[]>> MatchedRelationships = new HashMap<>();

    public Map<String, String> conceptNames;

    private boolean useSplitInference = true;


//    public static void main(String[] args) throws Exception {
//        OWLManager test = new OWLManager();
//        test.parseAndIntegrateChanges("http://dbserv2.informatik.uni-leipzig.de/~hartung/MA_1-197.owl", "http://dbserv2.informatik.uni-leipzig.de/~hartung/MA_1-206.owl");
//    }

    public void getAssistConcept(OWLOntology ontology) { //用于获取非ATS组分的那些实体
        OWLReasonerFactory reasonerFactory = new ElkReasonerFactory();
        OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(ontology);

//		NodeSet<OWLClass> childC = reasoner.getSubClasses("owl:things",true);
//		// get relationships
//		for (Node<OWLClass> expr : superC.getNodes()) {
//			String relName = null, relValue = null;
//			for (OWLClass cExpr : expr.getEntities()) { //寻找is_a关系，通过推理机寻找对应实体的父类，即子类 is_a 父类
//				if (!cExpr.isAnonymous()) {
//					OWLClass rel = (OWLClass) cExpr;
//					relName = "is_a";
//					relValue = rel.getIRI().getFragment();  //IRI 即本体的编号对象，
//					if (relValue != null && relName != null) {
//						objRelSet.addRelationship(relValue, importObj.getAccessionNumber(), relName);  //结构：头，尾，关系
//					} else {
//						System.out.println("NULL VALUE");
//					}
//				}
//			}
//		}
    }

    public void parseOntology(OWLOntology Ontology) {

        this.initAssistComponents();
        this.readOWLOntology(Ontology);
        this.getAllConcepts( ); //结构：id
        this.getAllAttributes( ); //结构：（id:<id,属性名，属性值>)
        this.getAllRelationships( ); //结构:id,<头实体、关系、尾实体>

    }
    public void designOntology(String scene, int version) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
        OntologyCreateUtil ocu = new OntologyCreateUtil(this);
        OWLOntology ont = ocu.createOntology(scene, version);
        ocu.addATSSceneComponents(ont);
        ocu.outputOntology(ont, scene + version);

    }
//    public void designOntology() throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
//        OntologyCreateUtil ocu = new OntologyCreateUtil(this);
//        OWLOntology ont = ocu.createOntology(scene, version);
//        ocu.addATSSceneComponents(ont);
//        ocu.outputOntology(ont, scene + version);
//
//    }

    public void initAssistComponents() {
        sceneConcepts = new Vector<>();
        sceneConceptsList = new Vector<>();
        matchedConcepts = new Vector<>();
        matchedConceptsList = new Vector<>();
    }

    public Set<String> getChildConceptsAndRelations(HashSet<String> concepts) {
        DataStructuralUtil dsu = new DataStructuralUtil();
        Set<String> childConcepts = new HashSet<>();
        Set<String> conceptClone = dsu.cloneHashSet(concepts);

        for (List<String[]> relations : ATSRelationships.values()) {
            for (String[] relation : relations) {

                if (conceptClone.contains(relation[2])) {
                    if (relation[1].equals("is_a")) {
                        childConcepts.add(relation[0]);
                        List<String[]> rels = Relationships.get(relation[0]);
                        if (rels == null) {
                            rels = new Vector<>();
                        }
                        rels.add(relation);
                        Concepts.add(relation[0]);
                        Relationships.put(relation[0], rels);
                        versionRelationshipSize++;
                    }
                }
            }
        }

        return childConcepts;

    }

    public Set<String> getFatherConceptsAndRelations(HashSet<String> concepts) {
        DataStructuralUtil dsu = new DataStructuralUtil();
        Set<String> fatherConcepts = new HashSet<>();
        Set<String> conceptClone = dsu.cloneHashSet(concepts);
        Iterator<String> conceptIterator = conceptClone.iterator();

        while (conceptIterator.hasNext()) {
            String child = conceptIterator.next();
            if (ATSRelationships.get(child) != null) {
                //获取所有概念 一一找其父类
                for (String[] strings : ATSRelationships.get(child)) {

                    if (strings[1].equals("is_a")) {
                        String potentialFather = strings[2];
                        if (!(sceneConceptsList.contains(potentialFather) || matchedConceptsList.contains(potentialFather))) {
                            Concepts.add(potentialFather);
                            fatherConcepts.add(potentialFather);
                            List<String[]> rels = Relationships.get(child);
                            if (rels == null) {
                                rels = new Vector<>();
                            }
                            rels.add(new String[]{child, "is_a", potentialFather});
                            Relationships.put(child, rels);
                            versionRelationshipSize++;
                        }
                    }
                }
            }
        }
        return fatherConcepts;
    }

    public HashSet<String> getLogicalRelevantConceptsAndRelationships(HashSet<String> concepts) { //oldConcepts
        //要求：相关概念作为头实体和尾实体的 都需要考虑在内
        //结果：找到了相关联的概念 及其所有关系
        DataStructuralUtil du = new DataStructuralUtil();
        Set<String> cloneConcepts = du.cloneHashSet(concepts);
        HashSet<String> res = new HashSet<>();

        for (String concept : cloneConcepts) {
            if (ATSRelationships.get(concept) != null) {
                for (String[] relation : ATSRelationships.get(concept)) {
                    if (concept.equals(relation[0])) {   //所述概念为头实体时 找相关尾实体
                        List<String[]> rels = Relationships.get(concept);
                        Concepts.add(relation[2]);
                        res.add(relation[2]);
                        if (rels == null) {
                            rels = new Vector<>();
                        }
                        if (!rels.contains(relation)) {
                            rels.add(new String[]{relation[0], relation[1], relation[2]});
                        }
                        Relationships.put(concept, rels);
                    }
                    if (concept.equals(relation[2])) { //所述概念为尾实体时 找相关头实体
                        List<String[]> rels = Relationships.get(relation[0]);
                        Concepts.add(relation[0]);
                        res.add(relation[2]);
                        if (rels == null) {
                            rels = new Vector<>();
                        }
                        if (!rels.contains(relation)) {
                            rels.add(new String[]{relation[0], relation[1], relation[2]});
                        }
                        Relationships.put(relation[0], rels);
                    }
                }
            }

        }


        return res;
    }

    public void getComponentAttributes() {

        Iterator<String> it = Concepts.iterator();
        while (it.hasNext()) {
            String concept = it.next();
            if (ATSAttributes.get(concept) != null) {
                Attributes.put(concept, ATSAttributes.get(concept));
            }
        }

    }

    public void removeDuplicateRelations() {

        for (String key : Relationships.keySet()) {
            List<String[]> rels = Relationships.get(key);
            HashSet<String[]> set = new HashSet(rels);
            rels.clear();
            rels.addAll(set);
        }
    }

    public void computeSceneElements(String scene, int layer) {
        //要求：直接关联场景要素的子类 父类 相关的概念及其父类 这些概念的所有关系和属性
        HashSet<String> assertedConcepts = new HashSet<>();
        int i = 0;
        //关联场景的所有要素
        for (String[] strings : SceneRelationships.get(scene)) {
            Concepts.add(strings[0]);
            //加入直接关联的场景要素
            assertedConcepts.add(strings[0]);  //暂存 因为不需要对子类找更多的父类
        }

        getLogicalRelevantConceptsAndRelationships(assertedConcepts);  //找到直接相关的逻辑上相关的要素

        Set<String> assertedChildren = getChildConceptsAndRelations(assertedConcepts);  //找子类

        while (assertedChildren.size() > 0) {   //递归寻找子类
            getLogicalRelevantConceptsAndRelationships((HashSet<String>) assertedChildren);   //找子类逻辑上相关的要素
            HashSet<String> assertedFather = new HashSet<>();
            for (String res : assertedChildren) {
                assertedConcepts.add(res);
                assertedFather.add(res);
            }
            assertedChildren = getChildConceptsAndRelations(assertedFather);
        }


        Set<String> fathers = getFatherConceptsAndRelations(Concepts);
        HashSet<String> logicalRelevantConcept = new HashSet<>();
        if (layer > 0) {
            logicalRelevantConcept = getLogicalRelevantConceptsAndRelationships((HashSet<String>) fathers);
        }

        while (fathers.size() > 0) {

            HashSet<String> children = new HashSet<>();
            for (String res : fathers) {
                Concepts.add(res);
                children.add(res);
            }
            for (String res : logicalRelevantConcept) {
                children.add(res);
            }

            fathers = getFatherConceptsAndRelations(children);   //循环寻找父类
            layer--;
            if (layer > 0) {
                logicalRelevantConcept = getLogicalRelevantConceptsAndRelationships((HashSet<String>) fathers);
            }
            i++;    //TODO：判断退出条件
            if (i > 10) {
                break;
            }

        }
    //关系去重
    removeDuplicateRelations();

    //找到了所有有关概念
    getComponentAttributes();

}




    public void readOWLString(String content) {
        try {
            objectsImport = new Vector<String[]>();      //要输出的实体表
            attributesImport = new Vector<String[]>();//要输出的属性表
            relationshipsImport = new Vector<String[]>();//要输出的关系表

            OWLOntologyManager manager = org.semanticweb.owlapi.apibinding.OWLManager.createOWLOntologyManager();
            // load an ontology from a physical URI


            //test parsing with this class...DAZU MAIN METHODE AUSKOMMENTIEREN
            //String location = "http://webrum.uni-mannheim.de/math/lski/anatomy09/mouse_anatomy_2008.owl";
            //FUNKTIONIERT
            //http://webrum.uni-mannheim.de/math/lski/anatomy09/nci_anatomy_2008.owl
            //FUNKTIONIERT NICHT - andere Struktur des OWL files
            //http://purl.org/obo/owl/EHDAA

            InputStream is = new ByteArrayInputStream(content.getBytes());
            OWLOntology ontology = manager.loadOntologyFromOntologyDocument(is);

            HashMap<String, ImportObj> objHashMap = new HashMap<String, ImportObj>();
            ImportSourceStructure objRelSet = new ImportSourceStructure();
            OWLReasonerFactory reasonerFactory = new ElkReasonerFactory();

            OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(ontology); //推理机，用来寻找概念间关系及推导隐含关系
            for (OWLClass owlClass : ontology.getClassesInSignature()) {        //对原本体中所有实体操作，获得其所有概念、属性、实体、关系，返回对应本体字符串
				/*if(owlClass.getURI().toString().contains("http://www.geneontology.org")){
						System.out.println(owlClass.toString());
				}*/
                String id = owlClass.getIRI().getFragment();
                ImportObj importObj = new ImportObj(id);
                List<OWLAnnotation> annos = EntitySearcher.getAnnotations(owlClass.getIRI(),
                        ontology).collect(Collectors.toCollection(ArrayList::new));
                // get importObj (id & name)
                for (OWLAnnotation axiom : annos) {  //寻找原本体文件中各概念包含的属性，原本体中这些属性以“#”分隔符，现转化为set形式，储存在缓存对象importObj
                    String attributeName, attributeValue;
                    if (axiom.getValue() instanceof OWLLiteral) {
                        attributeName = axiom.getProperty().toString();
                        attributeName = attributeName.replace("<", "");
                        attributeName = attributeName.replace(">", "");
                        attributeValue = ((OWLLiteral) axiom.getValue()).getLiteral();

                        if (attributeValue.contains(".owl#")) {    //NORMALISIERUNG AN # IN STRING
                            String[] attSplitted = attributeValue.split("#");
                            attributeValue = attSplitted[1];
                        }
                        //if (attributeValue.contains("\'")){	System.out.println(attributeValue);	}
                        importObj.addAttribute(attributeName, "N/A", DataTypes.STRING, attributeValue);  //结构：（实体id,属性<属性名，范围，属性数据类型，属性值>）
                        //System.out.println(attributeName +  " - " + attributeValue);
                    } else {
                        try {

                            attributeName = axiom.getProperty().getIRI().getFragment();
                            attributeValue = axiom.getValue().toString();//.replace("&#39;", "'")
                            //OWLAnonymousIndividual testIndi = ((OWLAnonymousIndividual)axiom.getValue());

                            if (attributeValue.contains("#")) {    //NORMALISIERUNG AN # IN STRING
                                String[] attSplitted = attributeValue.split("#");
                                attributeValue = attSplitted[1];
                            }
                            //if (attributeValue.contains("\'")){	System.out.println(attributeValue);	}
                            //System.out.println(attributeName +  " - " + attributeValue);
                            importObj.addAttribute(attributeName, "N/A", DataTypes.STRING, attributeValue);
                        } catch (NullPointerException e) {
                        }
                    }
                }

                // add ImportObj to the objHashMap
                //OLD COMMENT (to get importObj directly by its id, for synonym&definition parsing)
                objHashMap.put(id, importObj);
                NodeSet<OWLClass> superC = reasoner.getSuperClasses(owlClass, true);
                // get relationships

                for (Node<OWLClass> expr : superC.getNodes()) {  //寻找is_a关系，通过推理机寻找对应实体的父类，即子类 is_a 父类

                    String relName = null, relValue = null;
                    for (OWLClass cExpr : expr.getEntities()) {
                        if (!cExpr.isAnonymous()) {
                            OWLClass rel = (OWLClass) cExpr;
                            relName = "is_a";
                            relValue = rel.getIRI().getFragment();  //IRI 即本体的编号对象，
                            if (relValue != null && relName != null) {
                                objRelSet.addRelationship(relValue, importObj.getAccessionNumber(), relName);
                            } else {
                                System.out.println("NULL VALUE");
                            }
                        }
                    }
                }
                for (OWLObjectPropertyDomainAxiom op : ontology.getAxioms(AxiomType.OBJECT_PROPERTY_DOMAIN)) {  //依据本体中的公理寻找其它类型关系 包含 Equivalent、Declaration、AnnotationAssertion、SubclassOf、DisjointClassOf
                    String relName = null, relValue = null;
                    if (op.getDomain().equals(owlClass)) {
                        for (OWLObjectProperty oop : op.getObjectPropertiesInSignature()) {
                            relName = oop.getIRI().toString();
                            relValue = oop.getNamedProperty().getIRI().getFragment();
//                            System.out.println(relValue);
                            if (relValue != null && relName != null) {
                                objRelSet.addRelationship(relValue, importObj.getAccessionNumber(), relName); //结构：头，尾，关系
                            } else {
                                System.out.println("NULL VALUE");
                            }
                        }

                    }
                }
            }

            // synonyms & definitions are parsed by the generic owl parser but return only a "genid"
            // replace synonyms & definitions by parsing the file
            // method returns the required objList instead of the objHashMap

            //Transform into COntoDiff format
            for (String objID : objHashMap.keySet()) {
                ImportObj obj = objHashMap.get(objID);
                //System.out.println(obj.getAccessionNumber());
                objectsImport.add(new String[]{obj.getAccessionNumber()});
                for (ImportObjAttribute objAtt : obj.getAttributeList()) {
                    if (objAtt.getAttName() != null) {
                        //System.out.println(obj.getAccessionNumber()+" "+objAtt.getAttName()+" "+objAtt.getValue());
                        attributesImport.add(new String[]{obj.getAccessionNumber(), objAtt.getAttName(), "N/A", objAtt.getValue()});

                        if (objAtt.getAttName().equals("rdfs:label")) {
                            this.conceptNames.put(obj.getAccessionNumber(), objAtt.getValue());
                            //System.out.println(obj.getAccessionNumber()+"\t"+objAtt.getValue());
                        }
                    }
                }
            }
            //System.out.println("number of concept names: " + conceptNames.size());
            for (ImportObjRelationship objRel : objRelSet.getRelationshipSet()) {
                //System.out.println(objRel.getToAccessionNumber()+" "+objRel.getType()+" "+objRel.getFromAccessionNumber());
                relationshipsImport.add(new String[]{objRel.getToAccessionNumber(), objRel.getFromAccessionNumber(), objRel.getType()});
            }

        } catch (OWLOntologyCreationException e) {
            System.out.println("The ontology could not be created: " + e.getMessage());
        }

    }

    public void readOWLOntology(OWLOntology ontology) {
        objectsImport = new Vector<String[]>(); //要输出的实体表
        attributesImport = new Vector<String[]>();//要输出的属性表
        relationshipsImport = new Vector<String[]>();//要输出的关系表
        matchedConcepts = new Vector<>();
//        matchedConcepts.add(new String[]{Globals.MATCHED_CONCEPT});
        sceneConcepts = new Vector<>();
//        sceneConcepts.add(new String[]{Globals.SCENE_CONCEPT});


        HashMap<String, ImportObj> objHashMap = new HashMap<String, ImportObj>();
        ImportSourceStructure objRelSet = new ImportSourceStructure();
        OWLReasonerFactory reasonerFactory = new ElkReasonerFactory();
        OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(ontology); //推理机，用来寻找概念间关系及推导隐含关系
        for (OWLClass owlClass : ontology.getClassesInSignature()) { //对原本体中所有实体操作，获得其所有概念、属性、实体、关系，返回对应本体字符串
			/*if(owlClass.getURI().toString().contains("http://www.geneontology.org")){
					System.out.println(owlClass.toString());
			}*/

            String id = owlClass.getIRI().getFragment();
            ImportObj importObj = new ImportObj(id);

            // get importObj (id & name)
            for (OWLAnnotationAssertionAxiom axiom : ontology.getAxioms(AxiomType.ANNOTATION_ASSERTION)) { //axiom：公理，即本体中的规则。寻找原本体文件中各概念包含的属性，原本体中这些属性以“#”分隔符，现转化为set形式，储存在缓存对象importObj
                OWLAnnotationSubject subject = axiom.getSubject();
                if (subject.equals(owlClass.getIRI())) {
                    String attributeName, attributeValue;
                    if (axiom.getValue() instanceof OWLLiteral) {
                        attributeName = axiom.getProperty().toString();
                        attributeName = attributeName.replace("<", "");
                        attributeName = attributeName.replace(">", "");
                        attributeName = attributeName.replace("http://www.godson.top/Auto" + Globals.DELIMITER, "");
                        attributeValue = ((OWLLiteral) axiom.getValue()).getLiteral();

                        if (attributeValue.contains(".owl#")) { //NORMALISIERUNG AN # IN STRING
                            String[] attSplitted = attributeValue.split("#");
                            attributeValue = attSplitted[1];
                        }
                        //if (attributeValue.contains("\'")){	System.out.println(attributeValue);	}
                        importObj.addAttribute(attributeName, "N/A", DataTypes.STRING, attributeValue); //结构：（实体id,属性<属性名，范围，属性数据类型，属性值>）
                        //System.out.println(attributeName +  " - " + attributeValue);
                    } else {
                        try {

                            attributeName = axiom.getProperty().getIRI().getFragment();
                            attributeValue = axiom.getValue().toString();//.replace("&#39;", "'")
                            //OWLAnonymousIndividual testIndi = ((OWLAnonymousIndividual)axiom.getValue());

                            if (attributeValue.contains("#")) { //NORMALISIERUNG AN # IN STRING
                                String[] attSplitted = attributeValue.split("#");
                                attributeValue = attSplitted[1];
                            }
                            //if (attributeValue.contains("\'")){	System.out.println(attributeValue);	}
                            //System.out.println(attributeName +  " - " + attributeValue);
                            importObj.addAttribute(attributeName, "N/A", DataTypes.STRING, attributeValue);
                        } catch (NullPointerException e) {
                        }
                    }
                }
            }
//			if(id != Globals.MATCHED_CONCEPT && id != Globals.SCENE_CONCEPT){
//
//			}
            // add ImportObj to the objHashMap
            //OLD COMMENT (to get importObj directly by its id, for synonym&definition parsing)
            objHashMap.put(id, importObj);
            NodeSet<OWLClass> superC = reasoner.getSuperClasses(owlClass, true);
            // get relationships
            int fatherNumber = superC.getNodes().size();
            for (Node<OWLClass> expr : superC.getNodes()) {
                String relName = null, relValue = null;

                for (OWLClass cExpr : expr.getEntities()) { //寻找is_a关系，通过推理机寻找对应实体的父类，即子类 is_a 父类

                    if (!cExpr.isAnonymous()) {
                        OWLClass rel = (OWLClass) cExpr;
                        relName = "is_a";
                        relValue = rel.getIRI().getFragment();  //IRI 即本体的编号对象，

                        if (relValue != null && relName != null) {
                            if (fatherNumber == 1 && relValue.equalsIgnoreCase(Globals.MATCHED_CONCEPT)) {
                                matchedConcepts.add(new String[]{importObj.getAccessionNumber()});
                            } else if (fatherNumber == 1 && relValue.equalsIgnoreCase(Globals.SCENE_CONCEPT)) {
                                sceneConcepts.add(new String[]{importObj.getAccessionNumber()});

                            }
                            objRelSet.addRelationship(relValue, importObj.getAccessionNumber(), relName);  //结构：尾，头，关系
                        } else {
                            System.out.println("NULL VALUE");
                        }
                    }
                }
            }
//			Set<OWLSubClassOfAxiom> sb = ontology.getAxioms(AxiomType.SUBCLASS_OF);
            for (OWLSubClassOfAxiom op : ontology.getAxioms(AxiomType.SUBCLASS_OF)) {  //依据本体中的公理寻找其它类型关系 包含 Equivalent、Declaration、AnnotationAssertion、SubclassOf、DisjointClassOf
                String relName = null, relValue = null;
                if (op.getSubClass().equals(owlClass)) {
                    for (OWLObjectProperty oop : op.getObjectPropertiesInSignature()) {
                        relName = oop.getIRI().toString();
                        relValue = oop.getNamedProperty().getIRI().getFragment();

                        if (relValue != null && relName != null) {
                            Set<OWLClass> set = op.getSuperClass().getClassesInSignature();
                            Iterator it = set.iterator();
                            OWLClass cs = (OWLClass) it.next();

                            objRelSet.addRelationship(cs.getIRI().getFragment(), importObj.getAccessionNumber(), relValue);


                        } else {
                            System.out.println("NULL VALUE");
                        }
                    }

                }
            }
//			importRel();

        }

        // synonyms & definitions are parsed by the generic owl parser but return only a "genid"
        // replace synonyms & definitions by parsing the file
        // method returns the required objList instead of the objHashMap

        //Transform into COntoDiff format
        for (String objID : objHashMap.keySet()) {
            ImportObj obj = objHashMap.get(objID);
            //System.out.println(obj.getAccessionNumber());
            objectsImport.add(new String[]{obj.getAccessionNumber()}); //结构：id
            for (ImportObjAttribute objAtt : obj.getAttributeList()) {
                if (objAtt.getAttName() != null) {
                    //System.out.println(obj.getAccessionNumber()+" "+objAtt.getAttName()+" "+objAtt.getValue());
                    attributesImport.add(new String[]{obj.getAccessionNumber(), objAtt.getAttName(), "N/A", objAtt.getValue()});//结构：id,属性名，作用范围，值

//					if (objAtt.getAttName().equals("rdfs:label")) {
//						this.conceptNames.put(obj.getAccessionNumber(), objAtt.getValue()); //对于所有概念，结构：概念id，概念的属性（标签）
//						//System.out.println(obj.getAccessionNumber()+"\t"+objAtt.getValue());
//					}
                }
            }
        }
        //System.out.println("number of concept names: " + conceptNames.size());
        for (ImportObjRelationship objRel : objRelSet.getRelationshipSet()) {
            //System.out.println(objRel.getToAccessionNumber()+" "+objRel.getType()+" "+objRel.getFromAccessionNumber());
            relationshipsImport.add(new String[]{objRel.getToAccessionNumber(), objRel.getFromAccessionNumber(), objRel.getType()}); //结构：起始，终点，关系
        }
        for (int i = 0; i < matchedConcepts.size(); i++) {
            matchedConceptsList.add(matchedConcepts.get(i)[0]);
        }
        for (int i = 0; i < sceneConcepts.size(); i++) {
            sceneConceptsList.add(sceneConcepts.get(i)[0]);
        }
    }

//	protected void importRel(OWLOntology ontology, String axioms, OWLClass owlClass,ImportSourceStructure objRelSet,ImportObj importObj) {
//
//		for (OWLObjectPropertyDomainAxiom op : ontology.getAxioms(AxiomType.OBJECT_PROPERTY_DOMAIN)) {  //依据本体中的公理寻找其它类型关系 包含 Equivalent、Declaration、AnnotationAssertion、SubclassOf、DisjointClassOf
//			String relName = null, relValue = null;
//			if (op.getDomain().equals(owlClass)) {
//				for (OWLObjectProperty oop : op.getObjectPropertiesInSignature()) {
//					relName = oop.getIRI().toString();
//					relValue = oop.getNamedProperty().getIRI().getFragment();
//					System.out.println(relValue);
//					if (relValue != null && relName != null) {
//						objRelSet.addRelationship(relValue, importObj.getAccessionNumber(), relName);
//					} else {
//						System.out.println("NULL VALUE");
//					}
//				}
//
//			}
//		}
//	}

    protected void addAttributeToTmpList(String[] newAttribute, List<String[]> attributesToImport) {
        boolean doubleAttribute = false;
        for (int i = 0; i < attributesToImport.size(); i++) {
            String[] tmp = attributesToImport.get(i);
            if (tmp[0].equals(newAttribute[0]) && tmp[1].equals(newAttribute[1]) && tmp[2].equals(newAttribute[2]) && tmp[3].equals(newAttribute[3])) {
                doubleAttribute = true;
                break;
            }
        }
        if (!doubleAttribute) {
            attributesToImport.add(newAttribute);
        }
    }

    public void getAllConcepts() {
       SceneConcepts.add("Thing");

        for (String[] object : this.objectsImport) {
            if (sceneConceptsList.contains(object[0])) {
                SceneConcepts.add(object[0]);    //代表场景分类的实体
                versionSceneConceptSize++;
                continue;
            }

            if (!(object.equals(Globals.SCENE_CONCEPT) || object.equals(Globals.MATCHED_CONCEPT) || object.equals("Thing"))) {
                ATSConcepts.add(object[0]);
                versionATSConceptSize++;
            }
        }
    }

    public void getAllRelationships() {
        HashMap<String, List<String[]>> allRelations = new HashMap<String, List<String[]>>(); //本体中所有定义的关系

        for (String[] relationship : this.relationshipsImport) {
            String source = relationship[0];
            String target = relationship[1];
            List<String[]> currentRels = allRelations.get(source);
            if (currentRels == null) {
                currentRels = new Vector<String[]>();
            }
            if (relationship[2].equalsIgnoreCase("is_a")) {


                if (sceneConceptsList.contains(source)) {
                    currentRels.add(new String[]{relationship[0], relationship[2], relationship[1]});
                    StructuralSceneRelationships.put(source, currentRels);
                    allRelations.put(source, currentRels);
                    continue;
                }
                if (matchedConceptsList.contains(source)) {
                    currentRels.add(new String[]{relationship[0], relationship[2], relationship[1]});
                    StructuralMatchedRelationships.put(source, currentRels);
                    allRelations.put(source, currentRels);
                    continue;
                }
                if (SceneConcepts.contains(relationship[1]) || matchedConcepts.contains(relationship[1])) {

                    List<String[]> rels = allRelations.get(target);
                    if (rels == null) {
                        rels = new Vector<String[]>();
                    }
                    if (sceneConceptsList.contains(target)) {
                        rels.add(new String[]{relationship[0], relationship[2], relationship[1]});
                        SceneRelationships.put(target, rels);
                        allRelations.put(target, rels);
                        continue;
                    }

                    if (matchedConceptsList.contains(target)) {
                        rels.add(new String[]{relationship[0], relationship[2], relationship[1]});
                        MatchedRelationships.put(target, rels);
                        allRelations.put(target, rels);
                        continue;
                    }
                }
            }
            currentRels.add(new String[]{relationship[0], relationship[2], relationship[1]});
            ATSRelationships.put(source, currentRels);
            versionATSRelationshipSize++;
            allRelations.put(source, currentRels);




        }
    }

    public void getAllAttributes() {
        HashMap<String, List<String[]>> result = new HashMap<String, List<String[]>>();

        for (String[] attribute : this.attributesImport) {
            String source = attribute[0];
            List<String[]> currentRels = result.get(source);
            if (currentRels == null) {
                currentRels = new Vector<String[]>();
            }
            if (sceneConceptsList.contains(source)) {
                currentRels.add(new String[]{attribute[0], attribute[1], attribute[3]});
                StructuralSceneAttributes.put(source, currentRels);

                result.put(source, currentRels);
                continue;
            }
            if (matchedConceptsList.contains(source)) {
                currentRels.add(new String[]{attribute[0], attribute[1], attribute[3]});
                StructuralMatchedAttributes.put(source, currentRels);

                result.put(source, currentRels);
                continue;
            }
            currentRels.add(new String[]{attribute[0], attribute[1], attribute[3]});
            ATSAttributes.put(source, currentRels);
            versionATSAttributeSize++;
            result.put(source, currentRels);
        }
    }

    public String getOBOContentFromFile(String location) {
        try {
            RandomAccessFile file = new RandomAccessFile(location, "r");
            String line;
            StringBuffer result = new StringBuffer();
            while ((line = file.readLine()) != null) {
                result.append(line + "\n");
            }
            file.close();
            return result.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<String[]> getMatchedConcepts() {
        return matchedConcepts;
    }

    public void setMatchedConcepts(List<String[]> matchedConcepts) {
        this.matchedConcepts = matchedConcepts;
    }

    public List<String[]> getSceneConcepts() {
        return sceneConcepts;
    }

    public void setSceneConcepts(List<String[]> sceneConcepts) {
        this.sceneConcepts = sceneConcepts;
    }

    public List<String> getMatchedConceptsList() {
        return matchedConceptsList;
    }

    public void setMatchedConceptsList(List<String> matchedConceptsList) {
        this.matchedConceptsList = matchedConceptsList;
    }

    public List<String> getSceneConceptsList() {
        return sceneConceptsList;
    }

    public void setSceneConceptsList(List<String> sceneConceptsList) {
        this.sceneConceptsList = sceneConceptsList;
    }

    public List<String[]> getObjectsImport() {
        return objectsImport;
    }

    public void setObjectsImport(List<String[]> objectsImport) {
        this.objectsImport = objectsImport;
    }

    public List<String[]> getAttributesImport() {
        return attributesImport;
    }

    public void setAttributesImport(List<String[]> attributesImport) {
        this.attributesImport = attributesImport;
    }

    public List<String[]> getRelationshipsImport() {
        return relationshipsImport;
    }

    public void setRelationshipsImport(List<String[]> relationshipsImport) {
        this.relationshipsImport = relationshipsImport;
    }

    public List<String> getRelationsName() {
        return relationsName;
    }

    public void setRelationsName(List<String> relationsName) {
        this.relationsName = relationsName;
    }
}

