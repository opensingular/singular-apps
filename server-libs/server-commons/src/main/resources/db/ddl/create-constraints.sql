-- Scripts de inserção FKs

ALTER TABLE DBSINGULAR.RL_PERMISSAO_PROCESSO
ADD CONSTRAINT FK_PERMISSAO_PROCESSO_DEFINICAO_PROCESSO FOREIGN KEY (CO_DEFINICAO_PROCESSO)
REFERENCES DBSINGULAR.TB_DEFINICAO_PROCESSO (CO_DEFINICAO_PROCESSO);

ALTER TABLE DBSINGULAR.RL_PERMISSAO_TAREFA
ADD CONSTRAINT FK_PERMISSAO_TAREFA_DEFINICAO_TAREFA FOREIGN KEY (CO_DEFINICAO_TAREFA)
REFERENCES DBSINGULAR.TB_DEFINICAO_TAREFA (CO_DEFINICAO_TAREFA);

ALTER TABLE DBSINGULAR.TB_DEFINICAO_PROCESSO
ADD CONSTRAINT FK_DEFINICAO_PROCESSO_CATEGORIA FOREIGN KEY (CO_CATEGORIA)
REFERENCES DBSINGULAR.TB_CATEGORIA (CO_CATEGORIA);

ALTER TABLE DBSINGULAR.TB_DEFINICAO_TAREFA
ADD CONSTRAINT FK_TB_DEFIN_FK_DEFINI_TB_DEFIN FOREIGN KEY (CO_DEFINICAO_PROCESSO)
REFERENCES DBSINGULAR.TB_DEFINICAO_PROCESSO (CO_DEFINICAO_PROCESSO);

ALTER TABLE DBSINGULAR.TB_HISTORICO_INSTANCIA_TAREFA
ADD CONSTRAINT FK_DMD_HISTORICO_ALOCACAO_TAREFA_CAD_PESSOA1 FOREIGN KEY (CO_ATOR_ALOCADO)
REFERENCES DBSINGULAR.TB_ATOR (CO_ATOR);

ALTER TABLE DBSINGULAR.TB_HISTORICO_INSTANCIA_TAREFA
ADD CONSTRAINT FK_DMD_HISTORICO_ALOCACAO_TAREFA_CAD_PESSOA2 FOREIGN KEY (CO_ATOR_ALOCADOR)
REFERENCES DBSINGULAR.TB_ATOR (CO_ATOR);

ALTER TABLE DBSINGULAR.TB_HISTORICO_INSTANCIA_TAREFA
ADD CONSTRAINT FK_DMD_HISTORICO_ALOCACAO_TAREFA_DMD_TAREFA FOREIGN KEY (CO_INSTANCIA_TAREFA)
REFERENCES DBSINGULAR.TB_INSTANCIA_TAREFA (CO_INSTANCIA_TAREFA)
  ON DELETE CASCADE;

ALTER TABLE DBSINGULAR.TB_HISTORICO_INSTANCIA_TAREFA
ADD CONSTRAINT FK_DMD_HISTORICO_ALOCACAO_TAREFA_DMD_TIPO_HISTORICO_VERSAO_TAREFA FOREIGN KEY (CO_TIPO_HISTORICO_TAREFA)
REFERENCES DBSINGULAR.TB_TIPO_HISTORICO_TAREFA (CO_TIPO_HISTORICO_TAREFA);

ALTER TABLE DBSINGULAR.TB_INSTANCIA_PAPEL
ADD CONSTRAINT FK_DMD_PAPEL_DEMANDA_CAD_PESSOA FOREIGN KEY (CO_ATOR)
REFERENCES DBSINGULAR.TB_ATOR (CO_ATOR);

ALTER TABLE DBSINGULAR.TB_INSTANCIA_PAPEL
ADD CONSTRAINT FK_DMD_PAPEL_DEMANDA_CAD_PESSOA1 FOREIGN KEY (CO_ATOR_ALOCADOR)
REFERENCES DBSINGULAR.TB_ATOR (CO_ATOR);

ALTER TABLE DBSINGULAR.TB_INSTANCIA_PAPEL
ADD CONSTRAINT FK_DMD_PAPEL_DEMANDA_DMD_DEMANDA FOREIGN KEY (CO_INSTANCIA_PROCESSO)
REFERENCES DBSINGULAR.TB_INSTANCIA_PROCESSO (CO_INSTANCIA_PROCESSO);

