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
import java.text.Collator;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.thespheres.nbc.stats.convert.model.Data;
import org.thespheres.nbc.stats.convert.model.QueryResult;
import org.thespheres.nbc.stats.convert.model.School;

/**
 *
 * @author boris.heithecker@netz-21.de
 */
public class CubeJSPagesPerSchoolReader extends AbstractCubeJSReader {

    public static final String QUERY = "{\"limit\":50000,\"measures\":[\"RawEvents.count\"],\"dimensions\":[\"RawEvents.page\",\"RawEvents.schoolId\"],\"timeDimensions\":[{\"dimension\":\"RawEvents.timeStamp\",\"dateRange\":\"last 70 days\",\"granularity\":\"year\"}]}";
    //public static final String QUERY = "{\"measures\":[\"Events.count\"],\"dimensions\":[\"Events.page\",\"Sessions.schoolId\"]}";
    final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("ddMMyyyy");
    final NumberFormat format = NumberFormat.getNumberInstance(Locale.GERMANY);
    private final Schools schools;

    public CubeJSPagesPerSchoolReader(final Schools schools, final String cubeJS, final Path outputDir) {
        super(cubeJS, outputDir);
        format.setMaximumFractionDigits(2);
        this.schools = schools;
    }

    public void read() throws InterruptedException, IOException {
        final String uri = cubeJS + "/cubejs-api/v1/load?query=" + URLEncoder.encode(QUERY, StandardCharsets.UTF_8);
        final QueryResult result = executeQuery(uri);

//        final QueryResult result = gson.fromJson(Files.newBufferedReader(Path.of(file)), QueryResult.class);
        final Map<LocalDate, Map<String, Map<String, Integer>>> map = new HashMap<>();
        for (final Data item : result.getQueryData()) {
            final String school = item.getSchoolId();
            if (school == null) {
                continue;
            }
            final String timeValue = item.getTimeStamp();
            final LocalDate ld = LocalDate.parse(timeValue.substring(0, 10), Data.DTF);
            final String ip = item.getPage().substring(1);
            final int i = ip.indexOf("/");
            final String page = i > 0 ? ip.substring(0, i) : ip;
            final int ec = item.getEventsCount();
            map.computeIfAbsent(ld, d -> new HashMap<>())
                    .computeIfAbsent(school, s -> new HashMap<>())
                    .compute(page, (p, c) -> c == null ? ec : c + ec);
        }

        final List<LocalDate> dates = map.keySet().stream()
                .sorted()
                .collect(Collectors.toList());

        for (final LocalDate file : dates) {
            final CsvWriter writer = createCsvWriter("pages" + file.format(dtf));

            final Map<String, Map<String, Integer>> sm = map.get(file);
            final List<String> pages = sm.entrySet().stream()
                    .flatMap(e -> e.getValue().keySet().stream())
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            final List<String> headers = new ArrayList<>();
            headers.add("Schule");
            pages.stream()
                    .forEach(headers::add);

            writer.writeHeaders(headers);

            final Map<String, List<String>> sorted = new TreeMap<>(Collator.getInstance(Locale.GERMANY));
            for (final Map.Entry<String, Map<String, Integer>> e : sm.entrySet()) {
                final String sid = e.getKey();
                final School school = schools.getSchool(sid);
                final String name = school.getName();
                final List<String> row = IntStream.range(0, pages.size() + 1)
                        .mapToObj(i -> "---")
                        .collect(Collectors.toList());
                row.set(0, name);
                for (final Map.Entry<String, Integer> ie : e.getValue().entrySet()) {
                    final int index = pages.indexOf(ie.getKey());
                    row.set(index + 1, Integer.toString(ie.getValue()));
                }
                sorted.put(name, row);
            }
            sorted.forEach(
                    (k, v) -> writer.writeRow(v));
            writer.close();
        }
    }

}
