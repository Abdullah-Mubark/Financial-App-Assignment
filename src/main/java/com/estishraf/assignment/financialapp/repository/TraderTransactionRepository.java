package com.estishraf.assignment.financialapp.repository;

import com.estishraf.assignment.financialapp.entity.TraderTransaction;
import com.estishraf.assignment.financialapp.utils.HibernateUtil;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class TraderTransactionRepository {

    private final SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    public TraderTransactionRepository() throws Exception {
    }

    public void Add(TraderTransaction traderTransaction) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            session.save(traderTransaction);
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            System.out.println("Failed while saving trader transaction data in db .. Exception: " + e.getMessage());
        }
    }
}
