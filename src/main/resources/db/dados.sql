INSERT INTO usuario (nome, login, senha, situacao) VALUES
    ('Administrador', 'admin', 'admin123', 'ATIVO');

INSERT INTO lancamento (descricao, data_lancamento, valor, tipo_lancamento, situacao) VALUES
    ('Salário mensal',             '2026-01-05', 5500.00, 'RECEITA', 'PAGO'),
    ('Aluguel apartamento',        '2026-01-10', 1200.00, 'DESPESA', 'PAGO'),
    ('Freelance desenvolvimento',  '2026-01-15', 800.00,  'RECEITA', 'PAGO'),
    ('Supermercado',               '2026-01-18', 430.50,  'DESPESA', 'PAGO'),
    ('Conta de energia',           '2026-01-20', 180.00,  'DESPESA', 'PAGO'),
    ('Plano de saúde',             '2026-01-22', 320.00,  'DESPESA', 'PAGO'),
    ('Venda de equipamento usado', '2026-02-01', 650.00,  'RECEITA', 'PAGO'),
    ('Internet banda larga',       '2026-02-05', 99.90,   'DESPESA', 'PENDENTE'),
    ('Consultoria TI',             '2026-02-10', 1200.00, 'RECEITA', 'PENDENTE'),
    ('Manutenção veículo',         '2026-02-15', 560.00,  'DESPESA', 'PENDENTE');
