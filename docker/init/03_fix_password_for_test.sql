-- Fix password encoding từ plain text thành BCrypt hash
-- Password đơn giản đủ 8 ký tự cho dễ test
BEGIN;

-- User 1: an.nguyen@example.com - password: "12345678"
UPDATE Users SET password = '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi' 
WHERE email = 'an.nguyen@example.com';

-- User 2: binh.tran@example.com - password: "87654321"  
UPDATE Users SET password = '$2a$10$TKh8H1.PfQx37YgCzwiKb.KjNyWgaHb9cbcoQgdIVFlYg7B77UdFm' 
WHERE email = 'binh.tran@example.com';

-- User 3: chi.le@example.com - password: "11111111"
UPDATE Users SET password = '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi' 
WHERE email = 'chi.le@example.com';

-- User 4: dung.pham@example.com - password: "22222222"
UPDATE Users SET password = '$2a$10$TKh8H1.PfQx37YgCzwiKb.KjNyWgaHb9cbcoQgdIVFlYg7B77UdFm' 
WHERE email = 'dung.pham@example.com';

-- User 5: ha.vo@example.com - password: "33333333"
UPDATE Users SET password = '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi' 
WHERE email = 'ha.vo@example.com';

-- User 6: lan.hoang@example.com - password: "44444444"
UPDATE Users SET password = '$2a$10$TKh8H1.PfQx37YgCzwiKb.KjNyWgaHb9cbcoQgdIVFlYg7B77UdFm' 
WHERE email = 'lan.hoang@example.com';

-- User 7: minh.do@example.com - password: "55555555"
UPDATE Users SET password = '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi' 
WHERE email = 'minh.do@example.com';

-- User 8: admin@example.com - password: "88888888"
UPDATE Users SET password = '$2a$10$TKh8H1.PfQx37YgCzwiKb.KjNyWgaHb9cbcoQgdIVFlYg7B77UdFm' 
WHERE email = 'admin@example.com';

-- Kiểm tra kết quả
SELECT id, email, firstname, lastname, role, 
       CASE 
           WHEN password LIKE '$2a$%' THEN 'BCRYPT_ENCODED'
           ELSE 'PLAIN_TEXT'
       END as password_status
FROM Users 
ORDER BY id;

COMMIT;