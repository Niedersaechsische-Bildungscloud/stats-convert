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
public class CubeJSPerSchoolReader extends AbstractCubeJSReader {

    public static final String QUERY = "{\"measures\":[\"Sessions.count\",\"Events.activeUsers\"],\"dimensions\":[\"Sessions.schoolId\"],\"timeDimensions\":[{\"dimension\":\"Events.timeStamp\",\"dateRange\":\"last 1000 days\",\"granularity\":\"day\"}]}";
    final NumberFormat format = NumberFormat.getNumberInstance(Locale.GERMANY);
    private final Schools schools;

    public enum Type {
        SESSIONS("Sessions"),
        ACTIVE_USERS("Active users"),
        SESSIONS_PER_ACTIVE_USER("Sessions per active user");
        final String name;

        Type(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

    }

    public CubeJSPerSchoolReader(final Schools schools, final String cubeJS, final Path outputDir) {
        super(cubeJS, outputDir);
        format.setMaximumFractionDigits(2);
        this.schools = schools;
    }

    public void read(final Type type) throws InterruptedException, IOException {
        final String uri = cubeJS + "/cubejs-api/v1/load?query=" + URLEncoder.encode(QUERY, StandardCharsets.UTF_8);
        final QueryResult result = executeQuery(uri);

//        final QueryResult result = gson.fromJson(Files.newBufferedReader(Path.of(file)), QueryResult.class);
        final Map<String, Map<LocalDate, Integer[]>> map = new HashMap<>();
        for (final Data item : result.getQueryData()) {
            final String school = item.getSchoolId();
            final String timeValue = item.getTimeStamp();
            final LocalDate ld = LocalDate.parse(timeValue.substring(0, 10), Data.DTF);
            final int count = item.getSessions();
            final int activeUsers = item.getActiveUsers();
            map.computeIfAbsent(school, s -> new HashMap<>())
                    .put(ld, new Integer[]{count, activeUsers});
        }

        final String typeName = type.getName().toLowerCase().replaceAll("\\s", "_");
        final CsvWriter writer = createCsvWriter(typeName);

        final List<LocalDate> dates = map.entrySet().stream()
                .flatMap(e -> e.getValue().keySet().stream())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        final DateTimeFormatter format = DateTimeFormatter.ofPattern("d. M.");
        final List<String> headers = new ArrayList<>();
        headers.add("Schule");
        dates.stream()
                .sorted()
                .map(format::format)
                .forEach(headers::add);

        writer.writeHeaders(headers);

        final Map<String, List<String>> sorted = new TreeMap<>(Collator.getInstance(Locale.GERMANY));
        for (final Map.Entry<String, Map<LocalDate, Integer[]>> e : map.entrySet()) {
            final String sid = e.getKey();
            final School school = schools.getSchool(sid);
            final String name = school.getName();
            final List<String> row = IntStream.range(0, dates.size() + 1)
                    .mapToObj(i -> "---")
                    .collect(Collectors.toList());
            row.set(0, name);
            final int eval = type.ordinal();
            for (final Map.Entry<LocalDate, Integer[]> ie : e.getValue().entrySet()) {
                final int index = dates.indexOf(ie.getKey());
                if (eval < 2) {
                    final int num = ie.getValue()[eval];
                    row.set(index + 1, Integer.toString(num));
                } else {
                    final int sessions = ie.getValue()[0];
                    final int activeUsers = ie.getValue()[1];
                    final double spa = (double) sessions / activeUsers;
                    row.set(index + 1, this.format.format(spa));
                }
            }
            sorted.put(name, row);
        }

        sorted.forEach((k, v) -> writer.writeRow(v));
        writer.close();
    }

}
