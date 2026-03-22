-- V1__create_initial_schema.sql

CREATE TABLE natureza (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    natureza       VARCHAR(100) NOT NULL,
    caracteristica VARCHAR(50),
    PRIMARY KEY (id),
    CONSTRAINT uq_natureza UNIQUE (natureza)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE delegacias (
    id        BIGINT       NOT NULL AUTO_INCREMENT,
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
    CONSTRAINT chk_quantidade CHECK (quantidade >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_ocorrencia_data      ON ocorrencia(data);
CREATE INDEX idx_ocorrencia_natureza  ON ocorrencia(natureza_id);
CREATE INDEX idx_ocorrencia_delegacia ON ocorrencia(delegacia_id);