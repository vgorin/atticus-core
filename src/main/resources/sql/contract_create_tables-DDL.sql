-- contract template contains text with placeholders
-- parties are not defined, placeholders' values too
CREATE TABLE contract_template(
  id          INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  account_id  INT UNSIGNED NOT NULL, -- owner/creator of the template
  title       VARCHAR(128) NOT NULL,
  version     VARCHAR(16),
  body        TEXT,
  -- initially template is editable (versioned on date is null)
  -- non-null value means template is not editable anymore
  -- template cannot go back to editable state once this date is set
  versioned   DATETIME, -- null value means template is editable
  deleted     DATETIME, -- null value means template is active
  published   DATETIME, -- null value means template is private
  modified    DATETIME NOT NULL DEFAULT NOW(),

  rec_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  rec_updated TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT  UQ_contract_template_COV UNIQUE(account_id, title, version),

  FOREIGN KEY FK_contract_template_account_id(account_id) REFERENCES user_acc(id)
);
-- contract / draft is a template with the placeholders defined
CREATE TABLE contract(
  id          INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  account_id  INT UNSIGNED NOT NULL, -- draft owner/creator
  template_id INT UNSIGNED, -- null value means contract is not based on the template
  memo        VARCHAR(128) NOT NULL, -- draft name not to be confused with template title
  body        TEXT, -- may contain placeholders as well
  -- initially contract is a draft and its editable (proposed on date is null)
  -- non-null value means contract is not a draft but a proposed contract
  -- contract cannot go back to draft state once it becomes a proposed contract
  -- contract variables cannot be modified for already proposed contracts
  proposed    DATETIME, -- null value means contract is a draft
  deleted     DATETIME, -- null value means contract is active
  modified    DATETIME NOT NULL DEFAULT NOW(),

  rec_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  rec_updated TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,

  FOREIGN KEY FK_contract_account_id(account_id) REFERENCES user_acc(id),
  FOREIGN KEY FK_contract_template_id(template_id) REFERENCES contract_template(id)
);
-- one to many mapping between contract and its variables/placeholders values
CREATE TABLE contract_variable(
  contract_id INT UNSIGNED NOT NULL,
  name        VARCHAR(16) NOT NULL, -- variable/placeholder name
  value       VARCHAR(64) NOT NULL, -- variable/placeholder value

  rec_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  rec_updated TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT  UQ_contract_variable_COV UNIQUE(contract_id, name),

  FOREIGN KEY FK_contract_variable_contract_id(contract_id) REFERENCES contract(id)
);
-- a dialog between parties to sign the contract
CREATE TABLE deal(
  id          INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  account_id  INT UNSIGNED NOT NULL, -- account which initiated the deal
  title       VARCHAR(128),
  deleted     DATETIME, -- null value means deals is active

  rec_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  rec_updated TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT  UQ_deal_COV UNIQUE(account_id, title),

  FOREIGN KEY FK_deal_account_id(account_id) REFERENCES user_acc(id)
);
-- contract proposals, messages and everything else related to a deal
-- append-only table
CREATE TABLE deal_dialog(
  id          INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  deal_id     INT UNSIGNED NOT NULL,
  account_id  INT UNSIGNED NOT NULL, -- the person who sends a message
  seq_num     INT UNSIGNED NOT NULL, -- message sequence number within dialog
  message     TEXT,
  attachment  BLOB,
  -- first message (seq_num = 0) must contain an initial proposal
  -- following messages (seq_num > 0) may contain counter proposals
  contract_id INT UNSIGNED,

  rec_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  rec_updated TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP, -- probably won't be used (messages are non-editable)

  CONSTRAINT  UQ_deal_dialog_COV UNIQUE(deal_id, seq_num),

  FOREIGN KEY FK_deal_dialog_deal_id(deal_id) REFERENCES deal(id),
  FOREIGN KEY FK_deal_dialog_account_id(account_id) REFERENCES user_acc(id),
  FOREIGN KEY FK_deal_dialog_contract_id(contract_id) REFERENCES contract(id)
);
-- one to many mapping between contract and parties
-- unsigned, not expired records are active contract proposals
CREATE TABLE contract_party(
  id          INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  contract_id INT UNSIGNED NOT NULL,
  account_id  INT UNSIGNED NOT NULL,
  party_label VARCHAR(16), -- party label as it is in contract template
  valid_until DATETIME NOT NULL,
  signature   BINARY(132),
  signed_on   DATETIME,

  rec_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  rec_updated TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT  UQ_contract_party_COV UNIQUE(contract_id, account_id),

  FOREIGN KEY FK_contract_party_contract_id(contract_id) REFERENCES contract(id),
  FOREIGN KEY FK_contract_party_user_account_id(account_id) REFERENCES user_acc(id)
);
