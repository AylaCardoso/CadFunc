package com.empresa.mbeans;

import com.empresa.modelo.dao.FuncionarioDAO;
import com.empresa.modelo.entities.Funcionario;
import com.empresa.util.excecoes.PersistenciaException;
import jakarta.faces.view.ViewScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@ViewScoped
public class FuncionarioMBean implements Serializable {

    private static final Logger logger = Logger.getLogger(FuncionarioMBean.class.getName());

    @Inject
    private FuncionarioDAO dao;

    private Funcionario funcionario = new Funcionario();
    private Part uploadedFile;
    private List<Funcionario> funcionarios;
    private boolean idInvalido = false;

    public void verificaRedirecionamento() throws IOException {
        if (funcionario.getId() == null || funcionario.getId() <= 0) {
            FacesContext.getCurrentInstance().getExternalContext()
                    .redirect("ListaFuncionarios.xhtml?faces-redirect=true");
        } else {
            carregarFuncionario();
        }
    }

    private void carregarFuncionario() {
        try {
            Optional<Funcionario> funcOptional = dao.buscarPorId(funcionario.getId());

            if (funcOptional.isPresent()) {
                Funcionario funcDoBanco = funcOptional.get();
                funcionario.setNome(funcDoBanco.getNome());
                funcionario.setCargo(funcDoBanco.getCargo());
                funcionario.setSalario(funcDoBanco.getSalario());
                funcionario.setDataAdm(funcDoBanco.getDataAdm());
                funcionario.setAtivo(funcDoBanco.isAtivo());
                funcionario.setFotoPath(funcDoBanco.getFotoPath());
            } else {
                mensagemErro("Funcionário não encontrado!");
                idInvalido = true;
            }
        } catch (Exception e) {
            mensagemErro("Erro ao carregar dados: " + e.getMessage());
            idInvalido = true;
            logger.log(Level.SEVERE, "Erro crítico", e);
        }
    }

    public String salvarNovo() {
        try {
            validarDados();
            processarUploadImagem();
            dao.salvar(funcionario);
            return sucessoRedirect("Funcionário cadastrado com sucesso!");
        } catch (IOException | PersistenciaException | IllegalArgumentException e) {
            return erro(e);
        } finally {
            resetarFormulario();
        }
    }

    public String editarExistente() {
        try {
            validarEdicao();
            processarUploadImagem();
            dao.editar(funcionario);
            return sucessoRedirect("Funcionário atualizado!");
        } catch (Exception e) {
            mensagemErro("Erro na edição: " + e.getMessage());
            return null;
        }
    }


    private void validarEdicao() throws PersistenciaException {
        if (funcionario.getId() == null || funcionario.getId() <= 0) {
            throw new PersistenciaException("ID inválido para edição");
        }
        validarDados();
    }

    private void validarDados() {
        if (funcionario.getSalario() == null || funcionario.getSalario().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Salário deve ser maior que zero");
        }
    }

    private void processarUploadImagem() throws IOException {
        if (uploadedFile != null && uploadedFile.getSize() > 0) {
            validarArquivo();
            String fotoAnterior = funcionario.getFotoPath();

            try {
                String novoNome = gerarNomeArquivoSeguro();
                salvarArquivo(novoNome);
                funcionario.setFotoPath(novoNome);

                if (funcionario.getId() != null && fotoAnterior != null) {
                    removerArquivoAntigo(fotoAnterior);
                }
            } catch (IOException e) {
                funcionario.setFotoPath(fotoAnterior);
                throw new IOException("Erro ao processar imagem: " + e.getMessage(), e);
            }
        }
    }

    private void validarArquivo() {
        long maxFileSize = 2 * 1024 * 1024;
        if (uploadedFile.getSize() > maxFileSize) {
            throw new IllegalArgumentException("Tamanho máximo permitido: 2MB");
        }

        String contentType = uploadedFile.getContentType();
        if (!"image/jpeg".equals(contentType) && !"image/png".equals(contentType)) {
            throw new IllegalArgumentException("Formatos aceitos: JPG/PNG");
        }
    }

    private String gerarNomeArquivoSeguro() {
        String fileName = uploadedFile.getSubmittedFileName();
        int lastDotIndex = fileName.lastIndexOf('.');
        String ext = (lastDotIndex > 0) ? fileName.substring(lastDotIndex) : "";
        return "func_" + System.currentTimeMillis() + ext;
    }

    private void salvarArquivo(String fileName) throws IOException {
        Path dir = Paths.get(
                FacesContext.getCurrentInstance()
                        .getExternalContext()
                        .getRealPath("/resources/imagens/imagensFunc")
        );

        Files.createDirectories(dir);

        try (InputStream input = uploadedFile.getInputStream()) {
            Path destino = dir.resolve(fileName);
            Files.copy(input, destino, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public void excluir(Funcionario f) {
        try {
            if (dao.excluir(f.getId())) {
                removerArquivoAntigo(f.getFotoPath());
                invalidarLista();
                sucesso("Funcionário excluído com sucesso!");
            }
        } catch (PersistenciaException | IOException e) {
            erro(e);
        }
    }

    private void removerArquivoAntigo(String fileName) throws IOException {
        if (fileName != null && !fileName.isEmpty()) {
            Path arquivo = Paths.get(
                    FacesContext.getCurrentInstance()
                            .getExternalContext()
                            .getRealPath("/resources/imagens/imagensFunc/" + fileName)
            );
            Files.deleteIfExists(arquivo);
        }
    }

    private String sucessoRedirect(String mensagem) {
        FacesContext.getCurrentInstance().getExternalContext()
                .getFlash().setKeepMessages(true);
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", mensagem));
        return "ListaFuncionarios.xhtml?faces-redirect=true";
    }

    private String sucesso(String mensagem) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", mensagem));
        return null;
    }

    private String erro(Exception e) {
        String msgUsuario = "Erro: " + getMensagemAmigavel(e);
        String msgLog = "Erro: " + e.getClass().getSimpleName() + " - " + e.getMessage();

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", msgUsuario));
        logger.log(Level.SEVERE, msgLog, e);

        return null;
    }

    private String getMensagemAmigavel(Exception e) {
        if (e instanceof PersistenciaException) {
            return e.getMessage();
        } else if (e instanceof IOException) {
            return "Erro ao processar arquivo. Verifique o formato (JPG/PNG) e tamanho (max 2MB)";
        } else if (e instanceof IllegalArgumentException) {
            return e.getMessage();
        }
        return "Erro interno. Contate o administrador";
    }

    private void mensagemErro(String mensagem) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", mensagem));
    }

    private void resetarFormulario() {
        this.funcionario = new Funcionario();
        this.uploadedFile = null;
    }

    private void invalidarLista() {
        this.funcionarios = null;
    }

    // Getters e Setters
    public List<Funcionario> getFuncionarios() {
        if (funcionarios == null) {
            atualizarLista();
        }
        return funcionarios;
    }

    private void atualizarLista() {
        try {
            funcionarios = dao.listarTodos();
        } catch (PersistenciaException e) {
            mensagemErro("Erro ao carregar lista de funcionários");
            logger.log(Level.SEVERE, "Erro ao listar funcionários", e);
        }
    }

    public Funcionario getFuncionario() {
        return funcionario;
    }

    public void setFuncionario(Funcionario funcionario) {
        this.funcionario = funcionario;
    }

    public Part getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(Part uploadedFile) {
        this.uploadedFile = uploadedFile;
    }
}
