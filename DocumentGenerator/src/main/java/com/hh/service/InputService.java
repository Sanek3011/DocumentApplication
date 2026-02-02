package com.hh.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;


public class InputService {

    public int readNFromFile() throws URISyntaxException, IOException {
        Path path = Path.of(
                getClass().getClassLoader()
                        .getResource("parameter.txt")
                        .toURI()
        );
        String s = Files.readString(path);
        return Integer.parseInt(s);
    }

}
