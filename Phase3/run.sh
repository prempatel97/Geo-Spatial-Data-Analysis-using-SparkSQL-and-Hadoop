#! /bin/bash
printf "Memory\t\tDisk\t\tCPU\t\tSend Rate\t\tReceive Rate\t\tCurrent Time\n"
end=$((SECONDS+3600))
while [ $SECONDS -lt $end ]; do
MEMORY=$(free -m | awk 'NR==2{printf "%.2f%%\t\t", $3*100/$2 }')
DISK=$(df -h | awk '$NF=="/"{printf "%s\t\t", $5}')
CPU=$(top -bn1 | grep load | awk '{printf "%.2f%%\t\t\n", $(NF-2)}')
SEND_RATE=$(sudo iftop -t -s 2 -n -N 2>/dev/null | awk '/Total send rate/ {printf "%s\t\t\t",  $6}')
RECEIVE_RATE=$(sudo iftop -t -s 2 -n -N 2>/dev/null | awk '/Total receive rate/ {printf "%s\t\t\t", $6}')
now=$(date +"%T")
echo "$MEMORY$DISK$CPU$SEND_RATE$RECEIVE_RATE$now"
done

