global.log = console.log.bind console
global.lerr = console.error.bind console

global.conf = {}
global.conf.mysql = ({
  connectionLimit : 10
  host            : "localhost",
  user            : "atticus",
  password        : "Atticus"
  database        : 'atticus'
})