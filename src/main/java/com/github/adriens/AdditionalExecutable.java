package com.github.adriens;

import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import schemacrawler.schema.Catalog;
import schemacrawler.tools.executable.BaseStagedExecutable;
import schemacrawler.tools.lint.LintUtility;
import schemacrawler.tools.lint.Linter;
import schemacrawler.tools.lint.LinterConfigs;
import schemacrawler.tools.lint.Linters;
import schemacrawler.tools.lint.executable.LintOptions;
import schemacrawler.tools.lint.executable.LintOptionsBuilder;

public class AdditionalExecutable
        extends BaseStagedExecutable {

    private static final Logger LOGGER = Logger
            .getLogger(AdditionalExecutable.class.getName());

    static final String COMMAND = "csv";

    private String FileName;
    private static final String NEW_LINE_SEPARATOR = "\n";
    private static final Object[] FILE_HEADER = {"linterId", "lintCount", "linterInstanceId", "severity", "summary", "description"};

    protected AdditionalExecutable() {
        super(COMMAND);
    }

    @Override
    public void executeOn(final Catalog catalog, final Connection connection)
            throws Exception {

        final LintOptions lintOptions = new LintOptionsBuilder().fromConfig(additionalConfiguration).toOptions();

        setFileName(additionalConfiguration.getStringValue("filename", "lints.csv"));
        File csvFile = new File(getFileName());


        CSVPrinter csvFilePrinter = null;
        FileWriter fileWriter = null;
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);

        final LinterConfigs linterConfigs = LintUtility.readLinterConfigs(lintOptions, getAdditionalConfiguration());
        final Linters linters = new Linters(linterConfigs);
        Iterator<Linter> lintIter = linters.iterator();
        // feed the csv
        Linter aLint;
        fileWriter = new FileWriter(getFileName());

        //initialize CSVPrinter object
        csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);

        //Create CSV file header
        csvFilePrinter.printRecord(FILE_HEADER);

        while (lintIter.hasNext()) {
            aLint = lintIter.next();
            List lintDataRecord = new ArrayList();
            lintDataRecord.add(aLint.getLinterId());
            lintDataRecord.add(String.valueOf(aLint.getLintCount()));
            lintDataRecord.add(aLint.getLinterInstanceId());
            lintDataRecord.add(aLint.getSeverity().toString().toUpperCase());
            lintDataRecord.add(aLint.getSummary());
            lintDataRecord.add(aLint.getDescription());

            csvFilePrinter.printRecord(lintDataRecord);
        }
        fileWriter.flush();
        fileWriter.close();
    }

    /**
     * @return the FileName
     */
    public String getFileName() {
        return FileName;
    }

    /**
     * @param FileName the FileName to set
     */
    public void setFileName(String FileName) {
        this.FileName = FileName;
    }

}
