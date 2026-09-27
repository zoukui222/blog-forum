@echo off
chcp 65001 >nul
setlocal
REM ============================================================
REM  前端构建 + 发布到 Nginx 静态资源目录
REM  用法：双击本文件，或在 cmd 中执行  deploy\deploy.bat
REM ============================================================
set "ROOT=%~dp0.."
set "DEPLOY_DIR=D:\deploy\blog-front"

echo [1/3] 构建前端（production 模式，接口地址读取 vue\.env.production）...
cd /d "%ROOT%\vue"
call npm run build
if errorlevel 1 (
    echo [错误] 构建失败，已中止。
    exit /b 1
)

echo [2/3] 发布构建产物到 %DEPLOY_DIR% ...
if not exist "%DEPLOY_DIR%" mkdir "%DEPLOY_DIR%"
xcopy /E /Y /I /Q "%ROOT%\vue\dist" "%DEPLOY_DIR%" >nul

echo [3/3] 完成。若 Nginx 已在运行，执行 nginx -s reload 使配置生效。
endlocal
