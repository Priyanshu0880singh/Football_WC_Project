package com.fifa.writer;

import com.fifa.model.MatchRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.List;

public class PostgresWriter {

    private static final Logger log = LoggerFactory.getLogger(PostgresWriter.class);

    private final String url;
    private final String user;
    private final String password;

    public PostgresWriter() {
        this.url      = System.getProperty("db.url",      "jdbc:postgresql://localhost:5432/fifa_db");
        this.user     = System.getProperty("db.user",     "postgres");
        this.password = System.getProperty("db.password", "Tiger");
    }

    public PostgresWriter(String url, String user, String password) {
        this.url = url; this.user = user; this.password = password;
    }

    public void upload(List<MatchRecord> clean, List<MatchRecord> invalid) {
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            conn.setAutoCommit(false);
            log.info("Connected to PostgreSQL: {}", url);
            createTables(conn);
            insertClean(conn, clean);
            insertInvalid(conn, invalid);
            conn.commit();
            log.info("Upload complete - clean={}, invalid={}", clean.size(), invalid.size());
        } catch (SQLException e) {
            log.error("Upload failed: {}", e.getMessage(), e);
            throw new RuntimeException("Upload failed", e);
        }
    }

    private void createTables(Connection conn) throws SQLException {
        String t1 = """
            CREATE TABLE IF NOT EXISTS fifa_matches (
                key_id VARCHAR(20), tournament_id VARCHAR(20), tournament_name VARCHAR(120),
                match_id VARCHAR(30) NOT NULL PRIMARY KEY, match_name VARCHAR(120),
                stage_name VARCHAR(60), group_name VARCHAR(20),
                group_stage SMALLINT, knockout_stage SMALLINT, replayed SMALLINT, replay SMALLINT,
                match_date DATE, match_time VARCHAR(10), stadium_id VARCHAR(20),
                stadium_name VARCHAR(120), city_name VARCHAR(60), country_name VARCHAR(60),
                home_team_id VARCHAR(20), home_team_name VARCHAR(80), home_team_code VARCHAR(5),
                away_team_id VARCHAR(20), away_team_name VARCHAR(80), away_team_code VARCHAR(5),
                score VARCHAR(15), home_team_score SMALLINT, away_team_score SMALLINT,
                home_team_score_margin SMALLINT, away_team_score_margin SMALLINT,
                extra_time SMALLINT, penalty_shootout SMALLINT, score_penalties VARCHAR(10),
                home_score_penalties SMALLINT, away_score_penalties SMALLINT,
                result VARCHAR(20), home_team_win SMALLINT, away_team_win SMALLINT, draw SMALLINT,
                total_goals SMALLINT, tournament_era VARCHAR(20), match_category VARCHAR(30),
                inserted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )""";
        String t2 = """
            CREATE TABLE IF NOT EXISTS fifa_invalid_records (
                id SERIAL PRIMARY KEY, key_id VARCHAR(20), match_id VARCHAR(30),
                match_name VARCHAR(120), raw_date VARCHAR(30), raw_home_score VARCHAR(20),
                raw_away_score VARCHAR(20), raw_result VARCHAR(30), invalid_reason TEXT,
                inserted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )""";
        try (Statement st = conn.createStatement()) {
            st.execute(t1); st.execute(t2);
            log.info("Tables verified/created");
        }
    }

    private void insertClean(Connection conn, List<MatchRecord> records) throws SQLException {
        String sql = """
            INSERT INTO fifa_matches (
                key_id,tournament_id,tournament_name,match_id,match_name,stage_name,group_name,
                group_stage,knockout_stage,replayed,replay,match_date,match_time,stadium_id,
                stadium_name,city_name,country_name,home_team_id,home_team_name,home_team_code,
                away_team_id,away_team_name,away_team_code,score,home_team_score,away_team_score,
                home_team_score_margin,away_team_score_margin,extra_time,penalty_shootout,
                score_penalties,home_score_penalties,away_score_penalties,result,
                home_team_win,away_team_win,draw,total_goals,tournament_era,match_category
            ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
            ON CONFLICT (match_id) DO UPDATE SET
                home_team_score=EXCLUDED.home_team_score,
                away_team_score=EXCLUDED.away_team_score,
                result=EXCLUDED.result,
                total_goals=EXCLUDED.total_goals,
                match_category=EXCLUDED.match_category,
                inserted_at=CURRENT_TIMESTAMP""";

        int done = 0;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (MatchRecord r : records) {
                ps.setString(1,  r.getKeyId());
                ps.setString(2,  r.getTournamentId());
                ps.setString(3,  r.getTournamentName());
                ps.setString(4,  r.getMatchId());
                ps.setString(5,  r.getMatchName());
                ps.setString(6,  r.getNormalizedStageName());
                ps.setString(7,  r.getGroupName());
                ps.setObject(8,  toInt(r.getGroupStage()),    Types.SMALLINT);
                ps.setObject(9,  toInt(r.getKnockoutStage()), Types.SMALLINT);
                ps.setObject(10, toInt(r.getReplayed()),      Types.SMALLINT);
                ps.setObject(11, toInt(r.getReplay()),        Types.SMALLINT);
                ps.setDate  (12, r.getCleanMatchDate() != null ? Date.valueOf(r.getCleanMatchDate()) : null);
                ps.setString(13, r.getMatchTime());
                ps.setString(14, r.getStadiumId());
                ps.setString(15, r.getStadiumName());
                ps.setString(16, r.getCityName());
                ps.setString(17, r.getCountryName());
                ps.setString(18, r.getHomeTeamId());
                ps.setString(19, r.getHomeTeamName());
                ps.setString(20, r.getHomeTeamCode());
                ps.setString(21, r.getAwayTeamId());
                ps.setString(22, r.getAwayTeamName());
                ps.setString(23, r.getAwayTeamCode());
                ps.setString(24, r.getScore());
                ps.setObject(25, r.getCleanHomeScore(),       Types.SMALLINT);
                ps.setObject(26, r.getCleanAwayScore(),       Types.SMALLINT);
                ps.setObject(27, toInt(r.getHomeTeamScoreMargin()), Types.SMALLINT);
                ps.setObject(28, toInt(r.getAwayTeamScoreMargin()), Types.SMALLINT);
                ps.setObject(29, toBool(r.getExtraTime()),    Types.SMALLINT);
                ps.setObject(30, toBool(r.getPenaltyShootout()), Types.SMALLINT);
                ps.setString(31, r.getScorePenalties());
                ps.setObject(32, toInt(r.getHomeTeamScorePenalties()), Types.SMALLINT);
                ps.setObject(33, toInt(r.getAwayTeamScorePenalties()), Types.SMALLINT);
                ps.setString(34, r.getNormalizedResult());
                ps.setObject(35, toBool(r.getHomeTeamWin()), Types.SMALLINT);
                ps.setObject(36, toBool(r.getAwayTeamWin()), Types.SMALLINT);
                ps.setObject(37, toBool(r.getDraw()),        Types.SMALLINT);
                ps.setInt   (38, r.getTotalGoals());
                ps.setString(39, r.getTournamentEra());
                ps.setString(40, r.getMatchCategory());
                ps.addBatch();
                if (++done % 100 == 0) ps.executeBatch();
            }
            ps.executeBatch();
        }
        log.info("Clean records inserted: {}", done);
    }

    private void insertInvalid(Connection conn, List<MatchRecord> records) throws SQLException {
        String sql = "INSERT INTO fifa_invalid_records (key_id,match_id,match_name,raw_date,raw_home_score,raw_away_score,raw_result,invalid_reason) VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (MatchRecord r : records) {
                ps.setString(1, r.getKeyId());    ps.setString(2, r.getMatchId());
                ps.setString(3, r.getMatchName()); ps.setString(4, r.getMatchDate());
                ps.setString(5, r.getHomeTeamScore()); ps.setString(6, r.getAwayTeamScore());
                ps.setString(7, r.getResult());    ps.setString(8, r.getInvalidReason());
                ps.addBatch();
            }
            ps.executeBatch();
        }
        log.info("Invalid records quarantined: {}", records.size());
    }

    private Integer toInt(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Integer.parseInt(s.trim()); } catch (NumberFormatException e) { return null; }
    }

    private Integer toBool(String s) {
        if (s == null || s.isBlank()) return null;
        String t = s.trim().toLowerCase();
        if (t.equals("1")||t.equals("true")||t.equals("yes")||t.equals("1.0")) return 1;
        if (t.equals("0")||t.equals("false")||t.equals("no")||t.equals("0.0")) return 0;
        return null;
    }
}