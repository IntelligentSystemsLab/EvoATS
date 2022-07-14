

package com.webdifftool.server;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import com.ats.evo.Globals;
import org.gomma.io.importer.models.ImportObj;
import org.gomma.io.importer.models.ImportObj.ImportObjAttribute;
import org.gomma.io.importer.models.ImportSourceStructure;
import org.gomma.io.importer.models.ImportSourceStructure.ImportObjRelationship;
import org.gomma.model.DataTypes;
import com.ats.evo.utils.OntologyCreateUtil;
//import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.search.EntitySearcher;

public class OWLManager {

    List<String[]> objectsImport;
    List<String[]> attributesImport;
    List<String[]> relationshipsImport;
    List<String> relationsName;

    Map<String, Integer> conceptLayer = new HashMap<>();


    private int versionConceptSize = 0;
    private int versionRelationshipSize = 0;
    private int versionAttributeSize = 0;


    HashSet<String> concepts = new HashSet<>();
    HashSet<String> tempConcepts = new HashSet<>();
    Map<String, List<String[]>> tempRelationships = new HashMap<>();
    Map<String, List<String[]>> attributes = new HashMap<>();
    Map<String, List<String[]>> relationships = new HashMap<>();

    public Map<String, String> conceptNames;
    private String KGName;

    private boolean useSplitInference = true;


//    public static void main(String[] args) throws Exception {
//        OWLManager test = new OWLManager();
//        test.parseAndIntegrateChanges("http://dbserv2.informatik.uni-leipzig.de/~hartung/MA_1-197.owl", "http://dbserv2.informatik.uni-leipzig.de/~hartung/MA_1-206.owl");
//    }


    public void parseOntology(OWLOntology Ontology) {


        this.readOWLOntology(Ontology);
        concepts = this.getAllConcepts(); //结构：id
        relationships = this.getAllRelationships(); //结构：（id:<id,属性名，属性值>)
        attributes = this.getAllAttributes(); //结构:id,<头实体、关系、尾实体>

    }

