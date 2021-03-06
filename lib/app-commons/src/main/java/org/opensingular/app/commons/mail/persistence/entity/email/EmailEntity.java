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
package org.opensingular.app.commons.mail.persistence.entity.email;


import org.hibernate.annotations.Type;
import org.opensingular.form.persistence.entity.AttachmentEntity;
import org.opensingular.lib.support.persistence.entity.BaseEntity;
import org.opensingular.lib.support.persistence.util.Constants;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Entity
@SequenceGenerator(name = EmailEntity.PK_GENERATOR_NAME, sequenceName = Constants.SCHEMA + ".SQ_CO_EMAIL", schema = Constants.SCHEMA)
@Table(name = "TB_EMAIL", schema = Constants.SCHEMA)
public class EmailEntity extends BaseEntity<Long> {

    public static final String PK_GENERATOR_NAME = "GENERATED_CO_EMAIL";

    @Id
    @Column(name = "CO_EMAIL")
    @GeneratedValue(generator = PK_GENERATOR_NAME, strategy = GenerationType.AUTO)
    private Long cod;

    @Column(name = "TX_RESPONDER_PARA", length = 200)
    private String replyTo;

    @Column(name = "TX_ASSUNTO", nullable = false, length = 200)
    private String subject;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "TX_CONTEUDO", nullable = false)
    private String content;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_CRIACAO", nullable = false)
    private Date creationDate;

    @OneToMany(mappedBy = "email")
    private List<EmailAddresseeEntity> recipients;

    @OneToMany
    @JoinTable(schema = Constants.SCHEMA, name = "TB_EMAIL_ARQUIVO",
            uniqueConstraints = {@UniqueConstraint(name = "UK_EMAIL_ARQUIVO", columnNames = "CO_ARQUIVO")},
            foreignKey = @javax.persistence.ForeignKey(name = "FK_EMAIL_ARQUIVO_EMAIL"),
            joinColumns = @JoinColumn(name = "CO_EMAIL"),
            inverseForeignKey = @javax.persistence.ForeignKey(name = "FK_EMAIL_ARQUIVO_ARQUIVO"),
            inverseJoinColumns = @JoinColumn(name = "CO_ARQUIVO"))
    private List<AttachmentEntity> attachments = new ArrayList<>();


    @Column(name = "CO_MODULO", length = 30)
    private String module;

    @Override
    public Long getCod() {
        return cod;
    }

    public void setCod(Long cod) {
        this.cod = cod;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public List<EmailAddresseeEntity> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<EmailAddresseeEntity> recipients) {
        this.recipients = recipients;
    }

    public List<AttachmentEntity> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<AttachmentEntity> attachments) {
        this.attachments = attachments;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }
}
