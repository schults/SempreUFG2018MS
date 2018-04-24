INSERT INTO localizacao_geografica (
  nome_cidade,
  nome_cidade_estrangeiro,
  nome_unidade_federativa,
  nome_unidade_federativa_estrangeiro,
  nome_pais,
  nome_pais_estrangeiro,
  sigla_unidade_federativa
) VALUES (
  'Goiânia',
  'Goiânia',
  'Goiás',
  'Goiás',
  'Brasil',
  'Brasil',
  'GO'
);

INSERT INTO area_conhecimento (codigo_area, nome_area) VALUES (
  12345,
  'Ciências Exatas e da Terra'
);

INSERT INTO unidade_academica_ufg (regional, nome)
VALUES ('Goiânia-Câmpus Colemar Natal e Silva', 'Instituto de Informática');


INSERT INTO curso_ufg (
  nome_curso,
  nivel,
  data_criacao,
  presencial,

  turno,
  area_conhecimento, unidade_academica
) VALUES (
  'Engenharia de Software',
  'Bacharelado',
  to_date('01/01/2012', 'dd/MM/yyyy'),
  TRUE,
  'Matutino',
  (SELECT codigo_area
   FROM area_conhecimento
   LIMIT 1),
  (SELECT id_unidade_academica
   FROM unidade_academica_ufg
   WHERE nome = 'Instituto de Informática')
);
