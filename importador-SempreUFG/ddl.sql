CREATE TYPE tipo_programa_academico
AS ENUM ('Iniciação Científica', 'Monitoria', 'Extensão', 'Intercâmbio');

CREATE TYPE tipo_nivel_curso
AS ENUM ('Bacharelado', 'Licenciatura', 'Aperfeiçoamento', 'Especialização', 'Mestrado', 'Doutorado');

CREATE TYPE tipo_turno_curso_ufg
AS ENUM ('Matutino', 'Vespertino', 'Integral');

CREATE TYPE tipo_visibilidade_dados_egresso
AS ENUM ('Público', 'Privado', 'Somente Egressos');

CREATE TYPE tipo_iem
AS ENUM ('Federal', 'Estadual', 'Municipal', 'Particular');

CREATE TYPE tipo_ies
AS ENUM ('Federal', 'Estadual', 'Municipal', 'Particular');

CREATE TYPE tipo_item
AS ENUM ('notícia', 'evento', 'curso', 'oportunidade de emprego', 'diversos');

CREATE TYPE natureza_organizacao
AS ENUM ('Pública', 'Privada', 'Trabalho Autônomo');

CREATE TYPE regional_unidade_academica
AS ENUM ('Goiânia-Câmpus Colemar Natal e Silva', 'Goiânia-Câmpus Samambaia', 'Aparecida de Goiânia',
  'Catalão', 'Goiás', 'Jataí');

CREATE TABLE localizacao_geografica (
  id_localizacao                      BIGSERIAL PRIMARY KEY,
  nome_cidade                         VARCHAR(100) NOT NULL,
  nome_cidade_estrangeiro             VARCHAR(100) NOT NULL,
  nome_unidade_federativa             VARCHAR(100) NOT NULL,
  nome_unidade_federativa_estrangeiro VARCHAR(100) NOT NULL,
  nome_pais                           VARCHAR(100) NOT NULL,
  nome_pais_estrangeiro               VARCHAR(100) NOT NULL,
  sigla_unidade_federativa            VARCHAR(20),
  latitude                            FLOAT,
  longitude                           FLOAT,
  UNIQUE (nome_cidade, nome_unidade_federativa)
);

CREATE TABLE egresso (
  id_egresso                  BIGSERIAL PRIMARY KEY,
  nome                        VARCHAR(100)                    NOT NULL,
  tipo_documento_identidade   VARCHAR(50),
  numero_documento_identidade VARCHAR(50),
  data_nascimento             DATE                            NOT NULL,
  visibilidade_dados          tipo_visibilidade_dados_egresso NOT NULL,
  data_ultima_atualizacao     DATE,
  localizacao_naturalidade              BIGSERIAL REFERENCES localizacao_geografica (id_localizacao),
  UNIQUE (tipo_documento_identidade, numero_documento_identidade)
);

CREATE TABLE organizacao (
  nome_razao_social  VARCHAR(100) PRIMARY KEY,
  endereco_comercial VARCHAR(300) NOT NULL,
  natureza           VARCHAR(50)  NOT NULL
);

CREATE TABLE area_conhecimento (
  codigo_area          NUMERIC(10) PRIMARY KEY,
  nome_area            VARCHAR(300),
  subarea_conhecimento NUMERIC(10) REFERENCES area_conhecimento (codigo_area)
);

CREATE TABLE atuacao_egresso_organizacao (
  id_egresso              BIGSERIAL                                                     NOT NULL REFERENCES egresso (id_egresso),
  organizacao_id          VARCHAR(100) REFERENCES organizacao (nome_razao_social)       NOT NULL,
  data_inicio             DATE                                                          NOT NULL,
  data_fim                DATE                                                          NULL,
  forma_ingresso          VARCHAR(100)                                                  NOT NULL,
  renda_mensal_media      FLOAT
                                                                                        NOT NULL,
  satisfacao_renda        VARCHAR(100)                                                  NOT NULL,
  perspectiva_futuro_area VARCHAR(300)                                                  NULL,
  area_conhecimento       NUMERIC(10) REFERENCES area_conhecimento (codigo_area)
);

CREATE TABLE unidade_academica_ufg (
  id_unidade_academica BIGSERIAL PRIMARY KEY,
  regional             regional_unidade_academica NOT NULL,
  nome                 VARCHAR(500)               NOT NULL
);

CREATE TABLE curso_ufg (
  id_curso_ufg      BIGSERIAL PRIMARY KEY,
  nome_curso        VARCHAR(500) UNIQUE,
  nivel             tipo_nivel_curso     NOT NULL,
  data_criacao      DATE                 NOT NULL,
  presencial        BOOLEAN              NOT NULL,
  turno             tipo_turno_curso_ufg NOT NULL,
  area_conhecimento NUMERIC(10) REFERENCES area_conhecimento (codigo_area),
  unidade_academica BIGSERIAL REFERENCES unidade_academica_ufg (id_unidade_academica)
);


CREATE TABLE item_divulgacao (
  id_item_divulgacao        BIGSERIAL PRIMARY KEY,
  data_hora_registro        DATE UNIQUE,
  identificacao_solicitante VARCHAR(300),
  tipo_item                 tipo_item,
  assunto                   VARCHAR(200),
  descricao                 VARCHAR(500),
  parecer_sobre_divulgacao  VARCHAR(500),
  data_divulgacao           DATE
);

