rmdir /s/q "build/lib"
mvn package dependency:copy-dependencies -DoutputDirectory=build/lib  -DincludeScope=runtime -Dmaven.test.skip=true


@pause