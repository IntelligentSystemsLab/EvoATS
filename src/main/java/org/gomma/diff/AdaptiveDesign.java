package org.gomma.diff;

import org.io.OntologyReader;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.webdifftool.client.model.DiffEvolutionMapping;
import org.webdifftool.server.OWLManager;
import uk.ac.manchester.cs.owl.owlapi.OWLOntologyImpl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AdaptiveDesign {

    public static void main(String[] args) throws OWLOntologyCreationException, IOException {
        FileWriter output = new FileWriter("output1.txt");
        OntologyReader reader = new OntologyReader();
        OWLOntology first = reader.loadOntology(new File(Globals.V1));
        OWLOntology second = reader.loadOntology(new File(Globals.V2));
        String scene = "Road_Driving";
        DiffEvolutionMapping dem = AdaptiveDiffCompute(first,second,scene);
        if (output != null){
            output.write(dem.getFulltextOfCompactDiff());
        }
        output.close();


    }

    public static DiffEvolutionMapping AdaptiveDiffCompute(OWLOntology first, OWLOntology second,String scene) {
        DiffComputation dc = new DiffComputation();
        DiffExecutor.getSingleton().setupRepository();

        OWLManager owl = new OWLManager();

        System.out.println("Loading ontology versions");
        owl.parseOntology(first, second);
        owl.computeSceneElements(scene,false,2);
        owl.computeSceneElements(scene,true, 2);
        owl.analysisBasicChange();
//		owl.parseAndIntegrateChanges(owl.getOBOContentFromFile(oldVersion), owl.getOBOContentFromFile(newVersion));
        owl.setConceptNames(new HashMap<>());
        Map<String, String> conceptNames = owl.conceptNames; //obo文件中没有定义概念（rdfs:label）

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
        DiffEvolutionMapping diffResult = new DiffEvolutionMapping(dc.getFullDiffMapping(conceptNames), dc.getCompactDiffMapping(), dc.getBasicDiffMapping());
        dc.computeWordFrequencies(diffResult);
        diffResult.computeDependenciesForBasicChanges();

        // Set version sizes
        diffResult.newVersionConceptSize = owl.getNewVersionConceptSize();
        diffResult.newVersionRelationshipSize = owl.getNewVersionRelationshipSize();
        diffResult.newVersionAttributeSize = owl.getNewVersionAttributeSize();
        diffResult.oldVersionConceptSize = owl.getOldVersionConceptSize();
        diffResult.oldVersionRelationshipSize = owl.getOldVersionRelationshipSize();
        diffResult.oldVersionAttributeSize = owl.getOldVersionAttributeSize();

        // Clean repository
        DiffExecutor.getSingleton().destroyRepository();

        // currentStatus = "Sending results";
        System.out.println("Sending results");
        return diffResult;
    }
}
