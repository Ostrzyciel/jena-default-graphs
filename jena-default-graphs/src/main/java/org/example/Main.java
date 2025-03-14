package org.example;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.riot.system.StreamRDFWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class Main {
    private static final RDFFormat[] FORMATS = {
        RDFFormat.NTRIPLES,
        RDFFormat.TURTLE_BLOCKS,
        RDFFormat.RDF_PROTO,
        RDFFormat.RDF_THRIFT,
    };

    public static void main(String[] args) {
        var triple = Triple.create(
            NodeFactory.createLiteralString("subject"),
            NodeFactory.createLiteralString("predicate"),
            NodeFactory.createLiteralString("object")
        );

        for (var format : FORMATS) {
            System.out.println();
            System.out.println();
            System.out.println(format.toString());

            var os = new ByteArrayOutputStream();
            var writerOk = false;
            var readerOk = false;
            try {
                var s = StreamRDFWriter.getWriterStream(os, format);
                s.start();
                s.triple(triple);
                s.finish();
                writerOk = true;
                try {
                    var is = new ByteArrayInputStream(os.toByteArray());
                    var dest = StreamRDFLib.print(System.out);
                    RDFParser.create().source(is).forceLang(format.getLang()).parse(dest);
                    readerOk = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            System.out.println("Writer: " + (writerOk ? "OK" : "FAIL"));
            System.out.println("Reader: " + (readerOk ? "OK" : "FAIL"));
        }
    }
}