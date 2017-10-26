package com.github.adriens;

import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.reducing.CrossTab;
import static tech.tablesaw.api.QueryHelper.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class LintToCsvUtils {

    public static final int CRITICAL_WEIGHT = 5;
    public static final int HIGH_WEIGHT = 3;
    public static final int MEDIUM_WEIGHT = 2;
    public static final int LOW_WEIGHT = 1;

    public static void generateTop10(String csvFileName) throws IOException {

        Table fullData = Table.read().csv(csvFileName);

        fullData.removeColumns("message", "value");

        Table workTable = CrossTab.xCount(fullData, fullData.categoryColumn("objectName"), fullData.categoryColumn("severity"));

        List<String> columns = workTable.columnNames();
        int[] zeroArray = new int[workTable.rowCount()];
        Arrays.fill(zeroArray, 0);
        if(!columns.contains("CRITICAL"))
            workTable.addColumn(new IntColumn("CRITICAL", zeroArray));
        if(!columns.contains("HIGH"))
            workTable.addColumn(new IntColumn("HIGH", zeroArray));
        if(!columns.contains("MEDIUM"))
            workTable.addColumn(new IntColumn("MEDIUM", zeroArray));
        if(!columns.contains("LOW"))
            workTable.addColumn(new IntColumn("LOW", zeroArray));

        workTable.addColumn(workTable.intColumn("CRITICAL").multiply(CRITICAL_WEIGHT));
        workTable.addColumn(workTable.intColumn("HIGH").multiply(HIGH_WEIGHT));
        workTable.addColumn(workTable.intColumn("MEDIUM").multiply(MEDIUM_WEIGHT));
        workTable.addColumn(workTable.intColumn("LOW").multiply(LOW_WEIGHT));
        workTable.addColumn(workTable.intColumn("CRITICAL * 5")
                .add(workTable.intColumn("HIGH * 3"))
                .add(workTable.intColumn("MEDIUM * 2"))
                .add(workTable.intColumn("LOW * 1"))
                .divide(workTable.intColumn("total")));



        workTable.column("CRITICAL * 5 + HIGH * 3 + MEDIUM * 2 + LOW * 1 / total").setName("SCORE");
        workTable.column("").setName("TABLE");

        workTable.retainColumns("SCORE", "TABLE");
        Table sortedScore = workTable.sortDescendingOn("SCORE");
        sortedScore = sortedScore.dropRows(0);

        System.out.println(sortedScore);

        sortedScore = sortedScore.first(10);

        sortedScore.write().csv("top10_worst_tables.csv");

    }

    public static void generateDatabaseScore(String csvFileName) throws IOException {

        Table fullData = Table.read().csv(csvFileName);

        fullData.removeColumns("message", "value");

        Table workTable = CrossTab.xCount(fullData, fullData.categoryColumn("objectName"), fullData.categoryColumn("severity"));

        List<String> columns = workTable.columnNames();
        int[] zeroArray = new int[workTable.rowCount()];
        Arrays.fill(zeroArray, 0);
        if(!columns.contains("CRITICAL"))
            workTable.addColumn(new IntColumn("CRITICAL", zeroArray));
        if(!columns.contains("HIGH"))
            workTable.addColumn(new IntColumn("HIGH", zeroArray));
        if(!columns.contains("MEDIUM"))
            workTable.addColumn(new IntColumn("MEDIUM", zeroArray));
        if(!columns.contains("LOW"))
            workTable.addColumn(new IntColumn("LOW", zeroArray));

        workTable.addColumn(workTable.intColumn("CRITICAL").multiply(CRITICAL_WEIGHT));
        workTable.addColumn(workTable.intColumn("HIGH").multiply(HIGH_WEIGHT));
        workTable.addColumn(workTable.intColumn("MEDIUM").multiply(MEDIUM_WEIGHT));
        workTable.addColumn(workTable.intColumn("LOW").multiply(LOW_WEIGHT));
        workTable.column("").setName("TABLE");

        Table score = workTable.selectWhere(column("TABLE").isEqualTo("Total"));
        score.retainColumns("CRITICAL * 5", "HIGH * 3", "MEDIUM * 2", "LOW * 1", "total");

        score.addColumn(score.intColumn("CRITICAL * 5")
                .add(score.intColumn("HIGH * 3"))
                .add(score.intColumn("MEDIUM * 2"))
                .add(score.intColumn("LOW * 1"))
                .divide(score.intColumn("total")));

        score.column("CRITICAL * 5 + HIGH * 3 + MEDIUM * 2 + LOW * 1 / total").setName("SCORE");
        score.retainColumns("SCORE");

        score.write().csv("score.csv");

    }

    public static void main(String[] args) throws IOException {
        generateDatabaseScore("samples/lints.csv");
    }


}
