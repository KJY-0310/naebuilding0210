DELETE FROM request_images;
DELETE FROM requests;
DELETE FROM users;

-- users: created_at / updated_at NOT NULL 대응
INSERT INTO users (user_id, login_id, password, nickname, email, role, created_at, updated_at)
VALUES
    (1, 'admin', '$2b$10$DClJrSOlDnHJbyYO1YtXR.vHX25F8EOa.m.RWBoFfMYdeQfWJMKCK', '사감', 'admin@dormfix.com', 'ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'user1', '$2b$10$UaQpWF5JglAbXEM0W9iqduY/2dHZcxBCoO/qo4RzymndvKRfEng0u', '101호학생', 'user1@dormfix.com', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- requests
INSERT INTO requests (request_id, title, content, category, location, status, writer_id, admin_note, created_at, updated_at)
VALUES
    (1, '테스트 민원', '상태 변경 테스트용', '시설', '101호', 'RECEIVED', 2, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
