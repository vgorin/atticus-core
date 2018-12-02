require './../config/local'

Q       = require 'q'
express = require 'express'
router  = express.Router()
crypto  = require 'crypto'

router.post '/', (req, res, next)->
  { email, username, password, legalName, languageCode, countryCode, timezone } = req.body or {}
  salt = crypto.randomBytes 32
  Q.npost crypto, 'pbkdf2', [
    password, salt, 100000, 32, 'sha256'
  ]
  .then (bytes)->
    password_hash = Buffer.concat [ salt, bytes ]
    Q.npost global.mysql_conn, 'query', [
      "INSERT INTO user_acc(email, username, password, legal_name, language_code, country_code, timezone_tz) VALUES(?, ? ,?, ?, ?, ?, ?)",
      [ email, username, password_hash, legalName, languageCode, countryCode, timezone ]
    ]
  .then (rs_json)->
    res.json Object.assign req.body, {
      account_id: rs_json[0]?.insertId
    }
  .catch next

router.get '/:account_id', (req, res, next)->
  { account_id } = req.params or {}
  Q.npost global.mysql_conn, 'query', [
    "SELECT * FROM user_acc WHERE id = ?",
    [ account_id ]
  ]
  .then (rs_json)->
    json = rs_json[0][0]
    delete json.password
    res.json json
  .catch next

module.exports = router