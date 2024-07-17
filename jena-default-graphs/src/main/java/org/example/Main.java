package org.example;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFBase;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

class Result {
    public String format;
    public String caseName;
    public String status;

    public Result(String format, String caseName, String status) {
        this.format = format;
        this.caseName = caseName;
        this.status = status;
    }
}

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

        var results = new Vector<Result>();

        for (var entry : quadsToTest.entrySet()) {
            var quad = entry.getValue();
            var datasetGraph = DatasetGraphFactory.create();
            datasetGraph.add(quad);

            for (var format : formats) {
                var output = new ByteArrayOutputStream();
                try {
                    RDFDataMgr.write(output, datasetGraph, format);
                } catch (Throwable e) {
                    System.out.println("Failed to write " + entry.getKey() + " in " + format);
                    e.printStackTrace();
                    results.add(new Result(format.toString(), entry.getKey(), "serialization failed"));
                    continue;
                }

                if (output.size() == 0) {
                    System.out.println("Empty output for " + entry.getKey() + " in " + format);
                    results.add(new Result(format.toString(), entry.getKey(), "empty output"));
                    continue;
                }

                var readStream = new StreamRDFBase() {
                    @Override
                    public void quad(Quad quad) {
                        if (quad.getGraph() == null) {
                            results.add(new Result(format.toString(), entry.getKey(),
                                    "null graph"));
                        } else if (quad.isDefaultGraphExplicit()) {
                            results.add(new Result(format.toString(), entry.getKey(),
                                    "Quad.defaultGraphIRI"));
                        } else if (quad.isDefaultGraphGenerated()) {
                            results.add(new Result(format.toString(), entry.getKey(),
                                    "Quad.defaultGraphNodeGenerated"));
                        } else {
                            results.add(new Result(format.toString(), entry.getKey(),
                                    "unexpected graph (???)"));
                        }
                    }

                    @Override
                    public void triple(Triple triple) {
                        results.add(new Result(format.toString(), entry.getKey(),
                                "triple instead of quad"));
                    }
                };

                var input = new ByteArrayInputStream(output.toByteArray());
                try {
                    RDFParser.source(input).lang(format.getLang()).parse(readStream);
                } catch (Throwable e) {
                    System.out.println("Failed to read " + entry.getKey() + " in " + format);
                    e.printStackTrace();
                    results.add(new Result(format.toString(), entry.getKey(), "deserialization failed"));
                }
            }
        }

        System.out.println();
        System.out.format("%-35s%-25s%-15s%n", "To serialize", "Format", "Status/deserialized");

        for (var result : results) {
            System.out.format("%-35s%-25s%-15s%n", result.caseName, result.format, result.status);
        }
    }
}