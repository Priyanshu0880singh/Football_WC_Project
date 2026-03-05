package com.fifa.aggregator;

import com.fifa.model.MatchRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class MatchAggregator {

    private static final Logger log = LoggerFactory.getLogger(MatchAggregator.class);

    // Total goals per tournament
    public Map<String, Integer> totalGoalsByTournament(List<MatchRecord> records) {
        return records.stream()
            .filter(r -> !r.isFlaggedInvalid() && r.getTournamentName() != null)
            .collect(Collectors.groupingBy(
                MatchRecord::getTournamentName,
                Collectors.summingInt(MatchRecord::getTotalGoals)
            ));
    }

    // Match count per stage
    public Map<String, Long> matchCountByStage(List<MatchRecord> records) {
        return records.stream()
            .filter(r -> !r.isFlaggedInvalid() && r.getNormalizedStageName() != null)
            .collect(Collectors.groupingBy(
                MatchRecord::getNormalizedStageName,
                Collectors.counting()
            ));
    }

    // Result distribution
    public Map<String, Long> resultDistribution(List<MatchRecord> records) {
        return records.stream()
            .filter(r -> !r.isFlaggedInvalid() && r.getNormalizedResult() != null)
            .collect(Collectors.groupingBy(
                MatchRecord::getNormalizedResult,
                Collectors.counting()
            ));
    }

    // Average goals per era
    public Map<String, Double> avgGoalsByEra(List<MatchRecord> records) {
        return records.stream()
            .filter(r -> !r.isFlaggedInvalid() && r.getTournamentEra() != null)
            .collect(Collectors.groupingBy(
                MatchRecord::getTournamentEra,
                Collectors.averagingInt(MatchRecord::getTotalGoals)
            ));
    }

    // Match category count
    public Map<String, Long> matchCategoryDistribution(List<MatchRecord> records) {
        return records.stream()
            .filter(r -> r.getMatchCategory() != null && !r.getMatchCategory().equals("INVALID"))
            .collect(Collectors.groupingBy(
                MatchRecord::getMatchCategory,
                Collectors.counting()
            ));
    }

    // Top N scoring matches
    public List<MatchRecord> topScoringMatches(List<MatchRecord> records, int n) {
        return records.stream()
            .filter(r -> !r.isFlaggedInvalid())
            .sorted(Comparator.comparingInt(MatchRecord::getTotalGoals).reversed())
            .limit(n)
            .collect(Collectors.toList());
    }

    // Print full summary
    public void printSummary(List<MatchRecord> records) {
        log.info("=== PIPELINE SUMMARY ===");
        log.info("Total: {} | Valid: {} | Invalid: {}",
            records.size(),
            records.stream().filter(r -> !r.isFlaggedInvalid()).count(),
            records.stream().filter(MatchRecord::isFlaggedInvalid).count());

        log.info("--- Goals by Tournament (top 5) ---");
        totalGoalsByTournament(records).entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(5)
            .forEach(e -> log.info("  {}: {}", e.getKey(), e.getValue()));

        log.info("--- Result Distribution ---");
        resultDistribution(records).forEach((k, v) -> log.info("  {}: {}", k, v));

        log.info("--- Avg Goals by Era ---");
        avgGoalsByEra(records).forEach((k, v) -> log.info("  {}: {}", k, String.format("%.2f", v)));

        log.info("--- Match Categories ---");
        matchCategoryDistribution(records).forEach((k, v) -> log.info("  {}: {}", k, v));

        log.info("--- Top 5 Scoring Matches ---");
        topScoringMatches(records, 5).forEach(r ->
            log.info("  {} | {} | {} goals", r.getCleanMatchDate(), r.getMatchName(), r.getTotalGoals()));
    }
}