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
import org.gomma.diff.utils.Utils;
import org.gomma.io.importer.models.ImportObj;
import org.gomma.io.importer.models.ImportObj.ImportObjAttribute;
import org.gomma.io.importer.models.ImportSourceStructure;
import org.gomma.io.importer.models.ImportSourceStructure.ImportObjRelationship;
import org.gomma.model.DataTypes;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.search.EntitySearcher;

public class OWLManager {

	List<String[]> matchedConcepts;
	List<String[]> sceneConcepts ;

	List<String[]> objectsImport; 
	List<String[]> attributesImport;
	List<String[]> relationshipsImport;
	List<String> relationsName;

	List<String[]> conceptMaps;
	List<String> conceptAdditions;
	List<String> conceptDeletions;
	
	List<String[]> relationshipMaps;
	List<String[]> relationshipAdditions;
	List<String[]> relationshipDeletions;
	
	List<String[]> attributeMaps;
	List<String[]> attributeAdditions;
	List<String[]> attributeDeletions;

	Set<String> oldConcepts = new HashSet<>();
	Map<String, List<String[]>> oldAttributes = new HashMap<>();
	Map<String, List<String[]>> oldRelationships = new HashMap<>();

	Set<String> oldATSConcepts= new HashSet<>();
	Map<String, List<String[]>> oldATSAttributes = new HashMap<>();
	Map<String, List<String[]>> oldATSRelationships = new HashMap<>();

	Set<String> oldSceneConcepts= new HashSet<>();
	Map<String, List<String[]>> oldSceneAttributes = new HashMap<>();
	Map<String, List<String[]>> oldSceneRelationships = new HashMap<>();

	Set<String> oldMatchedConcepts= new HashSet<>();
	Map<String, List<String[]>> oldMatchedAttributes = new HashMap<>();
	Map<String, List<String[]>> oldMatchedRelationships = new HashMap<>();

	Set<String> newConcepts= new HashSet<>();
	Map<String, List<String[]>> newAttributes = new HashMap<>();
	Map<String, List<String[]>> newRelationships = new HashMap<>();

	Set<String> newATSConcepts= new HashSet<>();
	Map<String, List<String[]>> newATSAttributes = new HashMap<>();
	Map<String, List<String[]>> newATSRelationships = new HashMap<>();

	Set<String> newSceneConcepts= new HashSet<>();
	Map<String, List<String[]>> newSceneAttributes = new HashMap<>();
	Map<String, List<String[]>> newSceneRelationships = new HashMap<>();

	Set<String> newMatchedConcepts= new HashSet<>();
	Map<String, List<String[]>> newMatchedAttributes = new HashMap<>();
	Map<String, List<String[]>> newMatchedRelationships = new HashMap<>();
	public Map<String,String> conceptNames;
	
	public boolean useSplitInference = true;
	
	public int oldVersionConceptSize = 0;
	public int oldVersionRelationshipSize = 0;
	public int oldVersionAttributeSize = 0;

	public int oldVersionATSConceptSize = 0;
	public int oldVersionATSRelationshipSize = 0;
	public int oldVersionATSAttributeSize = 0;

	public int oldVersionMatchedConceptSize = 0;
	public int oldVersionMatchedRelationshipSize = 0;
	public int oldVersionMatchedAttributeSize = 0;

	public int oldVersionSceneConceptSize = 0;
	public int oldVersionSceneRelationshipSize = 0;
	public int oldVersionSceneAttributeSize = 0;

	public int newVersionConceptSize = 0;
	public int newVersionRelationshipSize = 0;
	public int newVersionAttributeSize = 0;
	
	public int newVersionATSConceptSize = 0;
	public int newVersionATSRelationshipSize = 0;
	public int newVersionATSAttributeSize = 0;

	public int newVersionMatchedConceptSize = 0;
	public int newVersionMatchedRelationshipSize = 0;
	public int newVersionMatchedAttributeSize = 0;

