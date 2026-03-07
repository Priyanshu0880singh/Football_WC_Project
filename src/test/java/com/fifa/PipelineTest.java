package com.fifa;

import com.fifa.cleaner.DataCleaner;
import com.fifa.model.MatchRecord;
import com.fifa.transformer.DataTransformer;
import com.fifa.util.CleaningUtils;
import com.fifa.validator.RecordValidator;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class PipelineTest {

    private MatchRecord make(String matchId, String hScore, String aScore,
                              String date, String result, String hCode, String aCode) {
        MatchRecord r = new MatchRecord();
        r.setMatchId(matchId);   r.setKeyId(matchId);
        r.setHomeTeamScore(hScore); r.setAwayTeamScore(aScore);
        r.setMatchDate(date);    r.setResult(result);
        r.setHomeTeamCode(hCode); r.setAwayTeamCode(aCode);
        r.setHomeTeamName("Team A"); r.setAwayTeamName("Team B");
        r.setTournamentName("2022 FIFA World Cup");
        r.setStageName("group stage"); r.setPenaltyShootout("0");
        return r;
    }

    // UC1
    @Test void testDeduplicate() {
        DataCleaner c = new DataCleaner();
        List<MatchRecord> list = new ArrayList<>();
        list.add(make("M-001","3","1","2022-11-22","home team win","ARG","FRA"));
        list.add(make("M-001","3","1","2022-11-22","home team win","ARG","FRA"));
        list.add(make("M-002","0","0","2022-11-23","draw","BRA","ENG"));
        assertEquals(2, c.deduplicateByMatchId(list).size());
    }

    // UC2
    @Test void testNormalizeNames() {
        DataCleaner c = new DataCleaner();
        MatchRecord r = new MatchRecord();
        r.setHomeTeamName("ARGENTINA"); r.setAwayTeamName("france");
        r.setStadiumName("  LUSAIL  "); r.setCityName("doha");
        r.setCountryName("QATAR"); r.setTournamentName("2022 fifa world cup");
        c.normalizeNames(List.of(r));
        assertEquals("Argentina", r.getHomeTeamName());
        assertEquals("France",    r.getAwayTeamName());
        assertEquals("Lusail",    r.getStadiumName());
        assertEquals("Doha",      r.getCityName());
    }

    // UC3
    @Test void testFixNumeric_valid() {
        DataCleaner c = new DataCleaner();
        MatchRecord r = make("M-003","4","1","2022-11-22","home team win","ARG","FRA");
        c.fixNumericFields(List.of(r));
        assertEquals(4, r.getCleanHomeScore());
        assertEquals(1, r.getCleanAwayScore());
    }

    @Test void testFixNumeric_negativeIsNull() {
        DataCleaner c = new DataCleaner();
        MatchRecord r = make("M-004","-1","2","2022-11-22","away team win","BRA","GER");
        c.fixNumericFields(List.of(r));
        assertNull(r.getCleanHomeScore());
    }

    @Test void testFixNumeric_nonNumericIsNull() {
        DataCleaner c = new DataCleaner();
        MatchRecord r = make("M-005","abc","N/A","2022-11-22","draw","ESP","GER");
        c.fixNumericFields(List.of(r));
        assertNull(r.getCleanHomeScore());
        assertNull(r.getCleanAwayScore());
    }

    // UC4
    @Test void testDates_ddMMyyyy() {
        DataCleaner c = new DataCleaner();
        MatchRecord r = new MatchRecord(); r.setMatchDate("22/11/2022");
        c.standardizeDates(List.of(r));
        assertEquals("2022-11-22", r.getCleanMatchDate());
    }

    @Test void testDates_ddMMyyyyDash() {
        DataCleaner c = new DataCleaner();
        MatchRecord r = new MatchRecord(); r.setMatchDate("22-11-2022");
        c.standardizeDates(List.of(r));
        assertEquals("2022-11-22", r.getCleanMatchDate());
    }

    @Test void testDates_compact() {
        DataCleaner c = new DataCleaner();
        MatchRecord r = new MatchRecord(); r.setMatchDate("20221122");
        c.standardizeDates(List.of(r));
        assertEquals("2022-11-22", r.getCleanMatchDate());
    }

    @Test void testDates_invalid_null() {
        DataCleaner c = new DataCleaner();
        MatchRecord r = new MatchRecord(); r.setMatchDate("9999-99-99");
        c.standardizeDates(List.of(r));
        assertNull(r.getCleanMatchDate());
    }

    // UC5
    @Test void testMapCodes() {
        DataTransformer t = new DataTransformer();
        MatchRecord r = new MatchRecord();
        r.setHomeTeamCode("fra"); r.setAwayTeamCode("  Arg  ");
        t.mapTeamCodes(List.of(r));
        assertEquals("FRA", r.getHomeTeamCode());
        assertEquals("ARG", r.getAwayTeamCode());
    }

    // UC6
    @Test void testNormalizeResult_aliases() {
        DataTransformer t = new DataTransformer();
        for (String alias : new String[]{"H","home win","1","HOME TEAM WIN"}) {
            MatchRecord r = new MatchRecord(); r.setResult(alias); r.setStageName("group stage");
            t.normalizeResultAndStage(List.of(r));
            assertEquals("home team win", r.getNormalizedResult(), "Failed for: " + alias);
        }
        for (String alias : new String[]{"A","away win","2"}) {
            MatchRecord r = new MatchRecord(); r.setResult(alias); r.setStageName("group stage");
            t.normalizeResultAndStage(List.of(r));
            assertEquals("away team win", r.getNormalizedResult(), "Failed for: " + alias);
        }
        for (String alias : new String[]{"D","Tie","DRAW","0"}) {
            MatchRecord r = new MatchRecord(); r.setResult(alias); r.setStageName("group stage");
            t.normalizeResultAndStage(List.of(r));
            assertEquals("draw", r.getNormalizedResult(), "Failed for: " + alias);
        }
    }

    // UC7
    @Test void testFlag_missingName() {
        MatchRecord r = make("M-020","2","1","2022-11-22","home team win","ARG","FRA");
        r.setHomeTeamName("");
        run(r); new RecordValidator().flagInvalidRecords(List.of(r));
        assertTrue(r.isFlaggedInvalid());
        assertTrue(r.getInvalidReason().contains("R2"));
    }

    @Test void testFlag_validRecord() {
        MatchRecord r = make("M-021","2","1","2022-11-22","home team win","ARG","FRA");
        run(r); new RecordValidator().flagInvalidRecords(List.of(r));
        assertFalse(r.isFlaggedInvalid());
    }

    // UC8
    @Test void testDerivedFields() {
        MatchRecord r = make("M-030","3","2","2022-11-22","home team win","ARG","FRA");
        new DataCleaner().fixNumericFields(List.of(r));
        new DataTransformer().computeDerivedFields(List.of(r));
        assertEquals(5, r.getTotalGoals());
        assertEquals("Contemporary", r.getTournamentEra());
    }

    @Test void testEra_classic() {
        MatchRecord r = new MatchRecord(); r.setTournamentName("1966 FIFA World Cup");
        new DataTransformer().computeDerivedFields(List.of(r));
        assertEquals("Classic", r.getTournamentEra());
    }

    // UC10
    @Test void testCategory_goalfest()     { assertEquals("goalfest",      cat("5","3","home team win","0")); }
    @Test void testCategory_highScoring()  { assertEquals("high-scoring",  cat("2","1","home team win","0")); }
    @Test void testCategory_goallessDraw() { assertEquals("goalless draw", cat("0","0","draw","0")); }
    @Test void testCategory_tightContest() { assertEquals("tight contest", cat("1","0","home team win","0")); }
    @Test void testCategory_lowScoring()   { assertEquals("low-scoring",   cat("1","1","draw","0")); }
    @Test void testCategory_penalty()      { assertEquals("penalty decider", cat("1","1","draw","1")); }

    // CleaningUtils
    @Test void testIsMissing() {
        assertTrue(CleaningUtils.isMissing(null));
        assertTrue(CleaningUtils.isMissing(""));
        assertTrue(CleaningUtils.isMissing("N/A"));
        assertTrue(CleaningUtils.isMissing("null"));
        assertTrue(CleaningUtils.isMissing("???"));
        assertFalse(CleaningUtils.isMissing("ARG"));
        assertFalse(CleaningUtils.isMissing("0"));
    }

    @Test void testParseBoolean() {
        assertEquals(1,  CleaningUtils.parseBoolean("1"));
        assertEquals(1,  CleaningUtils.parseBoolean("true"));
        assertEquals(1,  CleaningUtils.parseBoolean("Yes"));
        assertEquals(0,  CleaningUtils.parseBoolean("0"));
        assertEquals(0,  CleaningUtils.parseBoolean("false"));
        assertEquals(-1, CleaningUtils.parseBoolean("N/A"));
        assertEquals(-1, CleaningUtils.parseBoolean(null));
    }

    // --- helpers ---

    private void run(MatchRecord r) {
        new DataCleaner().fixNumericFields(List.of(r));
        new DataCleaner().standardizeDates(List.of(r));
        new DataTransformer().normalizeResultAndStage(List.of(r));
    }

    private String cat(String h, String a, String result, String penalty) {
        MatchRecord r = make("M-CAT", h, a, "2022-11-22", result, "ARG", "FRA");
        r.setPenaltyShootout(penalty);
        new DataCleaner().fixNumericFields(List.of(r));
        new DataCleaner().standardizeDates(List.of(r));
        new DataTransformer().normalizeResultAndStage(List.of(r));
        new DataTransformer().computeDerivedFields(List.of(r));
        new RecordValidator().flagInvalidRecords(List.of(r));
        new RecordValidator().categorizeMatches(List.of(r));
        return r.getMatchCategory();
    }
}
