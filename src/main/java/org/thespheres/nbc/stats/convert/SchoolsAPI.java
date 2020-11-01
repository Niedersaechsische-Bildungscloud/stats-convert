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
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
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
class SchoolsAPI extends Schools {

    final CloseableHttpClient client;
    private final String api;
    private final String token;

    SchoolsAPI(final String apiEndpoint, final String jwt) {
        RequestConfig.Builder requestBuilder = RequestConfig.custom();
        requestBuilder.setConnectTimeout(5000);
        requestBuilder.setConnectionRequestTimeout(5000);
        final HttpClientBuilder builder = HttpClients.custom();
        builder.setDefaultRequestConfig(requestBuilder.build());
        client = builder.build();
        api = apiEndpoint;
        token = jwt;
    }

    @Override
    protected School fetch(final String id) throws IOException {
        final String uri = api + "/schools/" + id;
        final HttpGet get = new HttpGet(uri);
        if (token != null) {
            get.setHeader("Cookie", "jwt=" + token);
            //get.setHeader("Authorization", "Bearer " + token);
        }
        final CloseableHttpResponse response = client.execute(get);
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            response.close();
            throw new IOException("Unexpected response status: " + response.getStatusLine() + " at: " + uri);
        }
        final HttpEntity entity = response.getEntity();
        final Gson gson = new Gson();
        final School result;
        //entity.writeTo(System.out);
        try (final InputStream is = entity.getContent();) {
            final BufferedInputStream bis = new BufferedInputStream(is);
            entity.getContentEncoding();
            result = gson.fromJson(new InputStreamReader(bis, "UTF-8"), School.class);
            EntityUtils.consume(entity);
        }
        return result;
    }

}