	public int newVersionSceneConceptSize = 0;
	public int newVersionSceneRelationshipSize = 0;
	public int newVersionSceneAttributeSize = 0;
	
	
	public static void main(String[] args) throws Exception {
		OWLManager test = new OWLManager();
		test.parseAndIntegrateChanges("http://dbserv2.informatik.uni-leipzig.de/~hartung/MA_1-197.owl", "http://dbserv2.informatik.uni-leipzig.de/~hartung/MA_1-206.owl");
	}
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
	public void parseAndIntegrateChanges(String oldVersionString, String newVersionString) {
		this.conceptNames = new HashMap<String, String>();
		
		this.readOWLString(oldVersionString);
		this.getAllConcepts(false);
		this.getAllAttributes(false);
		this.getAllRelationships(false);
		
		this.readOWLString(newVersionString);
		this.getAllConcepts(true);
		this.getAllAttributes(true);
		this.getAllRelationships(true);
		
		this.computeBasicConceptChanges(oldConcepts, newConcepts, oldAttributes, newAttributes);
		this.computeBasicRelationshipChanges(oldRelationships, newRelationships);
		this.computeBasicAttributeChanges(oldAttributes, newAttributes);
		
		this.integrateBasicChanges();
	}

	public void parseAndIntegrateChanges(OWLOntology oldVersion, OWLOntology newVersion) {
		this.conceptNames = new HashMap<String, String>();
		
		this.readOWLOntology(oldVersion); //对原本体中所有实体操作，获得其所有概念、属性、实体、关系，返回对应本体字符串
		this.getAllConcepts(false); //结构：id
		this.getAllAttributes(false); //结构：（id:<id,属性名，属性值>)
		this.getAllRelationships(false); //结构:id,<头实体、关系、尾实体>

		this.readOWLOntology(newVersion);
		this.getAllConcepts(true);
		this.getAllAttributes(true);
		this.getAllRelationships(true);
		
		this.computeBasicConceptChanges(oldConcepts, newConcepts, oldAttributes, newAttributes);
		this.computeBasicRelationshipChanges(oldRelationships, newRelationships);
		this.computeBasicAttributeChanges(oldAttributes, newAttributes);
		
		this.integrateBasicChanges();
	}

