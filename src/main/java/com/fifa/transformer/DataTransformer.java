package com.fifa.transformer;

import com.fifa.model.MatchRecord;
import com.fifa.util.CleaningUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.*;

public class DataTransformer {

    private static final Logger log = LoggerFactory.getLogger(DataTransformer.class);
    private static final Pattern YEAR = Pattern.compile("(\\d{4})");

    // UC5 - Map Codes
    public void mapTeamCodes(List<MatchRecord> records) {
        int count = 0;
        for (MatchRecord r : records) {
            String h = normalizeCode(r.getHomeTeamCode());
            String a = normalizeCode(r.getAwayTeamCode());
            if (!h.equals(r.getHomeTeamCode()) || !a.equals(r.getAwayTeamCode())) count++;
            r.setHomeTeamCode(h);
            r.setAwayTeamCode(a);
        }
        log.info("UC5 - {} records had team codes updated", count);
    }

    // UC6 - Normalize Result and Stage
    public void normalizeResultAndStage(List<MatchRecord> records) {

        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("home team win", "home team win");
        resultMap.put("home win",      "home team win");
        resultMap.put("home",          "home team win");
        resultMap.put("h",             "home team win");
        resultMap.put("1",             "home team win");
        resultMap.put("away team win", "away team win");
        resultMap.put("away win",      "away team win");
        resultMap.put("away",          "away team win");
        resultMap.put("a",             "away team win");
        resultMap.put("2",             "away team win");
        resultMap.put("draw",          "draw");
        resultMap.put("tie",           "draw");
        resultMap.put("d",             "draw");
        resultMap.put("0",             "draw");

        Map<String, String> stageMap = new HashMap<>();
        stageMap.put("group stage",          "group stage");
        stageMap.put("group_stage",          "group stage");
        stageMap.put("grp stage",            "group stage");
        stageMap.put("second group stage",   "second group stage");
        stageMap.put("final round",          "final round");
        stageMap.put("round of 16",          "round of 16");
        stageMap.put("round-of-16",          "round of 16");
        stageMap.put("r16",                  "round of 16");
        stageMap.put("quarter-finals",       "quarter-finals");
        stageMap.put("quarter finals",       "quarter-finals");
        stageMap.put("qf",                   "quarter-finals");
        stageMap.put("semi-finals",          "semi-finals");
        stageMap.put("semi finals",          "semi-finals");
        stageMap.put("sf",                   "semi-finals");
        stageMap.put("final",                "final");
        stageMap.put("the final",            "final");
        stageMap.put("third-place match",    "third-place match");
        stageMap.put("3rd place",            "third-place match");
        stageMap.put("third place match",    "third-place match");

        int rf = 0, su = 0;
        for (MatchRecord r : records) {
            String res = CleaningUtils.safe(r.getResult()).toLowerCase();
            String mapped = resultMap.get(res);
            if (mapped != null) { r.setNormalizedResult(mapped); if (!mapped.equals(res)) rf++; }
            else { r.setNormalizedResult(res); if (!CleaningUtils.isMissing(res)) { su++; log.warn("UC6 - Unknown result '{}' matchId={}", r.getResult(), r.getMatchId()); } }

            String stage = CleaningUtils.safe(r.getStageName()).toLowerCase();
            String ms = stageMap.get(stage);
            r.setNormalizedStageName(ms != null ? ms : stage);
        }
        log.info("UC6 - {} results fixed, {} unrecognised", rf, su);
    }

    // UC8 - Derived Fields
    public void computeDerivedFields(List<MatchRecord> records) {
        int ok = 0, skip = 0;
        for (MatchRecord r : records) {
            if (r.getCleanHomeScore() != null && r.getCleanAwayScore() != null) {
                r.setTotalGoals(r.getCleanHomeScore() + r.getCleanAwayScore());
                ok++;
            } else { skip++; }
            r.setTournamentEra(deriveEra(r.getTournamentName()));
        }
        log.info("UC8 - totalGoals computed for {} records, {} skipped", ok, skip);
    }

    private String normalizeCode(String code) {
        if (CleaningUtils.isMissing(code)) return "";
        return code.trim().toUpperCase().replaceAll("\\s+", "");
    }

    private String deriveEra(String name) {
        if (CleaningUtils.isMissing(name)) return "Unknown";
        Matcher m = YEAR.matcher(name);
        if (m.find()) {
            int y = Integer.parseInt(m.group(1));
            if (y <= 1970) return "Classic";
            if (y <= 1998) return "Modern";
            return "Contemporary";
        }
        return "Unknown";
    }
}