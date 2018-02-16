-- Como ajustar o script gerado pelo PowerDesigner:

-- Remover todos WITH FILLFACTOR= 95
-- Remover todos ON "PRIMARY"
-- Remover todos GO
-- Remover todos os COLLATE SQL_LATIN1_GENERAL_CP1_CI_AS 
-- Substituir VARCHAR(MAX) por VARCHAR(8000)
-- Incluir virgula depois de todos NULL fim de linha
-- Remover constraint primary key das tabelas com IDENTITY
-- inverter ordem do default
-- Colocar index no fim
-- Colocar script de insert no fim
-- Executar em comandos separados o DROP, depois os creates de tabela e schema, depois incluir FKs, depois os inserts

--SET DATABASE SQL SYNTAX MSS TRUE;

--DROP SCHEMA DBSINGULAR
--IF EXISTS CASCADE;
CREATE SCHEMA if not exists DBSINGULAR;

/*==============================================================*/
/* Table: RL_PERMISSAO_PROCESSO                                 */
/*==============================================================*/
CREATE TABLE DBSINGULAR.RL_PERMISSAO_PROCESSO (
  CO_DEFINICAO_PROCESSO BIGINT  NOT NULL,
  TP_PERMISSAO          CHAR(1) NOT NULL,
  CONSTRAINT CKC_TP_PERMISSAO_RL_PERMI CHECK (TP_PERMISSAO IN ('A', 'I', 'C', 'R')),
  CONSTRAINT PK_PERFIL_PROCESSO PRIMARY KEY (CO_DEFINICAO_PROCESSO, TP_PERMISSAO)
);

/*==============================================================*/
/* Table: RL_PERMISSAO_TAREFA                                   */
/*==============================================================*/
CREATE TABLE DBSINGULAR.RL_PERMISSAO_TAREFA (
   CO_PERMISSAO_TAREFA  BIGINT               IDENTITY,
   CO_DEFINICAO_TAREFA  BIGINT               NOT NULL,
   CO_PERMISSAO            VARCHAR(500)         NOT NULL,
   CONSTRAINT PK_PERMISSAO_TAREFA PRIMARY KEY (CO_PERMISSAO_TAREFA)
);


CREATE TABLE DBSINGULAR.TB_FUNCIONALIDADE_REQUISICAO
(
   CO_FUNCIONALIDADE    VARCHAR(300)        NOT NULL,
   CO_PERMISSAO         VARCHAR(50)         NOT NULL,
   CO_MODULO_SINGULAR   VARCHAR(50)         NOT NULL,
   CONSTRAINT PK_TB_FUNCIONALIDADE_REQUISICAO PRIMARY KEY (CO_FUNCIONALIDADE, CO_PERMISSAO, CO_MODULO_SINGULAR)
);

CREATE INDEX IX_FUNC_REQ_FUNCIONALIDADE ON DBSINGULAR.TB_FUNCIONALIDADE_REQUISICAO(CO_FUNCIONALIDADE);
CREATE INDEX IX_FUNC_REQ_PERMISSAO ON DBSINGULAR.TB_FUNCIONALIDADE_REQUISICAO(CO_PERMISSAO);
CREATE INDEX IX_FUNC_REQ_MODULO_SINGULAR ON DBSINGULAR.TB_FUNCIONALIDADE_REQUISICAO(CO_MODULO_SINGULAR);
CREATE INDEX IX_FUNC_REQ_PK ON DBSINGULAR.TB_FUNCIONALIDADE_REQUISICAO(CO_FUNCIONALIDADE, CO_PERMISSAO, CO_MODULO_SINGULAR);

/*==============================================================*/
/* Table: TB_ATOR                                               */
/*==============================================================*/
CREATE TABLE DBSINGULAR.TB_ATOR (
   CO_ATOR  BIGINT       IDENTITY,
   CO_USUARIO           VARCHAR(60)          NOT NULL,
  CONSTRAINT PK_ATOR PRIMARY KEY (CO_ATOR)
);

/*==============================================================*/
/* Table: TB_CATEGORIA                                          */
/*==============================================================*/
CREATE TABLE DBSINGULAR.TB_CATEGORIA (
  CO_CATEGORIA BIGINT IDENTITY,
  NO_CATEGORIA VARCHAR(100) NOT NULL,
  CONSTRAINT PK_CATEGORIA PRIMARY KEY (CO_CATEGORIA),
  CONSTRAINT AK_AK_CATEGORIA_TB_CATEG UNIQUE (NO_CATEGORIA)
);

