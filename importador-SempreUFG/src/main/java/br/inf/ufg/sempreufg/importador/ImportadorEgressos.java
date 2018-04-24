package br.inf.ufg.sempreufg.importador;

import br.inf.ufg.sempreufg.conexao.DatabaseConnection;
import br.inf.ufg.sempreufg.excecao.ConsultaSemRetornoException;

import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/*
*
*
* */

public class ImportadorEgressos {

    private final static String NOME = "NOME";
    private final static String TIPO_DOCUMENTO = "TIPO_DOCUMENTO";
    private final static String NUMERO_DOCUMENTO = "NUMERO_DOCUMENTO";
    private final static String DATA_NASCIMENTO = "DATA_NASCIMENTO";
    private final static String NOME_CURSO = "NOME_CURSO";
    private final static String MES_ANO_INGRESSO = "MES_ANO_INGRESSO";
    private final static String MES_ANO_CONCLUSAO = "MES_ANO_CONCLUSAO";
    private final static String MATRICULA = "MATRICULA";
    private final static String TITULO_TCC = "TITULO_TCC";
    private final static String TIPO_PROGRAMA_ACADEMICO = "TIPO_PROGRAMA_ACADEMICO";
    private final static String DATA_INICIO_PROGRAMA_ACADEMICO = "DATA_INICIO_PROGRAMA_ACADEMICO";
    private final static String DATA_FIM_PROGRAMA_ACADEMICO = "DATA_FIM_PROGRAMA_ACADEMICO";
    private final static String DESCRICAO_PROGRAMA_ACADEMICO = "DESCRICAO_PROGRAMA_ACADEMICO";
    private static Connection connection;
    private static Statement statement;
    private static PrintWriter arquivoRelatoImportacao;
    private static int egressosAdicionados = 0;

