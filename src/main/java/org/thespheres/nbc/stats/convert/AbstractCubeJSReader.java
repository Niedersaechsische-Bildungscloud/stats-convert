/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.nbc.stats.convert;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
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
import org.thespheres.nbc.stats.convert.model.QueryResult;

/**
 *
 * @author boris.heithecker@netz-21.de
 */
public class AbstractCubeJSReader {

    public static final int MAX_RETRY = 10;
    protected final CloseableHttpClient client;
    protected final String cubeJS;
    protected final Path outputDir;

    protected AbstractCubeJSReader(final String cubeJS, final Path outputDir) {
        final HttpClientBuilder builder = HttpClients.custom();
        this.client = builder.build();
        this.cubeJS = cubeJS;
        this.outputDir = outputDir;
    }

    protected QueryResult executeQuery(final String uri) throws IOException, InterruptedException {
        final Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        final HttpGet get = new HttpGet(uri);
        QueryResult result = executeQueryImpl(get, uri, gson);
        int retry = 0;
        while (result == null || result.getQueryData() == null && retry++ <= MAX_RETRY) {
            Logger.getLogger(SchoolsAPI.class.getName()).log(Level.INFO, "Retry {0}", retry);
            Thread.sleep(10 * 1000);
            result = executeQueryImpl(get, uri, gson);
        }
        gson.toJson(result, System.out);
        return result;
    }

    private QueryResult executeQueryImpl(final HttpGet get, final String uri, final Gson gson) throws JsonIOException, JsonSyntaxException, IOException {
        //        get.addHeader(uri, uri);
        CloseableHttpResponse response = client.execute(get);
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new IOException("Unexpected response status: " + response.getStatusLine() + " at: " + uri);
        }
        HttpEntity entity = response.getEntity();
//        String content = EntityUtils.toString(entity);
//        System.out.println(content);
//        if (true) {
//            return null;
//        }
        final QueryResult result;
        try (final InputStream is = entity.getContent()) {
            final BufferedInputStream bis = new BufferedInputStream(is);
            entity.getContentEncoding();
            result = gson.fromJson(new InputStreamReader(bis, "UTF-8"), QueryResult.class);
            EntityUtils.consume(entity);
        }
        return result;
    }

    protected CsvWriter createCsvWriter(final String typeName) throws IOException {
        final String today = LocalDate.now().toString();
        final String file = "stat_" + today + "_" + typeName + ".csv";
        return new CsvWriter(Files.newBufferedWriter(outputDir.resolve(file)), new CsvWriterSettings());
    }
}
