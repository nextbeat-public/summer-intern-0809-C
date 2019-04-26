
-- ユーザ定義
--------------
CREATE TABLE user (
  id         INT          NOT     NULL AUTO_INCREMENT PRIMARY KEY,
  name_first VARCHAR(255) NOT     NULL,
  name_last  VARCHAR(255) NOT     NULL,
  phone      VARCHAR(16)  DEFAULT NULL,
  email      VARCHAR(255) DEFAULT NULL,
  pref       VARCHAR(8)   DEFAULT NULL,
  address    VARCHAR(255) DEFAULT NULL,
  updated_at timestamp    NOT     NULL DEFAULT CURRENT_TIMESTAMP,
  created_at timestamp    NOT     NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ユーザ・セッション
----------------------
CREATE TABLE user_session (
  id         INT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
  session_id VARCHAR(64) NOT NULL,
  exprity    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `ukey01` (`session_id`)
) ENGINE=InnoDB;
