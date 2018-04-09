package utils;


import android.webkit.MimeTypeMap;

import com.facebook.stetho.inspector.console.CLog;
import com.google.api.services.drive.model.File;

public class CloudResource {
    public enum Type {
        FILE, FOLDER
    }

    public enum Provider {
        GOOGLE_DRIVE, DROPBOX
    }

    private String name;
    private Type type;
    private File.Thumbnail thumbnail;
    private String mimeType;
    private Provider provider;
    private String id;

    public CloudResource(Provider provider, Type type,
                         String name, String mimeType, String id) {
        this.name = name;
        this.type = type;
        this.mimeType = mimeType;
        this.provider = provider;
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public Type getType() {
        return this.type;
    }

    public String getMimeType() { return this.mimeType; }

    public Provider getProvider() { return this.provider; }

    public String getId() { return this.id; }

    public static String getMimeTypeFromFileName(String name) {
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String ext = name.substring(name.indexOf(".") + 1);
        return mime.getMimeTypeFromExtension(ext);
    }
}
