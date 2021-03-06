/*
 * Copyright (C) 2016 Singular Studios (a.k.a Atom Tecnologia) - www.opensingular.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opensingular.requirement.connector.sei31.model;

import org.opensingular.requirement.connector.sei31.ws.Assunto;

import java.io.Serializable;

public class AssuntoSei implements Serializable {

    private String sigla;
    private String nome;

    public AssuntoSei(String sigla, String nome) {
        this.sigla = sigla;
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public String getSigla() {
        return sigla;
    }

    public Assunto toAssunto() {
        Assunto assunto = new Assunto();
        assunto.setCodigoEstruturado(sigla);
        assunto.setDescricao(nome);
        return assunto;
    }

}
