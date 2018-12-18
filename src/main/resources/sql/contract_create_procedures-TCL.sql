-- clones contract into account specified
-- returns number of rows inserted and ID of new contract
CREATE PROCEDURE sp_clone_contract(
  IN contract_id INT UNSIGNED,
  IN account_id INT UNSIGNED,
  OUT row_count INT UNSIGNED,
  OUT insert_id INT UNSIGNED
)
BEGIN
  DECLARE EXIT HANDLER FOR SQLEXCEPTION
  BEGIN
    ROLLBACK;
  END;

  START TRANSACTION;
    SET @contract_id = contract_id;
    SET @account_id = account_id;

    INSERT INTO contract(
      account_id,
      template_id,
      memo,
      body,
      modified
    )
    SELECT
      @account_id,
      template_id,
      memo,
      body,
      modified
    FROM contract
    WHERE
      id = @contract_id AND
      deleted IS NULL AND (
        -- account must be owner of the contract or
        account_id = @account_id OR (
          -- account can also be a party of the contract
          SELECT 1 FROM contract_party
          WHERE
            account_id = @account_id AND
            contract_id = @contract_id
        ) IS NOT NULL
      );

    SET row_count = ROW_COUNT();
    IF row_count = 1 THEN
      SET insert_id = LAST_INSERT_ID();
    END IF;
  COMMIT;
END;
//


-- clones contract template into account specified
-- returns number of rows inserted and ID of new template
CREATE PROCEDURE sp_clone_contract_template(
  IN template_id INT UNSIGNED,
  IN account_id INT UNSIGNED,
  OUT row_count INT UNSIGNED,
  OUT insert_id INT UNSIGNED
)
BEGIN
  DECLARE EXIT HANDLER FOR SQLEXCEPTION
  BEGIN
    ROLLBACK;
  END;

  START TRANSACTION;
    SET @template_id = template_id;
    SET @account_id = account_id;

    INSERT INTO contract_template(
      account_id,
      title,
      body,
      modified
    )
    SELECT
      @account_id,
      title,
      body,
      modified
    FROM contract_template
    WHERE
      id = @template_id AND
      deleted IS NULL AND (
        -- account must be owner of the contract template to copy or
        account_id = @account_id OR
        -- contract template can be publicly accessible
        published IS NOT NULL
      );

    SET row_count = ROW_COUNT();
    IF row_count = 1 THEN
      SET insert_id = LAST_INSERT_ID();
    END IF;
  COMMIT;
END;
//


-- creates a contract from contract template into account specified
-- returns number of rows inserted and ID of new template
CREATE PROCEDURE sp_create_contract_from_template(
  IN template_id INT UNSIGNED,
  IN account_id INT UNSIGNED,
  OUT row_count INT UNSIGNED,
  OUT insert_id INT UNSIGNED
)
BEGIN
  DECLARE EXIT HANDLER FOR SQLEXCEPTION
  BEGIN
    ROLLBACK;
  END;

  START TRANSACTION;
    SET @template_id = template_id;
    SET @account_id = account_id;

    INSERT INTO contract(
      account_id,
      template_id,
      memo,
      body
    )
    SELECT
      @account_id,
      @template_id,
      title,
      body
    FROM contract_template
    WHERE
      id = @template_id AND
      deleted IS NULL AND (
        -- account must be owner of the contract template or
        account_id = @account_id OR
        -- contract template can be publicly accessible
        published IS NOT NULL
      );

    SET row_count = ROW_COUNT();
    IF row_count = 1 THEN
      SET insert_id = LAST_INSERT_ID();
    END IF;
  COMMIT;
END;
//


-- post a message into dialog, used primarily in counter proposal flow
CREATE PROCEDURE sp_post_nth_dialog_message(
  IN deal_id INT UNSIGNED,
  IN account_id INT UNSIGNED,
  IN message TEXT,
  IN attachment BLOB,
  IN contract_id INT UNSIGNED,
  OUT row_count INT UNSIGNED,
  OUT insert_id INT UNSIGNED
)
BEGIN
  DECLARE EXIT HANDLER FOR SQLEXCEPTION
  BEGIN
    ROLLBACK;
  END;

  START TRANSACTION;
    SET @deal_id = deal_id;
    SET @account_id = account_id;
    SET @seq_num = 1 + (
      SELECT MAX(seq_num)
      FROM deal_dialog
      WHERE deal_id = @deal_id
    );
    SET @message = message;
    SET @attachment = attachment;
    SET @contract_id = contract_id;

    IF
      @message IS NOT NULL OR
      @attachment IS NOT NULL OR
      @contract_id IS NOT NULL
    THEN
      INSERT INTO deal_dialog(
        deal_id,
        account_id,
        seq_num,
        message,
        attachment,
        contract_id
      )
      VALUES(
        @deal_id,
        @account_id,
        @seq_num,
        @message,
        @attachment,
        @contract_id
      );
    END IF;

    SET row_count = ROW_COUNT();
    IF row_count = 1 THEN
      SET insert_id = LAST_INSERT_ID();
    END IF;
  COMMIT;
END;
//