/*==============================================================*/
/* Table: TB_DEFINICAO_PROCESSO                                 */
/*==============================================================*/
CREATE TABLE DBSINGULAR.TB_DEFINICAO_PROCESSO (
  CO_DEFINICAO_PROCESSO BIGINT IDENTITY,
  SG_PROCESSO           VARCHAR(200) NOT NULL,
  NO_PROCESSO           VARCHAR(200) NOT NULL,
  NO_CLASSE_JAVA        VARCHAR(500) NOT NULL,
  CO_CATEGORIA           BIGINT       NULL,
  CO_MODULO    VARCHAR(30)	NOT NULL,
  CONSTRAINT PK_DEFINICAO_PROCESSO PRIMARY KEY (CO_DEFINICAO_PROCESSO),
  CONSTRAINT AK_AK_DEFINICAO_PROCE_TB_DEFIN UNIQUE (SG_PROCESSO)
);

/*==============================================================*/
/* Table: TB_DEFINICAO_TAREFA                                   */
/*==============================================================*/
CREATE TABLE DBSINGULAR.TB_DEFINICAO_TAREFA (
  CO_DEFINICAO_TAREFA   BIGINT IDENTITY,
  CO_DEFINICAO_PROCESSO BIGINT       NOT NULL,
  SG_TAREFA             VARCHAR(100) NOT NULL,
  TP_ESTRATEGIA_SEGURANCA    CHAR(1)      NULL
      CONSTRAINT CKC_TP_ESTRATEGIA_SEG_TB_VERSA CHECK (TP_ESTRATEGIA_SEGURANCA IS NULL OR (TP_ESTRATEGIA_SEGURANCA IN ('D','E'))),
  CONSTRAINT PK_DEFINICAO_TAREFA PRIMARY KEY (CO_DEFINICAO_TAREFA),
  CONSTRAINT AK_AK_DEFINICAO_TAREF_TB_DEFIN UNIQUE (CO_DEFINICAO_PROCESSO, SG_TAREFA)
);

/*==============================================================*/
/* Table: TB_HISTORICO_INSTANCIA_TAREFA                         */
/*==============================================================*/
CREATE TABLE DBSINGULAR.TB_HISTORICO_INSTANCIA_TAREFA (
  CO_HISTORICO_ALOCACAO    BIGINT IDENTITY,
  CO_INSTANCIA_TAREFA      BIGINT        NOT NULL,
  DT_INICIO_ALOCACAO       DATETIME      NOT NULL,
  CO_TIPO_HISTORICO_TAREFA BIGINT        NOT NULL,
  DT_FIM_ALOCACAO          DATETIME      NULL,
  CO_ATOR_ALOCADO          BIGINT        NULL,
  CO_ATOR_ALOCADOR         BIGINT        NULL,
  DS_COMPLEMENTO           VARCHAR(8000) NULL,
  CONSTRAINT PK_HISTORICO_INSTANCIA_TAREFA PRIMARY KEY (CO_HISTORICO_ALOCACAO)
);

/*==============================================================*/
/* Table: TB_INSTANCIA_PAPEL                                    */
/*==============================================================*/
CREATE TABLE DBSINGULAR.TB_INSTANCIA_PAPEL (
  CO_INSTANCIA_PAPEL    BIGINT IDENTITY,
  CO_INSTANCIA_PROCESSO BIGINT   NOT NULL,
  CO_DEFINICAO_PAPEL              BIGINT   NOT NULL,
  CO_ATOR               BIGINT   NOT NULL,
  DT_CRIACAO            DATETIME NOT NULL,
  CO_ATOR_ALOCADOR      BIGINT   NULL,
  CONSTRAINT PK_INSTANCIA_PAPEL PRIMARY KEY (CO_INSTANCIA_PAPEL),
  CONSTRAINT AK_AK_INSTANCIA_PAPEL_TB_INSTA UNIQUE (CO_INSTANCIA_PROCESSO, CO_DEFINICAO_PAPEL)
);

/*==============================================================*/
/* Table: TB_INSTANCIA_PROCESSO                                 */
/*==============================================================*/
CREATE TABLE DBSINGULAR.TB_INSTANCIA_PROCESSO (
  CO_INSTANCIA_PROCESSO   BIGINT IDENTITY,
  CO_VERSAO_PROCESSO             BIGINT       NOT NULL,
  DT_INICIO               DATETIME     NOT NULL,
  DT_FIM                  DATETIME     NULL,
  DS_INSTANCIA_PROCESSO   VARCHAR(300) NULL,
  CO_ATOR_CRIADOR         BIGINT       NULL,
  CO_INSTANCIA_TAREFA_PAI BIGINT       NULL,
  CONSTRAINT PK_INSTANCIA_PROCESSO PRIMARY KEY (CO_INSTANCIA_PROCESSO)
);

