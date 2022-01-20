package org.io;

import org.gomma.diff.Globals;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.webdifftool.server.OWLManager;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class OntologyCreateUtil {
    static String base = Globals.BASE_URL+Globals.DELIMITER;
    static PrefixManager pm = new DefaultPrefixManager(base);
    static OWLOntologyManager m = org.semanticweb.owlapi.apibinding.OWLManager.createOWLOntologyManager();
    static OWLManager owl;
    public OntologyCreateUtil(OWLManager owl){
        this.owl = owl;
    }

    public OWLOntology createOntology(String scene,int version) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {

        IRI sceneOntologyIRI = IRI.create(base+scene+".owl");

        IRI sceneOntologyVersionIRI = IRI.create(sceneOntologyIRI + "/version"+version);
        OWLOntology sceneOntology = m.createOntology(new OWLOntologyID(sceneOntologyIRI,sceneOntologyVersionIRI));

        return sceneOntology;
    }

    public void addATSSceneComponents(OWLOntology ontology){
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
    public void outputOntology(OWLOntology ontology,String outputLocation) throws OWLOntologyStorageException, IOException {
        File file = new File(outputLocation+".owl");
        m.saveOntology(ontology,IRI.create(file.toURI()));
    }






}
class Main{
    public static void main(String[] args) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
//        OntologyCreateUtil ocu = new OntologyCreateUtil();
//        OWLOntology ont = ocu.createOntology("scene","v1");





    }
}
