--
-- Dumping routines for database 'OrangeTestDB'
--
DELIMITER ;;
/*!50003 DROP PROCEDURE IF EXISTS `sp_UpdateCounter` */;;
/*!50003 SET SESSION SQL_MODE=""*/;;
/*!50003 CREATE*/ /*!50020 DEFINER=`root`@`localhost`*/ /*!50003 PROCEDURE `sp_UpdateCounter`(
IN IN_BAL_GRP INT,
IN IN_COUNTER INT,
IN IN_REC_ID INT,
IN IN_VALID_FROM INT,
IN IN_VALID_TO INT,
IN UPDATE_VAL double
)
BEGIN
DECLARE rowExist int;
DECLARE currentBal,newBal double;
select count(*) from COUNTER_BALS where BALANCE_GROUP = IN_BAL_GRP and COUNTER_ID=IN_COUNTER and VALID_FROM=IN_VALID_FROM into rowExist;
If rowExist=0 then
INSERT INTO COUNTER_BALS (BALANCE_GROUP,COUNTER_ID,RECORD_ID,VALID_FROM,VALID_TO,CURRENT_BAL) VALUES (IN_BAL_GRP,IN_COUNTER,IN_REC_ID,IN_VALID_FROM,IN_VALID_TO,UPDATE_VAL);
else
select CURRENT_BAL from COUNTER_BALS where BALANCE_GROUP = IN_BAL_GRP and COUNTER_ID=IN_COUNTER and VALID_FROM=IN_VALID_FROM into currentBal;
set newBal = currentBal + UPDATE_VAL;
update COUNTER_BALS set CURRENT_BAL = newBal where BALANCE_GROUP = IN_BAL_GRP and COUNTER_ID=IN_COUNTER and VALID_FROM=IN_VALID_FROM;
end if;
END */;;
DELIMITER ;

