package com.empresa.modelo.entities;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

@Entity
@Table(name = "funcionarios")
public class Funcionario implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 50)
    private String cargo;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal salario;

    @Temporal(TemporalType.DATE)
    @Column(name = "data_admissao", nullable = false)
    private Date dataAdm;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "foto_path", length = 255)
    private String fotoPath;

    // Construtor padrão necessário para JPA
    public Funcionario() {
    }

    // Métodos auxiliares para formatação
    public String getSalarioFormatado() {
        if (salario == null) return "R$ 0,00";
        return NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(salario);
    }

    public String getDataAdmissaoFormatada() {
        if (dataAdm == null) return "";
        return new java.text.SimpleDateFormat("dd/MM/yyyy").format(dataAdm);
    }

    public String getFotoWebPath() {
        return (fotoPath != null && !fotoPath.isEmpty())
                ? "/resources/imagens/imagensFunc/" + fotoPath
                : "/imagem-padrao";
    }

    // Getters e Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public BigDecimal getSalario() {
        return salario;
    }

    public void setSalario(BigDecimal salario) {
        this.salario = salario;
    }

    public Date getDataAdm() {
        return dataAdm;
    }

    public void setDataAdm(Date dataAdm) {
        this.dataAdm = dataAdm;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public String getFotoPath() {
        return fotoPath;
    }

    public void setFotoPath(String fotoPath) {
        this.fotoPath = fotoPath;
    }

    // Métodos de integração
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Funcionario)) return false;
        return id != null && id.equals(((Funcionario) o).id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Funcionario{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", cargo='" + cargo + '\'' +
                '}';
    }

    // Método de clonagem segura
    public Funcionario deepCopy() {
        Funcionario clone = new Funcionario();
        clone.setId(this.id);
        clone.setNome(this.nome);
        clone.setCargo(this.cargo);
        clone.setSalario(this.salario != null ? new BigDecimal(this.salario.toString()) : null);
        clone.setDataAdm(this.dataAdm != null ? (Date) this.dataAdm.clone() : null);
        clone.setAtivo(this.ativo);
        clone.setFotoPath(this.fotoPath);
        return clone;
    }
}
