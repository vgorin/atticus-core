CREATE PROCEDURE sp_post_nth_dialog_message(
  IN deal_id INT UNSIGNED,
  IN account_id INT UNSIGNED,
  IN contract_id INT UNSIGNED,
  OUT dialog_id INT UNSIGNED
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
  SET @contract_id = contract_id;

  INSERT INTO deal_dialog(
      deal_id,
      account_id,
      seq_num,
      contract_id
      )
  VALUES(
            @deal_id,
            @account_id,
            @seq_num,
            @contract_id
            );

  SET dialog_id = LAST_INSERT_ID();
  COMMIT;
END;
//