ALTER TABLE DBSINGULAR.TB_INSTANCIA_PAPEL
ADD CONSTRAINT FK_DMD_PAPEL_DEMANDA_DMD_PAPEL FOREIGN KEY (CO_DEFINICAO_PAPEL)
REFERENCES DBSINGULAR.TB_DEFINICAO_PAPEL (CO_DEFINICAO_PAPEL);

ALTER TABLE DBSINGULAR.TB_INSTANCIA_PROCESSO
ADD CONSTRAINT FK_INSTANCIA_PROCESSO_ATOR_CRIADOR FOREIGN KEY (CO_ATOR_CRIADOR)
REFERENCES DBSINGULAR.TB_ATOR (CO_ATOR);

ALTER TABLE DBSINGULAR.TB_INSTANCIA_PROCESSO
ADD CONSTRAINT FK_DMD_DEMANDA_DMD_TAREFA FOREIGN KEY (CO_INSTANCIA_TAREFA_PAI)
REFERENCES DBSINGULAR.TB_INSTANCIA_TAREFA (CO_INSTANCIA_TAREFA);

ALTER TABLE DBSINGULAR.TB_INSTANCIA_PROCESSO
ADD CONSTRAINT FK_DMD_DEMANDA_DMD_DEFINICAO FOREIGN KEY (CO_VERSAO_PROCESSO)
REFERENCES DBSINGULAR.TB_VERSAO_PROCESSO (CO_VERSAO_PROCESSO);

ALTER TABLE DBSINGULAR.TB_INSTANCIA_PROCESSO
ADD CONSTRAINT FK_TB_INSTA_REFERENCE_TB_TAREF FOREIGN KEY (CO_VERSAO_TAREFA_ATUAL)
REFERENCES DBSINGULAR.TB_DEFINICAO_TAREFA (CO_DEFINICAO_TAREFA);

ALTER TABLE DBSINGULAR.TB_INSTANCIA_TAREFA
ADD CONSTRAINT FK_DMD_TAREFA_CAD_PESSOA1 FOREIGN KEY (CO_ATOR_ALOCADO)
REFERENCES DBSINGULAR.TB_ATOR (CO_ATOR);

ALTER TABLE DBSINGULAR.TB_INSTANCIA_TAREFA
ADD CONSTRAINT FK_DMD_TAREFA_CAD_PESSOA2 FOREIGN KEY (CO_ATOR_CONCLUSAO)
REFERENCES DBSINGULAR.TB_ATOR (CO_ATOR);

ALTER TABLE DBSINGULAR.TB_INSTANCIA_TAREFA
ADD CONSTRAINT FK_DMD_TAREFA_DMD_DEMANDA1 FOREIGN KEY (CO_INSTANCIA_PROCESSO)
REFERENCES DBSINGULAR.TB_INSTANCIA_PROCESSO (CO_INSTANCIA_PROCESSO)
  ON DELETE CASCADE;

ALTER TABLE DBSINGULAR.TB_INSTANCIA_TAREFA
ADD CONSTRAINT FK_DMD_TAREFA_DMD_SITUACAO FOREIGN KEY (CO_VERSAO_TAREFA)
REFERENCES DBSINGULAR.TB_VERSAO_TAREFA (CO_VERSAO_TAREFA);

ALTER TABLE DBSINGULAR.TB_INSTANCIA_TAREFA
ADD CONSTRAINT FK_TB_INSTA_FK_INSTAN_TB_TRANS FOREIGN KEY (CO_VERSAO_TRANSICAO_EXECUTADA)
REFERENCES DBSINGULAR.TB_VERSAO_TRANSICAO (CO_VERSAO_TRANSICAO);

ALTER TABLE DBSINGULAR.TB_DEFINICAO_PAPEL
ADD CONSTRAINT FK_PAPEL_DEFINICAO_PROCESSO FOREIGN KEY (CO_DEFINICAO_PROCESSO)
REFERENCES DBSINGULAR.TB_DEFINICAO_PROCESSO (CO_DEFINICAO_PROCESSO);

