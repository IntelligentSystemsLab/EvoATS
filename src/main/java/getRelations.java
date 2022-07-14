import com.io.OntologyReader;
import com.webdifftool.server.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

//--add-opens java.base/java.lang=ALL-UNNAMED
public class getRelations {
    public static void main(String[] args) throws OWLOntologyCreationException {
        OWLManager owl = new OWLManager();
        OntologyReader reader = new OntologyReader();
        OWLOntology ontology = reader.loadOntology(new File("/Users/godson/Library/CloudStorage/OneDrive-pfm6T/文档/研究生/Autonomous Driving Graph/L3/L3.owl"));
        owl.parseOntology(ontology);
//        owl.getRelevantRelation("自主泊车");
//        System.out.println("----------");
//        owl.getRelevantRelation("线控系统");
//        System.out.println("----------");
//        owl.getRelevantRelation("车身");

        Scanner sc = new Scanner(System.in);
        while (sc.hasNext()){
            owl.getRelevantRelation(sc.next());
            System.out.println("----------");
        }
    }
}