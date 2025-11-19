@echo off
SETLOCAL

:: Define variables
set EC2_IP=35.166.207.216
set REMOTE_USERNAME=ec2-user
set KEY_PATH=C:\Dev\personal\secrets\terrabyte_01.ppk
set REMOTE_PATH=/deployments/terrabyte/build/usedfurniture.restapi
set LOCAL_FILE_PATH=.\target\used_furniture.restapi-1.0-SNAPSHOT.war


:: 	Moving files
echo pscp -i "%KEY_PATH%" "%LOCAL_FILE_PATH%" %REMOTE_USERNAME%@%EC2_IP%:%REMOTE_PATH%
pscp -i "%KEY_PATH%" "%LOCAL_FILE_PATH%" %REMOTE_USERNAME%@%EC2_IP%:%REMOTE_PATH%


:: Check for errors
if %ERRORLEVEL% neq 0 (
    echo Error occurred during file transfer.
) else (
    echo File transfer completed successfully.
)

	ENDLOCAL
	
