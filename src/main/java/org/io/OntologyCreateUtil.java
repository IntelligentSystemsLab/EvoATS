package org.io;

import org.gomma.diff.Globals;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.webdifftool.server.OWLManager;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class OntologyCreateUtil {
    static String base = Globals.BASE_URL+Globals.DELIMITER;
    static PrefixManager pm = new DefaultPrefixManager(base);
    static OWLOntologyManager m = org.semanticweb.owlapi.apibinding.OWLManager.createOWLOntologyManager();

    public OWLOntology createOntology(String scene,String version) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {

        IRI sceneOntologyIRI = IRI.create(base+scene+".owl");

        IRI sceneOntologyVersionIRI = IRI.create(sceneOntologyIRI + "/version"+version);
        OWLOntology sceneOntology = m.createOntology(new OWLOntologyID(sceneOntologyIRI,sceneOntologyVersionIRI));

        return sceneOntology;
    }

    public void addATSSceneComponents(OWLOntology ontology, OWLManager owl, Boolean newVersion){
        OWLDataFactory factory = m.getOWLDataFactory();
        if(!newVersion) {
            Iterator<String> it = owl.getOldATSConcepts().iterator();
            while (it.hasNext()) {
                m.addAxiom(ontology, factory.getOWLDeclarationAxiom(factory.getOWLClass(it.next(), pm)));
            }
        }
        else {
            Iterator<String> it = owl.getNewATSConcepts().iterator();
            while (it.hasNext()) {
                m.addAxiom(ontology, factory.getOWLDeclarationAxiom(factory.getOWLClass(it.next(), pm)));
            }
        }
    }






}
class Main{
    public static void main(String[] args) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {



    }
}
