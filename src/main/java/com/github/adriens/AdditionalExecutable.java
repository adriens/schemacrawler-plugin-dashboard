package com.github.adriens;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import schemacrawler.schema.Catalog;
import schemacrawler.tools.executable.BaseStagedExecutable;
import schemacrawler.tools.lint.*;
import schemacrawler.tools.lint.executable.LintOptions;
import schemacrawler.tools.lint.executable.LintOptionsBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import schemacrawler.schemacrawler.DatabaseSpecificOverrideOptions;

public class AdditionalExecutable
        extends BaseStagedExecutable {

    private static final Logger LOGGER = Logger
            .getLogger(AdditionalExecutable.class.getName());

    static final String COMMAND = "dashboard";

    private String outputFolder;

    private String rScriptFilename = "dashboard_rscript.R";
    private String lintsFilename = "lints.csv";
    private static final String NEW_LINE_SEPARATOR = "\n";
    private static final Object[] FILE_HEADER = {"linterId", "severity", "objectName", "message", "value"};

    protected AdditionalExecutable() {
        super(COMMAND);
    }

    @Override
    public void executeOn(final Catalog catalog, final Connection connection, DatabaseSpecificOverrideOptions databaseSpecificOverrideOptions)
            throws Exception {

        LOGGER.log(Level.CONFIG, "Start executing dashboard command");
        final LintOptions lintOptions = new LintOptionsBuilder().fromConfig(additionalConfiguration).toOptions();
        setOutputFolder(additionalConfiguration.getStringValue("outputdir", "r_dashboard"));

        CSVPrinter csvFilePrinter;
        FileWriter fileWriter;
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);

        File outputFolderFile = new File(getOutputFolder());
        outputFolderFile.mkdir();

        LOGGER.log(Level.CONFIG, "Performing lint analyze");
        final LinterConfigs linterConfigs = LintUtility.readLinterConfigs(lintOptions, getAdditionalConfiguration());
        final Linters linters = new Linters(linterConfigs);
        final LintedCatalog lintedCatalog = new LintedCatalog(catalog, connection, linters);
        Iterator<Lint<?>> lintIter = lintedCatalog.getCollector().iterator();
        // feed the csv
        Lint aLint;
        fileWriter = new FileWriter(getOutputFolder() + getLintsFilename());

        //initialize CSVPrinter object
        csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);

        //Create CSV file header
        csvFilePrinter.printRecord(FILE_HEADER);
        while (lintIter.hasNext()) {
            aLint = lintIter.next();
            List lintDataRecord = new ArrayList();
            lintDataRecord.add(aLint.getLinterId());
            lintDataRecord.add(aLint.getSeverity().toString().toUpperCase());
            lintDataRecord.add(aLint.getObjectName());
            lintDataRecord.add(aLint.getMessage());
            lintDataRecord.add(aLint.getValueAsString());

            csvFilePrinter.printRecord(lintDataRecord);
        }
        fileWriter.flush();
        fileWriter.close();

        LOGGER.log(Level.CONFIG, "Computing results in dashboard.");
        LintToCsvUtils.generateDashboardData(getOutputFolder(), getLintsFilename());
        InputStream is = ClassLoader.getSystemResourceAsStream("dashboard_of_lints.rmd");
        FileUtils.copyInputStreamToFile(is, new File(getOutputFolder() +  "dashboard_of_lints.rmd"));
        generateHtmlDashboard(outputFolder);
    }


    /**
     * Generate HTML dashboard using csv file and Rmarkdown file
     * @param outputFolder the folder where the html will be created
     * @return execution return code
     * @throws IOException
     */
    private int generateHtmlDashboard(String outputFolder) throws IOException {

        File scriptFile = new File(getOutputFolder() + rScriptFilename);
        if(scriptFile.exists()){
            scriptFile.delete();
        }
        scriptFile.createNewFile();
        scriptFile.setExecutable(true);


        List<String> lines = Arrays.asList("#!/usr/bin/env Rscript", "rmarkdown::render(\"" + getOutputFolder() + "dashboard_of_lints.rmd\")");
        Path file = Paths.get(getOutputFolder() + rScriptFilename);
        Files.write(file, lines, Charset.forName("UTF-8"));

        String command = outputFolder + "dashboard_rscript.R";

        final ProcessBuilder processBuilder;
        final Process process;
        int exitCode = -1;
        try {
            processBuilder = new ProcessBuilder(command);
            process = processBuilder.start();
            exitCode = process.waitFor();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "File " + command + " not found.", e);
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "System not supported, please execute manually R script in " + outputFolder + " folder.");
        }
        LOGGER.log(Level.SEVERE, ""+exitCode);
        return exitCode;
    }

    /**
     * @return the outputFolder
     */
    public String getOutputFolder() {
        return outputFolder;
    }

    /**
     * @param outputFolder the outputFolder to set
     */
    public void setOutputFolder(String outputFolder) {
        this.outputFolder = outputFolder +"/";
    }

    public String getLintsFilename() {
        return lintsFilename;
    }


}