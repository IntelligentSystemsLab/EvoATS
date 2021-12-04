package org.gomma.diff.utils;

import org.gomma.diff.Globals;
import org.io.OntologyReader;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.*;
import org.webdifftool.server.OWLManager;
import uk.ac.manchester.cs.owl.owlapi.OWLOntologyImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLOntologyManagerImpl;

import java.io.File;
import java.io.IOException;

public class OntologyCreateUtil {
    public OWLOntology createOntology(OWLOntology joint) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
        OWLOntologyManager m = org.semanticweb.owlapi.apibinding.OWLManager.createOWLOntologyManager();
        String base = "http://www.godson.top/";
        IRI sceneOntologyIRI = IRI.create(base+joint.getOntologyID().getOntologyIRI().getFragment()+ "/"+"scene"+".owl");

        IRI sceneOntologyVersionIRI = IRI.create(sceneOntologyIRI + "/version1");
        OWLOntology sceneOntology = m.createOntology(new OWLOntologyID(sceneOntologyIRI,sceneOntologyVersionIRI));
        OWLDataFactory factory = m.getOWLDataFactory();





        return null;
    }

}
class Main{
    public static void main(String[] args) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
        OntologyCreateUtil OCU = new OntologyCreateUtil();
        OntologyReader reader = new OntologyReader();
        OWLOntology first = reader.loadOntology(new File("/Users/godson/Documents/auto/L3.owl"));
        OCU.createOntology(first);
    }
}
