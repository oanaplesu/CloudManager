package utils.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

@Entity(indices = {@Index(value = {"account"},
        unique = true)})
public class DropboxUser {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String account;
    public String token;


    public DropboxUser(String account, String token) {
        this.account = account;
        this.token = token;
    }

}

