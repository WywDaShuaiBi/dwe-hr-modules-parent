@echo off
title DWE-Server

java -server -Xms1024M -Xmx2048M -Dfile.encoding=utf-8 -XX:PermSize=512M -XX:MaxPermSize=1024M -XX:-UseGCOverheadLimit -XX:+UseParallelOldGC -Dfile.encoding=utf-8 -cp "lib/*" com.dwsoft.webapp.Starter