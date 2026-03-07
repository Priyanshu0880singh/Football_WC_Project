# Football_WC_Project

A Java-based ETL (Extract, Transform, Load) pipeline for processing FIFA World Cup match data using Core Java, Maven, and PostgreSQL.

## Project Overview

This project reads raw FIFA World Cup match data from a CSV file, performs data cleaning, transformation, validation, and aggregation, and uploads the processed records to a PostgreSQL database. It implements 10 Core Java use cases covering the full data pipeline lifecycle.

## Project Structure

Football_WC_Project/
├── src/
│    ├── main/
│    │    ├── java/com/fifa/
│    │    │    ├── model/
│    │    │    │    └── MatchRecord.java
│    │    │    ├── reader/
│    │    │    │    └── CsvMatchReader.java
│    │    │    ├── util/
│    │    │    │    └── CleaningUtils.java
│    │    │    ├── cleaner/
│    │    │    │    └── DataCleaner.java
│    │    │    ├── transformer/
│    │    │    │    └── DataTransformer.java
│    │    │    ├── validator/
│    │    │    │    └── RecordValidator.java
│    │    │    ├── aggregator/
│    │    │    │    └── MatchAggregator.java
│    │    │    ├── writer/
│    │    │    │    └── PostgresWriter.java
│    │    │    └── pipeline/
│    │    │         └── DataPipeline.java
│    │    └── resources/
│    │         ├── FIFA_WC_Raw_Dataset.csv
│    │         └── logback.xml
│    └── test/
│         └── java/com/fifa/
│              └── PipelineTest.java
├── .gitignore
└── pom.xml


## Use Cases Implemented

| Use Case | Description | Class |
|----------|-------------|-------|
| UC1 | Remove duplicate records | DataCleaner |
| UC2 | Normalize team names, stadium, city, country | DataCleaner |
| UC3 | Fix invalid score values | DataCleaner |
| UC4 | Standardize date formats | DataCleaner |
| UC5 | Map country codes to full names | DataTransformer |
| UC6 | Normalize match result labels | DataTransformer |
| UC7 | Flag invalid records with reasons | RecordValidator |
| UC8 | Add derived fields like total goals | DataTransformer |
| UC9 | Aggregate match statistics by team and year | MatchAggregator |
| UC10 | Categorize matches by score range | RecordValidator |
