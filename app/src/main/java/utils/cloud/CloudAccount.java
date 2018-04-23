package utils.cloud;


public class CloudAccount {
    public enum Provider {
        GOOGLE_DRIVE, DROPBOX
    }

    private String mEmail;
    private Provider mProvider;

    public CloudAccount(String email, Provider provider) {
        this.mEmail = email;
        this.mProvider = provider;
    }

    public String getEmail() {
        return this.mEmail;
    }

    public Provider getProvider() {
        return this.mProvider;
    }
}
