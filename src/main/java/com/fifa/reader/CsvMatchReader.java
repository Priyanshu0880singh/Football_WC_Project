package com.fifa.reader;

import com.fifa.model.MatchRecord;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CsvMatchReader {

    private static final Logger log = LoggerFactory.getLogger(CsvMatchReader.class);

    private static final int COL_KEY_ID                 = 0;
    private static final int COL_TOURNAMENT_ID          = 1;
    private static final int COL_TOURNAMENT_NAME        = 2;
    private static final int COL_MATCH_ID               = 3;
    private static final int COL_MATCH_NAME             = 4;
    private static final int COL_STAGE_NAME             = 5;
    private static final int COL_GROUP_NAME             = 6;
    private static final int COL_GROUP_STAGE            = 7;
    private static final int COL_KNOCKOUT_STAGE         = 8;
    private static final int COL_REPLAYED               = 9;
    private static final int COL_REPLAY                 = 10;
    private static final int COL_MATCH_DATE             = 11;
    private static final int COL_MATCH_TIME             = 12;
    private static final int COL_STADIUM_ID             = 13;
    private static final int COL_STADIUM_NAME           = 14;
    private static final int COL_CITY_NAME              = 15;
    private static final int COL_COUNTRY_NAME           = 16;
    private static final int COL_HOME_TEAM_ID           = 17;
    private static final int COL_HOME_TEAM_NAME         = 18;
    private static final int COL_HOME_TEAM_CODE         = 19;
    private static final int COL_AWAY_TEAM_ID           = 20;
    private static final int COL_AWAY_TEAM_NAME         = 21;
    private static final int COL_AWAY_TEAM_CODE         = 22;
    private static final int COL_SCORE                  = 23;
    private static final int COL_HOME_TEAM_SCORE        = 24;
    private static final int COL_AWAY_TEAM_SCORE        = 25;
    private static final int COL_HOME_SCORE_MARGIN      = 26;
    private static final int COL_AWAY_SCORE_MARGIN      = 27;
    private static final int COL_EXTRA_TIME             = 28;
    private static final int COL_PENALTY_SHOOTOUT       = 29;
    private static final int COL_SCORE_PENALTIES        = 30;
    private static final int COL_HOME_SCORE_PENALTIES   = 31;
    private static final int COL_AWAY_SCORE_PENALTIES   = 32;
    private static final int COL_RESULT                 = 33;
    private static final int COL_HOME_TEAM_WIN          = 34;
    private static final int COL_AWAY_TEAM_WIN          = 35;
    private static final int COL_DRAW                   = 36;

    public List<MatchRecord> readFromClasspath(String resourcePath) {
        log.info("Reading CSV from classpath: {}", resourcePath);
        InputStream is = getClass().getResourceAsStream(resourcePath);
        if (is == null) {
            log.error("File not found on classpath: {}", resourcePath);
            return new ArrayList<>();
        }
        return parse(is, resourcePath);
    }

    public List<MatchRecord> readFromFile(String absolutePath) {
        log.info("Reading CSV from file: {}", absolutePath);
        try {
            return parse(new FileInputStream(absolutePath), absolutePath);
        } catch (FileNotFoundException e) {
            log.error("File not found: {}", absolutePath);
            return new ArrayList<>();
        }
    }

    private List<MatchRecord> parse(InputStream is, String source) {
        List<MatchRecord> records = new ArrayList<>();
        int line = 1;
        try {
            CSVReader csv = new CSVReader(new InputStreamReader(is, "ISO-8859-1"));
            csv.readNext(); // skip header
            String[] row;
            while ((row = csv.readNext()) != null) {
                line++;
                if (row.length < 10) { log.warn("Line {} skipped - too few columns", line); continue; }
                try { records.add(mapRow(row)); }
                catch (Exception e) { log.error("Line {} mapping failed: {}", line, e.getMessage()); }
            }
            csv.close();
        } catch (IOException | CsvValidationException e) {
            log.error("Error reading {}: {}", source, e.getMessage());
        }
        log.info("Loaded {} records from {}", records.size(), source);
        return records;
    }

    private MatchRecord mapRow(String[] row) {
        MatchRecord r = new MatchRecord();
        r.setKeyId(                  get(row, COL_KEY_ID));
        r.setTournamentId(           get(row, COL_TOURNAMENT_ID));
        r.setTournamentName(         get(row, COL_TOURNAMENT_NAME));
        r.setMatchId(                get(row, COL_MATCH_ID));
        r.setMatchName(              get(row, COL_MATCH_NAME));
        r.setStageName(              get(row, COL_STAGE_NAME));
        r.setGroupName(              get(row, COL_GROUP_NAME));
        r.setGroupStage(             get(row, COL_GROUP_STAGE));
        r.setKnockoutStage(          get(row, COL_KNOCKOUT_STAGE));
        r.setReplayed(               get(row, COL_REPLAYED));
        r.setReplay(                 get(row, COL_REPLAY));
        r.setMatchDate(              get(row, COL_MATCH_DATE));
        r.setMatchTime(              get(row, COL_MATCH_TIME));
        r.setStadiumId(              get(row, COL_STADIUM_ID));
        r.setStadiumName(            get(row, COL_STADIUM_NAME));
        r.setCityName(               get(row, COL_CITY_NAME));
        r.setCountryName(            get(row, COL_COUNTRY_NAME));
        r.setHomeTeamId(             get(row, COL_HOME_TEAM_ID));
        r.setHomeTeamName(           get(row, COL_HOME_TEAM_NAME));
        r.setHomeTeamCode(           get(row, COL_HOME_TEAM_CODE));
        r.setAwayTeamId(             get(row, COL_AWAY_TEAM_ID));
        r.setAwayTeamName(           get(row, COL_AWAY_TEAM_NAME));
        r.setAwayTeamCode(           get(row, COL_AWAY_TEAM_CODE));
        r.setScore(                  get(row, COL_SCORE));
        r.setHomeTeamScore(          get(row, COL_HOME_TEAM_SCORE));
        r.setAwayTeamScore(          get(row, COL_AWAY_TEAM_SCORE));
        r.setHomeTeamScoreMargin(    get(row, COL_HOME_SCORE_MARGIN));
        r.setAwayTeamScoreMargin(    get(row, COL_AWAY_SCORE_MARGIN));
        r.setExtraTime(              get(row, COL_EXTRA_TIME));
        r.setPenaltyShootout(        get(row, COL_PENALTY_SHOOTOUT));
        r.setScorePenalties(         get(row, COL_SCORE_PENALTIES));
        r.setHomeTeamScorePenalties( get(row, COL_HOME_SCORE_PENALTIES));
        r.setAwayTeamScorePenalties( get(row, COL_AWAY_SCORE_PENALTIES));
        r.setResult(                 get(row, COL_RESULT));
        r.setHomeTeamWin(            get(row, COL_HOME_TEAM_WIN));
        r.setAwayTeamWin(            get(row, COL_AWAY_TEAM_WIN));
        r.setDraw(                   get(row, COL_DRAW));
        return r;
    }

    private String get(String[] row, int i) {
        return (i < row.length && row[i] != null) ? row[i] : "";
    }
}
