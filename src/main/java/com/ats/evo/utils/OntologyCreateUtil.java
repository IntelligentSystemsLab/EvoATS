package com.ats.evo.utils;

import com.ats.evo.Globals;
//import com.io.VowlConvertor;
import com.io.VowlConvertor;
import com.webdifftool.server.JointGraphOWLManager;
import com.webdifftool.server.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class OntologyCreateUtil {
    static String base = Globals.BASE_URL+ Globals.DELIMITER;
    static PrefixManager pm = new DefaultPrefixManager(base);
    static OWLOntologyManager m = org.semanticweb.owlapi.apibinding.OWLManager.createOWLOntologyManager();
    com.webdifftool.server.OWLManager owl;
    public OntologyCreateUtil(com.webdifftool.server.OWLManager owl){
        this.owl = owl;
    }

    public static OWLOntology createOntology(String require,String version) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {

        IRI requireOntologyIRI = IRI.create(base+require+".owl");

        IRI requireOntologyVersionIRI = IRI.create(requireOntologyIRI + "/version"+version);
        OWLOntology requireOntology = m.createOntology(new OWLOntologyID(requireOntologyIRI,requireOntologyVersionIRI));

        return requireOntology;
    }

    public static OWLOntology createOntology() throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {

        IRI requireOntologyIRI = IRI.create(base+".owl");

//        IRI requireOntologyVersionIRI = IRI.create(requireOntologyIRI + "/version");
        IRI requireOntologyVersionIRI = IRI.create(String.valueOf(requireOntologyIRI));
        OWLOntology requireOntology = m.createOntology(new OWLOntologyID(requireOntologyIRI,requireOntologyVersionIRI));

        return requireOntology;
    }

    /**
     * 使用本地OWLManager创建
     * @param ontology
     */
    public void addComponents(OWLOntology ontology){
        OWLDataFactory factory = m.getOWLDataFactory();
            for (String s : owl.getConcepts()) { //add Concepts
                m.addAxiom(ontology, factory.getOWLDeclarationAxiom(factory.getOWLClass(s, pm)));
            }
            for(List<String[]> ls : owl.getAttributes().values()){ //add Attributes
                for(String[] s : ls){
//                    if(s[1].contains("ats")){
//                        continue;
//                    }
                    OWLAnnotationProperty oap = factory.getOWLAnnotationProperty(s[1],pm);
                    m.addAxiom(ontology,factory.getOWLAnnotationAssertionAxiom(factory.getOWLClass(s[0],pm).getIRI(),factory.getOWLAnnotation(oap,factory.getOWLLiteral(s[2]))));
                }
            }
            for (List<String[]> ls : owl.getRelationships().values()){
                for(String[] s : ls){
                    if(!(s[1].equals("is_a"))){
                        m.addAxiom(ontology,factory.getOWLSubClassOfAxiom(factory.getOWLClass(s[0],pm),factory.getOWLObjectSomeValuesFrom(factory.getOWLObjectProperty(s[1],pm),factory.getOWLClass(s[2],pm))));
                    } else {
                        m.addAxiom(ontology,factory.getOWLSubClassOfAxiom(factory.getOWLClass(s[0],pm),factory.getOWLClass(s[2],pm)));
                    }
                }
            }
        
    }
    public static void outputOntology(String outputPath,OWLOntology ontology,String fileName) throws OWLOntologyStorageException, IOException {
        VowlConvertor convertor = new VowlConvertor();
        File file = new File(outputPath+"/owl/"+fileName+".owl");
        m.saveOntology(ontology,IRI.create(file.toURI()));
        File file1 = new File(outputPath+"/json/");
        if(!file1.exists()&&!file1.isDirectory()) {
            file1.mkdirs();
        }
        convertor.convertToVowl(file.getPath(),file1.getPath()+"/"+fileName+".json");

    }

    public void generateRequirementOntology(OWLOntology joint, OWLOntology requirement,String outputLocation){ //接受一个ATS版本本体，和一个需求定义本体，输出该需求的架构本体


    }

    public static com.webdifftool.server.OWLManager initOntologyManager(OWLOntology ontology){
        com.webdifftool.server.OWLManager manager = new com.webdifftool.server.OWLManager();
        manager.parseOntology(ontology);
        return manager;
    }
    public static JointGraphOWLManager initJointOntologyManager(OWLOntology ontology){
        JointGraphOWLManager manager = new JointGraphOWLManager();
        manager.parseOntology(ontology);
        return manager;
    }


    public static void completeOntology(JointGraphOWLManager joint, OWLManager req){ //得到需求对应的功能图在层级上的完整表示
        joint.computeRequirementFullGraph(req);
    }







}
class Main{
    public static void main(String[] args) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
//        OntologyCreateUtil ocu = new OntologyCreateUtil();
//        OWLOntology ont = ocu.createOntology("scene","v1");





    }
}
