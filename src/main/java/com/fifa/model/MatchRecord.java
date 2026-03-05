package com.fifa.model;

public class MatchRecord {

    // --- RAW fields (String, exactly as read from CSV) ---
    private String keyId;
    private String tournamentId;
    private String tournamentName;
    private String matchId;
    private String matchName;
    private String stageName;
    private String groupName;
    private String groupStage;
    private String knockoutStage;
    private String replayed;
    private String replay;
    private String matchDate;
    private String matchTime;
    private String stadiumId;
    private String stadiumName;
    private String cityName;
    private String countryName;
    private String homeTeamId;
    private String homeTeamName;
    private String homeTeamCode;
    private String awayTeamId;
    private String awayTeamName;
    private String awayTeamCode;
    private String score;
    private String homeTeamScore;
    private String awayTeamScore;
    private String homeTeamScoreMargin;
    private String awayTeamScoreMargin;
    private String extraTime;
    private String penaltyShootout;
    private String scorePenalties;
    private String homeTeamScorePenalties;
    private String awayTeamScorePenalties;
    private String result;
    private String homeTeamWin;
    private String awayTeamWin;
    private String draw;

    // --- DERIVED / CLEANED fields (set by cleaner and transformer) ---
    private String  cleanMatchDate;        // yyyy-MM-dd
    private Integer cleanHomeScore;        // null if invalid
    private Integer cleanAwayScore;        // null if invalid
    private String  normalizedResult;      // "home team win" | "away team win" | "draw"
    private String  normalizedStageName;   // canonical lowercase
    private int     totalGoals;            // cleanHomeScore + cleanAwayScore
    private String  tournamentEra;         // "Classic" | "Modern" | "Contemporary"
    private String  matchCategory;         // "goalfest" | "high-scoring" | etc.

    // --- VALIDATION flags (set by validator) ---
    private boolean flaggedInvalid;
    private String  invalidReason;

    // --- Getters and Setters ---

    public String getKeyId() { return keyId; }
    public void setKeyId(String keyId) { this.keyId = keyId; }

    public String getTournamentId() { return tournamentId; }
    public void setTournamentId(String tournamentId) { this.tournamentId = tournamentId; }

    public String getTournamentName() { return tournamentName; }
    public void setTournamentName(String tournamentName) { this.tournamentName = tournamentName; }

    public String getMatchId() { return matchId; }
    public void setMatchId(String matchId) { this.matchId = matchId; }

    public String getMatchName() { return matchName; }
    public void setMatchName(String matchName) { this.matchName = matchName; }

    public String getStageName() { return stageName; }
    public void setStageName(String stageName) { this.stageName = stageName; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getGroupStage() { return groupStage; }
    public void setGroupStage(String groupStage) { this.groupStage = groupStage; }

    public String getKnockoutStage() { return knockoutStage; }
    public void setKnockoutStage(String knockoutStage) { this.knockoutStage = knockoutStage; }

    public String getReplayed() { return replayed; }
    public void setReplayed(String replayed) { this.replayed = replayed; }

    public String getReplay() { return replay; }
    public void setReplay(String replay) { this.replay = replay; }

    public String getMatchDate() { return matchDate; }
    public void setMatchDate(String matchDate) { this.matchDate = matchDate; }

    public String getMatchTime() { return matchTime; }
    public void setMatchTime(String matchTime) { this.matchTime = matchTime; }

    public String getStadiumId() { return stadiumId; }
    public void setStadiumId(String stadiumId) { this.stadiumId = stadiumId; }

    public String getStadiumName() { return stadiumName; }
    public void setStadiumName(String stadiumName) { this.stadiumName = stadiumName; }

    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }

    public String getCountryName() { return countryName; }
    public void setCountryName(String countryName) { this.countryName = countryName; }

    public String getHomeTeamId() { return homeTeamId; }
    public void setHomeTeamId(String homeTeamId) { this.homeTeamId = homeTeamId; }

    public String getHomeTeamName() { return homeTeamName; }
    public void setHomeTeamName(String homeTeamName) { this.homeTeamName = homeTeamName; }

    public String getHomeTeamCode() { return homeTeamCode; }
    public void setHomeTeamCode(String homeTeamCode) { this.homeTeamCode = homeTeamCode; }

