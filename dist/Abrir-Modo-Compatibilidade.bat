@echo off
rem Abre o app sem usar a placa de video e sem escala de tela (para PCs com tela branca/bugada).
set JAVA_TOOL_OPTIONS=-Dprism.order=sw -Dglass.win.uiScale=1.0 -Dprism.dirtyopts=false
start "" "%~dp0GerenciadorDeProjetos.exe"
