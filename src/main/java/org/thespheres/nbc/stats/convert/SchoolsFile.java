/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.nbc.stats.convert;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.thespheres.nbc.stats.convert.model.School;

/**
 *
 * @author boris.heithecker@netz-21.de
 */
public class SchoolsFile extends Schools {

    private final School[] arr;

    private SchoolsFile(School[] arr) {
        this.arr = arr;
    }

    static SchoolsFile read(final Path json) throws IOException {
        final Gson gson = new Gson();
        final School[] arr;
        try (final InputStream is = Files.newInputStream(json)) {
            arr = gson.fromJson(new InputStreamReader(is, "UTF-8"), School[].class);
        }
        return new SchoolsFile(arr);
    }

    @Override
    public School fetch(String id) throws IOException {
        final List<School> l = Arrays.stream(arr)
                .filter(s -> id.equals(s.getId()))
                .collect(Collectors.toList());
        if (l.size() == 1) {
            return l.get(0);
        } else {
            throw new IOException("Not found.");
        }
    }

}
