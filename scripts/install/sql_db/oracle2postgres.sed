#! /usr/bin/env sed
# 
# Convert oracle datatypes to postgres datatypes
# Credit: http://www.sqlines.com/oracle-to-postgresql 
#
# Run this on the oracle crate statements to get postgres create statements, like so:
# $ sed -nrf oracle2postgres.sed create_2.0_oracle.sql > create_2.0_postgres.sql

s@varchar2\(([0-9]+) char\)@VARCHAR(\1)@ig;
s@number\(1,0\)@SMALLINT@gi;
s@number\((9|1[0-8])(\s*,[0-9]+)?\)@BIGINT@gi;
s@number\(19,0\)@DECIMAL(19)@gi;
s@number\(19,2\)@DECIMAL(19,2)@gi;
