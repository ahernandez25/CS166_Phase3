#! /bin/bash
DBNAME=$1

echo "Checking server status"
pg_ctl status

echo "Copying csv files ... "
cp ../data/*.csv /tmp/$USER/test/data/.

echo "Initializing tables .. "
sleep 1
psql -h /tmp/$LOGNAME/sockets $DBNAME < ../sql/create.sql



#! /bin/bash
# Ashlys Server
#DBNAME=$1

#echo "Checking server status"
#pg_ctl status

#echo "Copying csv files ... "
#cp ../data/*.csv /tmp/ahern122/test/data/.

#echo "Initializing tables .. "
#sleep 1
#psql -h /tmp/ahern122/sockets $DBNAME < ../sql/create.sql