    public static void main(String[] args) {

        String caminhoArquivo;
        if (args.length == 0) {
            caminhoArquivo = "";
        } else {
            caminhoArquivo = args[0];
        }
        connection = DatabaseConnection.getInstancia().getConnection();

        try {
            File file = new File("Relato de Importacao.txt");
            arquivoRelatoImportacao = new PrintWriter(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            connection.setAutoCommit(false);
            statement = connection.createStatement();
            lerArquivoTxt(caminhoArquivo);
            connection.commit();
            arquivoRelatoImportacao.write(egressosAdicionados + " egressos foram adicionados com sucesso.");
            arquivoRelatoImportacao.flush();
        } catch (Exception e) {
            try {
                connection.rollback();
                arquivoRelatoImportacao.flush();
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
            }
        }
        System.out.println("Programa Finalizado");
    }

    private static void lerArquivoTxt(String caminhoArquivo) throws Exception {
        try {
            FileReader fileReader = new FileReader(caminhoArquivo + "Egressos-para-Importar.txt");
            BufferedReader br = new BufferedReader(fileReader);

            String line = br.readLine();
            while (line != null) {
                String[] fields = line.split("\\\\");

                Map<String, String> camposObtidos;
                if ("Reg.1".equals(fields[0])) {
                    camposObtidos = converterCamposEgressosParaMapa(fields);
                    salvarEgresso(camposObtidos);
                } else if ("Reg.2".equals(fields[0])) {
                    camposObtidos = converterCamposProgramaAcademicoParaMapa(fields);
                    salvarProgramaAcademico(camposObtidos);
                } else {
                    arquivoRelatoImportacao.write("Tipo de Registro inexistente.");
                    throw new Exception();
                }

                line = br.readLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Map<String, String> converterCamposEgressosParaMapa(String[] fields) throws Exception {
        Map<String, String> campos = new HashMap<String, String>();

        if (!isDataValida(fields[4])) {
            arquivoRelatoImportacao.write("Data no formato inválido. Por favor utilize dd/MM/yyyy.");
            throw new Exception();
        }

        campos.put(NOME, fields[1]);
        campos.put(TIPO_DOCUMENTO, fields[2]);
        campos.put(NUMERO_DOCUMENTO, fields[3]);
        campos.put(DATA_NASCIMENTO, fields[4]);
        campos.put(NOME_CURSO, fields[5]);
        campos.put(MES_ANO_INGRESSO, fields[6]);
        campos.put(MES_ANO_CONCLUSAO, fields[7]);
        campos.put(MATRICULA, fields[8]);
        campos.put(TITULO_TCC, fields[9]);
        return campos;
    }

    private static Map<String, String> converterCamposProgramaAcademicoParaMapa(String[] fields) throws Exception {
        Map<String, String> campos = new HashMap<String, String>();

        if (!isDataValida(fields[5]) || !isDataValida(fields[6])) {
            arquivoRelatoImportacao.write("Data no formato inválido. Por favor utilize dd/MM/yyyy.");
            throw new Exception();
        }

        campos.put(TIPO_DOCUMENTO, fields[1]);
        campos.put(NUMERO_DOCUMENTO, fields[2]);
        campos.put(NOME_CURSO, fields[3]);
        campos.put(TIPO_PROGRAMA_ACADEMICO, fields[4]);
        campos.put(DATA_INICIO_PROGRAMA_ACADEMICO, fields[5]);
        campos.put(DATA_FIM_PROGRAMA_ACADEMICO, fields[6]);
        campos.put(DESCRICAO_PROGRAMA_ACADEMICO, fields[7]);
        return campos;
    }

    private static void salvarEgresso(Map<String, String> camposEgressos) throws SQLException, ConsultaSemRetornoException {
        String nomeEgresso = camposEgressos.get(NOME);
        String tipoDocumento = camposEgressos.get(TIPO_DOCUMENTO);
        String numeroDocumento = camposEgressos.get(NUMERO_DOCUMENTO);
        String dataNascimento = camposEgressos.get(DATA_NASCIMENTO);
        String nomeCurso = camposEgressos.get(NOME_CURSO);
        String mesAnoIngresso = camposEgressos.get(MES_ANO_INGRESSO);
        String mesAnoConclusao = camposEgressos.get(MES_ANO_CONCLUSAO);
        String matricula = camposEgressos.get(MATRICULA);
        String tituloTCC = camposEgressos.get(TITULO_TCC);

        long idCurso = getIdCursoByNome(nomeCurso);

        if (idCurso != 0) {
            long idLocalizacao = getIdLocalizacao();
            if (idLocalizacao != 0) {
                long idEgresso = insertEgressoReturningId(nomeEgresso, numeroDocumento, tipoDocumento,
                        dataNascimento, idLocalizacao);
                if (idEgresso != 0) {
                    long idEgressoHistorico = insertHistoricoDeEgressoReturningIdEgresso(idCurso, idEgresso,
                            Integer.valueOf(mesAnoIngresso), Integer.valueOf(mesAnoConclusao), matricula,
                            tituloTCC);
                    if (idEgressoHistorico != 0) {
                        egressosAdicionados++;
                    }
                }
            }
        }
    }

    private static void salvarProgramaAcademico(Map<String, String> camposEgressos) throws SQLException, ConsultaSemRetornoException {
        String tipoDocumentoEgresso = camposEgressos.get(TIPO_DOCUMENTO);
        String numeroDocumentoEgresso = camposEgressos.get(NUMERO_DOCUMENTO);
        String nomeCurso = camposEgressos.get(NOME_CURSO);
        String tipo = camposEgressos.get(TIPO_PROGRAMA_ACADEMICO);
        String dataInicio = camposEgressos.get(DATA_INICIO_PROGRAMA_ACADEMICO);
        String dataFim = camposEgressos.get(DATA_FIM_PROGRAMA_ACADEMICO);
        String descricao = camposEgressos.get(DESCRICAO_PROGRAMA_ACADEMICO);

        long idCurso = getIdCursoByNome(nomeCurso);
        if (idCurso != 0) {
            long idEgresso = getIdEgressoByTipoDocumentoAndNumeroDocumento(tipoDocumentoEgresso, numeroDocumentoEgresso);
            if (idEgresso != 0) {
                insertProgramaAcademicoReturningIdCurso(idCurso, idEgresso, tipo, dataInicio, dataFim, descricao);
            }
        }
    }

    private static String montaSqlEgresso(String nome, String numero_documento_identidade, String tipo_documento_identidade,
                                          String dataNascimento, Long idLocalizacao) {

        /*Visibilidade dos dados não foi passado no arquivo de importação. Usando Privado como padrão.
        * Localização geográfica adicionada na carga do banco usada como padrão.
        * */
        return "INSERT INTO egresso (" +
                "nome," +
                "numero_documento_identidade," +
                "tipo_documento_identidade," +
                "data_nascimento," +
                "visibilidade_dados," +
                "localizacao_naturalidade" +
                ") VALUES (" +
                concatenaVirgulaNoTexto(nome) +
                concatenaVirgulaNoTexto(numero_documento_identidade) +
                concatenaVirgulaNoTexto(tipo_documento_identidade) +
                "to_date('" + dataNascimento + "', 'dd/MM/yyyy')," +
                concatenaVirgulaNoTexto("Privado") +
                idLocalizacao +
                ")" +
                "RETURNING id_egresso;";
    }

    private static String montaSqlHistoricoUfg(long idCurso, long idEgresso, long mesAnoIngresso,
                                               long mesAnoInclusao, String matricula, String titulo_tcc) {
        return "INSERT INTO historico_ufg (" +
                "id_curso_ufg," +
                "id_egresso," +
                "mes_ano_ingresso," +
                "mes_ano_conclusao," +
                "matricula," +
                "titulo_trabalho_final" +
                ") VALUES (" +
                concatenaVirgulaNoTexto(String.valueOf(idCurso)) +
                concatenaVirgulaNoTexto(String.valueOf(idEgresso)) +
                concatenaVirgulaNoTexto(mesAnoIngresso) +
                concatenaVirgulaNoTexto(mesAnoInclusao) +
                concatenaVirgulaNoTexto(matricula) +
                "'" + titulo_tcc + "'" +
                ")" +
                "RETURNING id_curso_ufg;";
    }

    private static String concatenaVirgulaNoTexto(String texto) {
        return "'" + texto + "',";
    }

    private static String concatenaVirgulaNoTexto(long numero) {
        return numero + ",";
    }

    private static long getIdLocalizacao() throws SQLException {
        long idLocalizacao = 0;
        try {
            ResultSet resultIdLocalizacao = statement.executeQuery("(SELECT id_localizacao AS id FROM localizacao_geografica LIMIT 1)");
            if (resultIdLocalizacao.next()) {
                idLocalizacao = resultIdLocalizacao.getLong("id");
            }
        } catch (SQLException e) {
            arquivoRelatoImportacao.write("Localização não encontrada. Exception: " + e.getMessage());
            throw e;
        }

        return idLocalizacao;
    }

    private static long insertEgressoReturningId(String nomeEgresso, String numeroDocumento, String tipoDocumento, String dataNascimento,
                                                 long idLocalizacao) throws SQLException {
        long idEgresso = 0;
        try {
            String sqlEgresso = montaSqlEgresso(nomeEgresso, numeroDocumento, tipoDocumento, dataNascimento, idLocalizacao);
            ResultSet rsInsertEgresso = statement.executeQuery(sqlEgresso);
            if (rsInsertEgresso.next()) {
                idEgresso = rsInsertEgresso.getLong("id_egresso");
            }
        } catch (SQLException e) {
            arquivoRelatoImportacao.write("Não foi possível inserir o egresso. Exception: " + e.getMessage());
            throw e;
        }
        return idEgresso;
    }

    private static long getIdCursoByNome(String nomeCurso) throws SQLException, ConsultaSemRetornoException {
        long idCurso = 0;

        String sqlObterIdCurso = "SELECT id_curso_ufg AS id FROM curso_ufg where nome_curso = '" + nomeCurso + "'";
        try {
            ResultSet resultIdCurso = statement.executeQuery(sqlObterIdCurso);
            if (resultIdCurso.next()) {
                idCurso = resultIdCurso.getLong("id");
            } else {
                arquivoRelatoImportacao.write("Nenhum curso com o nome: " + nomeCurso + " foi encontrado.");
                throw new ConsultaSemRetornoException();
            }
        } catch (SQLException e) {
            arquivoRelatoImportacao.write("Erro ao tentar obter id do curso. Exception: " + e.getMessage());
            throw e;
        }

        return idCurso;
    }

    private static long getIdEgressoByTipoDocumentoAndNumeroDocumento(String tipoDocumento, String numeroDocumento) throws SQLException {
        long idEgresso = 0;
        try {
            String sqlObterIdCurso = "SELECT id_egresso AS id FROM egresso " +
                    "where tipo_documento_identidade = '" + tipoDocumento + "'" +
                    "AND numero_documento_identidade = '" + numeroDocumento + "'";
            ResultSet resultIdCurso = statement.executeQuery(sqlObterIdCurso);
            if (resultIdCurso.next()) {
                idEgresso = resultIdCurso.getLong("id");
            }
        } catch (SQLException e) {
            arquivoRelatoImportacao.write("Nenhum egresso com o documento do tipo: " + tipoDocumento + "" +
                    "e com número" + numeroDocumento + "foi encontrado. Exception: " + e.getMessage());
            throw e;
        }
        return idEgresso;
    }

    private static long insertHistoricoDeEgressoReturningIdEgresso(long idCurso, long idEgresso, int mesAnoIngresso,
                                                                   int mesAnoConclusao, String matricula,
                                                                   String tituloTCC) throws SQLException {

        long idEgressoHistorico = 0;
        try {
            String sqlHistoricoUFG = montaSqlHistoricoUfg(idCurso, idEgresso, mesAnoIngresso,
                    mesAnoConclusao, matricula, tituloTCC);
            ResultSet rsHistoricoUFG = statement.executeQuery(sqlHistoricoUFG);
            if (rsHistoricoUFG.next()) {
                idEgressoHistorico = rsHistoricoUFG.getLong("id_curso_ufg");
            }
        } catch (SQLException e) {
            arquivoRelatoImportacao.write("Erro ao tentar inserir histórico na UFG. Exception: " + e.getMessage());
            throw e;
        }
        return idEgressoHistorico;
    }

    private static String montaSqlProgramaAcademico(long idCurso, long idEgresso, String tipoProgramaAcademico,
                                                    String dataInicio, String dataFim, String descricao) {
        return "INSERT INTO realizacao_programa_academico (" +
                "id_curso_ufg," +
                "id_egresso," +
                "tipo," +
                "data_inicio," +
                "data_fim," +
                "  descricao" +
                ") VALUES (" +
                concatenaVirgulaNoTexto(idCurso) +
                concatenaVirgulaNoTexto(idEgresso) +
                concatenaVirgulaNoTexto(tipoProgramaAcademico) +
                "to_date('" + dataInicio + "', 'dd/MM/yyyy')," +
                "to_date('" + dataFim + "', 'dd/MM/yyyy')," +
                "'" + descricao + "'" +
                ")" +
                "RETURNING id_curso_ufg;";
    }

    private static void insertProgramaAcademicoReturningIdCurso(long idCurso, long idEgresso, String tipoProgramaAcademico,
                                                                String dataInicio, String dataFim, String descricao) throws SQLException {
        String sqlInserirProgramaAcademico = montaSqlProgramaAcademico(idCurso, idEgresso, tipoProgramaAcademico, dataInicio,
                dataFim, descricao);
        try {
            statement.executeQuery(sqlInserirProgramaAcademico);
        } catch (SQLException e) {
            arquivoRelatoImportacao.write("Não foi possível inserir o programa acadêmico. Exception: " + e.getMessage());
            throw e;
        }
    }

    private static boolean isDataValida(String campoData) {
        Date date;
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
            date = simpleDateFormat.parse(campoData);
            if (!campoData.equals(simpleDateFormat.format(date))) {
                date = null;
            }
        } catch (ParseException e) {
            return false;
        }

        return date != null;
    }

}
