#! /bin/bash
pg_ctl -o "-c unix_socket_directories=/tmp/$LOGNAME/sockets" -D /tmp/$LOGNAME/test/data stop


#Ashly's Server
#pg_ctl -o "-c unix_socket_directories=/tmp/ahern122/sockets" -D /tmp/ahern122/test/data stop
#pg_ctl -o "-c unix_socket_directories=/tmp/ahern122/sockets" -D /extra/ahern122/cs166/mydb/data stop

