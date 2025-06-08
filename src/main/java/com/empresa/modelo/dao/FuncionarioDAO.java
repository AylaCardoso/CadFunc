package com.empresa.modelo.dao;

import com.empresa.modelo.entities.Funcionario;
import com.empresa.util.excecoes.PersistenciaException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class FuncionarioDAO {

    private static final Logger LOGGER = Logger.getLogger(FuncionarioDAO.class.getName());
    private static final String ERRO_PADRAO = "Erro na operação com funcionário: ";

    public void salvar(Funcionario funcionario) throws PersistenciaException {
        validarEntidade(funcionario);
        EntityManager em = ConnectionFactory.getEntityManager();
        EntityTransaction transaction = null;

        try {
            transaction = em.getTransaction();
            transaction.begin();
            em.persist(funcionario);
            transaction.commit();
            LOGGER.log(Level.INFO, "Funcionário salvo: ID {0}", funcionario.getId());
        } catch (PersistenceException e) {
            rollback(transaction, "salvar", funcionario.getId(), e);
            throw new PersistenciaException(ERRO_PADRAO + "ID " + funcionario.getId(), e);
        } finally {
            fecharEntityManager(em);
        }
    }

    public Funcionario editar(Funcionario funcionario) throws PersistenciaException {
        validarId(funcionario.getId());
        EntityManager em = ConnectionFactory.getEntityManager();
        EntityTransaction transaction = null;

        try {
            transaction = em.getTransaction();
            transaction.begin();

            verificarExistencia(em, funcionario.getId());
            Funcionario atualizado = em.merge(funcionario);

            transaction.commit();
            LOGGER.log(Level.INFO, "Funcionário atualizado: ID {0}", funcionario.getId());
            return atualizado;
        } catch (PersistenceException e) {
            rollback(transaction, "editar", funcionario.getId(), e);
            throw new PersistenciaException(ERRO_PADRAO + "ID " + funcionario.getId(), e);
        } finally {
            fecharEntityManager(em);
        }
    }

    public boolean excluir(Integer id) throws PersistenciaException {
        validarId(id);
        EntityManager em = ConnectionFactory.getEntityManager();
        EntityTransaction transaction = null;

        try {
            transaction = em.getTransaction();
            transaction.begin();

            Funcionario funcionario = em.find(Funcionario.class, id);
            if (funcionario != null) {
                em.remove(funcionario);
                transaction.commit();
                LOGGER.log(Level.INFO, "Funcionário excluído: ID {0}", id);
                return true;
            }
            LOGGER.log(Level.WARNING, "Funcionário não encontrado: ID {0}", id);
            return false;
        } catch (PersistenceException e) {
            rollback(transaction, "excluir", id, e);
            throw new PersistenciaException(ERRO_PADRAO + "ID " + id, e);
        } finally {
            fecharEntityManager(em);
        }
    }

    public List<Funcionario> listarTodos() throws PersistenciaException {
        EntityManager em = ConnectionFactory.getEntityManager();
        try {
            return em.createQuery("SELECT f FROM Funcionario f ORDER BY f.nome", Funcionario.class)
                    .getResultList();
        } catch (PersistenceException e) {
            LOGGER.log(Level.SEVERE, "Erro ao listar funcionários", e);
            throw new PersistenciaException(ERRO_PADRAO + "listagem", e);
        } finally {
            fecharEntityManager(em);
        }
    }

    public Optional<Funcionario> buscarPorId(Integer id) throws PersistenciaException {
        validarId(id);
        EntityManager em = ConnectionFactory.getEntityManager();
        try {
            return Optional.ofNullable(em.find(Funcionario.class, id));
        } catch (PersistenceException e) {
            LOGGER.log(Level.SEVERE, "Erro ao buscar funcionário ID: {0}", id);
            throw new PersistenciaException(ERRO_PADRAO + "busca ID " + id, e);
        } finally {
            fecharEntityManager(em);
        }
    }

    // Métodos auxiliares
    private void verificarExistencia(EntityManager em, Integer id) throws PersistenciaException {
        if (em.find(Funcionario.class, id) == null) {
            throw new PersistenciaException("Funcionário não encontrado: ID " + id);
        }
    }

    private void validarId(Integer id) throws PersistenciaException {
        if (id == null || id <= 0) {
            throw new PersistenciaException("ID inválido: " + id);
        }
    }

    private void validarEntidade(Funcionario funcionario) throws PersistenciaException {
        if (funcionario == null) {
            throw new PersistenciaException("Funcionário não pode ser nulo");
        }
    }

    private void rollback(EntityTransaction transaction, String operacao, Integer id, Exception e) {
        if (transaction != null && transaction.isActive()) {
            try {
                transaction.rollback();
                LOGGER.log(Level.WARNING, "Rollback na operação {0} para ID {1}", new Object[]{operacao, id});
            } catch (IllegalStateException ex) {
                LOGGER.log(Level.SEVERE, "Falha no rollback: {0} - ID {1}", new Object[]{operacao, id});
            }
        }
    }

    private void fecharEntityManager(EntityManager em) {
        try {
            if (em != null && em.isOpen()) {
                em.close();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao fechar EntityManager", e);
        }
    }
}
