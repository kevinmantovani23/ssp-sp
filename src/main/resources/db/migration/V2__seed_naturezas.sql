-- V2__seed_naturezas.sql
-- Naturezas de ocorrência baseadas nos dados públicos da SSP-SP

INSERT INTO natureza (natureza, caracteristica) VALUES
('HOMICIDIO DOLOSO',                                  'HOMICIDIO'),
('Nº DE VITIMAS EM HOMICIDIO DOLOSO',                 'HOMICIDIO'),
('HOMICIDIO DOLOSO POR ACIDENTE DE TRANSITO',         'TRANSITO'),
('Nº DE VITIMAS EM HOMICIDIO DOLOSO POR AC. TRANSITO','TRANSITO'),
('HOMICIDIO CULPOSO POR ACIDENTE DE TRANSITO',        'TRANSITO'),
('HOMICIDIO CULPOSO OUTROS',                          'HOMICIDIO'),
('TENTATIVA DE HOMICIDIO',                            'HOMICIDIO'),
('LESAO CORPORAL SEGUIDA DE MORTE',                   'LESAO'),
('LESAO CORPORAL DOLOSA',                             'LESAO');