package com.github.adriens;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import tech.tablesaw.api.CategoryColumn;
import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;
import tech.tablesaw.mapping.StringMapUtils;
import tech.tablesaw.reducing.CrossTab;
import tech.tablesaw.reducing.NumericSummaryTable;
import tech.tablesaw.reducing.functions.Count;

import static tech.tablesaw.api.QueryHelper.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

        Table biggestHitsSeverity = workTable.selectWhere(column("").isEqualTo("Total"));
        biggestHitsSeverity.retainColumns("CRITICAL", "HIGH", "LOW", "MEDIUM");
        Integer maxCount = 0;
        String maxSeverity = "";
        Integer value;
        for (Column columnName : biggestHitsSeverity.columns()) {
            value = Integer.valueOf(biggestHitsSeverity.column(columnName.name()).getString(0));
            if(value.compareTo(maxCount) >= 0){
                maxCount = value;
                maxSeverity = columnName.name();
            }
        }



        biggestHitsSeverity.retainColumns(maxSeverity);
        biggestHitsSeverity.write().csv("biggest_hits_severity.csv");

        workTable.column("CRITICAL * 5 + HIGH * 3 + MEDIUM * 2 + LOW * 1 / total").setName("SCORE");
        workTable.column("").setName("TABLE");

        workTable.retainColumns("SCORE", "TABLE", "total");
        Table sortedScore = workTable.sortDescendingOn("SCORE");
        sortedScore = sortedScore.dropRows(0);
        sortedScore = sortedScore.first(10);

        List<String> tables = new ArrayList<>();
        for (int i = 0; i < 10; i++){
            tables.add(sortedScore.column("TABLE").getString(i));
        }

        Table top10 = fullData.selectWhere(column("objectName").isIn(tables.toArray(new String[tables.size()])));
//        CategoryColumn cat = ((StringMapUtils)top10.column("objectName")).replaceAll("bilan_adm.", "");
        top10.removeColumns("linterId");
        top10.write().csv("top10_worst_tables.csv");

    }

    public static void generateGlobalScore(String csvFileName) throws IOException {

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

        fullData.write().csv("aggregated_lints.csv");
        score.retainColumns("SCORE");
        score.write().csv("global_score.csv");

    }


    public static void severityRepartition(String csvFileName) throws IOException {

        Table fullData = Table.read().csv(csvFileName);

        fullData.retainColumns("objectName", "severity");

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

        Table repartition = workTable.selectWhere(column("").isEqualTo("Total"));
        repartition.removeColumns("", "total");
        repartition.write().csv("severity_repartition.csv");

    }

    public static void prepareDataForTreeMap(String csvFileName) throws IOException {

        Table fullData = Table.read().csv(csvFileName);
        fullData.removeColumns("message");

        IntArrayList intList = new IntArrayList(new ArrayList<>(Collections.nCopies(fullData.rowCount(), 1)));
        IntColumn intColumn = new IntColumn("count", intList);
        Table countTable = fullData.fullCopy();
        countTable.addColumn(intColumn);

        countTable = countTable.sum("count").by("linterId", "objectName", "severity");
        countTable.column("Sum [count]").setName("lint_count");
        countTable.column("objectName").setName("table");
        countTable.write().csv("aggregated_lints.csv");

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
        workTable.column("").setName("table");

        workTable.addColumn(workTable.intColumn("CRITICAL * 5")
                .add(workTable.intColumn("HIGH * 3"))
                .add(workTable.intColumn("MEDIUM * 2"))
                .add(workTable.intColumn("LOW * 1"))
                .divide(workTable.intColumn("total")));

        workTable.column("CRITICAL * 5 + HIGH * 3 + MEDIUM * 2 + LOW * 1 / total").setName("score");
        workTable.retainColumns("table", "score");

        Table score = workTable.selectWhere(column("table").isNotEqualTo("Total"));
        score.write().csv("tables_score.csv");

    }

    public static void generateWorstSeverityDetail(String csvFileName) throws IOException {

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

        // Recuperation des tables ayant le niveau de severite la plus haute
        Table summary = workTable.selectWhere(column("").isEqualTo("Total"));
        Table worstSeverity = null;
        if(Integer.valueOf(summary.column("CRITICAL").getString(0)) > 0){
            worstSeverity = workTable.selectWhere(column("CRITICAL").isGreaterThan(0));
        }else if (Integer.valueOf(summary.column("HIGH").getString(0)) > 0){
            worstSeverity = workTable.selectWhere(column("HIGH").isGreaterThan(0));
        }
        else if (Integer.valueOf(summary.column("MEDIUM").getString(0)) > 0){
            worstSeverity = workTable.selectWhere(column("MEDIUM").isGreaterThan(0));
        }
        else if (Integer.valueOf(summary.column("LOW").getString(0)) > 0){
            worstSeverity = workTable.selectWhere(column("LOW").isGreaterThan(0));
        }
        List<String> tables = new ArrayList<>();
        for (int i = 0; i < worstSeverity.rowCount(); i++){
            tables.add(worstSeverity.column("").getString(i));
        }
        worstSeverity = fullData.selectWhere(column("objectName").isIn(tables.toArray(new String[tables.size()])));
        worstSeverity.retainColumns("objectName", "severity");

        if(worstSeverity!=null) {
            worstSeverity.write().csv("worst_severity_tables.csv");
        }

        // Calcul de la severité ayant le plus de hit
        Table biggestHitsSeverity = workTable.selectWhere(column("").isEqualTo("Total"));
        biggestHitsSeverity.retainColumns("CRITICAL", "HIGH", "LOW", "MEDIUM");
        Integer maxCount = 0;
        String maxSeverity = "";
        Integer value;
        for (Column columnName : biggestHitsSeverity.columns()) {
            value = Integer.valueOf(biggestHitsSeverity.column(columnName.name()).getString(0));
            if(value.compareTo(maxCount) >= 0){
                maxCount = value;
                maxSeverity = columnName.name();
            }
        }

        biggestHitsSeverity.retainColumns(maxSeverity);
        biggestHitsSeverity.write().csv("biggest_hits_severity.csv");

    }

    public static void generateSummary(String csvFileName) throws IOException {

        Table fullData = Table.read().csv(csvFileName);

        fullData.removeColumns("message", "value");

        Table workTable = CrossTab.xCount(fullData, fullData.categoryColumn("objectName"), fullData.categoryColumn("severity"));

        List<String> columns = workTable.columnNames();
        int[] zeroArray = new int[workTable.rowCount()];
        Arrays.fill(zeroArray, 0);
        if (!columns.contains("CRITICAL"))
            workTable.addColumn(new IntColumn("CRITICAL", zeroArray));
        if (!columns.contains("HIGH"))
            workTable.addColumn(new IntColumn("HIGH", zeroArray));
        if (!columns.contains("MEDIUM"))
            workTable.addColumn(new IntColumn("MEDIUM", zeroArray));
        if (!columns.contains("LOW"))
            workTable.addColumn(new IntColumn("LOW", zeroArray));

        workTable.column("").setName("TABLE");
        workTable.write().csv("summary.csv");
    }

    public static void main(String[] args) throws IOException {
        generateTop10("samples/lints.csv");
        generateGlobalScore("samples/lints.csv");
        severityRepartition("samples/lints.csv");
        prepareDataForTreeMap("samples/lints.csv");
        generateWorstSeverityDetail("samples/lints.csv");
        generateSummary("samples/lints.csv");
    }


}
