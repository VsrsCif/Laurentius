#!/bin/sh

# Set database dialect
# choose appropriate from page 
# https://docs.jboss.org/hibernate/orm/5.2/javadocs/org/hibernate/dialect/package-summary.html
# examples:
# DB_DIALECT='org.hibernate.dialect.H2Dialect'
# DB_DIALECT='org.hibernate.dialect.Oracle10gDialect'
# DB_DIALECT='org.hibernate.dialect.Oracle12cDialect'
# DB_DIALECT='org.hibernate.dialect.PostgreSQL9Dialect'
# DB_DIALECT='org.hibernate.dialect.PostgreSQL95Dialect'
# DB_DIALECT='org.hibernate.dialect.SQLServer2008Dialect'
DB_DIALECT='org.hibernate.dialect.H2Dialect'

# set db action where init parameter is true.
# only validate (validate), 'update' or recreate (create) database objects.
# recreate  - delete all data in a tables.
#https://docs.jboss.org/hibernate/orm/5.0/manual/en-US/html/ch03.html

#DB_INI_ACTION='validate'
DB_INI_ACTION='create'
#DB_INI_ACTION='update'

# inet mask for access 0.0.0.0 - all access
LISTEN_MASK=0.0.0.0

quit () {
	echo "\nUsage:\n"
	echo "laurentius-demo.sh --init -d [DOMAIN] -l [LAU_HOME]\n"
	echo "  --init  initialize laurentius (database and demo data are loaded to database)"
	echo "  -d DOMAIN  -  if --init is setted than domain must be given (ex.: company.org, test-bank.org, etc.)"
	echo "  -l LAU_HOME  - path tom application home folder  (laurentius.home) if is not given and --init is setted than '[WILDFLY_HOME]\standalone\data\' is setted."	
        exit
}


while [ "$#" -gt 0 ]
do
key="$1"

  case $key in
    -w|--wildfly)
      WILDFLY_HOME="$2"
      shift # past argument
    ;;
      -l|--laurentius.home)
      LAU_HOME="$2"
      shift # past argument
    ;;
    --init)
      INIT="TRUE"      
    ;;
   -d|--domain)
      LAU_DOMAIN="$2"
      shift # past argument
    ;;

    *)
      # unknown option
  echo "unknown option: $key or bad list of arguments"
    ;;
  esac
  shift # past argument or value
done



DIRNAME=`dirname "$0"`
RESOLVED_WILDFLY_HOME=`cd "$DIRNAME/.." >/dev/null; pwd`
if [ "x$WILDFLY_HOME" = "x" ]; then
    # get the full path (without any relative bits)
    WILDFLY_HOME=$RESOLVED_WILDFLY_HOME
else
 SANITIZED_WILDFLY_HOME=`cd "$WILDFLY_HOME"; pwd`
 if [ "$RESOLVED_WILDFLY_HOME" != "$SANITIZED_WILDFLY_HOME" ]; then
   echo ""
   echo "   WARNING:  WILDFLY_HOME may be pointing to a different installation - unpredictable results may occur."
   echo ""
   echo "             WILDFLY_HOME: $WILDFLY_HOME"
   echo ""
   sleep 2s
 fi
fi


if [ "x$WILDFLY_HOME" = "x" ]; then
	echo "WILDFLY_HOME folder not defined! Check parameters!"
	quit;
fi

if [ ! -d "$WILDFLY_HOME" ]; then
	echo "WILDFLY_HOME folder not exists! Check parameters!"
	quit;
fi

if [ "x$LAU_HOME" = "x" ]; then
	LAU_HOME="$WILDFLY_HOME/standalone/data/laurentius-home";
fi

LAU_OPTS=" -c standalone-laurentius.xml -Dlaurentius.home=$LAU_HOME/";

if [ "$INIT" = "TRUE" ]; then
	read -r -p "Init will recreate database tables if exists. All data in tables will be lost. Do you want to continue? (Enter Y to continue) " answer
	case "$answer" in
    	[yY][eE][sS]|[yY]) 
    	    break;;
    	*)
    	    exit;;
	esac



	if [ "x$LAU_DOMAIN" = "x" ]; then
		echo "Missing domain for initialization! Put domain after --init parameter. Ex.: laurentios-demo.sh --init -d test-company.org"
		quit;
	fi
	LAU_OPTS="$LAU_OPTS -Dlaurentius.hibernate.hbm2ddl.auto=$DB_INI_ACTION -Dlaurentius.hibernate.dialect=$DB_DIALECT -Dlaurentius.init=true -Dlaurentius.domain=$LAU_DOMAIN";
fi

echo "*********************************************************************************************************************************"
echo "* WILDFLY_HOME =  $WILDFLY_HOME"
echo "* LAU_HOME     =  $LAU_HOME"
echo "* INIT         =  $INIT"
echo "* LAU_OPTS     =  $LAU_OPTS"
echo "*********************************************************************************************************************************"

#org.hibernate.dialect.H2Dialect
$WILDFLY_HOME/bin/standalone.sh $LAU_OPTS -b $LISTEN_MASK




