/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.nbc.stats.convert;

import com.univocity.parsers.csv.CsvWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.thespheres.nbc.stats.convert.model.Data;
import org.thespheres.nbc.stats.convert.model.QueryResult;

/**
 *
 * @author boris.heithecker@netz-21.de
 */
public class CubeJSReader extends AbstractCubeJSReader {

    public static final String QUERY = "{\"measures\":[\"Sessions.count\",\"Events.activeUsers\"],\"timeDimensions\":[{\"dimension\":\"Events.timeStamp\",\"dateRange\":\"last 1000 days\",\"granularity\":\"day\"}]}";
    static final NumberFormat DF = NumberFormat.getNumberInstance(Locale.GERMANY);

    public CubeJSReader(final String cubeJS, final Path outputDir) {
        super(cubeJS, outputDir);
    }

    public void read() throws InterruptedException, IOException {
        final String uri = cubeJS + "/cubejs-api/v1/load?query=" + URLEncoder.encode(QUERY, StandardCharsets.UTF_8);
        final QueryResult result = executeQuery(uri);

//        final QueryResult result = gson.fromJson(Files.newBufferedReader(Path.of(file)), QueryResult.class);
        final Map<LocalDate, Integer[]> map = new HashMap<>();
        for (final Data item : result.getQueryData()) {
            final String timeValue = item.getTimeStamp();
            final LocalDate ld = LocalDate.parse(timeValue.substring(0, 10), Data.DTF);
            final int count = item.getSessions();
            final int activeUsers = item.getActiveUsers();
            map.put(ld, new Integer[]{count, activeUsers});
        }

        final CsvWriter writer = createCsvWriter("gesamt");

        final List<LocalDate> dates = map.keySet().stream()
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        final DateTimeFormatter format = DateTimeFormatter.ofPattern("d. M.");
        final List<String> headers = new ArrayList<>();
        headers.add("Datum");
        headers.add("Sessions");
        headers.add("Active Users");

        writer.writeHeaders(headers);

        for (final LocalDate l : dates) {
            final Integer[] val = map.get(l);
            final int sessions = val[0];
            final int activeUsers = val[1];

            writer.writeRow(new String[]{l.format(format), Integer.toString(sessions), Integer.toString(activeUsers)});
        }

        writer.close();
    }

}
