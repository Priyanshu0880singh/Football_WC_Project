package com.fifa.validator;

import com.fifa.model.MatchRecord;
import com.fifa.util.CleaningUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class RecordValidator {

    private static final Logger log = LoggerFactory.getLogger(RecordValidator.class);

    // UC7 - Flag Invalid Records
    public void flagInvalidRecords(List<MatchRecord> records) {
        int flagged = 0;
        for (MatchRecord r : records) {
            List<String> reasons = new ArrayList<>();

            // R1 - matchId
            String mid = CleaningUtils.safe(r.getMatchId());
            if (CleaningUtils.isMissing(mid) || mid.equals("M-????") || mid.equals("INVALID"))
                reasons.add("R1:missing matchId");

            // R2 - team names
            if (CleaningUtils.isMissing(CleaningUtils.safe(r.getHomeTeamName())) || r.getHomeTeamName().equals("???"))
                reasons.add("R2:missing homeTeamName");
            if (CleaningUtils.isMissing(CleaningUtils.safe(r.getAwayTeamName())) || r.getAwayTeamName().equals("???"))
                reasons.add("R2:missing awayTeamName");

            // R3 - numeric scores
            if (r.getCleanHomeScore() == null) reasons.add("R3:invalid homeScore='" + r.getHomeTeamScore() + "'");
            if (r.getCleanAwayScore() == null) reasons.add("R3:invalid awayScore='" + r.getAwayTeamScore() + "'");

            // R4 - parsed date
            if (r.getCleanMatchDate() == null) reasons.add("R4:unparseable date='" + r.getMatchDate() + "'");

            // R5 - result value
            String res = r.getNormalizedResult();
            if (res != null && !res.equals("home team win") && !res.equals("away team win") && !res.equals("draw"))
                reasons.add("R5:unknown result='" + r.getResult() + "'");

            // R6 - team codes
            if (!validCode(r.getHomeTeamCode())) reasons.add("R6:invalid homeCode='" + r.getHomeTeamCode() + "'");
            if (!validCode(r.getAwayTeamCode()))  reasons.add("R6:invalid awayCode='"  + r.getAwayTeamCode()  + "'");

            if (!reasons.isEmpty()) {
                r.setFlaggedInvalid(true);
                r.setInvalidReason(String.join("; ", reasons));
                flagged++;
            }
        }
        log.info("UC7 - {} records flagged invalid out of {}", flagged, records.size());
    }

    public List<MatchRecord> filterValid(List<MatchRecord> records) {
        return records.stream().filter(r -> !r.isFlaggedInvalid()).collect(Collectors.toList());
    }

    public List<MatchRecord> filterInvalid(List<MatchRecord> records) {
        return records.stream().filter(MatchRecord::isFlaggedInvalid).collect(Collectors.toList());
    }

    // UC10 - Categorize Matches
    public void categorizeMatches(List<MatchRecord> records) {
        int done = 0;
        for (MatchRecord r : records) {
            if (r.isFlaggedInvalid()) { r.setMatchCategory("INVALID"); continue; }

            if (CleaningUtils.parseBoolean(r.getPenaltyShootout()) == 1) {
                r.setMatchCategory("penalty decider");
            } else {
                int g = r.getTotalGoals();
                String res = r.getNormalizedResult();
                if      (g >= 5)                           r.setMatchCategory("goalfest");
                else if (g >= 3)                           r.setMatchCategory("high-scoring");
                else if (g == 0 && "draw".equals(res))    r.setMatchCategory("goalless draw");
                else if (g <= 1)                           r.setMatchCategory("tight contest");
                else                                       r.setMatchCategory("low-scoring");
            }
            done++;
        }
        log.info("UC10 - {} records categorized", done);
    }

    private boolean validCode(String code) {
        if (CleaningUtils.isMissing(code)) return false;
        return code.matches("[A-Z]{2,4}");
    }
}