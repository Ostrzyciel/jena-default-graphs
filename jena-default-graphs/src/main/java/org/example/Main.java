package org.example;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        var quadsToTest = new HashMap<String, Quad>();
        quadsToTest.put("null graph", Quad.create(
                null,
                NodeFactory.createBlankNode(),
                NodeFactory.createURI("https://example.org/predicate"),
                NodeFactory.createBlankNode()
        ));
        quadsToTest.put("Quad.defaultGraphIRI", Quad.create(
                Quad.defaultGraphIRI,
                NodeFactory.createBlankNode(),
                NodeFactory.createURI("https://example.org/predicate"),
                NodeFactory.createBlankNode()
        ));
        quadsToTest.put("Quad.defaultGraphNodeGenerated", Quad.create(
                Quad.defaultGraphNodeGenerated,
                NodeFactory.createBlankNode(),
                NodeFactory.createURI("https://example.org/predicate"),
                NodeFactory.createBlankNode()
        ));

        var formats = List.of(
                RDFFormat.TRIG, RDFFormat.JSONLD11, RDFFormat.NQUADS, RDFFormat.RDF_PROTO, RDFFormat.RDF_THRIFT
        );

        for (var entry : quadsToTest.entrySet()) {
            var quad = entry.getValue();
            var datasetGraph = DatasetGraphFactory.create();
            datasetGraph.add(quad);

            for (var format : formats) {
                try {
                    RDFDataMgr.write(OutputStream.nullOutputStream(), datasetGraph, format);
                } catch (Throwable e) {
                    System.out.println("Failed to write " + entry.getKey() + " in " + format);
                    e.printStackTrace();
                }
            }
        }
    }
}