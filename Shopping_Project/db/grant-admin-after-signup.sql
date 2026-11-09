USE petshop;

-- 웹에서 /signup 으로 destiny66 / 1234 가입 완료 후 실행
UPDATE members
SET role = 'ADMIN'
WHERE username = 'destiny66';

SELECT username, role, CHAR_LENGTH(password) AS len, LEFT(password, 7) AS prefix
FROM members
WHERE username = 'destiny66';

-- prefix 가 $2a$10$ 이고 len 이 60 이면 정상
-- 로그인: destiny66 / 1234
-- 관리자: /admin/products