/*==============================================================*/
/* Table: TB_INSTANCIA_TAREFA                                   */
/*==============================================================*/
CREATE TABLE DBSINGULAR.TB_INSTANCIA_TAREFA (
  CO_INSTANCIA_TAREFA    BIGINT IDENTITY,
  CO_INSTANCIA_PROCESSO  BIGINT   NOT NULL,
  CO_VERSAO_TAREFA              BIGINT   NOT NULL,
  DT_INICIO              DATETIME NOT NULL,
  DT_FIM                 DATETIME NULL,
  DT_ESPERADA_FIM        DATETIME NULL,
  CO_ATOR_ALOCADO        BIGINT   NULL,
  CO_ATOR_CONCLUSAO      BIGINT   NULL,
  CO_VERSAO_TRANSICAO_EXECUTADA BIGINT   NULL,
  NU_VERSAO              BIGINT NULL,
  CONSTRAINT PK_INSTANCIA_TAREFA PRIMARY KEY (CO_INSTANCIA_TAREFA)
);

/*==============================================================*/
/* Table: TB_DEFINICAO_PAPEL                                              */
/*==============================================================*/
CREATE TABLE DBSINGULAR.TB_DEFINICAO_PAPEL (
  CO_DEFINICAO_PAPEL              BIGINT IDENTITY,
  CO_DEFINICAO_PROCESSO BIGINT       NULL,
  SG_PAPEL              VARCHAR(100) NOT NULL,
  NO_PAPEL              VARCHAR(300) NOT NULL,
  CONSTRAINT PK_DEFINICAO_PAPEL PRIMARY KEY (CO_DEFINICAO_PAPEL),
  CONSTRAINT AK_AK_PAPEL_TB_DEFINICAO_PAPEL UNIQUE (CO_DEFINICAO_PROCESSO, SG_PAPEL)
);

/*==============================================================*/
/* Table: TB_VERSAO_PROCESSO                                           */
/*==============================================================*/
CREATE TABLE DBSINGULAR.TB_VERSAO_PROCESSO (
  CO_VERSAO_PROCESSO           BIGINT IDENTITY,
  CO_DEFINICAO_PROCESSO BIGINT   NOT NULL,
  DT_VERSAO             DATETIME NOT NULL,
  CONSTRAINT PK_VERSAO_PROCESSO PRIMARY KEY (CO_VERSAO_PROCESSO)
);

/*==============================================================*/
/* Table: TB_VERSAO_TAREFA                                             */
/*==============================================================*/
CREATE TABLE DBSINGULAR.TB_VERSAO_TAREFA (
  CO_VERSAO_TAREFA           BIGINT       IDENTITY,
  CO_VERSAO_PROCESSO         BIGINT       NOT NULL,
  CO_DEFINICAO_TAREFA        BIGINT       NOT NULL,
  NO_TAREFA                  VARCHAR(300) NOT NULL,
  CO_TIPO_TAREFA             BIGINT       NOT NULL,
  CONSTRAINT PK_VERSAO_TAREFA PRIMARY KEY (CO_VERSAO_TAREFA),
  CONSTRAINT AK_AK_TAREFA_TB_TAREF UNIQUE (CO_VERSAO_PROCESSO, CO_DEFINICAO_TAREFA),
  CONSTRAINT AK_AK_TAREFA_NOME_TB_TAREF UNIQUE (CO_VERSAO_PROCESSO, NO_TAREFA)
);

/*==============================================================*/
/* Table: TB_TIPO_HISTORICO_TAREFA                              */
/*==============================================================*/
CREATE TABLE DBSINGULAR.TB_TIPO_HISTORICO_TAREFA (
  CO_TIPO_HISTORICO_TAREFA BIGINT IDENTITY,
  DS_TIPO_HISTORICO_TAREFA VARCHAR(100) NOT NULL,
  CONSTRAINT PK_TIPO_HISTORICO_TAREFA PRIMARY KEY (CO_TIPO_HISTORICO_TAREFA),
  CONSTRAINT AK_AK_TIPO_HISTORICO__TB_TIPO_ UNIQUE (DS_TIPO_HISTORICO_TAREFA)
);

