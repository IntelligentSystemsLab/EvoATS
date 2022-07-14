package com.ats.evo;

import com.ats.evo.utils.OntologyCreateUtil;
import com.io.OntologyReader;
import com.webdifftool.server.JointGraphOWLManager;
import com.webdifftool.server.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import java.io.File;
import java.io.IOException;

public class completeGraph {

    static JointGraphOWLManager jowl = new JointGraphOWLManager();

    public static void main(String[] args) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
        OWLManager owl = new OWLManager();
        OntologyReader reader = new OntologyReader();
        OntologyCreateUtil ocu = new OntologyCreateUtil(owl);
        OWLOntology jointOntology = reader.loadOntology(new File("/Users/godson/Library/CloudStorage/OneDrive-pfm6T/文档/研究生/Autonomous Driving Graph/L3/L3.owl"));
        OWLOntology requirementOntology = reader.loadOntology(new File("/Users/godson/Library/CloudStorage/OneDrive-pfm6T/文档/研究生/Autonomous Driving Graph/L3/碰撞避免.owl"));
        jowl.parseOntology(jointOntology);
        owl.parseOntology(requirementOntology);
        jowl.computeRequirementFullGraph(owl);
        OWLOntology output = OntologyCreateUtil.createOntology();
        ocu.addComponents(output);
        OntologyCreateUtil.outputOntology("/Users/godson/Library/CloudStorage/OneDrive-pfm6T/文档/研究生/Autonomous Driving Graph/L3", output, "C3" );

    }
}
