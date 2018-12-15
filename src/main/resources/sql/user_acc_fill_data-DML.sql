-- define some user roles
INSERT INTO user_role(id, name) VALUES(1, 'user');
INSERT INTO user_role(id, name) VALUES(255, 'admin');

-- TODO: insert more countries
INSERT INTO country(code, name)
VALUES('US', 'United States');

-- TODO: insert more languages
INSERT INTO language(code, name)
VALUES('eng', 'English');

-- TODO: insert all timezones
INSERT INTO timezone(tz, offset, name)
VALUES('America/Los_Angeles', -28800000, 'Pacific Standard Time');

-- insert test users
INSERT INTO user_acc(email, username, password, legal_name, language_code, country_code, timezone_tz)
VALUES('1', '1', 0xD207812475E4A5D240311ACE8C8CE0FBFAC068F5F66A4BCE61C80F410D1B89A5B9DB923A64214C3B54EF297D1F9677CFFB618E364B3CADEE1C429B0BFC03EB39, 'Mr. One', 'eng', 'US', 'America/Los_Angeles');
INSERT INTO user_acc(email, username, password, legal_name, language_code, country_code, timezone_tz)
VALUES('2', '2', 0x88DC6AE462EFA901B3EE2A01608B603D8B22B1A4A99C80E3186FF8F98B660DEF032A97E41A6A66E1D3D5E137464F8C9B8CE2B0D542D785A53410AD4916A34862, 'Mrs. Two', 'eng', 'US', 'America/Los_Angeles');
INSERT INTO user_acc(email, username, password, legal_name, language_code, country_code, timezone_tz)
VALUES('3', '3', 0x5AEE418ECE5D3AF090289470D4E217173C89566A4FA871E8E50FB588BFCBFA4DBAFA6AA1D3DFB33A98136DFC865BCAD994585593A2FA248C76126ACA4909518B, 'Ms. Three', 'eng', 'US', 'America/Los_Angeles');
INSERT INTO user_acc(email, username, password, legal_name, language_code, country_code, timezone_tz)
VALUES('4', '4', 0xA7825BA5CE78D340C0CEBC54927F2B6ED3CBE9A5DE5ADD421A1E93CFDBAA2DC0FDA1CCB82F6663C19B183362B192834B6B298507A49FB3B0D06DD427EE7C47BD, 'Dr. Four', 'eng', 'US', 'America/Los_Angeles');
INSERT INTO user_acc(email, username, password, legal_name, language_code, country_code, timezone_tz)
VALUES('5', '5', 0x19D7E05D1E14AAF1F906B58CCB503163EF198A3443DCED52A1BC34F27FF513D7C090BBCA0F13C408B5E9313FB2A21B45272E5C97BBAECECEF1DB5666517F1B44, 'Dj. Five', 'eng', 'US', 'America/Los_Angeles');
INSERT INTO user_acc(email, username, password, legal_name, language_code, country_code, timezone_tz)
VALUES('sam@atticus.info', 'SamREye', 0xED15D120D9E64E35BBE9E038407F1A8A7FE138E46FD1417ED3EC1D99EFB639683218DB0BB34D6C5B2ADFD6D1D8B08C590001FD9142E042BB629694649316A2CD, 'Samuel Bourque', 'eng', 'US', 'America/Los_Angeles');
INSERT INTO user_acc(email, username, password, legal_name, language_code, country_code, timezone_tz)
VALUES('troy@atticus.info', 'Troy', 0xA045452CFF23029B87734613310A14758A568834E1BF7D8D00F1DEC41E1E39437ADDC92E31639ADD430C173A0A6C9D5C7ECECED58F6841FFBFE0763AC472B5E2, 'Troy Martz', 'eng', 'US', 'America/Los_Angeles');
INSERT INTO user_acc(email, username, password, legal_name, language_code, country_code, timezone_tz)
VALUES('craig@atticus.info', 'Craig', 0xA045452CFF23029B87734613310A14758A568834E1BF7D8D00F1DEC41E1E39437ADDC92E31639ADD430C173A0A6C9D5C7ECECED58F6841FFBFE0763AC472B5E2, 'Craig Wright', 'eng', 'US', 'America/Los_Angeles');

-- insert admin account
INSERT INTO user_acc(role_id, email, username, password, language_code, country_code, timezone_tz)
VALUES(255, 'admin@atticus.com', 'admin', 0x457DCC63CA48F642E567919F67BF53BF2F01B982978CFE3B39522ED25B84A2E1ABDB800855DA3B5F74228EA05A81593BDA9529A9105EAC691C9F7ACC55C3CF74, 'eng', 'US', 'America/Los_Angeles');
