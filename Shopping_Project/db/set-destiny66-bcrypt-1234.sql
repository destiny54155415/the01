USE petshop;

-- 1) 반드시 이 해시 전체를 한 줄로 복사 (대소문자 그대로)
UPDATE members
SET password = '$2a$10$smu7CPUgH3O4iHi6g63SxeI4fkp91FFgo7t6ra71sXxzyLwQzANpS',
    role = 'ADMIN'
WHERE username = 'destiny66';

-- 2) 저장 결과 확인 (아래 prefix 가 동일해야 함)
SELECT
  username,
  role,
  password,
  CHAR_LENGTH(password) AS len,
  password = '$2a$10$smu7CPUgH3O4iHi6g63SxeI4fkp91FFgo7t6ra71sXxzyLwQzANpS' AS exact_match
FROM members
WHERE username = 'destiny66';

-- exact_match 가 1 이어야 로그인 가능
-- 로그인: destiny66 / 1234
