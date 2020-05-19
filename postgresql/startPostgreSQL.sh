#! /bin/bash
export PGDATA=/tmp/$LOGNAME/test/data

sleep 1
#Starts the database server
pg_ctl -o "-c unix_socket_directories=/tmp/$LOGNAME/sockets" -D $PGDATA -l /tmp/$LOGNAME/logfile start


#! /bin/bash
#export PGDATA=/tmp/ahern122/test/data

#sleep 1
#Starts the database server
#pg_ctl -o "-c unix_socket_directories=/tmp/ahern122/sockets" -D $PGDATA -l /tmp/ahern122/logfile start
#pg_ctl -o "-c unix_socket_directories=/tmp/ahern122/sockets" -D /extra/ahern122/cs166/mydb/data -l logfile start
