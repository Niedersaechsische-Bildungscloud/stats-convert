/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.nbc.stats.convert;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.thespheres.nbc.stats.convert.model.School;

/**
 *
 * @author boris
 */
public abstract class Schools {

    @Expose(deserialize = false)
    private final Map<String, School> schools = new HashMap<>();

    public School getSchool(final String schoolId) {
        return schools.computeIfAbsent(schoolId, id -> {
            try {
                return fetch(id);
            } catch (IOException ex) {
                Logger.getLogger(SchoolsAPI.class.getName()).log(Level.INFO, "Not found {0}", schoolId);
                return new School(id, id);
            }
        });
    }

    abstract School fetch(final String id) throws IOException;
    
    public void dump(final Writer out) throws IOException {
        final School[] arr = schools.values()
                .toArray(School[]::new);
        final Gson gson = new Gson();
        gson.toJson(arr, out);
        out.close();
    }

}
