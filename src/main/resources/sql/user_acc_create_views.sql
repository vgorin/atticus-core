CREATE VIEW user_account (
  id,
  role,
  email,
  username,
  password,
  legal_name,
  language,
  country,
  timezone,
  created,
  updated,
  deleted
) AS
SELECT
  a.id,
  r.name,
  a.email,
  a.username,
  a.password,
  a.legal_name,
  l.name,
  c.name,
  t.name,
  a.rec_created,
  a.rec_updated,
  a.deleted
FROM user_acc a
  LEFT JOIN user_role r
    ON a.role_id = r.id
  LEFT JOIN language l
    ON a.language_code = l.code
  LEFT JOIN country c
    ON a.country_code = c.code
  LEFT JOIN timezone t
    ON a.timezone_tz = t.tz;