    public String designOntology(String outputPath,String require, String version) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
        OntologyCreateUtil ocu = new OntologyCreateUtil(this);
        OWLOntology ont = OntologyCreateUtil.createOntology(require, version);
        ocu.addComponents(ont);
        Date date = new Date();
        String name = require + "_" + date.getTime();
        OntologyCreateUtil.outputOntology(outputPath,ont, name);
        return name;
    }

    /**
     * 给定一个实体名，查询与它存在直接关联对实体及关系
     * @param conceptName
     */
    public void getRelevantRelation(String conceptName){
        for(String[] relations : relationshipsImport){
            if(relations[0].equalsIgnoreCase(conceptName)){
                System.out.println(Arrays.toString(relations));
            } else if(relations[1].equalsIgnoreCase(conceptName)){
                System.out.println(Arrays.toString(relations));
            }
        }
    }

    public static void addElementsForOWLManager(OWLManager owl, OWLManager subOWL) {
        owl.getConcepts().addAll(subOWL.getConcepts());
        owl.getRelationships().putAll(subOWL.getRelationships());
        owl.getAttributes().putAll(subOWL.getAttributes());
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
            OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();

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
//        matchedConcepts = new Vector<>();
////        matchedConcepts.add(new String[]{Globals.MATCHED_CONCEPT});
//        sceneConcepts = new Vector<>();
//        sceneConcepts.add(new String[]{Globals.SCENE_CONCEPT});


        HashMap<String, ImportObj> objHashMap = new HashMap<String, ImportObj>();
        ImportSourceStructure objRelSet = new ImportSourceStructure();
        OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
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
//                        attributeName = attributeName.replace("http://www.godson.top/Auto" + Globals.DELIMITER, ""); //TODO:测试用，修改了IRI
                        attributeName = attributeName.replace("http://www.semanticweb.org/Auto" + Globals.DELIMITER, "");
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
//        for (int i = 0; i < matchedConcepts.size(); i++) {
//            matchedConceptsList.add(matchedConcepts.get(i)[0]);
//        }
//        for (int i = 0; i < sceneConcepts.size(); i++) {
//            sceneConceptsList.add(sceneConcepts.get(i)[0]);
//        }
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

    public HashSet<String> getAllConcepts() {
        HashSet<String> result = new HashSet<String>();
        for (String[] object : this.objectsImport) {
            result.add(object[0]);
            versionConceptSize++;
        }
        return result;
    }

    public HashMap<String, List<String[]>> getAllRelationships() {
        HashMap<String, List<String[]>> result = new HashMap<String, List<String[]>>();
        for (String[] relationship : this.relationshipsImport) {
            if (relationship[1].equalsIgnoreCase("Thing")) {
                continue;
            }
            String source = relationship[0];
            List<String[]> currentRels = result.get(source);
            if (currentRels == null) {
                currentRels = new Vector<String[]>();
            }
            currentRels.add(new String[]{relationship[0], relationship[2], relationship[1]});
            result.put(source, currentRels);

            versionRelationshipSize++;

        }
        return result;
    }

    public HashMap<String, List<String[]>> getAllAttributes() {
        HashMap<String, List<String[]>> result = new HashMap<String, List<String[]>>();
        for (String[] attribute : this.attributesImport) {
            String source = attribute[0];
            List<String[]> currentAtts = result.get(source);
            if (currentAtts == null) {
                currentAtts = new Vector<String[]>();
            }
            currentAtts.add(new String[]{attribute[0], attribute[1], attribute[3]});
            result.put(source, currentAtts);

            versionAttributeSize++;

        }
        return result;
    }

    /**
     * 通过关系名称和尾实体名称寻找头实体集合
     * @param relationships 知识图谱关系集合
     * @param relationName 知识图谱关系名
     * @param tail 尾实体
     * @return 头实体集合
     */
    public Set<String> getHeadEntities(Map<String, List<String[]>> relationships, String relationName, String tail) {
        Set<String> res = new HashSet<>();
        for (List<String[]> tripleList : relationships.values()) {
            for (String[] triples : tripleList) {
                if (triples[2].equals(tail)) {
                    if (triples[1].equals(relationName)) {
                        res.add(triples[0]);
                    }
                }
            }
        }
        return res;
    }

    public Set<String> getTailEntities(Map<String, List<String[]>> relationships, String relationName, String head) {
        Set<String> res = new HashSet<>();
        List<String[]> tripleList = relationships.get(head);
        for (String[] triples : tripleList) {
            if (triples[1].equals(relationName)) {
                res.add(triples[2]);
            }
        }
        return res;
    }

    public void insertRelations(Map<String, List<String[]>> relationships,String head,String relationName,String tail){
        List<String[]> rels = relationships.get(head);
        if (rels == null) {
            rels = new Vector<>();
        }
        rels.add(new String[]{head,relationName,tail});
        relationships.put(head,rels);
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

    public int getVersionConceptSize() {
        return versionConceptSize;
    }


    public int getVersionRelationshipSize() {
        return versionRelationshipSize;
    }

    public int getVersionAttributeSize() {
        return versionAttributeSize;
    }

    public void setVersionAttributeSize(int versionAttributeSize) {
        this.versionAttributeSize = versionAttributeSize;
    }

    public HashSet<String> getConcepts() {
        return concepts;
    }

    public void setConcepts(HashSet<String> concepts) {
        this.concepts = concepts;
    }

    public Map<String, List<String[]>> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, List<String[]>> attributes) {
        this.attributes = attributes;
    }

    public Map<String, List<String[]>> getRelationships() {
        return relationships;
    }

    public void setRelationships(Map<String, List<String[]>> relationships) {
        this.relationships = relationships;
    }

    public String getKGName() {
        return KGName;
    }

    public void setKGName(String KGName) {
        this.KGName = KGName;
    }

    public HashSet<String> getTempConcepts() {
        return tempConcepts;
    }

    public void addTempConcepts(HashSet<String> tempConcepts) {
        this.tempConcepts.addAll(tempConcepts);
    }

    public Map<String, List<String[]>> getTempRelationships() {
        return tempRelationships;
    }

    public void addTempRelationships(Map<String, List<String[]>> tempRelationships) {
        this.tempRelationships.putAll(tempRelationships);
    }
}

