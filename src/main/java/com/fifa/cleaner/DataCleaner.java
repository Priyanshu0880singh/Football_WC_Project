package com.fifa.cleaner;

import com.fifa.model.MatchRecord;
import com.fifa.util.CleaningUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class DataCleaner {

    private static final Logger log = LoggerFactory.getLogger(DataCleaner.class);

    private static final List<DateTimeFormatter> DATE_PARSERS = List.of(
        DateTimeFormatter.ofPattern("M/d/yyyy"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("dd-MM-yyyy"),
        DateTimeFormatter.ofPattern("MM-dd-yyyy"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd"),
        DateTimeFormatter.ofPattern("dd.MM.yyyy"),
        DateTimeFormatter.ofPattern("yyyyMMdd"),
        DateTimeFormatter.ofPattern("d/M/yyyy"),
        DateTimeFormatter.ofPattern("M/dd/yyyy")
    );

    private static final DateTimeFormatter OUTPUT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // UC1 - Remove Duplicates
    public List<MatchRecord> deduplicateByMatchId(List<MatchRecord> records) {
        Map<String, MatchRecord> seen = new LinkedHashMap<>();
        int dupes = 0;
        for (MatchRecord r : records) {
            String key = CleaningUtils.safe(r.getMatchId());
            if (key.isEmpty()) key = CleaningUtils.safe(r.getKeyId());
            if (key.isEmpty()) { seen.put(UUID.randomUUID().toString(), r); continue; }
            if (seen.containsKey(key)) { dupes++; }
            else { seen.put(key, r); }
        }
        log.info("UC1 - {} duplicates removed, {} unique records kept", dupes, seen.size());
        return new ArrayList<>(seen.values());
    }

    // UC2 - Normalize Names
    public void normalizeNames(List<MatchRecord> records) {
        int count = 0;
        for (MatchRecord r : records) {
            boolean changed = false;
            String tn = CleaningUtils.toTitleCase(r.getTournamentName());
            if (!tn.equals(r.getTournamentName())) { r.setTournamentName(tn); changed = true; }
            String hn = CleaningUtils.toTitleCase(r.getHomeTeamName());
            if (!hn.equals(r.getHomeTeamName())) { r.setHomeTeamName(hn); changed = true; }
            String an = CleaningUtils.toTitleCase(r.getAwayTeamName());
            if (!an.equals(r.getAwayTeamName())) { r.setAwayTeamName(an); changed = true; }
            String sn = CleaningUtils.toTitleCase(r.getStadiumName());
            if (!sn.equals(r.getStadiumName())) { r.setStadiumName(sn); changed = true; }
            String cn = CleaningUtils.toTitleCase(r.getCityName());
            if (!cn.equals(r.getCityName())) { r.setCityName(cn); changed = true; }
            String co = CleaningUtils.toTitleCase(r.getCountryName());
            if (!co.equals(r.getCountryName())) { r.setCountryName(co); changed = true; }
            String mn = CleaningUtils.safe(r.getMatchName());
            if (!mn.equals(r.getMatchName())) { r.setMatchName(mn); changed = true; }
            if (changed) count++;
        }
        log.info("UC2 - {} records name-normalized", count);
    }

    // UC3 - Fix Numeric Fields
    public void fixNumericFields(List<MatchRecord> records) {
        int ok = 0, bad = 0;
        for (MatchRecord r : records) {
            Integer h = CleaningUtils.parseScore(r.getHomeTeamScore());
            Integer a = CleaningUtils.parseScore(r.getAwayTeamScore());
            if (h != null) { r.setCleanHomeScore(h); ok++; } else bad++;
            if (a != null) { r.setCleanAwayScore(a); }
        }
        log.info("UC3 - {} valid scores parsed, {} invalid home scores", ok, bad);
    }

    // UC4 - Standardize Dates
    public void standardizeDates(List<MatchRecord> records) {
        int ok = 0, fail = 0;
        for (MatchRecord r : records) {
            String raw = CleaningUtils.safe(r.getMatchDate());
            if (CleaningUtils.isMissing(raw)) { fail++; continue; }
            LocalDate d = tryParse(raw);
            if (d != null) { r.setCleanMatchDate(d.format(OUTPUT)); ok++; }
            else { log.warn("UC4 - Cannot parse date '{}' matchId={}", raw, r.getMatchId()); fail++; }
        }
        log.info("UC4 - {} dates parsed OK, {} failed", ok, fail);
    }

    private LocalDate tryParse(String s) {
        for (DateTimeFormatter f : DATE_PARSERS) {
            try { return LocalDate.parse(s.trim(), f); }
            catch (DateTimeParseException ignored) {}
        }
        return null;
    }
}