/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.nbc.stats.convert;

import com.google.gson.Gson;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.thespheres.nbc.stats.convert.model.School;

/**
 *
 * @author boris.heithecker@netz-21.de
 */
public class Schools {

    final CloseableHttpClient client;
    private final Map<String, School> schools = new HashMap<>();
    private final String api;

    public Schools(final String apiEndpoint) {
        final HttpClientBuilder builder = HttpClients.custom();
        client = builder.build();
        api = apiEndpoint;
    }

    public School getSchool(final String schoolId) {
        return schools.computeIfAbsent(schoolId, id -> {
            try {
                return fetch(id);
            } catch (IOException ex) {
                Logger.getLogger(Schools.class.getName()).log(Level.INFO, null, ex);
                return new School(id, id);
            }
        });
    }

    School fetch(final String id) throws IOException {
        final String uri = api + "/schools/" + id;
        final HttpGet get = new HttpGet(uri);
        final CloseableHttpResponse response = client.execute(get);
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new IOException("Unexpected response status: " + response.getStatusLine() + " at: " + uri);
        }
        final HttpEntity entity = response.getEntity();
        final Gson gson = new Gson();
        final School result;
        try (final InputStream is = entity.getContent();) {
            final BufferedInputStream bis = new BufferedInputStream(is);
            entity.getContentEncoding();
            result = gson.fromJson(new InputStreamReader(bis, "UTF-8"), School.class);
            EntityUtils.consume(entity);
        }
        return result;
    }

}