/*==============================================================*/
/* Table: TB_TIPO_TAREFA                                        */
/*==============================================================*/
CREATE TABLE DBSINGULAR.TB_TIPO_TAREFA (
  CO_TIPO_TAREFA BIGINT       IDENTITY,
  DS_TIPO_TAREFA VARCHAR(100) NOT NULL,
  CONSTRAINT PK_TIPO_TAREFA PRIMARY KEY (CO_TIPO_TAREFA),
  CONSTRAINT AK_AK_TIPO_TAREFA_TB_TIPO_ UNIQUE (DS_TIPO_TAREFA)
);

/*==============================================================*/
/* Table: TB_TIPO_VARIAVEL                                      */
/*==============================================================*/
CREATE TABLE DBSINGULAR.TB_TIPO_VARIAVEL (
  CO_TIPO_VARIAVEL BIGINT IDENTITY,
  NO_CLASSE_JAVA   VARCHAR(300) NOT NULL,
  DS_TIPO_VARIAVEL VARCHAR(100) NOT NULL,
  CONSTRAINT PK_TIPO_VARIAVEL PRIMARY KEY (CO_TIPO_VARIAVEL),
  CONSTRAINT AK_AK_TIPO_VARIAVEL_TB_TIPO_ UNIQUE (NO_CLASSE_JAVA)
);

/*==============================================================*/
/* Table: TB_VERSAO_TRANSICAO                                          */
/*==============================================================*/
CREATE TABLE DBSINGULAR.TB_VERSAO_TRANSICAO (
  CO_VERSAO_TRANSICAO      BIGINT IDENTITY,
  CO_VERSAO_TAREFA_ORIGEM  BIGINT              NOT NULL,
  CO_VERSAO_TAREFA_DESTINO BIGINT              NOT NULL,
  NO_TRANSICAO      VARCHAR(300)        NOT NULL,
  SG_TRANSICAO      VARCHAR(100)        NOT NULL,
  TP_TRANSICAO      CHAR(1) DEFAULT 'E' NOT NULL,
  CONSTRAINT CKC_TP_TRANSICAO_TB_TRANS CHECK (TP_TRANSICAO IN ('E', 'A', 'H')),
  CONSTRAINT PK_VERSAO_TRANSICAO PRIMARY KEY (CO_VERSAO_TRANSICAO),
  CONSTRAINT AK_AK_TRANSICAO_TB_TRANS UNIQUE (CO_VERSAO_TAREFA_ORIGEM, SG_TRANSICAO)
);

/*==============================================================*/
/* Table: TB_VARIAVEL                                           */
/*==============================================================*/
CREATE TABLE DBSINGULAR.TB_VARIAVEL (
  CO_VARIAVEL           BIGINT IDENTITY,
  CO_INSTANCIA_PROCESSO BIGINT        NOT NULL,
  NO_VARIAVEL           VARCHAR(100)  NOT NULL,
  CO_TIPO_VARIAVEL      BIGINT        NOT NULL,
  VL_VARIAVEL           VARCHAR(8000) NULL,
  CONSTRAINT PK_VARIAVEL PRIMARY KEY (CO_VARIAVEL),
  CONSTRAINT AK_AK_VARIAVEL_TB_VARIA UNIQUE (CO_INSTANCIA_PROCESSO, NO_VARIAVEL)
);

/*==============================================================*/
/* Table: TB_VARIAVEL_EXECUCAO_TRANSICAO                        */
/*==============================================================*/
CREATE TABLE DBSINGULAR.TB_VARIAVEL_EXECUCAO_TRANSICAO (
  CO_VARIAVEL_EXECUCAO_TRANSICAO BIGINT IDENTITY,
  CO_INSTANCIA_TAREFA_ORIGEM     BIGINT        NOT NULL,
  NO_VARIAVEL                    VARCHAR(100)  NOT NULL,
  CO_INSTANCIA_TAREFA_DESTINO    BIGINT        NOT NULL,
  CO_INSTANCIA_PROCESSO          BIGINT        NOT NULL,
  VL_NOVO                        VARCHAR(8000) NULL,
  DT_HISTORICO                   DATETIME      NOT NULL,
  CO_TIPO_VARIAVEL               BIGINT        NOT NULL,
  CO_VARIAVEL                    BIGINT        NULL,
  CONSTRAINT PK_VARIAVEL_EXECUCAO_TRANSICAO PRIMARY KEY (CO_VARIAVEL_EXECUCAO_TRANSICAO),
  CONSTRAINT AK_AK_VARIAVEL_EXECUC_TB_VARIA UNIQUE (CO_INSTANCIA_TAREFA_ORIGEM, NO_VARIAVEL)
);

