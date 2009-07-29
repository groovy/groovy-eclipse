@echo off
if '%1' == '?' goto help
if not '%BATLST%' == '' echo on
rem - build        - Build the program

del ivjperf.h
call javah.exe -classpath "z:\jars\perfmsr.jar" -jni -o ivjperf.h org.eclipse.perfmsr.core.PerformanceMonitor
echo After running this step you still need to do a build inside of VC
@goto exit

:help
echo Build the program
goto exit

:exit