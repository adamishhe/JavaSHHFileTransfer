package ru.adamishhe.javashhfiletransfer;

import com.jcraft.jsch.Session;

public class SSHSessionManager {
    private static SSHSessionManager instance;
    private Session session;

    private SSHSessionManager() {
        // Приватный конструктор
    }

    public static SSHSessionManager getInstance() {
        if (instance == null) {
            synchronized (SSHSessionManager.class) {
                if (instance == null) {
                    instance = new SSHSessionManager();
                }
            }
        }
        return instance;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public Session getSession() {
        return session;
    }
}