/*==============================================================*/
/* Table: TB_MODULO                                     */
/*==============================================================*/
CREATE TABLE DBSINGULAR.TB_MODULO (
   CO_MODULO    VARCHAR(30) NOT NULL,
   NO_MODULO             VARCHAR(100)         NOT NULL,
   URL_CONEXAO          VARCHAR(300)         NOT NULL,
   CONSTRAINT PK_GRUPO PRIMARY KEY (CO_MODULO)
);


/*==============================================================*/
/* Table: TB_REQUISITANTE                                       */
/*==============================================================*/


CREATE TABLE DBSINGULAR.TB_REQUISITANTE (
   CO_REQUISITANTE      BIGINT               IDENTITY,
   DS_NOME              VARCHAR(200)         NOT NULL,
   ID_PESSOA            VARCHAR(32)          NOT NULL,
   NU_CPF_CNPJ          VARCHAR(14)          NULL,
   TP_PESSOA            CHAR(1)              NOT NULL DEFAULT 'J'
      CONSTRAINT CKC_TP_PESSOA_TB_PETIC CHECK (TP_PESSOA IN ('J','F')),
   CONSTRAINT PK_TB_REQUISITANTE PRIMARY KEY (CO_REQUISITANTE)
);

/*==============================================================*/
/* Table: RL_PAPEL_TAREFA                                       */
/*==============================================================*/
CREATE TABLE DBSINGULAR.RL_PAPEL_TAREFA (
   CO_PAPEL_TAREFA      BIGINT               IDENTITY,
   CO_DEFINICAO_PAPEL   BIGINT               NOT NULL,
   CO_DEFINICAO_TAREFA  BIGINT               NOT NULL,
   CONSTRAINT PK_RL_PAPEL_TAREFA PRIMARY KEY (CO_PAPEL_TAREFA)
);

/*==============================================================*/
/* Table: TB_RASCUNHO                                           */
/*==============================================================*/



CREATE TABLE DBSINGULAR.TB_RASCUNHO (
   CO_RASCUNHO           BIGINT               IDENTITY,
   CO_FORMULARIO         BIGINT               NOT NULL,
   DT_INICIO             SMALLDATETIME        NOT NULL,
   DT_EDICAO             SMALLDATETIME        NOT NULL,
   CONSTRAINT PK_TB_RASCUNHO PRIMARY KEY (CO_RASCUNHO)
);

/*==============================================================*/
/* Table: TB_REQUISICAO                                         */
/*==============================================================*/
CREATE TABLE DBSINGULAR.TB_REQUISICAO
(
   CO_REQUISICAO        NUMBER               IDENTITY,
   CO_INSTANCIA_PROCESSO NUMBER               NULL,
   CO_DEFINICAO_PROCESSO NUMBER               NULL,
   CO_REQUISITANTE      NUMBER                NULL,
   DS_REQUISICAO        VARCHAR2(200),
   CO_REQUISICAO_RAIZ   NUMBER,
   CO_REQUISICAO_PAI    NUMBER,
   CO_DEFINICAO_REQUISICAO NUMBER               NOT NULL,
   CONSTRAINT PK_REQUISICAO PRIMARY KEY (CO_REQUISICAO)
);


/*==============================================================*/
/* TB_HISTORICO_CONTEUDO_REQUISIC                               */
/*==============================================================*/

CREATE TABLE DBSINGULAR.TB_HISTORICO_CONTEUDO_REQUISIC (
   CO_HISTORICO         BIGINT                  IDENTITY,
   CO_REQUISICAO           BIGINT                  NOT NULL,
   DT_HISTORICO         SMALLDATETIME        NOT NULL,
   CO_AUTOR             BIGINT                  NULL,
   CO_INSTANCIA_TAREFA  BIGINT                  NULL,
   CO_REQUISITANTE      BIGINT                  NULL,
   CONSTRAINT PK_TB_HISTORICO_CONTEUDO_PETIC PRIMARY KEY (CO_HISTORICO)
);

/*==============================================================*/
/* Table: RL_HIST_CONT_REQ_VER_ANOTACAO                         */
/*==============================================================*/
CREATE TABLE DBSINGULAR.RL_HIST_CONT_REQ_VER_ANOTACAO
(
   CO_HISTORICO         INTEGER              NOT NULL,
   CO_VERSAO_ANOTACAO   INTEGER              NOT NULL,
   CONSTRAINT PK_RL_HIST_CONT_PET_VER_ANOTAC PRIMARY KEY (CO_HISTORICO, CO_VERSAO_ANOTACAO)
);

