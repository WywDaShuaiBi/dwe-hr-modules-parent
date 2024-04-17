appName=`ls -t |grep .jar$ |head -n1`
appId=`ps -ef |grep java|grep $appName|awk '{print $2}'`
if [ -z $appId ];then
	echo "Maybe the service $appName not running, please check it..."
else
	echo "The service $appName is stopping...pid$appId"
	kill $appId
fi
