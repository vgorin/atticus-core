require './config/local'

Q = require 'q'
Q.longStackSupport = true
express = require 'express'
bodyParser = require 'body-parser'
path = require 'path'
mysql = require 'mysql'
request = require 'request'

argv = require('minimist')(process.argv.slice(2))

{ port } = argv

app = express()

app.get /favicon\.ico/, (req, res)->
  res.end ''

app.use bodyParser.json()
app.use bodyParser.urlencoded extended: true

# cores
app.use (req,res,next)->
  res.header "Access-Control-Allow-Origin","*"
  res.header "Access-Control-Allow-Headers","Origin, X-Requested-With, Content-Type, Accept, x-access-token"
  res.header "Access-Control-Allow-Methods", "GET,POST,PUT,DELETE"
  next()

app.get '/test', (req, res, next)->
  res.end 'test ok'

#app.use '/auth',    require './routes/auth.coffee'
#app.use '/account', require './routes/account.coffee'

# catch 404 and forward to error handler
app.use (req, res, next) ->
  # do proxy
  log req.method, req.body, req.params, req.query
  options =
    url : 'http://1:1@localhost:5000' + req.url
    method : req.method
    json : req.body
    qs : req.query
  log options
  Q.npost request, req.method.toLowerCase(), [options]
  .then (res_json)->
    res.json res_json
  .catch next

  #err = new Error "Not Found: "+req.url
  #err.status = 404
  #next err

# error handler
app.use (err, req, res, next) ->
  lerr err
  request = Object.assign {}, req.params, req.query
  error =
    code    : err.code or -1
    message : err.message or err or 'UNKNOWN_ERROR'
  res.json { request, error }

# ===

init = ->
  global.mysql_conn = mysql.createConnection global.conf.mysql
  Q.npost global.mysql_conn, 'connect'
  .then ->
    log 'MySQL connected'

init()
.then ->
  server = app.listen argv.port or 5001, ->
    log "Listening on port " + server.address().port
.catch (e)->
  lerr e.stack or e
