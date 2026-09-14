-- V1__create_initial_schema.sql

CREATE TABLE natureza (
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    natureza VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_natureza UNIQUE (natureza)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO natureza(natureza) VALUES
('LESÃO CORPORAL CULPOSA POR ACIDENTE DE TRÂNSITO'),
('LESÃO CORPORAL CULPOSA - OUTRAS'),
('FURTO DE VEÍCULO'),
('ROUBO DE VEÍCULO'),
('ROUBO A BANCO'),
('ROUBO DE CARGA'),
('HOMICÍDIO DOLOSO (2)'),
('Nº DE VÍTIMAS EM HOMICÍDIO DOLOSO (3)'),
('HOMICÍDIO DOLOSO POR ACIDENTE DE TRÂNSITO'),
('Nº DE VÍTIMAS EM HOMICÍDIO DOLOSO POR ACIDENTE DE TRÂNSITO'),
('HOMICÍDIO CULPOSO POR ACIDENTE DE TRÂNSITO'),
('HOMICÍDIO CULPOSO OUTROS'),
('TENTATIVA DE HOMICÍDIO'),
('LESÃO CORPORAL SEGUIDA DE MORTE'),
('LESÃO CORPORAL DOLOSA'),
('LATROCÍNIO'),
('Nº DE VÍTIMAS EM LATROCÍNIO'),
('TOTAL DE ESTUPRO (4)'),
('ESTUPRO'),
('ESTUPRO DE VULNERÁVEL'),
('TOTAL DE ROUBO - OUTROS (1)'),
('ROUBO - OUTROS'),
('FURTO - OUTROS');

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