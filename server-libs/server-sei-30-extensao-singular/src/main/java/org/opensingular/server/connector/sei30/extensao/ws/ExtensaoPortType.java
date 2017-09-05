
package org.opensingular.server.connector.sei30.extensao.ws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.9-b130926.1035
 * Generated source version: 2.2
 * 
 */
@WebService(name = "extensaoPortType", targetNamespace = "extensaons")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@XmlSeeAlso({
    ObjectFactory.class
})
public interface ExtensaoPortType {


    /**
     * Assinar documento
     * 
     * @param identificacaoServico
     * @param idUnidade
     * @param siglaSistema
     * @param dadosAssinatura
     * @return
     *     returns java.lang.String
     */
    @WebMethod(action = "extensaons/assinarDocumento")
    @WebResult(name = "retorno", partName = "retorno")
    public String assinarDocumento(
        @WebParam(name = "SiglaSistema", partName = "SiglaSistema")
        String siglaSistema,
        @WebParam(name = "IdentificacaoServico", partName = "IdentificacaoServico")
        String identificacaoServico,
        @WebParam(name = "IdUnidade", partName = "IdUnidade")
        String idUnidade,
        @WebParam(name = "DadosAssinatura", partName = "DadosAssinatura")
        DadosAssinatura dadosAssinatura);

    /**
     * Listar assinantes por unidade
     * 
     * @param identificacaoServico
     * @param idUnidade
     * @param siglaSistema
     * @return
     *     returns org.opensingular.server.connector.sei30.extensao.ws.ArrayOfAssinante
     */
    @WebMethod(action = "extensaons/listarAssinantesPorUnidade")
    @WebResult(name = "retorno", partName = "retorno")
    public ArrayOfAssinante listarAssinantesPorUnidade(
        @WebParam(name = "SiglaSistema", partName = "SiglaSistema")
        String siglaSistema,
        @WebParam(name = "IdentificacaoServico", partName = "IdentificacaoServico")
        String identificacaoServico,
        @WebParam(name = "IdUnidade", partName = "IdUnidade")
        String idUnidade);

    /**
     * Listar documentos por processo
     * 
     * @param identificacaoServico
     * @param idUnidade
     * @param siglaSistema
     * @param idProcedimento
     * @return
     *     returns org.opensingular.server.connector.sei30.extensao.ws.ArrayOfRetornoConsultaDocumento
     */
    @WebMethod(action = "extensaons/listarDocumentosPorProcedimento")
    @WebResult(name = "retorno", partName = "retorno")
    public ArrayOfRetornoConsultaDocumento listarDocumentosPorProcedimento(
        @WebParam(name = "SiglaSistema", partName = "SiglaSistema")
        String siglaSistema,
        @WebParam(name = "IdentificacaoServico", partName = "IdentificacaoServico")
        String identificacaoServico,
        @WebParam(name = "IdUnidade", partName = "IdUnidade")
        String idUnidade,
        @WebParam(name = "IdProcedimento", partName = "IdProcedimento")
        String idProcedimento);

    /**
     * Autentica um usuario externo. true: autenticado, false: nao autenticado
     * 
     * @param senha
     * @param identificacaoServico
     * @param idUnidade
     * @param permitePendente
     * @param siglaSistema
     * @param login
     * @return
     *     returns boolean
     */
    @WebMethod(action = "extensaons/autenticarUsuarioExterno")
    @WebResult(name = "autenticado", partName = "autenticado")
    public boolean autenticarUsuarioExterno(
        @WebParam(name = "SiglaSistema", partName = "SiglaSistema")
        String siglaSistema,
        @WebParam(name = "IdentificacaoServico", partName = "IdentificacaoServico")
        String identificacaoServico,
        @WebParam(name = "IdUnidade", partName = "IdUnidade")
        String idUnidade,
        @WebParam(name = "Login", partName = "Login")
        String login,
        @WebParam(name = "Senha", partName = "Senha")
        String senha,
        @WebParam(name = "PermitePendente", partName = "PermitePendente")
        boolean permitePendente);

    /**
     * Autentica um usuario interno. true: autenticado, false: nao autenticado
     * 
     * @param identificacaoServico
     * @param idUnidade
     * @param siglaSistema
     * @param loginInterno
     * @return
     *     returns boolean
     */
    @WebMethod(action = "extensaons/autenticarUsuarioInterno")
    @WebResult(name = "Autenticado", partName = "Autenticado")
    public boolean autenticarUsuarioInterno(
        @WebParam(name = "SiglaSistema", partName = "SiglaSistema")
        String siglaSistema,
        @WebParam(name = "IdentificacaoServico", partName = "IdentificacaoServico")
        String identificacaoServico,
        @WebParam(name = "IdUnidade", partName = "IdUnidade")
        String idUnidade,
        @WebParam(name = "LoginInterno", partName = "LoginInterno")
        LoginInterno loginInterno);

}
