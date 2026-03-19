INSERT IGNORE INTO transactions (transaction_id, user_id, amount, currency, status, idempotency_key)
VALUES
('tx1001', 1, 100.00, 'USD', 'SUCCESS', 'idem-1001'),
('tx1002', 1, 200.00, 'USD', 'SUCCESS', 'idem-1002'),
('tx1003', 2, 150.00, 'USD', 'SUCCESS', 'idem-1003'),
('tx1004', 2, 300.00, 'USD', 'FAILED',  'idem-1004'),
('tx1005', 3, 50.00,  'USD', 'SUCCESS', 'idem-1005'),
('tx1006', 3, 75.00,  'USD', 'SUCCESS', 'idem-1006'),
('tx1007', 4, 500.00, 'USD', 'SUCCESS', 'idem-1007'),
('tx1008', 4, 20.00,  'USD', 'PENDING', 'idem-1008'),
('tx1009', 5, 60.00,  'USD', 'SUCCESS', 'idem-1009'),
('tx1010', 5, 80.00,  'USD', 'SUCCESS', 'idem-1010');