ALTER TABLE DBSINGULAR.RL_HIST_CONT_REQ_VER_ANOTACAO
   ADD CONSTRAINT FK_RL_HIST__REFERENCE_TB_HISTO FOREIGN KEY (CO_HISTORICO)
      REFERENCES DBSINGULAR.TB_HISTORICO_CONTEUDO_REQUISIC (CO_HISTORICO);

ALTER TABLE DBSINGULAR.RL_HIST_CONT_REQ_VER_ANOTACAO
   ADD CONSTRAINT FK_RL_HIST__REFERENCE_TB_VERSA FOREIGN KEY (CO_VERSAO_ANOTACAO)
      REFERENCES DBSINGULAR.TB_VERSAO_ANOTACAO_FORMULARIO (CO_VERSAO_ANOTACAO);


/*==============================================================*/
/* Table: TB_FORMULARIO_REQUISICAO                              */
/*==============================================================*/

CREATE TABLE DBSINGULAR.TB_FORMULARIO_REQUISICAO
(
   CO_FORMULARIO_REQUISICAO INTEGER            IDENTITY,
   CO_REQUISICAO           INTEGER              NOT NULL,
   CO_FORMULARIO        INTEGER              NULL,
   CO_DEFINICAO_TAREFA  INTEGER,
   ST_FORM_PRINCIPAL    CHAR                 NOT NULL,
   CO_RASCUNHO_ATUAL    BIGINT               NULL,
   CONSTRAINT CKC_ST_FORM_PRINCIPAL_TB_FORMU CHECK (ST_FORM_PRINCIPAL IN ('S','N')),
   CONSTRAINT PK_TB_FORMULARIO_REQUISICAO PRIMARY KEY (CO_FORMULARIO_REQUISICAO)
);


/*==============================================================*/
/* Table: RL_VERSAO_FORMULARIO_HISTORICO                        */
/*==============================================================*/
CREATE TABLE DBSINGULAR.TB_HISTORICO_VERSAO_FORMULARIO
(
   CO_HISTORICO         INTEGER              NOT NULL,
   CO_VERSAO_FORMULARIO INTEGER              NOT NULL,
   ST_FORM_PRINCIPAL    CHAR                 NOT NULL,
   CONSTRAINT PK_HISTORICO_VERSAO_FORMULARIO PRIMARY KEY (CO_HISTORICO, CO_VERSAO_FORMULARIO)
);

/*==============================================================*/
/* Table: TB_PARAMETRO                                          */
/*==============================================================*/
CREATE TABLE DBSINGULAR.TB_PARAMETRO
(
   CO_PARAMETRO         INTEGER              IDENTITY,
   CO_MODULO    VARCHAR2(30),
   NO_PARAMETRO         VARCHAR2(100)        NOT NULL,
   VL_PARAMETRO         VARCHAR2(1000)       NOT NULL,
   CONSTRAINT PK_PARAMETRO PRIMARY KEY (CO_PARAMETRO)
);

/*==============================================================*/
/* Table: TB_CAIXA                                              */
/*==============================================================*/
CREATE TABLE DBSINGULAR.TB_CAIXA
(
   CO_CAIXA             NUMBER               IDENTITY,
   CO_MODULO            VARCHAR2(30)         NOT NULL,
   NO_CAIXA             VARCHAR(100)         NOT NULL,
   DS_CAIXA             VARCHAR(500)         NULL,
   NO_ICONE             VARCHAR(100)         NOT NULL,
   CONSTRAINT PK_CAIXA PRIMARY KEY (CO_CAIXA)
);

/*==============================================================*/
/* Table: TB_DEFINICAO_REQUISICAO                               */
/*==============================================================*/
CREATE TABLE DBSINGULAR.TB_DEFINICAO_REQUISICAO
(
   CO_DEFINICAO_REQUISICAO NUMBER              IDENTITY,
   CO_TIPO_FORMULARIO        NUMBER               NOT NULL,
   CO_MODULO            VARCHAR2(30)         NOT NULL,
   NO_DEFINICAO_REQUISICAO VARCHAR2(300)        NOT NULL,
   CONSTRAINT PK_DEFINICAO_REQUISICAO PRIMARY KEY (CO_DEFINICAO_REQUISICAO)
);