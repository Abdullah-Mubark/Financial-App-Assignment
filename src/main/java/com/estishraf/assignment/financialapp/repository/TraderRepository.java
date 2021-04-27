package com.estishraf.assignment.financialapp.repository;

import com.estishraf.assignment.financialapp.entity.Trader;
import com.estishraf.assignment.financialapp.utils.HibernateUtil;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class TraderRepository {

    private final SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    public TraderRepository() throws Exception {
    }

    public void Add(Trader trader) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            session.save(trader);
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            System.out.println("Failed while saving trader data in db .. Exception: " + e.getMessage());
        }
    }

    public void Update(Trader trader) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            session.update(trader);
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            System.out.println("Failed while updating trader data in db .. Exception: " + e.getMessage());
        }
    }

    public Trader Get(String name) {
        Trader trader;
        try (Session session = sessionFactory.openSession()) {
            trader = session.get(Trader.class, name);
        } catch (HibernateException e) {
            System.out.println("Failed while fetching trader data in db .. Exception: " + e.getMessage());
            throw e;
        }
        return trader;
    }
}
