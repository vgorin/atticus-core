CREATE TABLE user_role(
  id            TINYINT UNSIGNED NOT NULL PRIMARY KEY,
  name          VARCHAR(16) NOT NULL
);
CREATE TABLE country (
  code          CHAR(2) NOT NULL PRIMARY KEY,
  name          VARCHAR(128) NOT NULL
);
CREATE TABLE language (
  code          CHAR(3) NOT NULL PRIMARY KEY,
  name          VARCHAR(128) NOT NULL
);
CREATE TABLE timezone (
  tz            VARCHAR(64) NOT NULL PRIMARY KEY,
  offset        INTEGER NOT NULL,
  name          VARCHAR(128) NOT NULL
);
CREATE TABLE user_acc (
  id            INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  role_id       TINYINT UNSIGNED NOT NULL DEFAULT 1,
  email         VARCHAR(64) NOT NULL UNIQUE KEY,
  username      VARCHAR(16) UNIQUE KEY,
  password      BINARY(64) NOT NULL,
  legal_name    VARCHAR(256),
  language_code CHAR(3),
  country_code  CHAR(2),
  timezone_tz   VARCHAR(64),
  deleted       DATETIME, -- null value means account is active

  rec_created   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  rec_updated   TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,

  FOREIGN KEY FK_user_account_language_code(language_code) REFERENCES language(code),
  FOREIGN KEY FK_user_account_country_code(country_code) REFERENCES country(code),
  FOREIGN KEY FK_user_account_timezone_tz(timezone_tz) REFERENCES timezone(tz),
  FOREIGN KEY FK_user_account_role_id(role_id) REFERENCES user_role(id)
);
