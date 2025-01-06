-- https://github.com/dtm-labs/dtm/blob/main/sqls/busi.mysql.sql
CREATE DATABASE if not exists dtm_app
/*!40100 DEFAULT CHARACTER SET utf8mb4 */
;
drop table if exists dtm_app.account;
create table if not exists dtm_app.account(
  id int(11) PRIMARY KEY AUTO_INCREMENT,
  user_id varchar(128) UNIQUE,
  balance DECIMAL(10, 2) not null default '0',
--  trading_balance DECIMAL(10, 2) not null default '0',
  create_time datetime DEFAULT now(),
  update_time datetime DEFAULT now(),
  key(create_time),
  key(update_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
insert into dtm_app.account (user_id, balance)
values ('alice', 10000),
  ('bob', 10000);