ALTER TABLE DBSINGULAR.TB_VERSAO_PROCESSO
ADD CONSTRAINT FK_TB_PROCE_FK_PROCES_TB_DEFIN FOREIGN KEY (CO_DEFINICAO_PROCESSO)
REFERENCES DBSINGULAR.TB_DEFINICAO_PROCESSO (CO_DEFINICAO_PROCESSO);

ALTER TABLE DBSINGULAR.TB_VERSAO_TAREFA
ADD CONSTRAINT FK_TB_TAREF_FK_TAREFA_TB_DEFIN FOREIGN KEY (CO_DEFINICAO_TAREFA)
REFERENCES DBSINGULAR.TB_DEFINICAO_TAREFA (CO_DEFINICAO_TAREFA);

ALTER TABLE DBSINGULAR.TB_VERSAO_TAREFA
ADD CONSTRAINT FK_DMD_SITUACAO_DMD_DEFINICAO FOREIGN KEY (CO_VERSAO_PROCESSO)
REFERENCES DBSINGULAR.TB_VERSAO_PROCESSO (CO_VERSAO_PROCESSO)
  ON DELETE CASCADE;

ALTER TABLE DBSINGULAR.TB_VERSAO_TAREFA
ADD CONSTRAINT FK_DMD_SITUACAO_DMD_TIPO_SITUACAO FOREIGN KEY (CO_TIPO_TAREFA)
REFERENCES DBSINGULAR.TB_TIPO_TAREFA (CO_TIPO_TAREFA);

ALTER TABLE DBSINGULAR.TB_VERSAO_TRANSICAO
ADD CONSTRAINT FK_TRANSICAO_TAREFA_DESTINO FOREIGN KEY (CO_VERSAO_TAREFA_DESTINO)
REFERENCES DBSINGULAR.TB_VERSAO_TAREFA (CO_VERSAO_TAREFA);

ALTER TABLE DBSINGULAR.TB_VERSAO_TRANSICAO
ADD CONSTRAINT FK_TRANSICAO_TAREFA_ORIGEM FOREIGN KEY (CO_VERSAO_TAREFA_ORIGEM)
REFERENCES DBSINGULAR.TB_VERSAO_TAREFA (CO_VERSAO_TAREFA);

ALTER TABLE DBSINGULAR.TB_VARIAVEL
ADD CONSTRAINT FK_DMD_VARIAVEL_DMD_DEMANDA FOREIGN KEY (CO_INSTANCIA_PROCESSO)
REFERENCES DBSINGULAR.TB_INSTANCIA_PROCESSO (CO_INSTANCIA_PROCESSO)
  ON DELETE CASCADE;

ALTER TABLE DBSINGULAR.TB_VARIAVEL
ADD CONSTRAINT FK_DMD_VARIAVEL_DMD_TIPO_VARIAVEL FOREIGN KEY (CO_TIPO_VARIAVEL)
REFERENCES DBSINGULAR.TB_TIPO_VARIAVEL (CO_TIPO_VARIAVEL);

ALTER TABLE DBSINGULAR.TB_VARIAVEL_EXECUCAO_TRANSICAO
ADD CONSTRAINT FK_DMD_HISTORICO_VARIAVEL_DMD_DEMANDA FOREIGN KEY (CO_INSTANCIA_PROCESSO)
REFERENCES DBSINGULAR.TB_INSTANCIA_PROCESSO (CO_INSTANCIA_PROCESSO);

ALTER TABLE DBSINGULAR.TB_VARIAVEL_EXECUCAO_TRANSICAO
ADD CONSTRAINT FK_TB_VARIA_FK_HISTOR_TB_TIPO_ FOREIGN KEY (CO_TIPO_VARIAVEL)
REFERENCES DBSINGULAR.TB_TIPO_VARIAVEL (CO_TIPO_VARIAVEL);

ALTER TABLE DBSINGULAR.TB_VARIAVEL_EXECUCAO_TRANSICAO
ADD CONSTRAINT PK_HISTORICO_VARIAVEL_INSTANCIA_TAREFA_DESTINO FOREIGN KEY (CO_INSTANCIA_TAREFA_DESTINO)
REFERENCES DBSINGULAR.TB_INSTANCIA_TAREFA (CO_INSTANCIA_TAREFA);

