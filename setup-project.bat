@echo off
echo Creating B2B Platform Project Structure...
echo.

REM Create main directories
if not exist "api-gateway" mkdir api-gateway
if not exist "services" mkdir services
if not exist "services\operator-service" mkdir services\operator-service
if not exist "services\bet-service" mkdir services\bet-service
if not exist "services\wallet-service" mkdir services\wallet-service
if not exist "services\settlement-service" mkdir services\settlement-service
if not exist "services\exposure-service" mkdir services\exposure-service
if not exist "services\session-service" mkdir services\session-service
if not exist "services\game-services" mkdir services\game-services
if not exist "services\game-services\roulette-service" mkdir services\game-services\roulette-service
if not exist "shared" mkdir shared
if not exist "shared\common-models" mkdir shared\common-models
if not exist "shared\common-utilities" mkdir shared\common-utilities
if not exist "infrastructure" mkdir infrastructure
if not exist "infrastructure\docker" mkdir infrastructure\docker
if not exist "infrastructure\kubernetes" mkdir infrastructure\kubernetes
if not exist "infrastructure\docker-compose" mkdir infrastructure\docker-compose
if not exist "docs" mkdir docs
if not exist "scripts" mkdir scripts

REM Create source directories for operator-service
if not exist "services\operator-service\src" mkdir services\operator-service\src
if not exist "services\operator-service\src\main" mkdir services\operator-service\src\main
if not exist "services\operator-service\src\main\java" mkdir services\operator-service\src\main\java
if not exist "services\operator-service\src\main\java\com" mkdir services\operator-service\src\main\java\com
if not exist "services\operator-service\src\main\java\com\b2bplatform" mkdir services\operator-service\src\main\java\com\b2bplatform
if not exist "services\operator-service\src\main\java\com\b2bplatform\operator" mkdir services\operator-service\src\main\java\com\b2bplatform\operator
if not exist "services\operator-service\src\main\java\com\b2bplatform\operator\controller" mkdir services\operator-service\src\main\java\com\b2bplatform\operator\controller
if not exist "services\operator-service\src\main\java\com\b2bplatform\operator\service" mkdir services\operator-service\src\main\java\com\b2bplatform\operator\service
if not exist "services\operator-service\src\main\java\com\b2bplatform\operator\repository" mkdir services\operator-service\src\main\java\com\b2bplatform\operator\repository
if not exist "services\operator-service\src\main\java\com\b2bplatform\operator\model" mkdir services\operator-service\src\main\java\com\b2bplatform\operator\model
if not exist "services\operator-service\src\main\resources" mkdir services\operator-service\src\main\resources
if not exist "services\operator-service\src\test" mkdir services\operator-service\src\test

echo Project structure created successfully!
echo.
echo Next steps:
echo 1. Run: scripts\start-infrastructure.bat
echo 2. Navigate to: services\operator-service
echo 3. Run: mvn spring-boot:run
echo.
pause