    public String getAwayTeamId() { return awayTeamId; }
    public void setAwayTeamId(String awayTeamId) { this.awayTeamId = awayTeamId; }

    public String getAwayTeamName() { return awayTeamName; }
    public void setAwayTeamName(String awayTeamName) { this.awayTeamName = awayTeamName; }

    public String getAwayTeamCode() { return awayTeamCode; }
    public void setAwayTeamCode(String awayTeamCode) { this.awayTeamCode = awayTeamCode; }

    public String getScore() { return score; }
    public void setScore(String score) { this.score = score; }

    public String getHomeTeamScore() { return homeTeamScore; }
    public void setHomeTeamScore(String homeTeamScore) { this.homeTeamScore = homeTeamScore; }

    public String getAwayTeamScore() { return awayTeamScore; }
    public void setAwayTeamScore(String awayTeamScore) { this.awayTeamScore = awayTeamScore; }

    public String getHomeTeamScoreMargin() { return homeTeamScoreMargin; }
    public void setHomeTeamScoreMargin(String v) { this.homeTeamScoreMargin = v; }

    public String getAwayTeamScoreMargin() { return awayTeamScoreMargin; }
    public void setAwayTeamScoreMargin(String v) { this.awayTeamScoreMargin = v; }

    public String getExtraTime() { return extraTime; }
    public void setExtraTime(String extraTime) { this.extraTime = extraTime; }

    public String getPenaltyShootout() { return penaltyShootout; }
    public void setPenaltyShootout(String penaltyShootout) { this.penaltyShootout = penaltyShootout; }

    public String getScorePenalties() { return scorePenalties; }
    public void setScorePenalties(String scorePenalties) { this.scorePenalties = scorePenalties; }

    public String getHomeTeamScorePenalties() { return homeTeamScorePenalties; }
    public void setHomeTeamScorePenalties(String v) { this.homeTeamScorePenalties = v; }

    public String getAwayTeamScorePenalties() { return awayTeamScorePenalties; }
    public void setAwayTeamScorePenalties(String v) { this.awayTeamScorePenalties = v; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getHomeTeamWin() { return homeTeamWin; }
    public void setHomeTeamWin(String homeTeamWin) { this.homeTeamWin = homeTeamWin; }

    public String getAwayTeamWin() { return awayTeamWin; }
    public void setAwayTeamWin(String awayTeamWin) { this.awayTeamWin = awayTeamWin; }

    public String getDraw() { return draw; }
    public void setDraw(String draw) { this.draw = draw; }

    public String getCleanMatchDate() { return cleanMatchDate; }
    public void setCleanMatchDate(String cleanMatchDate) { this.cleanMatchDate = cleanMatchDate; }

    public Integer getCleanHomeScore() { return cleanHomeScore; }
    public void setCleanHomeScore(Integer cleanHomeScore) { this.cleanHomeScore = cleanHomeScore; }

    public Integer getCleanAwayScore() { return cleanAwayScore; }
    public void setCleanAwayScore(Integer cleanAwayScore) { this.cleanAwayScore = cleanAwayScore; }

    public String getNormalizedResult() { return normalizedResult; }
    public void setNormalizedResult(String normalizedResult) { this.normalizedResult = normalizedResult; }

    public String getNormalizedStageName() { return normalizedStageName; }
    public void setNormalizedStageName(String normalizedStageName) { this.normalizedStageName = normalizedStageName; }

    public int getTotalGoals() { return totalGoals; }
    public void setTotalGoals(int totalGoals) { this.totalGoals = totalGoals; }

    public String getTournamentEra() { return tournamentEra; }
    public void setTournamentEra(String tournamentEra) { this.tournamentEra = tournamentEra; }

    public String getMatchCategory() { return matchCategory; }
    public void setMatchCategory(String matchCategory) { this.matchCategory = matchCategory; }

    public boolean isFlaggedInvalid() { return flaggedInvalid; }
    public void setFlaggedInvalid(boolean flaggedInvalid) { this.flaggedInvalid = flaggedInvalid; }

    public String getInvalidReason() { return invalidReason; }
    public void setInvalidReason(String invalidReason) { this.invalidReason = invalidReason; }

    @Override
    public String toString() {
        return "MatchRecord{matchId='" + matchId + "', matchName='" + matchName +
               "', date='" + matchDate + "', score='" + score + "', flagged=" + flaggedInvalid + "}";
    }
}