	public void integrateBasicChanges() { //将更改保存至h2数据库
		try {
			DataBaseHandler.getInstance().executeDml("TRUNCATE TABLE "+Globals.WORKING_TABLE); //切换到change数据表
			
			PreparedStatement oneValueChange = DataBaseHandler.getInstance().prepareStatement("MERGE INTO "+Globals.WORKING_TABLE+" (actionMD5,change_action,value1) KEY (actionMD5) VALUES (?,?,?)");
			PreparedStatement twoValueChange = DataBaseHandler.getInstance().prepareStatement("MERGE INTO "+Globals.WORKING_TABLE+" (actionMD5,change_action,value1,value2) KEY (actionMD5) VALUES (?,?,?,?)");
			PreparedStatement threeValueChange = DataBaseHandler.getInstance().prepareStatement("MERGE INTO "+Globals.WORKING_TABLE+" (actionMD5,change_action,value1,value2,value3) KEY (actionMD5) VALUES (?,?,?,?,?)");
			PreparedStatement sixValueChange = DataBaseHandler.getInstance().prepareStatement("INSERT INTO "+Globals.WORKING_TABLE+" (actionMD5,change_action,value1,value2,value3,value4,value5,value6) VALUES (?,?,?,?,?,?,?,?)");
			
			for (String concept : conceptAdditions) {
				oneValueChange.setString(1, Utils.MD5(new String[]{"addC",concept}));
				oneValueChange.setString(2, "addC");
				oneValueChange.setString(3, concept);
				oneValueChange.addBatch();
			}
			oneValueChange.executeBatch();
			
			for (String concept : conceptDeletions) {
				oneValueChange.setString(1, Utils.MD5(new String[]{"delC",concept}));
				oneValueChange.setString(2, "delC");
				oneValueChange.setString(3, concept);
				oneValueChange.addBatch();
			}
			oneValueChange.executeBatch();
			
			for (String[] concepts : conceptMaps) {
				twoValueChange.setString(1, Utils.MD5(new String[]{"mapC",concepts[0],concepts[1]}));
				twoValueChange.setString(2, "mapC");
				twoValueChange.setString(3, concepts[0]);
				twoValueChange.setString(4, concepts[1]);
				twoValueChange.addBatch();
			}
			twoValueChange.executeBatch();
			
			for (String[] relationship : relationshipAdditions) {
				threeValueChange.setString(1, Utils.MD5(new String[]{"addR",relationship[0],relationship[1],relationship[2]}));
				threeValueChange.setString(2, "addR");
				threeValueChange.setString(3, relationship[0]);
				threeValueChange.setString(4, relationship[1]);
				threeValueChange.setString(5, relationship[2]);
				threeValueChange.addBatch();
			}
			threeValueChange.executeBatch();
			
			for (String[] relationship : relationshipDeletions) {
				threeValueChange.setString(1, Utils.MD5(new String[]{"delR",relationship[0],relationship[1],relationship[2]}));
				threeValueChange.setString(2, "delR");
				threeValueChange.setString(3, relationship[0]);
				threeValueChange.setString(4, relationship[1]);
				threeValueChange.setString(5, relationship[2]);
				threeValueChange.addBatch();
			}
			threeValueChange.executeBatch();
			
			for (String[] relationship : relationshipMaps) {
				sixValueChange.setString(1, Utils.MD5(new String[]{"mapR",relationship[0],relationship[1],relationship[2],relationship[3],relationship[4],relationship[5]}));
				sixValueChange.setString(2, "mapR");
				sixValueChange.setString(3, relationship[0]);
				sixValueChange.setString(4, relationship[1]);
				sixValueChange.setString(5, relationship[2]);
				sixValueChange.setString(6, relationship[3]);
				sixValueChange.setString(7, relationship[4]);
				sixValueChange.setString(8, relationship[5]);
				sixValueChange.addBatch();
			}
			sixValueChange.executeBatch();
			
			for (String[] attribute : attributeAdditions) {
				threeValueChange.setString(1, Utils.MD5(new String[]{"addA",attribute[0],attribute[1],attribute[2]}));
				threeValueChange.setString(2, "addA");
				threeValueChange.setString(3, attribute[0]);
				threeValueChange.setString(4, attribute[1]);
				threeValueChange.setString(5, attribute[2]);
				threeValueChange.addBatch();
			}
			threeValueChange.executeBatch();
			
			for (String[] attribute : attributeDeletions) {
				threeValueChange.setString(1, Utils.MD5(new String[]{"delA",attribute[0],attribute[1],attribute[2]}));
				threeValueChange.setString(2, "delA");
				threeValueChange.setString(3, attribute[0]);
				threeValueChange.setString(4, attribute[1]);
				threeValueChange.setString(5, attribute[2]);
				threeValueChange.addBatch();
			}
			threeValueChange.executeBatch();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void computeBasicConceptChanges(Set<String> oldConcepts,Set<String> newConcepts,Map<String,
			List<String[]>> oldAttributes,Map<String, List<String[]>> newAttributes) {
		conceptMaps = new Vector<String[]>();
		conceptAdditions = new Vector<String>();
		conceptDeletions = new Vector<String>();
		HashSet<String> directMatchDomain = new HashSet<String>(); //直接匹配的头节点
		HashSet<String> directMatchRange = new HashSet<String>();
		
		//Synonym-Match (b-COG rule b3)  找单映射
		for (String conceptAcc : newAttributes.keySet()) {
			for (String[] newAttribute : newAttributes.get(conceptAcc)) {
				if (newAttribute[1].contains("ats:Evo")) {
					if (oldConcepts.contains(newAttribute[2])) {
						conceptMaps.add(new String[]{newAttribute[2],newAttribute[0]});
						directMatchDomain.add(newAttribute[2]);
						directMatchRange.add(newAttribute[0]);
					}
				}
			}
		}
		for (String conceptAcc : oldAttributes.keySet()) {
			for (String[] oldAttribute : oldAttributes.get(conceptAcc)) {
				if (oldAttribute[1].contains("ats:Evo")) {
					if (newConcepts.contains(oldAttribute[2])) {
						conceptMaps.add(new String[]{oldAttribute[2],oldAttribute[0]});
						directMatchDomain.add(oldAttribute[2]);
						directMatchRange.add(oldAttribute[0]);
					}
				}
			}
		}
		
		//Direct-Match accession (b-COG rule b4,b5) 在单映射的基础上找多映射（一个实体的意义在不同代际间仍有原意map（a,a）） //如a的意义发生变化，则应采用新的实体（id）表示
		List<String[]> tmpConceptMaps = new Vector<String[]>();
		for (String[] map : conceptMaps) {
			if (oldConcepts.contains(map[0])&&newConcepts.contains(map[0])) {
				tmpConceptMaps.add(new String[]{map[0],map[0]});
			}
			if (oldConcepts.contains(map[1])&&newConcepts.contains(map[1])) {
				tmpConceptMaps.add(new String[]{map[1],map[1]});
			}
		}
		conceptMaps.addAll(tmpConceptMaps);
			
		for (String oldConcept : oldConcepts) {
			if (newConcepts.contains(oldConcept)) {
				//conceptMaps.add(new String[]{oldConcept,oldConcept});
				directMatchDomain.add(oldConcept);
				directMatchRange.add(oldConcept);
			}
		}
		
		
		//Concept additions (b-COG rule b1) 映射到的概念在原表中可能不存在，添加操作
		for (String newConcept : newConcepts) {
			if (!directMatchRange.contains(newConcept)) {
				conceptAdditions.add(newConcept);
			}
		}
		
		//Concept deletions (b-COG rule b2)
		for (String oldConcept : oldConcepts) {
			if (!directMatchDomain.contains(oldConcept)) {
				conceptDeletions.add(oldConcept);
			}
		}
		
		//Split Handling
		if (useSplitInference) {
			this.handleSplitMappings();
		}
		
		System.out.println("#ConceptMaps: "+conceptMaps.size());
		System.out.println("#ConceptAdditions: "+conceptAdditions.size());
		System.out.println("#CocneptDeletions: "+conceptDeletions.size());
	}
	
	public void computeBasicRelationshipChanges(Map<String, List<String[]>> oldRelationships, Map<String, List<String[]>> newRelationships) {
		relationshipMaps = new Vector<String[]>();
		relationshipDeletions = new Vector<String[]>();
		relationshipAdditions = new Vector<String[]>();
		
		for (String conceptAcc : oldRelationships.keySet()) {
			List<String[]> tmpOldRelationships = oldRelationships.get(conceptAcc);
			if (newRelationships.containsKey(conceptAcc)) {
				List<String[]> tmpNewRelationships = newRelationships.get(conceptAcc);
				for (String[] tmpOldRel : tmpOldRelationships) {
					boolean found = false;
					for (String[] tmpNewRel : tmpNewRelationships) {
						if (tmpOldRel[2].equals(tmpNewRel[2])) {//头尾实体相同的关系：b8.关系不同，采用映射 b7.尾实体没有相同的，删除关系/对于old图中a概念的所有关系，new图中没有a对应的任何关系
							if (!tmpOldRel[1].equals(tmpNewRel[1])) {
								relationshipMaps.add(new String[]{tmpOldRel[0],tmpOldRel[1],tmpOldRel[2],tmpNewRel[0],tmpNewRel[1],tmpNewRel[2]});
							}
							found = true;
							break;
						}
					}
					if (!found) {
						relationshipDeletions.add(new String[]{tmpOldRel[0],tmpOldRel[1],tmpOldRel[2]});
					}
				}
			} else {
				relationshipDeletions.addAll(tmpOldRelationships);
			}
		}
		
		for (String conceptAcc : newRelationships.keySet()) {
			List<String[]> tmpNewRelationships = newRelationships.get(conceptAcc);
			if (oldRelationships.containsKey(conceptAcc)) {
				List<String[]> tmpOldRelationships = oldRelationships.get(conceptAcc);
				for (String[] tmpNewRel : tmpNewRelationships) {
					boolean found = false;
					for (String[] tmpOldRel : tmpOldRelationships) {
						if (tmpNewRel[2].equals(tmpOldRel[2])) {
							found = true;
							break;
						}
					}
					if (!found) { //b6.添加new图新出现的关系或新出现实体的所有新关系
						relationshipAdditions.add(new String[]{tmpNewRel[0],tmpNewRel[1],tmpNewRel[2]});
					}
				}
			} else {
				relationshipAdditions.addAll(tmpNewRelationships);
			}
		}
		
		System.out.println("#RelMaps: "+relationshipMaps.size());
		System.out.println("#RelAdditions: "+relationshipAdditions.size());
		System.out.println("#RelDeletions: "+relationshipDeletions.size());
	}
	
	public void computeBasicAttributeChanges(Map<String, List<String[]>> oldAttributes,
											 Map<String, List<String[]>> newAttributes) {
		attributeMaps = new Vector<String[]>();
		attributeDeletions = new Vector<String[]>();
		attributeAdditions = new Vector<String[]>();
		
		for (String conceptAcc : oldAttributes.keySet()) {
			List<String[]> tmpOldAttributes = oldAttributes.get(conceptAcc);
			if (newAttributes.containsKey(conceptAcc)) {
				List<String[]> tmpNewAttributes = newAttributes.get(conceptAcc);
				for (String[] tmpOldAtt : tmpOldAttributes) {
					boolean found = false;
					for (String[] tmpNewAtt : tmpNewAttributes) {
						if (tmpOldAtt[1].equals(tmpNewAtt[1])&&tmpOldAtt[2].equals(tmpNewAtt[2])) {
							//relationshipMaps.add(new String[]{tmpOldRel[0],tmpOldRel[1],tmpOldRel[2],tmpNewRel[0],tmpNewRel[1],tmpNewRel[2]});
							found = true;
							break;
						}
					}
					if (!found) {
						attributeDeletions.add(new String[]{tmpOldAtt[0],tmpOldAtt[1],tmpOldAtt[2]});
					}
				}
			} else {
				attributeDeletions.addAll(tmpOldAttributes);
			}
		}
		
		for (String conceptAcc : newAttributes.keySet()) {
			List<String[]> tmpNewAttributes = newAttributes.get(conceptAcc);
			if (oldAttributes.containsKey(conceptAcc)) {
				List<String[]> tmpOldAttributes = oldAttributes.get(conceptAcc);
				for (String[] tmpNewAtt : tmpNewAttributes) {
					boolean found = false;
					for (String[] tmpOldAtt : tmpOldAttributes) {
						if (tmpNewAtt[1].equals(tmpOldAtt[1])&&tmpNewAtt[2].equals(tmpOldAtt[2])) {
							found = true;
							break;
						}
					}
					if (!found) {
						attributeAdditions.add(new String[]{tmpNewAtt[0],tmpNewAtt[1],tmpNewAtt[2]});
					}
				}
			} else {
				attributeAdditions.addAll(tmpNewAttributes);
			}
		}
		
		System.out.println("#AttMaps: "+attributeMaps.size());
		System.out.println("#AttAdditions: "+attributeAdditions.size());
		System.out.println("#AttDeletions: "+attributeDeletions.size());
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
			
			InputStream is = new ByteArrayInputStream( content.getBytes ());
			OWLOntology ontology = manager.loadOntologyFromOntologyDocument(is);
			
			HashMap<String, ImportObj>	objHashMap	= new HashMap<String, ImportObj>();
			ImportSourceStructure		objRelSet	= new ImportSourceStructure();
			OWLReasonerFactory reasonerFactory = new ElkReasonerFactory();

			OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(ontology); //推理机，用来寻找概念间关系及推导隐含关系
			for (OWLClass owlClass : ontology.getClassesInSignature()) {		//对原本体中所有实体操作，获得其所有概念、属性、实体、关系，返回对应本体字符串
				/*if(owlClass.getURI().toString().contains("http://www.geneontology.org")){
						System.out.println(owlClass.toString());
				}*/
				String id = owlClass.getIRI().getFragment();
				ImportObj importObj = new ImportObj(id);
				List<OWLAnnotation> annos = EntitySearcher.getAnnotations(owlClass.getIRI(),
						ontology).collect(Collectors.toCollection(ArrayList::new));
				// get importObj (id & name)
				for (OWLAnnotation axiom: annos){  //寻找原本体文件中各概念包含的属性，原本体中这些属性以“#”分隔符，现转化为set形式，储存在缓存对象importObj
					String attributeName,attributeValue;
					if(axiom.getValue() instanceof OWLLiteral){
						attributeName = axiom.getProperty().toString();
						attributeName = attributeName.replace("<", "");
						attributeName = attributeName.replace(">", "");
						attributeValue = ((OWLLiteral)axiom.getValue()).getLiteral();

						if (attributeValue.contains(".owl#")) {	//NORMALISIERUNG AN # IN STRING
							String [] attSplitted = attributeValue.split("#");
							attributeValue = attSplitted[1];
						}
						//if (attributeValue.contains("\'")){	System.out.println(attributeValue);	}
						importObj.addAttribute(attributeName, "N/A", DataTypes.STRING, attributeValue);  //结构：（实体id,属性<属性名，范围，属性数据类型，属性值>）
						//System.out.println(attributeName +  " - " + attributeValue);
					}else{
						try {

							attributeName = axiom.getProperty().getIRI().getFragment();
							attributeValue = axiom.getValue().toString();//.replace("&#39;", "'")
							//OWLAnonymousIndividual testIndi = ((OWLAnonymousIndividual)axiom.getValue());

							if (attributeValue.contains("#")) {	//NORMALISIERUNG AN # IN STRING
								String [] attSplitted = attributeValue.split("#");
								attributeValue = attSplitted[1];
							}
							//if (attributeValue.contains("\'")){	System.out.println(attributeValue);	}
							//System.out.println(attributeName +  " - " + attributeValue);
							importObj.addAttribute(attributeName, "N/A", DataTypes.STRING, attributeValue);
						} catch (NullPointerException e) {}
					}
				}
				
				// add ImportObj to the objHashMap
				//OLD COMMENT (to get importObj directly by its id, for synonym&definition parsing)
				objHashMap.put(id,importObj);
				NodeSet<OWLClass> superC =  reasoner.getSuperClasses(owlClass, true);
				// get relationships

				for(Node<OWLClass> expr : superC.getNodes()){  //寻找is_a关系，通过推理机寻找对应实体的父类，即子类 is_a 父类

					String relName = null, relValue = null;
					for (OWLClass cExpr: expr.getEntities()) {
						if (!cExpr.isAnonymous()){
							OWLClass rel = (OWLClass) cExpr;
							relName = "is_a";
							relValue = rel.getIRI().getFragment();  //IRI 即本体的编号对象，
							if (relValue!=null&&relName!=null) {
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
						for(OWLObjectProperty oop : op.getObjectPropertiesInSignature()){
							relName = oop.getIRI().toString();
							relValue = oop.getNamedProperty().getIRI().getFragment();
							System.out.println(relValue);
							if (relValue!=null&&relName!=null) {
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
					if (objAtt.getAttName()!=null){
						//System.out.println(obj.getAccessionNumber()+" "+objAtt.getAttName()+" "+objAtt.getValue());
						attributesImport.add(new String[]{obj.getAccessionNumber(),objAtt.getAttName(),"N/A",objAtt.getValue()});
					
						if(objAtt.getAttName().equals("rdfs:label")){
							this.conceptNames.put(obj.getAccessionNumber(), objAtt.getValue());
							//System.out.println(obj.getAccessionNumber()+"\t"+objAtt.getValue());
						}
					}
				}
			}
			//System.out.println("number of concept names: " + conceptNames.size());
			for (ImportObjRelationship objRel : objRelSet.getRelationshipSet()) {
				//System.out.println(objRel.getToAccessionNumber()+" "+objRel.getType()+" "+objRel.getFromAccessionNumber());
				relationshipsImport.add(new String[]{objRel.getToAccessionNumber(),objRel.getFromAccessionNumber(),objRel.getType()});
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
		matchedConcepts.add(new String[] {Globals.MATCHED_CONCEPT});
		sceneConcepts = new Vector<>();
		sceneConcepts.add(new String[]{Globals.SCENE_CONCEPT});


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
			for (OWLAnnotationAssertionAxiom  axiom : ontology.getAxioms(AxiomType.ANNOTATION_ASSERTION)) { //axiom：公理，即本体中的规则。寻找原本体文件中各概念包含的属性，原本体中这些属性以“#”分隔符，现转化为set形式，储存在缓存对象importObj
				OWLAnnotationSubject subject = axiom.getSubject();
				if (subject.equals(owlClass.getIRI())){
					String attributeName, attributeValue;
					if (axiom.getValue() instanceof OWLLiteral) {
						attributeName = axiom.getProperty().toString();
						attributeName = attributeName.replace("<", "");
						attributeName = attributeName.replace(">", "");
						attributeName = attributeName.replace("http://www.godson.top/Auto"+Globals.DELIMITER,"");
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
			NodeSet<OWLClass> superC =  reasoner.getSuperClasses(owlClass, true);
			// get relationships
			int fatherNumber = superC.getNodes().size();
			for(Node<OWLClass> expr : superC.getNodes()){
				String relName = null, relValue = null;

				for (OWLClass cExpr: expr.getEntities()) { //寻找is_a关系，通过推理机寻找对应实体的父类，即子类 is_a 父类

					if (!cExpr.isAnonymous()){
						OWLClass rel = (OWLClass) cExpr;
						relName = "is_a";
						relValue = rel.getIRI().getFragment();  //IRI 即本体的编号对象，

						if (relValue!=null&&relName!=null) {
							if(fatherNumber==1&& relValue.equalsIgnoreCase(Globals.MATCHED_CONCEPT)){
								matchedConcepts.add(new String[] {importObj.getAccessionNumber()});
							}else if(fatherNumber==1&& relValue.equalsIgnoreCase(Globals.SCENE_CONCEPT)){
								sceneConcepts.add(new String[] {importObj.getAccessionNumber()});

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
					for(OWLObjectProperty oop : op.getObjectPropertiesInSignature()){
						relName = oop.getIRI().toString();
						relValue = oop.getNamedProperty().getIRI().getFragment();
						System.out.println(relValue);
						if (relValue!=null&&relName!=null) {
							Set<OWLClass> set = op.getSuperClass().getClassesInSignature() ;
							Iterator it = set.iterator();
							OWLClass cs = (OWLClass) it.next();

							objRelSet.addRelationship(cs.getIRI().getFragment(),importObj.getAccessionNumber(),relValue);


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
			objectsImport.add(new String[] { obj.getAccessionNumber() }); //结构：id
			for (ImportObjAttribute objAtt : obj.getAttributeList()) {
				if (objAtt.getAttName() != null) {
					//System.out.println(obj.getAccessionNumber()+" "+objAtt.getAttName()+" "+objAtt.getValue());
					attributesImport.add(new String[] { obj.getAccessionNumber(), objAtt.getAttName(), "N/A", objAtt.getValue() });//结构：id,属性名，作用范围，值

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
			relationshipsImport.add(new String[] { objRel.getToAccessionNumber(), objRel.getFromAccessionNumber(), objRel.getType() }); //结构：起始，终点，关系
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
		for (int i=0;i<attributesToImport.size();i++) {
			String[] tmp = attributesToImport.get(i);
			if (tmp[0].equals(newAttribute[0])&&tmp[1].equals(newAttribute[1])&&tmp[2].equals(newAttribute[2])&&tmp[3].equals(newAttribute[3])) {
				doubleAttribute = true;
				break;
			}
		}
		if (!doubleAttribute) {
			attributesToImport.add(newAttribute);
		}
	}
	public void getAllConcepts(boolean newVersion) {
		HashSet<String> result = new HashSet<String>();
		if(!newVersion) {
			for (String[] object : this.objectsImport) {
				if(sceneConcepts.contains(object)) {
					oldSceneConcepts.add(object[0]);
					oldVersionSceneConceptSize++;
					continue;
				}
				if(matchedConcepts.contains(object)){
					oldMatchedConcepts.add(object[0]);
					oldVersionMatchedConceptSize++;
					continue;
				}
				oldATSConcepts.add(object[0]);
				oldVersionATSConceptSize++;
			}
		} else{
			for (String[] object : this.objectsImport) {
			if(sceneConcepts.contains(object)) {
				newSceneConcepts.add(object[0]);
				newVersionSceneConceptSize++;
				continue;
			}
			if(matchedConcepts.contains(object)){
				newMatchedConcepts.add(object[0]);
				newVersionMatchedConceptSize++;
				continue;
			}
			newATSConcepts.add(object[0]);
			newVersionATSConceptSize++;
		}

		}
	}
	public void getAllRelationships(boolean newVersion) {
		HashMap<String, List<String[]>> result = new HashMap<String, List<String[]>>();
		if(!newVersion) {
			for (String[] relationship : this.relationshipsImport) {
				String source = relationship[0];
				List<String[]> currentRels = result.get(source);
				if (currentRels == null) {
					currentRels = new Vector<String[]>();
				}

				if (sceneConcepts.contains(source)) {
					currentRels.add(new String[]{relationship[0], relationship[2], relationship[1]});
					oldSceneRelationships.put(source, currentRels);
					oldVersionSceneRelationshipSize++;
					continue;
				}
				if (matchedConcepts.contains(source)) {
					currentRels.add(new String[]{relationship[0], relationship[2], relationship[1]});
					oldMatchedRelationships.put(source, currentRels);
					oldVersionMatchedRelationshipSize++;
					continue;
				}
				currentRels.add(new String[]{relationship[0], relationship[2], relationship[1]});
				oldATSRelationships.put(source, currentRels);
				oldVersionATSRelationshipSize++;
			}
		}else {
			for (String[] relationship : this.relationshipsImport) {
				String source = relationship[0];
				List<String[]> currentRels = result.get(source);
				if (currentRels == null) {
					currentRels = new Vector<String[]>();
				}
				if (sceneConcepts.contains(source)) {
					currentRels.add(new String[]{relationship[0], relationship[2], relationship[1]});
					newSceneRelationships.put(source, currentRels);
					newVersionSceneRelationshipSize++;
					continue;
				}
				if (matchedConcepts.contains(source)) {
					currentRels.add(new String[]{relationship[0], relationship[2], relationship[1]});
					newMatchedRelationships.put(source, currentRels);
					newVersionMatchedRelationshipSize++;
					continue;
				}
				currentRels.add(new String[]{relationship[0], relationship[2], relationship[1]});
				newATSRelationships.put(source, currentRels);
				newVersionATSRelationshipSize++;
			}
		}
	}
	
	public void getAllAttributes(boolean newVersion) {
		HashMap<String, List<String[]>> result = new HashMap<String, List<String[]>>();
		if(!newVersion) {
			for (String[] attribute : this.attributesImport) {
				String source = attribute[0];
				List<String[]> currentRels = result.get(source);
				if (currentRels == null) {
					currentRels = new Vector<String[]>();
				}
				if (sceneConcepts.contains(source)) {
					currentRels.add(new String[]{attribute[0], attribute[1], attribute[3]});
					oldSceneAttributes.put(source, currentRels);
					oldVersionSceneAttributeSize++;
					continue;
				}
				if (matchedConcepts.contains(source)) {
					currentRels.add(new String[]{attribute[0], attribute[1], attribute[3]});
					oldMatchedAttributes.put(source, currentRels);
					oldVersionMatchedAttributeSize++;
					continue;
				}
				currentRels.add(new String[]{attribute[0], attribute[1], attribute[3]});
				oldATSAttributes.put(source, currentRels);
				oldVersionATSAttributeSize++;
			}
		}else {
			for (String[] attribute : this.attributesImport) {
				String source = attribute[0];
				List<String[]> currentRels = result.get(source);
				if (currentRels == null) {
					currentRels = new Vector<String[]>();
				}
				if (sceneConcepts.contains(source)) {
					currentRels.add(new String[]{attribute[0], attribute[1], attribute[3]});
					newSceneAttributes.put(source, currentRels);
					newVersionSceneAttributeSize++;
					continue;
				}
				if (matchedConcepts.contains(source)) {
					currentRels.add(new String[]{attribute[0], attribute[0], attribute[3]});
					newMatchedAttributes.put(source, currentRels);
					newVersionMatchedAttributeSize++;
					continue;
				}
				currentRels.add(new String[]{attribute[0], attribute[0], attribute[3]});
				newATSAttributes.put(source, currentRels);
				newVersionATSAttributeSize++;
			}
		}
	}
	
	private void handleSplitMappings() {
			
			Map<String, List<String>> oldVersionChildren = new HashMap<String, List<String>>();
			
			for (String conceptAcc : oldRelationships.keySet()) { //找子父类关系
				for (String[] rel : oldRelationships.get(conceptAcc)) {
					if (rel[1].equalsIgnoreCase("is_a")||rel[1].equalsIgnoreCase("part_of")) {
						String child = rel[0];
						String parent = rel[2];
						List<String> children = oldVersionChildren.get(parent);
						if (children==null) {
							children = new Vector<String>();
						}

						children.add(child);
						oldVersionChildren.put(parent, children);
					}
				}
			}

			Map<String, List<String>> newVersionChildren = new HashMap<String, List<String>>();
			
			for (String conceptAcc : newRelationships.keySet()) {
				for (String[] rel : newRelationships.get(conceptAcc)) {
					if (rel[1].equalsIgnoreCase("is_a")||rel[1].equalsIgnoreCase("part_of")) {
						String child = rel[0];
						String parent = rel[2];
						List<String> children = newVersionChildren.get(parent);
						if (children==null) {
							children = new Vector<String>();
						}
						children.add(child);
						newVersionChildren.put(parent, children);
					}
				}
			}
			
			for (String accession : oldConcepts) {
				
				if (newConcepts.contains(accession)
						&&!oldVersionChildren.containsKey(accession)
						&&newVersionChildren.containsKey(accession)) {
					List<String> newChildren = newVersionChildren.get(accession);
					//Split nur wenn mehr als zwei Kinder involviert !!
					if (newChildren.size()>1) {
						boolean allNewAndLeaf = true;
						for (String newChild : newChildren) {
							//Muss eine Addition sein !!
							if (!conceptAdditions.contains(newChild)) {
								allNewAndLeaf = false;
							}
							//Muss wirklich ein Leaf sein !!
							if (newVersionChildren.containsKey(newChild)) {
								allNewAndLeaf = false;
							}
						}
						if (allNewAndLeaf) {
							
							for (String newChild : newChildren) {
								conceptMaps.add(new String[]{accession,newChild});
								conceptAdditions.remove(newChild);
							}
							conceptMaps.add(new String[]{accession,accession});
						}
					}
				}
			}
			
	}
	
	public String getOBOContentFromFile(String location) {
		try {
			RandomAccessFile file = new RandomAccessFile(location, "r");
			String line;
			StringBuffer result = new StringBuffer();
			while ((line=file.readLine())!=null) {
				result.append(line+"\n");
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

}
