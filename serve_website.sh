#!/bin/bash
while true; do
    if ! pgrep -f "http.server 3000" > /dev/null; then
        echo "[$(date)] Starting web server on port 3000..." >> /tmp/webserver.log
        python3 -m http.server 3000 --directory /app/applet/website >> /tmp/webserver.log 2>&1
    fi
    sleep 3
done
