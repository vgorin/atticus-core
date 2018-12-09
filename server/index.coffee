require './config/local'

Q = require 'q'
Q.longStackSupport = true
express = require 'express'
bodyParser = require 'body-parser'
path = require 'path'
mysql = require 'mysql'
request = require 'request'
auth = require 'basic-auth'
pm2 = require 'pm2'

argv = require('minimist')(process.argv.slice(2))

{ port } = argv

app = express()

# cores
app.use (req,res,next)->
  res.header "Access-Control-Allow-Origin","*"
  res.header "Access-Control-Allow-Headers","Origin, X-Requested-With, Content-Type, Accept, x-access-token"
  res.header "Access-Control-Allow-Methods", "GET,POST,PUT,DELETE"
  next()

app.get /favicon\.ico/, (req, res)->
  res.end ''

app.use bodyParser.json()
app.use bodyParser.urlencoded extended: true
app.use (req, res, next)->
  { name, pass } = auth(req) or {}

  if not name or not pass
    res.statusCode = 401
    res.setHeader 'WWW-Authenticate', 'Basic realm="Yo need to authorize"'
    return res.end 'Authorisation required'

  next()

app.get '/test', (req, res, next)->
  res.end 'test ok'

#app.use '/auth',    require './routes/auth.coffee'
#app.use '/account', require './routes/account.coffee'

# catch 404 and forward to error handler
app.use (req, res, next) ->
  # do proxy
  { name, pass } = auth(req) or {}
  log req.method, req.body, req.params, req.query
  options =
    #url : "http://#{name}:#{pass}@localhost:5000" + req.url
    url : "http://#{name}:#{pass}@192.168.1.147:5000" + req.url
    method : req.method
    json : req.body
    qs : req.query
  log options
  Q.npost request, req.method.toLowerCase(), [options]
  .then (rs)->
    res.json rs[1]
  .catch next

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
  Q.npost pm2, 'connect'
  .then ->
    Q.npost pm2, 'start', [{
      "name"             : "atticus-core",
      "script"           : "start.sh",
      "cwd"              : "/home/vgrn/ac/",
      "exec_mode"        : "fork_mode",
      "merge_logs"       : true,
      "log_date_format"  : "YYYY-MM-DD HH:mm:ss Z"
    }]
  .then ->
    Q.npost pm2, 'disconnect'
  .then ->
    global.mysql_conn = mysql.createConnection global.conf.mysql
    Q.npost global.mysql_conn, 'connect'
  .then ->
    log 'MySQL connected'
  .catch (e)->
    lerr e

init()
.then ->
  server = app.listen argv.port or 5001, ->
    log "Listening on port " + server.address().port
.catch (e)->
  lerr e.stack or e

# auto-deploy
child_process = require 'child_process'
last_revision = null;
setInterval ->
  child_process.exec 'git rev-parse HEAD', (err, stdout)->
    last_revision ?= stdout
    if last_revision != stdout
      # or restart current proc
      process.exit 0
, 60*1000

