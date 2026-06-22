CREATE TABLE IF NOT EXISTS matches (
    gameId INTEGER,
    nickname VARCHAR(20),
    score INTEGER NOT NULL,
    nPlayers INTEGER NOT NULL,
    matchDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (gameId, nickname)
);

CREATE OR REPLACE VIEW last_id AS 
    SELECT DISTINCT gameId
    FROM matches
    WHERE gameId = (SELECT MAX(gameId) FROM matches);


CREATE OR REPLACE VIEW totals AS 
    SELECT nickname, nPlayers, SUM(score) as num
    FROM matches
    GROUP BY nickname, nPlayers;

