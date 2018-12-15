-- insert few contract templates
INSERT INTO contract_template(account_id, title, body)
VALUES(1, 'Consultant Agreement',
'{{parties.client.name}} (information)

(“Client”) engages

{{parties.vendor.name}} (“vendor”) (collectively “Parties”) for technology consulting services.

The intent captured within this memorandum (”Amendment”) is to amend the existing signed agreement between parties signed on {{contract.date_signed}}(Agreement).');
INSERT INTO contract_template(account_id, title, body)
VALUES(1, 'Software Dev Agreement',
'{{parties.client.name}} (information)

(“Client”) engages

{{parties.vendor.name}} (“vendor”) (collectively “Parties”) for technology consulting services.

The intent captured within this memorandum (”Amendment”) is to amend the existing signed agreement between parties signed on {{contract.date_signed}}(Agreement).');
INSERT INTO contract_template(account_id, title, body)
VALUES(1, 'Rental Agreement',
'{{parties.client.name}} (information)

(“Client”) engages

{{parties.vendor.name}} (“vendor”) (collectively “Parties”) for technology consulting services.

The intent captured within this memorandum (”Amendment”) is to amend the existing signed agreement between parties signed on {{contract.date_signed}}(Agreement).');


-- insert few contract drafts
INSERT INTO contract(account_id, memo, body)
VALUES(1, 'Consultant Agreement',
'{{parties.client.name}} (information)

(“Client”) engages

{{parties.vendor.name}} (“vendor”) (collectively “Parties”) for technology consulting services.

The intent captured within this memorandum (”Amendment”) is to amend the existing signed agreement between parties signed on {{contract.date_signed}}(Agreement).');
INSERT INTO contract(account_id, memo, body)
VALUES(1, 'Software Dev Agreement',
'{{parties.client.name}} (information)

(“Client”) engages

{{parties.vendor.name}} (“vendor”) (collectively “Parties”) for technology consulting services.

The intent captured within this memorandum (”Amendment”) is to amend the existing signed agreement between parties signed on {{contract.date_signed}}(Agreement).');
INSERT INTO contract(account_id, memo, body)
VALUES(1, 'Rental Agreement',
'{{parties.client.name}} (information)

(“Client”) engages

{{parties.vendor.name}} (“vendor”) (collectively “Parties”) for technology consulting services.

The intent captured within this memorandum (”Amendment”) is to amend the existing signed agreement between parties signed on {{contract.date_signed}}(Agreement).');