CREATE TABLE relacao_item_area_conhecimento (
  id_item_divulgacao BIGSERIAL REFERENCES item_divulgacao (id_item_divulgacao),
  area_conhecimento  NUMERIC(10) REFERENCES area_conhecimento (codigo_area)
);

CREATE TABLE relacao_item_curso_ufg (
  id_item_divulgacao BIGSERIAL REFERENCES item_divulgacao (id_item_divulgacao),
  id_curso_ufg       BIGSERIAL REFERENCES curso_ufg (id_curso_ufg)
);

CREATE TABLE relacao_item_unidade_academica (
  id_item_divulgacao BIGSERIAL REFERENCES item_divulgacao (id_item_divulgacao),
  unidade_academica  BIGSERIAL REFERENCES unidade_academica_ufg (id_unidade_academica)
);


CREATE TABLE historico_ufg (
  id_curso_ufg          BIGSERIAL REFERENCES curso_ufg (id_curso_ufg),
  id_egresso            BIGSERIAL REFERENCES egresso (id_egresso),
  mes_ano_ingresso      NUMERIC(6),
  mes_ano_conclusao     NUMERIC(6),
  matricula             NUMERIC(10),
  titulo_trabalho_final VARCHAR(500),
  PRIMARY KEY (id_curso_ufg, id_egresso)
);

CREATE TABLE instituicao_ensino_medio (
  id_iem         BIGSERIAL PRIMARY KEY,
  nome           VARCHAR(500) UNIQUE,
  tipo_iem       tipo_iem NOT NULL,
  id_localizacao BIGSERIAL REFERENCES localizacao_geografica (id_localizacao)
);

CREATE TABLE historico_iem (
  id_egresso     BIGSERIAL REFERENCES egresso (id_egresso),
  id_iem         BIGSERIAL REFERENCES instituicao_ensino_medio (id_iem),
  mes_ano_inicio NUMERIC(6),
  mes_ano_fim    NUMERIC(6)
);

CREATE TABLE avaliacao_curso_pelo_egresso (
  id_curso_ufg                  BIGSERIAL REFERENCES curso_ufg (id_curso_ufg),
  id_egresso                    BIGSERIAL REFERENCES egresso (id_egresso),
  data_avaliacao                DATE,
  satisfacao_curso              VARCHAR(50),
  motivacao_curso               VARCHAR(50),
  conceito_curso                NUMERIC(2),
  preparacao_mercado            VARCHAR(50),
  capacitacao_comunicacao       VARCHAR(50),
  etica_responsabilidade_social VARCHAR(50),
  habilidades_especificas       VARCHAR(50)
);

CREATE TABLE realizacao_programa_academico (
  id_curso_ufg BIGSERIAL NOT NULL,
  id_egresso   BIGSERIAL NOT NULL,
  tipo         tipo_programa_academico,
  data_inicio  DATE,
  data_fim     DATE,
  descricao    VARCHAR(500),
  FOREIGN KEY (id_curso_ufg, id_egresso) REFERENCES historico_ufg (id_curso_ufg, id_egresso)
);

CREATE TABLE curso_outra_ies (
  id_curso_outra_ies     BIGSERIAL PRIMARY KEY,
  nome_curso             VARCHAR(500),
  nome_curso_estrangeiro VARCHAR(500),
  nivel                  tipo_nivel_curso NOT NULL,
  nome_unidade_academica VARCHAR(500)     NULL,
  ies_curso              VARCHAR(500)     NOT NULL,
  ies_curso_estrangeiro  VARCHAR(500)     NOT NULL,
  tipo_ies               VARCHAR(50)      NOT NULL,
  localizacao            BIGSERIAL REFERENCES localizacao_geografica (id_localizacao),
  area_conhecimento      NUMERIC(10) REFERENCES area_conhecimento (codigo_area)
);

CREATE TABLE historico_outra_ies (
  id_curso_outra_ies BIGSERIAL REFERENCES curso_outra_ies (id_curso_outra_ies),
  id_egresso         BIGSERIAL REFERENCES egresso (id_egresso),
  mes_ano_ingresso   NUMERIC(6),
  mes_ano_conclusao  NUMERIC(6)
);

CREATE TABLE avaliacao (
  id_curso_ufg                                  BIGSERIAL   NOT NULL,
  id_egresso                                    BIGSERIAL   NOT NULL,
  data_avaliacao                                DATE        NOT NULL,
  satisfacao_curso                              VARCHAR(50) NOT NULL,
  motivacao_escolha_curso                       VARCHAR(50) NOT NULL,
  conceito_global_curso                         NUMERIC(2)  NOT NULL,
  preparacao_curso                              VARCHAR(50) NOT NULL,
  melhoria_capacidade_comunicacao               VARCHAR(50) NOT NULL,
  capacidade_curso_etica_resposabilidade_social VARCHAR(50) NOT NULL,
  capacidade_curso_habilidades_especificas      VARCHAR(50) NOT NULL,
  FOREIGN KEY (id_curso_ufg, id_egresso) REFERENCES historico_ufg (id_curso_ufg, id_egresso)
);


CREATE TABLE residencia (
  id_egresso     BIGSERIAL REFERENCES egresso (id_egresso),
  id_localizacao BIGSERIAL REFERENCES localizacao_geografica (id_localizacao),
  data_inicio    DATE UNIQUE,
  data_fim       DATE,
  endereco       VARCHAR(300)
);

