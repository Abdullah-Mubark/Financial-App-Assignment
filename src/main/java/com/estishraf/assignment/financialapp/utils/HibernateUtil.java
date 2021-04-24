package com.estishraf.assignment.financialapp.utils;


import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import java.util.logging.Level;

public class HibernateUtil {
    private static SessionFactory sessionFactory;

    private static SessionFactory buildSessionFactory() throws Exception {
        java.util.logging.Logger.getLogger("org.hibernate").setLevel(Level.SEVERE);

        Configuration configuration = new Configuration()
                .setProperties(AppUtil.GetAppProperties())
                .addAnnotatedClass(com.estishraf.assignment.financialapp.entity.TraderTransaction.class);

        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(configuration.getProperties()).build();

        return configuration.buildSessionFactory(serviceRegistry);
    }

    public static SessionFactory getSessionFactory() throws Exception {
        if (sessionFactory == null)
            sessionFactory = buildSessionFactory();

        return sessionFactory;
    }
}
