USE petshop;

-- 1) 기존 계정 삭제
DELETE FROM members WHERE username = 'destiny66';

-- 확인 (0건이어야 함)
SELECT * FROM members WHERE username = 'destiny66';
