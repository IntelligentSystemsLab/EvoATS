package org.gomma.diff;

import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.io.OntologyCreateUtil;
import org.io.OntologyReader;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.webdifftool.client.model.DiffEvolutionMapping;
import org.webdifftool.server.BasicDiffManager;
import org.webdifftool.server.OWLManager;
import uk.ac.manchester.cs.owl.owlapi.OWLOntologyImpl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AdaptiveDesign {
    OWLManager oldManager;
    OWLManager newManager;
    OWLManager owl = new OWLManager();

    public static void main(String[] args) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
        AdaptiveDesign adaptiveDesign = new AdaptiveDesign();
        FileWriter output = new FileWriter("output1.txt");
        OntologyReader reader = new OntologyReader();
        OWLOntology first = reader.loadOntology(new File(Globals.V1));
        OWLOntology second = reader.loadOntology(new File(Globals.V2));
        String scene = "Road_Driving";
        adaptiveDesign.initOntologyManager(first,second);
        DiffEvolutionMapping dem =adaptiveDesign.AdaptiveDiffCompute(scene);
        if (output != null){
            output.write(dem.getFulltextOfCompactDiff());
        }
        output.close();


    }
    public void initOntologyManager(OWLOntology first,OWLOntology second){
        oldManager = new OWLManager();
        newManager = new OWLManager();
        oldManager.parseOntology(first);
        newManager.parseOntology(second);
    }

    public DiffEvolutionMapping AdaptiveDiffCompute(String scene) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
        DiffComputation dc = new DiffComputation();
        DiffExecutor.getSingleton().setupRepository();
        BasicDiffManager basicDiffManager = new BasicDiffManager(this.oldManager,this.newManager);
        System.out.println("Loading ontology versions");
        oldManager.computeSceneElements(scene,0);
        newManager.computeSceneElements(scene,0);

        oldManager.designOntology(scene,1);
        newManager.designOntology(scene,2);

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
        DiffEvolutionMapping diffResult = new DiffEvolutionMapping(dc.getFullDiffMapping(conceptNames), dc.getCompactDiffMapping(), dc.getBasicDiffMapping());
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
