-- Fictional development fixtures. Never included in Flyway migrations.
INSERT INTO stores (id, slug, name, active) VALUES
('11111111-1111-4111-8111-111111111111', 'aurora-centro', 'Padaria Aurora — Centro (fictícia)', true),
('22222222-2222-4222-8222-222222222222', 'aurora-jardins', 'Padaria Aurora — Jardins (fictícia)', true),
('33333333-3333-4333-8333-333333333333', 'aurora-reserva', 'Padaria Aurora — Reserva (fictícia)', false)
ON CONFLICT DO NOTHING;
