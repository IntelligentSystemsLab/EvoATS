package com.ats.evo;

import com.ats.evo.utils.OntologyCreateUtil;
import com.webdifftool.client.model.DiffEvolutionMapping;
import com.webdifftool.server.BasicDiffManager;
import com.webdifftool.server.OWLManager;
import com.io.OntologyReader;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EvolutionExtraction {

    com.webdifftool.server.OWLManager owl = new com.webdifftool.server.OWLManager();

    public static void main(String[] args) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
        EvolutionExtraction evolutionExtraction = new EvolutionExtraction();
        FileWriter output = new FileWriter("output1.txt");
        OntologyReader reader = new OntologyReader();
        OWLOntology first = reader.loadOntology(new File(Globals.V1));
        OWLOntology second = reader.loadOntology(new File(Globals.V2));
        com.webdifftool.server.OWLManager oldManager;
        com.webdifftool.server.OWLManager newManager;
        oldManager = OntologyCreateUtil.initOntologyManager(first);
        newManager = OntologyCreateUtil.initOntologyManager(second);
        com.webdifftool.client.model.DiffEvolutionMapping dem =evolutionExtraction.computeEvolution(oldManager,newManager);
        if (output != null){
            output.write(dem.getFulltextOfCompactDiff());
        }
        output.close();


    }


    public com.webdifftool.client.model.DiffEvolutionMapping computeEvolution(com.webdifftool.server.OWLManager oldManager, OWLManager newManager) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
        DiffComputation dc = new DiffComputation();
        DiffExecutor.getSingleton().setupRepository();
        BasicDiffManager basicDiffManager = new BasicDiffManager(oldManager,newManager);
//        System.out.println("Loading ontology versions");
//        oldManager.computeRequirementElements(scene,0);
//        newManager.computeRequirementElements(scene,0);
//
//        oldManager.designOntology(scene,1);
//        newManager.designOntology(scene,2);

        basicDiffManager.analysisBasicChange();
//		owl.parseAndIntegrateChanges(owl.getOBOContentFromFile(oldVersion), owl.getOBOContentFromFile(newVersion));

        Map<String, String> conceptNames = new HashMap<>(); //obo文件中没有定义概念（rdfs:label）

        System.out.println("Loading rules");
        dc.loadConfigForDiffExecutor();
        System.out.println("Applying rules");
        DiffExecutor.getSingleton().applyRules();
        // currentStatus = "Aggregation of changes";
        System.out.println("Aggregation of changes");
        DiffExecutor.getSingleton().mergeResultActions();
        // currentStatus = "Building of final diff result";
        System.out.println("Building of final diff result");
        DiffExecutor.getSingleton().retrieveAndStoreHighLevelActions();
        com.webdifftool.client.model.DiffEvolutionMapping diffResult = new DiffEvolutionMapping(dc.getFullDiffMapping(conceptNames), dc.getCompactDiffMapping(), dc.getBasicDiffMapping());
        dc.computeWordFrequencies(diffResult);
        diffResult.computeDependenciesForBasicChanges();

        // Set version sizes
        diffResult.newVersionConceptSize = newManager.getVersionConceptSize();
        diffResult.newVersionRelationshipSize = newManager.getVersionRelationshipSize();
        diffResult.newVersionAttributeSize = newManager.getVersionAttributeSize();
        diffResult.oldVersionConceptSize = oldManager.getVersionConceptSize();
        diffResult.oldVersionRelationshipSize = oldManager.getVersionRelationshipSize();
        diffResult.oldVersionAttributeSize = oldManager.getVersionAttributeSize();

        // Clean repository
        DiffExecutor.getSingleton().destroyRepository();

        // currentStatus = "Sending results";
        System.out.println("Sending results");
        return diffResult;
    }
}
