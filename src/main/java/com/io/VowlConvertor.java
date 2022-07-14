package com.io;

import de.uni_stuttgart.vis.vowl.owl2vowl.converter.Converter;
import de.uni_stuttgart.vis.vowl.owl2vowl.converter.IRIConverter;
import de.uni_stuttgart.vis.vowl.owl2vowl.export.types.Exporter;
import de.uni_stuttgart.vis.vowl.owl2vowl.export.types.FileExporter;
import org.apache.commons.io.FilenameUtils;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyID;

import java.io.File;
import java.util.Arrays;

public class VowlConvertor {




    public void convertToVowl(String inputFilePath,String outputFilePath){

        IRI ontologyIri = IRI.create(new File(inputFilePath));
        try {
            Converter converter = new IRIConverter(ontologyIri);
            converter.convert();
            Exporter exp = this.generateFileExporter(ontologyIri,outputFilePath);
            converter.export(exp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    protected FileExporter generateFileExporter(IRI ontologyIri, String filePath) {
        String filename;
        if (filePath != null) {
            filename = filePath;
        } else {
            try {
                filename = FilenameUtils.removeExtension(ontologyIri.getFragment()) + ".json";
            } catch (Exception var6) {
                System.out.println("Failed to extract filename from iri");
                System.out.println("Reason: " + var6);
                String defaultName = "default.json";
                System.out.println("Writing to '" + defaultName + "'");
                filename = defaultName;
            }
        }

        return new FileExporter(new File(filename));
    }
}

