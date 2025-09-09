@echo off
REM — 1) Ve al monorepo local
cd /d C:\Users\Zak\Dev\Rocket\gitbackcomp\rocketcourier

REM — 2) (Opcional) Actualiza el repo
echo Haciendo git pull...
git pull

REM — 3) Ve al backend y compila sin tests
cd backend
echo Compilando WAR (skip tests)...
mvn clean package -DskipTests

REM — 4) Verifica que el WAR existe
set WAR=target\rocket-1.10-SNAPSHOT.war
if not exist %WAR% (
  echo.
  echo ❌ ERROR: No se encontró %WAR%
  pause
  exit /b 1
)

REM — 5) Súbelo al servidor y renómbralo a rocket.war
echo.
echo Subiendo y renombrando a rocket.war en el servidor...
"C:\Program Files\PuTTY\pscp.exe" -i "C:\Users\Zak\Dev\Rocket\scios-rocket.ppk" ^
  "%WAR%" ^
  ec2-user@18.118.189.189:/home/ec2-user/rocket.war

REM — 6) Copia en WildFly y reinicia WildFly via SSH (Plink)
echo.
echo Desplegando en WildFly y reiniciando servicio...
"C:\Program Files\PuTTY\plink.exe" -i "C:\Users\Zak\Dev\Rocket\scios-rocket.ppk" ^
  ec2-user@18.118.189.189 ^
  "sudo cp /home/ec2-user/rocket.war /opt/wildfly/standalone/deployments/rocket.war && sudo systemctl restart wildfly"

echo.
echo ✅ Deploy completo.
pause
