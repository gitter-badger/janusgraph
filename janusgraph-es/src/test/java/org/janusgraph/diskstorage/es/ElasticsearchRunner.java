// Copyright 2017 JanusGraph Authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.janusgraph.diskstorage.es;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.janusgraph.DaemonRunner;
import org.janusgraph.example.GraphOfTheGodsFactory;
import org.janusgraph.util.system.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

/**
 * Start and stop a separate Elasticsearch server process.
 */
public class ElasticsearchRunner extends DaemonRunner<ElasticsearchStatus> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchRunner.class);
    
    private static final String ES_PID_FILE = "/tmp/janusgraph-test-es.pid";
    private static final String DEFAULT_HOME_DIR;
    static {
        try {
            DEFAULT_HOME_DIR = new File(ElasticsearchRunner.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().getAbsolutePath();
        } catch (URISyntaxException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    
    public static final boolean IS_EXTERNAL = Boolean.valueOf(System.getProperty("is.external.es", "false"));

    private final String elasticsearchDirectory;

    public ElasticsearchRunner(String esHome) throws IOException {
        final ClassLoader classLoader = getClass().getClassLoader();
        try (final InputStream in = classLoader.getResourceAsStream("janusgraph-es.properties")) {
            if (in != null) {
                Properties properties = new Properties();
                properties.load(new InputStreamReader(in, Charsets.UTF_8));
                this.elasticsearchDirectory = esHome + File.separator + "elasticsearch-" + properties.getProperty("elasticsearch.version");
            } else {
                throw new RuntimeException("Unable to read Elasticsearch version from properties");
            }
        }
        
        try (final InputStream in = classLoader.getResourceAsStream("elasticsearch.yml")) {
            if (in != null) {
                Files.asByteSink(Paths.get(this.elasticsearchDirectory, "config", "elasticsearch.yml").toFile()).writeFrom(in);
            } else {
                throw new RuntimeException("Unable to read elasticsearch.yml from resources");
            }
        }
    }

    public ElasticsearchRunner() throws IOException {
        this(DEFAULT_HOME_DIR);
    }

    @Override
    protected String getDaemonShortName() {
        return "Elasticsearch";
    }

    @Override
    protected void killImpl(ElasticsearchStatus stat) throws IOException {
        LOGGER.info("Killing {} pid {}...", getDaemonShortName(), stat.getPid());

        runCommand("/bin/kill", String.valueOf(stat.getPid()));

        LOGGER.info("Sent SIGTERM to {} pid {}", getDaemonShortName(), stat.getPid());

        try {
            watchLog(" closed", 60L, TimeUnit.SECONDS);
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        stat.getFile().delete();

        LOGGER.info("Deleted {}", stat.getFile());
    }

    @Override
    protected ElasticsearchStatus startImpl() throws IOException {

        File data = new File(elasticsearchDirectory + File.separator + "data");
        File logs = new File(elasticsearchDirectory + File.separator + "logs");

        if (data.exists() && data.isDirectory()) {
            LOGGER.info("Deleting {}", data);
            FileUtils.deleteDirectory(data);
        }

        if (logs.exists() && logs.isDirectory()) {
            LOGGER.info("Deleting {}", logs);
            FileUtils.deleteDirectory(logs);
        }

        runCommand(elasticsearchDirectory + File.separator + "bin" + File.separator + "elasticsearch", "-d", "-p", ES_PID_FILE);
        try {
            watchLog(" started", 60L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return readStatusFromDisk();
    }

    @Override
    protected ElasticsearchStatus readStatusFromDisk() {
        return ElasticsearchStatus.read(ES_PID_FILE);
    }

    private void watchLog(String suffix, long duration, TimeUnit unit) throws InterruptedException {
        long startMS = System.currentTimeMillis();
        long durationMS = TimeUnit.MILLISECONDS.convert(duration, unit);
        long elapsedMS;

        File logFile = new File(elasticsearchDirectory + File.separator + "logs" + File.separator + "elasticsearch.log");

        LOGGER.info("Watching ES logfile {} for {} token", logFile, suffix);

        while ((elapsedMS = System.currentTimeMillis() - startMS) < durationMS) {

            // Grep for a logline ending in the suffix and assume that means ES is ready
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(logFile));
                String line;
                while (null != (line = br.readLine())) {
                    if (line.endsWith(suffix)) {
                        LOGGER.debug("Read line \"{}\" from ES logfile {}", line, logFile);
                        return;
                    }
                }
            } catch (FileNotFoundException e) {
                LOGGER.debug("Elasticsearch logfile {} not found", logFile, e);
            } catch (IOException e) {
                LOGGER.debug("Elasticsearch logfile {} could not be read", logFile, e);
            } finally {
                IOUtils.closeQuietly(br);
            }

            Thread.sleep(500L);
        }

        LOGGER.info("Elasticsearch logfile timeout ({} {})", elapsedMS, TimeUnit.MILLISECONDS);
    }

    /**
     * Start Elasticsearch process, load GraphOfTheGods, and stop process. Used for integration testing.
     * 
     * @param args a singleton array containing a path to a JanusGraph config properties file
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        final ElasticsearchRunner runner = new ElasticsearchRunner();
        runner.start();
        GraphOfTheGodsFactory.main(args);
        runner.stop();
    }

}
