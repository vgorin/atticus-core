global.log = console.log.bind console
global.lerr = console.error.bind console

global.conf = {}
global.conf.mysql = ({
  connectionLimit : 10
  host            : "192.168.1.147",
  user            : "atticus",
  password        : "Atticus"
  database        : 'atticus'
})