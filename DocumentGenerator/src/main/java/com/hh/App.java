package com.hh;

import com.hh.service.GeneratorService;
import com.hh.service.InputService;

import java.io.IOException;
import java.net.URISyntaxException;

public class App {

    private GeneratorService generatorService = new GeneratorService();
    private InputService inputService = new InputService();

    public static void main( String[] args ) throws URISyntaxException, IOException {
        App app = new App();
        app.generateDocs();
    }

    public void generateDocs() throws URISyntaxException, IOException {
        int i = inputService.readNFromFile();
        generatorService.generateNDocuments(i);
    }
}
