
SELECT `defvalue` FROM `s_config` WHERE `varname`='station_name';

UPDATE `s_config` set `defvalue` = '2' WHERE `varname` = 'stock_statistics_date';