ALTER TABLE DBSINGULAR.TB_VARIAVEL_EXECUCAO_TRANSICAO
ADD CONSTRAINT PK_HISTORICO_VARIAVEL_INSTANCIA_TAREFA_ORIGEM FOREIGN KEY (CO_INSTANCIA_TAREFA_ORIGEM)
REFERENCES DBSINGULAR.TB_INSTANCIA_TAREFA (CO_INSTANCIA_TAREFA);

ALTER TABLE DBSINGULAR.TB_VARIAVEL_EXECUCAO_TRANSICAO
ADD CONSTRAINT FK_HISTORICO_VARIAVEL_VARIAVEL FOREIGN KEY (CO_VARIAVEL_EXECUCAO_TRANSICAO)
REFERENCES DBSINGULAR.TB_VARIAVEL (CO_VARIAVEL);

ALTER TABLE DBSINGULAR.TB_DEFINICAO_PROCESSO
ADD CONSTRAINT FK_TB_DEFIN_REFERENCE_TB_GRUPO FOREIGN KEY (CO_GRUPO_PROCESSO)
REFERENCES DBSINGULAR.TB_GRUPO_PROCESSO (CO_GRUPO_PROCESSO);

ALTER TABLE DBSINGULAR.TB_PETICAO
ADD CONSTRAINT FK_PETICAO_TRANSACAO FOREIGN KEY (NU_TRANSACAO_INTERNET)
REFERENCES DBARRECAD.TL_TRANSACAO (NU_TRANSACAO_INTERNET);

ALTER TABLE DBSINGULAR.TB_PETICAO
ADD CONSTRAINT FK_PETICAO_INSTANCIAPROCESSO FOREIGN KEY (CO_INSTANCIA_PROCESSO)
REFERENCES DBSINGULAR.TB_INSTANCIA_PROCESSO (CO_INSTANCIA_PROCESSO);

/*==============================================================*/
/* Index: IX_INSTANCIA_PROCESSO                                 */
/*==============================================================*/
CREATE INDEX IX_INSTANCIA_PROCESSO ON DBSINGULAR.TB_INSTANCIA_PROCESSO (
  CO_VERSAO_PROCESSO ASC,
  DT_INICIO ASC
);

/*==============================================================*/
/* Index: IX_HISTORICO_INSTANCIA_TAREFA                         */
/*==============================================================*/
CREATE INDEX IX_HISTORICO_INSTANCIA_TAREFA ON DBSINGULAR.TB_HISTORICO_INSTANCIA_TAREFA (
  CO_INSTANCIA_TAREFA ASC,
  DT_INICIO_ALOCACAO ASC
);

/*==============================================================*/
/* Index: IX_INSTANCIA_TAREFA                                   */
/*==============================================================*/
CREATE INDEX IX_INSTANCIA_TAREFA ON DBSINGULAR.TB_INSTANCIA_TAREFA (
  CO_INSTANCIA_PROCESSO ASC,
  DT_INICIO ASC
);

/*==============================================================*/
/* Index: IX_PROCESSO                                           */
/*==============================================================*/
CREATE INDEX IX_PROCESSO ON DBSINGULAR.TB_VERSAO_PROCESSO (
  CO_DEFINICAO_PROCESSO ASC,
  DT_VERSAO ASC
);

/*==============================================================*/
/* Index: IX_CLASSE_DEFINICAO                                   */
/*==============================================================*/
CREATE UNIQUE INDEX IX_CLASSE_DEFINICAO ON DBSINGULAR.TB_DEFINICAO_PROCESSO (
	NO_CLASSE_JAVA ASC
);

/*==============================================================*/
/* Index: IX_GRUPO_NOME                                         */
/*==============================================================*/
CREATE UNIQUE INDEX IX_GRUPO_NOME ON DBSINGULAR.TB_GRUPO_PROCESSO (NO_GRUPO ASC);

/*==============================================================*/
/* Index: IX_GRUPO_CONEXAO                                      */
/*==============================================================*/
CREATE UNIQUE INDEX IX_GRUPO_CONEXAO ON DBSINGULAR.TB_GRUPO_PROCESSO (URL_CONEXAO ASC);