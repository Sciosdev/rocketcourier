@echo off
cd /d C:\Users\Zak\Dev\Rocket\gitbackcomp\rocketcourier
echo Escribe el mensaje del commit:
set /p MSG="Mensaje: "
git add .
git commit -m "%MSG%"
git push
pause