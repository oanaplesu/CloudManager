package utils.misc;

import android.Manifest;

public enum FileAction {
    DOWNLOAD(Manifest.permission.WRITE_EXTERNAL_STORAGE),
    UPLOAD(Manifest.permission.READ_EXTERNAL_STORAGE);

    private static final FileAction [] values = values();

    private final String [] permissions;

    FileAction(String ... permissions) {
        this.permissions = permissions;
    }

    public int getCode() {
        return ordinal();
    }

    public String [] getPermissions() {
        return permissions;
    }

    public static FileAction fromCode(int code) {
        if (code < 0 || code >= values.length) {
            throw new IllegalArgumentException("Invalid FileAction code: " + code);
        }
        return values[code];
    }
}
