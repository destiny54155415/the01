@echo off
chcp 65001 > nul

git add .
git commit -m "초기 DB, CSS 설정해서 올립니다"
git push origin main

pause
