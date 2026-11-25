CREATE TABLE IF NOT EXISTS produto (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    preco DECIMAL(15, 2) NOT NULL CHECK (preco > 0),
    quantidade INT NOT NULL CHECK (quantidade >= 0),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_produto_nome UNIQUE (nome)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS cliente (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    documento VARCHAR(32) NOT NULL,
    email VARCHAR(254) NOT NULL,
    CONSTRAINT uq_cliente_documento UNIQUE (documento),
    CONSTRAINT uq_cliente_email UNIQUE (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS pedido (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero INT NOT NULL CHECK (numero > 0),
    cliente_id BIGINT NOT NULL,
    situacao VARCHAR(12) NOT NULL DEFAULT 'ABERTO',
    comprovante VARCHAR(255),
    CONSTRAINT uq_pedido_numero UNIQUE (numero),
    CONSTRAINT ck_pedido_situacao CHECK (situacao IN ('ABERTO', 'FINALIZADO', 'PAGO')),
    CONSTRAINT fk_pedido_cliente FOREIGN KEY (cliente_id)
        REFERENCES cliente(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS item_pedido (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id BIGINT NOT NULL,
    produto_id BIGINT NOT NULL,
    quantidade INT NOT NULL CHECK (quantidade > 0),
    preco_praticado DECIMAL(15, 2) NOT NULL CHECK (preco_praticado > 0),
    CONSTRAINT fk_item_pedido FOREIGN KEY (pedido_id)
        REFERENCES pedido(id) ON DELETE CASCADE,
    CONSTRAINT fk_item_produto FOREIGN KEY (produto_id)
        REFERENCES produto(id) ON DELETE RESTRICT
) ENGINE=InnoDB;
