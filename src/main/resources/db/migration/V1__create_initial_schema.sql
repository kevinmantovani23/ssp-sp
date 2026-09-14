-- V1__create_initial_schema.sql

CREATE TABLE natureza (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    natureza       VARCHAR(100) NOT NULL,
    caracteristica VARCHAR(50),
    PRIMARY KEY (id),
    CONSTRAINT uq_natureza UNIQUE (natureza)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO natureza(natureza, caracteristica) VALUES
('LESÃO CORPORAL CULPOSA POR ACIDENTE DE TRÂNSITO', 'Criminal'),
('LESÃO CORPORAL CULPOSA - OUTRAS', 'Criminal'),
('FURTO DE VEÍCULO', 'Criminal'),
('ROUBO DE VEÍCULO', 'Criminal'),
('ROUBO A BANCO', 'Criminal'),
('ROUBO DE CARGA', 'Criminal'),
('HOMICÍDIO DOLOSO (2)', NULL),
('Nº DE VÍTIMAS EM HOMICÍDIO DOLOSO (3)', NULL),
('HOMICÍDIO DOLOSO POR ACIDENTE DE TRÂNSITO', NULL),
('Nº DE VÍTIMAS EM HOMICÍDIO DOLOSO POR ACIDENTE DE TRÂNSITO', NULL),
('HOMICÍDIO CULPOSO POR ACIDENTE DE TRÂNSITO', NULL),
('HOMICÍDIO CULPOSO OUTROS', NULL),
('TENTATIVA DE HOMICÍDIO', NULL),
('LESÃO CORPORAL SEGUIDA DE MORTE', NULL),
('LESÃO CORPORAL DOLOSA', NULL),
('LATROCÍNIO', NULL),
('Nº DE VÍTIMAS EM LATROCÍNIO', NULL),
('TOTAL DE ESTUPRO (4)', NULL),
('ESTUPRO', NULL),
('ESTUPRO DE VULNERÁVEL', NULL),
('TOTAL DE ROUBO - OUTROS (1)', NULL),
('ROUBO - OUTROS', NULL),
('FURTO - OUTROS', NULL);

CREATE TABLE delegacias (
    id        BIGINT       NOT NULL AUTO_INCREMENT,
    idSSP     INT		   NOT NULL,
    delegacia VARCHAR(100) NOT NULL,
    regiao    VARCHAR(100),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE ocorrencia (
    id           BIGINT NOT NULL AUTO_INCREMENT,
    natureza_id  BIGINT NOT NULL,
    delegacia_id BIGINT NOT NULL,
    quantidade   INT    NOT NULL DEFAULT 0,
    data         DATE   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_ocorrencia_natureza  FOREIGN KEY (natureza_id)  REFERENCES natureza(id),
    CONSTRAINT fk_ocorrencia_delegacia FOREIGN KEY (delegacia_id) REFERENCES delegacias(id),
    CONSTRAINT chk_quantidade CHECK (quantidade >= 0),
    CONSTRAINT UNIQUE (delegacia_id, natureza_id, data)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_ocorrencia_data      ON ocorrencia(data);
CREATE INDEX idx_ocorrencia_natureza  ON ocorrencia(natureza_id);
CREATE INDEX idx_ocorrencia_delegacia ON ocorrencia(delegacia_id);