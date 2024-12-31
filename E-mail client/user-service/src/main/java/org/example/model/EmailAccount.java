package org.example.model;
import lombok.Getter;
import org.example.model.common.ProtocolType;

@Getter
public class EmailAccount {
    private String emailAddress;
    private String password;
    private ServerConnection incomingServer;
    private ServerConnection outgoingServer;
    private Boolean autoconfig;
    private EmailAccount(EmailAccountBuilder emailAccountBuilder) {
        this.emailAddress = emailAccountBuilder.emailAddress;
        this.password = emailAccountBuilder.password;
        this.incomingServer = emailAccountBuilder.incomingServer;
        this.outgoingServer = emailAccountBuilder.outgoingServer;
        this.autoconfig = emailAccountBuilder.autoconfig;
    }
    public static class EmailAccountBuilder {
        private String emailAddress;
        private String password;
        private ServerConnection incomingServer;
        private ServerConnection outgoingServer;

        private Boolean autoconfig;


        public EmailAccountBuilder(String emailAddress, String password) {
            this.emailAddress = emailAddress;
            this.password = password;
        }
        public EmailAccountBuilder setAutoconfig(Boolean autoconfig){
            if (autoconfig){
                autoConfigure();
            }
            this.autoconfig = autoconfig;
            return this;
        }


        public EmailAccountBuilder setIncomingServer(ServerConnection serverConnection){
            this.incomingServer = serverConnection;
            return this;
        }
        public EmailAccountBuilder setOutgoingServer(ServerConnection serverConnection){
            this.outgoingServer = serverConnection;
            return this;
        }

        private void autoConfigure() {
            String domain = this.emailAddress.split("@")[1];
            switch (domain) {
                case "gmail.com" -> {
                    this.incomingServer = new ServerConnection("imap.gmail.com", 993, ProtocolType.IMAP);
                    this.outgoingServer = new ServerConnection("smtp.gmail.com", 465, ProtocolType.SMTP);
                }
                case "ukr.net" -> {
                    this.incomingServer = new ServerConnection("imap.ukr.net", 993, ProtocolType.IMAP);
                    this.outgoingServer = new ServerConnection("smtp.ukr.net", 465, ProtocolType.SMTP);
                }
                case "i.ua" -> {
                    this.incomingServer = new ServerConnection("imap.i.ua", 993, ProtocolType.IMAP);
                    this.outgoingServer = new ServerConnection("smtp.i.ua", 465, ProtocolType.SMTP);
                }
                default -> System.out.println("Автонастройка недоступна для домену: " + domain);
            }
        }

        public EmailAccount build(){
            return new EmailAccount(this);
        }
    }

}