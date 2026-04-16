package pt.uc.dei.proj5.beans;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.InputStream;
import java.util.Properties;

@ApplicationScoped // Fica vivo enquanto o servidor estiver a correr
public class ConfigBean {

    private int sessionTimeoutMinutos = 60; // Valor por defeito como rede de segurança

    @PostConstruct
    public void init() {
        // Tenta ler o ficheiro application.properties
        try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                Properties prop = new Properties();
                prop.load(input);
                String timeoutStr = prop.getProperty("session.timeout.minutos");
                if (timeoutStr != null && !timeoutStr.trim().isEmpty()) {
                    sessionTimeoutMinutos = Integer.parseInt(timeoutStr.trim());
                    System.out.println("⚙️ [CONFIG] Timeout de Sessão carregado: " + sessionTimeoutMinutos + " minutos.");
                }
            }
        } catch (Exception e) {
            System.err.println("Aviso: Falha ao ler application.properties. A usar timeout de " + sessionTimeoutMinutos + "m.");
        }
    }

    public int getSessionTimeoutMinutos() {
        return sessionTimeoutMinutos;
    }
}