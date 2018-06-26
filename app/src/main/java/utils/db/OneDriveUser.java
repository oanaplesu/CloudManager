package utils.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

@Entity(indices = {@Index(value = {"account"},
        unique = true)})
public class OneDriveUser {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String account;
    public String token;

    public OneDriveUser(String account, String token) {
        this.account = account;
        this.token = token;
    }
}