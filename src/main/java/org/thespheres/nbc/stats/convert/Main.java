/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.nbc.stats.convert;

import com.google.gson.Gson;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author boris.heithecker@netz-21.de
 */
public class Main {

    public static void main(final String[] args) throws Exception {
        final CommandLineParser parser = new DefaultParser();
        final Options options = new Options();
        options.addOption("c", "configuration", true, "The configuration file.");
        options.addOption("t", "token-file", true, "The file containing the JWT.");
        options.addOption("b", "basic-auth-file", true, "The file containing the basic auth string.");
        final CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException pex) {
            System.out.println(pex.getLocalizedMessage());
            return;
        }
        final Path config;
        if (cmd.hasOption("c")) {
            config = Paths.get(cmd.getOptionValue("c"));
        } else {
            config = Paths.get("config.json");
        }
        if (!Files.exists(config)) {
            throw new Exception("Configuration file does not exist.");
        }
        final Gson gson = new Gson();
        final Config cfg;
        try (final Reader r = Files.newBufferedReader(config)) {
            cfg = gson.fromJson(r, Config.class);
        }
        final Path schoolsDump = Paths.get("schools.json");
        final Path out = Paths.get(cfg.getOutDir());
        final Schools schools;
        final String api = cfg.getApi();
        final boolean useToken = cmd.hasOption("t");
        final boolean useBasicAuth = cmd.hasOption("b");
        if (Files.exists(schoolsDump)) {
            schools = SchoolsFile.read(schoolsDump);
        } else if (api != null) {
            String token = null;
            if (useToken) {
                final Path tokenFile = Paths.get(cmd.getOptionValue("t"));
                if (!Files.exists(tokenFile)) {
                    throw new Exception("JWT file does not exist.");
                }
                token = Files.readString(tokenFile, StandardCharsets.UTF_8);
            }
            String auth = null;
            if (useBasicAuth) {
                final Path authFile = Paths.get(cmd.getOptionValue("b"));
                if (!Files.exists(authFile)) {
                    throw new Exception("Basic auth string file does not exist.");
                }
                auth = Files.readString(authFile, StandardCharsets.UTF_8);
            }
            schools = new SchoolsAPI(api, token, auth);
        } else {
            final Path schoolsFile = Paths.get(cfg.getSchoolsFile());
            schools = SchoolsFile.read(schoolsFile);
        }

        try (final WritableByteChannel ch = Files.newByteChannel(schoolsDump, StandardOpenOption.CREATE, StandardOpenOption.WRITE); final Writer w = Channels.newWriter(ch, "utf8")) {
            schools.dump(w);
        }

        final CubeJSReader r = new CubeJSReader(cfg.getCubeJS(), out);
        r.read();

        final CubeJSPagesPerSchoolReader p = new CubeJSPagesPerSchoolReader(schools, cfg.getCubeJS(), out);
        p.read();

        final CubeJSPerSchoolReader sr = new CubeJSPerSchoolReader(schools, cfg.getCubeJS(), out);
        for (final CubeJSPerSchoolReader.Type type : CubeJSPerSchoolReader.Type.values()) {
            sr.read(type);
        }

        if (useToken) {
            try (final WritableByteChannel ch = Files.newByteChannel(schoolsDump, StandardOpenOption.CREATE, StandardOpenOption.WRITE); final Writer w = Channels.newWriter(ch, "utf8")) {
                schools.dump(w);
            }
        }
    }

}
