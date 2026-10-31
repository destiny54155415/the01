@echo off
chcp 65001 > nul

git add .
git commit -m "."
git push origin main

pause
