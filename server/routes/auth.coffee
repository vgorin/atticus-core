require './../config/local'

Q       = require 'q'
express = require 'express'
router  = express.Router()
crypto  = require 'crypto'

router.use '/login', (req, res, next)->
  { email, password } = req.query or req.body or {}
  log req.query, req.body, { email, password }
  Q.npost global.mysql_conn, 'query', [
    "SELECT * FROM user_acc WHERE email = ?",
    [ email ]
  ]
  .then (rs_json)->
    # todo - get slat
    return res.send 'todo'
#    json = rs_json[0][0]
#    Q.npost crypto, 'pbkdf2', [
#      password, salt, 100000, 32, 'sha256'
#    ]
#    res.json json
  .catch next

router.post '/logout', (req, res, next)->
  { email, password } = req.body or {}


module.exports = router