-- Dados iniciais (idempotente)

INSERT INTO usuario (nome, login, email, senha, situacao, perfil)
SELECT 'Administrador', 'admin', 'admin@email.com', 'admin123', 'ATIVO', 'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM usuario u WHERE u.login = 'admin');

UPDATE usuario SET perfil = 'ADMIN' WHERE login = 'admin';

INSERT INTO lancamento (descricao, data_lancamento, valor, tipo_lancamento, situacao)
SELECT 'Salário mensal', DATE '2026-01-05', 5500.00, 'RECEITA', 'PAGO'
WHERE NOT EXISTS (
    SELECT 1 FROM lancamento l WHERE l.descricao = 'Salário mensal' AND l.data_lancamento = DATE '2026-01-05'
);

INSERT INTO lancamento (descricao, data_lancamento, valor, tipo_lancamento, situacao)
SELECT 'Aluguel apartamento', DATE '2026-01-10', 1200.00, 'DESPESA', 'PAGO'
WHERE NOT EXISTS (
    SELECT 1 FROM lancamento l WHERE l.descricao = 'Aluguel apartamento' AND l.data_lancamento = DATE '2026-01-10'
);

INSERT INTO lancamento (descricao, data_lancamento, valor, tipo_lancamento, situacao)
SELECT 'Freelance desenvolvimento', DATE '2026-01-15', 800.00, 'RECEITA', 'PAGO'
WHERE NOT EXISTS (
    SELECT 1 FROM lancamento l WHERE l.descricao = 'Freelance desenvolvimento' AND l.data_lancamento = DATE '2026-01-15'
);

INSERT INTO lancamento (descricao, data_lancamento, valor, tipo_lancamento, situacao)
SELECT 'Supermercado', DATE '2026-01-18', 430.50, 'DESPESA', 'PAGO'
WHERE NOT EXISTS (
    SELECT 1 FROM lancamento l WHERE l.descricao = 'Supermercado' AND l.data_lancamento = DATE '2026-01-18'
);

INSERT INTO lancamento (descricao, data_lancamento, valor, tipo_lancamento, situacao)
SELECT 'Conta de energia', DATE '2026-01-20', 180.00, 'DESPESA', 'PAGO'
WHERE NOT EXISTS (
    SELECT 1 FROM lancamento l WHERE l.descricao = 'Conta de energia' AND l.data_lancamento = DATE '2026-01-20'
);

INSERT INTO lancamento (descricao, data_lancamento, valor, tipo_lancamento, situacao)
SELECT 'Plano de saúde', DATE '2026-01-22', 320.00, 'DESPESA', 'PAGO'
WHERE NOT EXISTS (
    SELECT 1 FROM lancamento l WHERE l.descricao = 'Plano de saúde' AND l.data_lancamento = DATE '2026-01-22'
);

INSERT INTO lancamento (descricao, data_lancamento, valor, tipo_lancamento, situacao)
SELECT 'Venda de equipamento usado', DATE '2026-02-01', 650.00, 'RECEITA', 'PAGO'
WHERE NOT EXISTS (
    SELECT 1 FROM lancamento l WHERE l.descricao = 'Venda de equipamento usado' AND l.data_lancamento = DATE '2026-02-01'
);

INSERT INTO lancamento (descricao, data_lancamento, valor, tipo_lancamento, situacao)
SELECT 'Internet banda larga', DATE '2026-02-05', 99.90, 'DESPESA', 'PENDENTE'
WHERE NOT EXISTS (
    SELECT 1 FROM lancamento l WHERE l.descricao = 'Internet banda larga' AND l.data_lancamento = DATE '2026-02-05'
);

INSERT INTO lancamento (descricao, data_lancamento, valor, tipo_lancamento, situacao)
SELECT 'Consultoria TI', DATE '2026-02-10', 1200.00, 'RECEITA', 'PENDENTE'
WHERE NOT EXISTS (
    SELECT 1 FROM lancamento l WHERE l.descricao = 'Consultoria TI' AND l.data_lancamento = DATE '2026-02-10'
);

INSERT INTO lancamento (descricao, data_lancamento, valor, tipo_lancamento, situacao)
SELECT 'Manutenção veículo', DATE '2026-02-15', 560.00, 'DESPESA', 'PENDENTE'
WHERE NOT EXISTS (
    SELECT 1 FROM lancamento l WHERE l.descricao = 'Manutenção veículo' AND l.data_lancamento = DATE '2026-02-15'
);
