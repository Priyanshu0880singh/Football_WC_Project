package com.fifa.pipeline;

import com.fifa.aggregator.MatchAggregator;
import com.fifa.cleaner.DataCleaner;
import com.fifa.model.MatchRecord;
import com.fifa.reader.CsvMatchReader;
import com.fifa.transformer.DataTransformer;
import com.fifa.validator.RecordValidator;
import com.fifa.writer.PostgresWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DataPipeline {

    private static final Logger log = LoggerFactory.getLogger(DataPipeline.class);
    private static final String CSV = "/FIFA_WC_Raw_Dataset.csv";

    public static void main(String[] args) {
        log.info("=== FIFA PIPELINE START ===");
        new DataPipeline().run();
        log.info("=== FIFA PIPELINE COMPLETE ===");
    }

    public void run() {

        // STEP 1 - Read CSV
        log.info("[STEP 1] Reading CSV");
        List<MatchRecord> records = new CsvMatchReader().readFromClasspath(CSV);
        log.info("[STEP 1] {} rows loaded", records.size());
        if (records.isEmpty()) { log.error("No records loaded. Aborting."); return; }

        DataCleaner cleaner = new DataCleaner();

        // STEP 2 - UC1 Deduplicate
        log.info("[STEP 2] UC1 - Deduplication");
        records = cleaner.deduplicateByMatchId(records);

        // STEP 3 - UC2 Normalize Names
        log.info("[STEP 3] UC2 - Normalize Names");
        cleaner.normalizeNames(records);

        // STEP 4 - UC3 Fix Numeric
        log.info("[STEP 4] UC3 - Fix Numeric Fields");
        cleaner.fixNumericFields(records);

        // STEP 5 - UC4 Standardize Dates
        log.info("[STEP 5] UC4 - Standardize Dates");
        cleaner.standardizeDates(records);

        DataTransformer transformer = new DataTransformer();

        // STEP 6 - UC5 Map Codes
        log.info("[STEP 6] UC5 - Map Team Codes");
        transformer.mapTeamCodes(records);

        // STEP 7 - UC6 Normalize Result & Stage
        log.info("[STEP 7] UC6 - Normalize Result and Stage");
        transformer.normalizeResultAndStage(records);

        // STEP 8 - UC8 Derived Fields
        log.info("[STEP 8] UC8 - Compute Derived Fields");
        transformer.computeDerivedFields(records);

        RecordValidator validator = new RecordValidator();

        // STEP 9 - UC7 Flag Invalid
        log.info("[STEP 9] UC7 - Flag Invalid Records");
        validator.flagInvalidRecords(records);
        List<MatchRecord> valid   = validator.filterValid(records);
        List<MatchRecord> invalid = validator.filterInvalid(records);
        log.info("[STEP 9] Valid={}, Invalid={}", valid.size(), invalid.size());

        // STEP 10 - UC10 Categorize
        log.info("[STEP 10] UC10 - Categorize Matches");
        validator.categorizeMatches(valid);

        // STEP 11 - UC9 Aggregate
        log.info("[STEP 11] UC9 - Aggregations");
        new MatchAggregator().printSummary(valid);

        // STEP 12 - Upload to PostgreSQL
        boolean skip = Boolean.parseBoolean(System.getProperty("pipeline.skipUpload", "false"));
        if (skip) {
            log.info("[STEP 12] Skipped (pipeline.skipUpload=true)");
        } else {
            log.info("[STEP 12] Uploading to PostgreSQL");
            new PostgresWriter().upload(valid, invalid);
        }
    }
}