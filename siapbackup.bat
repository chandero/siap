@echo off
For /f "tokens=1-4 delims=//" %%a in ('date /t') do (set mydate=%%c-%%b-%%a)
For /f "tokens=1-2 delims=/:" %%a in ('time /t') do (set mytime=%%a%%b)
SET PGPASSWORD=2bw5K3ESxkP988
C:\Postgresql\bin\pg_dump.exe -i -U siapbackup -w -F t -b -v -n siap -f "D:\OFICINA\backupsiap\backupsiap_%mydate%_%mytime%.backup" siapisag