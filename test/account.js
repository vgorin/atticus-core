const Q = require('q');
const expect = require('chai').expect;
const request = require('request');

describe('account', function(){
    it('create', function(done) { // !!! do not change "function" to an arrow
        this.timeout(25000);

        let req = {
            //url  : 'http://192.168.1.236:8080/account',
            url  : 'http://localhost:28081/account',
            json : {
                account_id : 2,
                email : 'despotix7@gmail.com',
                username : 'Despotix7',
                password : 'test1',
                legal_name : 'Legal',
                language_code : 'eng',
                country_code : 'US',
                timezone : 'America/Los_Angeles'
            }
        };

        Q.npost(request, 'post', [req])
        .then((r) => {
            let [res, json] = r;
            expect(json.email).to.equal(req.json.email);
            done();
        }).catch( (err)=>{
            done(err);
        });
    });

    it('get by account id', function(done) { // !!! do not change "function" to an arrow
        let account_id = 10;
        let req = {
            url  : 'http://localhost:28081/account/'+account_id,
            json : true
        };

        Q.npost(request, 'get', [req])
        .then((r) => {
            let [res, json] = r;
            expect(json.account_id).to.equal(req.json.account_id);
            done();
        }).catch( (err)=>{
            done(err);
        });
    });

    it.only('/login', function(done) { // !!! do not change "function" to an arrow
        let account_id = 10;
        let req = {
            url  : 'http://localhost:28081/auth/login/',
            json : {
                email : 'despotix12@gmail.com',
                password : 1
            }
        };

        Q.npost(request, 'post', [req])
            .then((r) => {
                let [res, json] = r;
                expect(json.account_id).to.equal(req.json.account_id);
                done();
            }).catch( (err)=>{
            done(err);
        });
    });
});
