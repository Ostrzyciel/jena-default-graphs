package org.example;

import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sys.JenaSystem;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class Main {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        var pool = Executors.newFixedThreadPool(8);

        var futures = IntStream.range(0, 16)
            .mapToObj(i -> pool.submit(() -> {
                if (i % 2 == 0) ModelFactory.createDefaultModel();
                else JenaSystem.init();
                return i;
            }))
            .toList();

        for (var future : futures) {
            System.out.println(future.get());
        }
    }
}