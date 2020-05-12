/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.nbc.stats.convert;

import com.google.gson.Gson;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author boris.heithecker@netz-21.de
 */
public class Main {

    public static void main(final String[] args) throws Exception {
        final Path config;
        if (args.length > 0) {
            config = Paths.get(args[0]);
        } else {
            config = Paths.get("config.json");
        }
        if (!Files.exists(config)) {
            throw new Exception("Usage: nbc-stats-convert <config-file.json>");
        }

        final Gson gson = new Gson();
        final Config cfg;
        try (final Reader r = Files.newBufferedReader(config)) {
            cfg = gson.fromJson(r, Config.class);
        }
        final Path out = Paths.get(cfg.getOutDir());
        final Schools schools = new Schools(cfg.getApi());
//        final CubeJSReader r = new CubeJSReader(cfg.getCubeJS(), out);
//        r.read();

        final CubeJSPerSchoolReader sr = new CubeJSPerSchoolReader(schools, cfg.getCubeJS(), out);
        for (final CubeJSPerSchoolReader.Type type : CubeJSPerSchoolReader.Type.values()) {
            sr.read(type);
        }
    }

//    static void readEvalAll(final String o1) throws IOException, InterruptedException {
//        Gson gson = new Gson();
//        final String query1 = "{\"measures\":[\"Sessions.count\",\"Events.activeUsers\"],\"timeDimensions\":[{\"dimension\":\"Events.timeStamp\",\"dateRange\":\"last 90 days\",\"granularity\":\"day\"}]}";
//        String uri = "http://localhost:4000/cubejs-api/v1/load" + "?query=" + URLEncoder.encode(query1, StandardCharsets.UTF_8);
//        final HttpClientBuilder builder = HttpClients.custom();
//        CloseableHttpClient client = builder.build();
//
//        HttpGet get = new HttpGet(uri);
//
//        client.execute(get);
//        Thread.sleep(5 * 1000);
//
//        CloseableHttpResponse response = client.execute(get);
//        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
//            throw new IOException("Unexpected response status: " + response.getStatusLine() + " at: " + uri);
//        }
//        HttpEntity entity = response.getEntity();
//        final QueryResult result;
//        try (final InputStream is = entity.getContent();) {
//            final BufferedInputStream bis = new BufferedInputStream(is);
//            entity.getContentEncoding();
//            result = gson.fromJson(new InputStreamReader(bis, "UTF-8"), QueryResult.class);
//            EntityUtils.consume(entity);
//        }
//
////        final QueryResult result = gson.fromJson(Files.newBufferedReader(Path.of(file)), QueryResult.class);
//        final Map<LocalDate, Integer[]> map = new HashMap<>();
//        for (final Data item : result.getQueryData()) {
//            final String timeValue = item.getTimeStamp();
//            final LocalDate ld = LocalDate.parse(timeValue.substring(0, 10), Data.DTF);
//            final int count = item.getSessions();
//            final int activeUsers = item.getActiveUsers();
//            map.put(ld, new Integer[]{count, activeUsers});
//        }
//
//        final CsvWriter writer = new CsvWriter(Files.newBufferedWriter(Path.of(o1)), new CsvWriterSettings());
//
//        final List<LocalDate> dates = map.keySet().stream()
//                .distinct()
//                .sorted()
//                .collect(Collectors.toList());
//
//        final DateTimeFormatter format = DateTimeFormatter.ofPattern("d. M.");
//        final List<String> headers = new ArrayList<>();
//        headers.add("Datum");
//        headers.add("Sessions");
//        headers.add("Active Users");
//
//        writer.writeHeaders(headers);
//
//        for (final LocalDate l : dates) {
//            final Integer[] val = map.get(l);
//            final int sessions = val[0];
//            final int activeUsers = val[1];
//
//            writer.writeRow(new String[]{l.format(format), Integer.toString(sessions), Integer.toString(activeUsers)});
//        }
//
//        writer.close();
//    }
//
//    static void readEval(final String o1, final Schools schools, final int eval) throws IOException, InterruptedException {
//        Gson gson = new Gson();
//        final String query1 = "{\"measures\":[\"Sessions.count\",\"Events.activeUsers\"],\"dimensions\":[\"Sessions.schoolId\"],\"timeDimensions\":[{\"dimension\":\"Events.timeStamp\",\"dateRange\":\"last 90 days\",\"granularity\":\"day\"}]}";
//        String uri = "http://localhost:4000/cubejs-api/v1/load" + "?query=" + URLEncoder.encode(query1, StandardCharsets.UTF_8);
//        final HttpClientBuilder builder = HttpClients.custom();
//        CloseableHttpClient client = builder.build();
//
//        HttpGet get = new HttpGet(uri);
//
//        client.execute(get);
//        Thread.sleep(5 * 1000);
//
//        CloseableHttpResponse response = client.execute(get);
//        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
//            throw new IOException("Unexpected response status: " + response.getStatusLine() + " at: " + uri);
//        }
//        HttpEntity entity = response.getEntity();
//        final QueryResult result;
//        try (final InputStream is = entity.getContent();) {
//            final BufferedInputStream bis = new BufferedInputStream(is);
//            entity.getContentEncoding();
//            result = gson.fromJson(new InputStreamReader(bis, "UTF-8"), QueryResult.class);
//            EntityUtils.consume(entity);
//        }
//
////        final QueryResult result = gson.fromJson(Files.newBufferedReader(Path.of(file)), QueryResult.class);
//        final Map<String, Map<LocalDate, Integer[]>> map = new HashMap<>();
//        for (final Data item : result.getQueryData()) {
//            final String school = item.getSchoolId();
//            final String timeValue = item.getTimeStamp();
//            final LocalDate ld = LocalDate.parse(timeValue.substring(0, 10), Data.DTF);
//            final int count = item.getSessions();
//            final int activeUsers = item.getActiveUsers();
//            map.computeIfAbsent(school, s -> new HashMap<>())
//                    .put(ld, new Integer[]{count, activeUsers});
//        }
//
//        final CsvWriter writer = new CsvWriter(Files.newBufferedWriter(Path.of(o1)), new CsvWriterSettings());
//
//        final List<LocalDate> dates = map.entrySet().stream()
//                .flatMap(e -> e.getValue().keySet().stream())
//                .distinct()
//                .sorted()
//                .collect(Collectors.toList());
//
//        final DateTimeFormatter format = DateTimeFormatter.ofPattern("d. M.");
//        final List<String> headers = new ArrayList<>();
//        headers.add("Schule");
//        dates.stream()
//                .sorted()
//                .map(format::format)
//                .forEach(headers::add);
//
//        writer.writeHeaders(headers);
//
//        final Map<String, List<String>> sorted = new TreeMap<>(Collator.getInstance(Locale.GERMANY));
//        for (final Map.Entry<String, Map<LocalDate, Integer[]>> e : map.entrySet()) {
//            final String sid = e.getKey();
//            final School school = schools.getSchool(sid);
//            String name = school.getName();
//            if (name == null) {
//                System.out.println("Unknown: " + sid);
//                name = sid;
//            }
//            final List<String> row = IntStream.range(0, dates.size() + 1)
//                    .mapToObj(i -> "---")
//                    .collect(Collectors.toList());
//            row.set(0, name);
//            for (final Map.Entry<LocalDate, Integer[]> ie : e.getValue().entrySet()) {
//                final int index = dates.indexOf(ie.getKey());
//                if (eval < 2) {
//                    final int num = ie.getValue()[eval];
//                    row.set(index + 1, Integer.toString(num));
//                } else {
//                    final int sessions = ie.getValue()[0];
//                    final int activeUsers = ie.getValue()[1];
//                    final double spa = (double) sessions / activeUsers;
//                    row.set(index + 1, DF.format(spa));
//                }
//            }
//            sorted.put(name, row);
//        }
//
//        sorted.forEach((k, v) -> writer.writeRow(v));
//        writer.close();
//    }
//
//    static Map<String, String> parseSchools(final String mongo) throws IOException {
//        final Map<String, String> schools = new HashMap<>();
//        final List<String> lines = Files.readAllLines(Path.of(mongo));
//        for (int i = 0; i < lines.size(); i++) {
//            final String l = lines.get(i).trim();
//            if (l.startsWith("\"name\"")) {
//                final String name = l.substring(l.indexOf(":") + 1).trim().replaceAll("[\",]*", "");
//                final String lb = lines.get(i - 1).trim();
//                final String id = lb.substring(l.indexOf(":") + 1).trim().replaceAll("[\"\\(\\),]*", "").replace("ObjectId", "");
//                schools.put(id, name);
//            }
//        }
//        return schools;
//    }
//
//    static Map<String, String> parseSchoolsFile() throws IOException {
//        final Map<String, String> schools = new HashMap<>();
//        final Gson gson = new Gson();
//        final APIData result;
//        try (final InputStream is = APIData.class.getResourceAsStream("schools_data.json")) {
//            result = gson.fromJson(new InputStreamReader(is, "UTF-8"), APIData.class);
//        }
//        Arrays.stream(result.getSchools())
//                .forEach(s -> schools.put(s.getId(), s.getName()));
//        return schools;
//    }
}
