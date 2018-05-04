package utils.cloud;


import android.webkit.MimeTypeMap;

import com.facebook.stetho.inspector.console.CLog;
import com.google.api.services.drive.model.File;

import utils.adapters.AccountAdapter;

public class CloudResource {
    public enum Type {
        FILE, FOLDER
    }

    private String name;
    private Type type;
    private String mimeType;
    private String id;
    private AccountType accountType;
    private String accountEmail;

    public CloudResource(AccountType accountType, Type type,
                         String name, String mimeType,
                         String id, String accountEmail) {
        this.name = name;
        this.type = type;
        this.mimeType = mimeType;
        this.accountType = accountType;
        this.id = id;
        this.accountEmail = accountEmail;
    }

    public String getName() {
        return this.name;
    }

    public Type getType() {
        return this.type;
    }

    public String getMimeType() { return this.mimeType; }

    public AccountType getAccountType() { return this.accountType; }

    public String getId() { return this.id; }

    public String getAccountEmail() { return this.accountEmail; }

    public static String getMimeTypeFromFileName(String name) {
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String ext = name.substring(name.indexOf(".") + 1);
        return mime.getMimeTypeFromExtension(ext);
    }
}
