-----------------------------------------------------------------
-- sequences
-----------------------------------------------------------------

-- hibernate bugfix/change  - from 4.x sequence  to 5.x sequence_name
select hibernate_sequence.nextval from dual;

-- value --> [NextVal]

-- create sequences
create sequence SEQ_LAU_CRON start with [NextVal] increment by 1;
create sequence SEQ_LAU_INBOX start with [NextVal] increment by 1;
create sequence SEQ_LAU_INBOX_EVENT start with [NextVal] increment by 1;
create sequence SEQ_LAU_INBOX_PAYLOAD start with [NextVal] increment by 1;
create sequence SEQ_LAU_INBOX_PAYLOAD_PROP start with [NextVal] increment by 1;
create sequence SEQ_LAU_INBOX_PROP start with [NextVal] increment by 1;
create sequence SEQ_LAU_INTERC_INST_PROP start with [NextVal] increment by 1;
create sequence SEQ_LAU_INTERC_RULE start with [NextVal] increment by 1;
create sequence SEQ_LAU_INTERCEPTOR start with [NextVal] increment by 1;
create sequence SEQ_LAU_OUTBOX start with [NextVal] increment by 1;
create sequence SEQ_LAU_OUTBOX_EVENT start with [NextVal] increment by 1;
create sequence SEQ_LAU_OUTBOX_PAYLOAD start with [NextVal] increment by 1;
create sequence SEQ_LAU_OUTBOX_PAYLOAD_PROP start with [NextVal] increment by 1;
create sequence SEQ_LAU_OUTBOX_PROP start with [NextVal] increment by 1;
create sequence SEQ_LAU_PROC_INST_PROP start with [NextVal] increment by 1;
create sequence SEQ_LAU_PROCESS_INSTANCE start with [NextVal] increment by 1;
create sequence SEQ_LAU_PROCESSOR start with [NextVal] increment by 1;
create sequence SEQ_LAU_PROCESSOR_RULE start with [NextVal] increment by 1;
create sequence SEQ_LAU_TASK_INSTANCE start with [NextVal] increment by 1;
create sequence SEQ_LAU_TASK_PROP start with [NextVal] increment by 1;


---------------------------------------------------------------
-- cron 1:1 task execution to cron 1:n task execution  with LAU_TASK_INSTANCE
---------------------------------------------------------------
-- create LAU_TASK_INSTANCE
create table LAU_TASK_INSTANCE (TASK_ID number(19,0) not null, TASK_TYPE varchar2(64 char), TASK_PLUGIN varchar2(64 char), TASK_PLUGIN_VERSION varchar2(32 char), TASK_NAME varchar2(128 char), TASK_ACTIVE 
number(1,0), CRON_ID number(19,2), IDX number(10,0), primary key (TASK_ID));
-- add costraint
alter table LAU_TASK_INSTANCE add constraint FK3dq8n8wou6jjjra0hksqi74ni foreign key (CRON_ID) references LAU_CRON;

-- fill LAU_TASK_INSTANCE
INSERT INTO LAU_TASK_INSTANCE (TASK_ID, TASK_TYPE, TASK_PLUGIN,TASK_PLUGIN_VERSION, TASK_NAME,TASK_ACTIVE, CRON_ID,IDX  );
SELECT SEQ_LAU_TASK_INSTANCE.nextval, TASK_TYPE,TASK_PLUGIN,TASK_PLUGIN_VERSION, NAME, 1, id, 0 FROM LAU_CRON;

---------------------------------------------------------------
-- change task properties - constraint to LAU_TASK_INSTANCE 
ALTER TABLE LAU_TASK_PROPERTY RENAME TO LAU_TASK_PROPERTY_BCK;
-- create new LAU_TASK_PROPERTY with constraint to task_id
create table LAU_TASK_PROPERTY (ID number(19,0) not null, TASK_PROP_KEY varchar2(64 char), TASK_PROP_VALUE varchar2(512 char), TASK_ID number(19,0) not null, primary key (ID));
alter table LAU_TASK_PROPERTY add constraint FK6yaein29p27bgvlvu7uj7tytk foreign key (TASK_ID) references LAU_TASK_INSTANCE;


-- insert into new LAU_TASK_PROPERTY 
INSERT INTO LAU_TASK_PROPERTY (ID,TASK_PROP_KEY,TASK_PROP_VALUE, TASK_ID  )
SELECT  SEQ_LAU_TASK_PROP.nextval, op.TASK_PROP_KEY, op.TASK_PROP_VALUE, ti.id 
	FROM LAU_TASK_INSTANCE ti, LAU_TASK_PROPERTY_BCK op
		where  ti.CRON_ID = op.CRON_ID;



ALTER TABLE LAU_CRON ADD CRON_IGNORE_ON_WORK_FREE_DAYS number(1,0) DEFAULT(1);
ALTER TABLE LAU_CRON DROP COLUMN TASK_TYPE, TASK_PLUGIN, TASK_PLUGIN_VERSION;

---------------------------------------------------------------
-- inbox/outbox  new columns - 
---------------------------------------------------------------

ALTER TABLE LAU_INBOX_PAYLOAD ADD (PART_IS_RECEIVED number(1,0), PART_IS_SENT number(1,0), PART_GENERATED_FROM_PART number(19,0));
ALTER TABLE LAU_INBOX_PAYLOAD_PROPERTY ADD TYPE varchar2(256 char);
ALTER TABLE LAU_INBOX_PROPERTY ADD TYPE varchar2(256 char);

---------------------------------------------------------------
ALTER TABLE LAU_OUTBOX_PAYLOAD ADD (PART_IS_RECEIVED number(1,0), PART_IS_SENT number(1,0), PART_GENERATED_FROM_PART number(19,0));
ALTER TABLE LAU_OUTBOX_PAYLOAD_PROPERTY ADD TYPE varchar2(256 char);
ALTER TABLE LAU_OUTBOX_PROPERTY ADD TYPE varchar2(256 char);


---------------------------------------------------------------
-- search optimization
create index idx_inpart_mid on LAU_INBOX_PAYLOAD (MAIL_ID);
create index idx_inpart_ebmsid on LAU_INBOX_PAYLOAD (PART_EBMS_ID);
create index idx_outpart_mid on LAU_OUTBOX_PAYLOAD (MAIL_ID);
create index idx_outpart_ebmsid on LAU_OUTBOX_PAYLOAD (PART_EBMS_ID);

-----------------------------------------------------------------
-- clear
-----------------------------------------------------------------
-- drop sequence
drop sequence hibernate_sequence;
-- drop table LAU_TASK_PROPERTY_BCK with all constraints
drop table LAU_TASK_PROPERTY_BCK cascade constraints;



