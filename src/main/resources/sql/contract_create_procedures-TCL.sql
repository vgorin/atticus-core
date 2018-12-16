CREATE PROCEDURE sp_clone_contract(
  IN contract_id INT UNSIGNED,
  IN to_account_id INT UNSIGNED,
  OUT to_contract_id INT UNSIGNED
)
  BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
      ROLLBACK;
    END;

    START TRANSACTION;
    SET @contract_id = contract_id;
    SET @account_id = to_account_id;

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
      id = @contract_id;

    SET to_contract_id = LAST_INSERT_ID();
    COMMIT;
  END;
//
