require './../config/local'

Q       = require 'q'
express = require 'express'
router  = express.Router()

router.post '/', (req, res, next)->
  { email, username, password, legalName, languageCode, countryCode, timezone } = req.body
  Q.npost global.mysql_conn, 'query', [
    "INSERT INTO user_acc(email, username, password, legal_name, language_code, country_code, timezone_tz) VALUES(?, ? ,?, ?, ?, ?, ?)",
    [ email, username, password, legalName, languageCode, countryCode, timezone ]
  ]
  .then (rs_json)->
    res.json Object.assign req.body, {
      account_id: rs_json[0]?.insertId
    }
  .catch next

module.exports = router