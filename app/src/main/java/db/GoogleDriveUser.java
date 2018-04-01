package db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

@Entity(indices = {@Index(value = {"account"},
        unique = true)})
public class GoogleDriveUser {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String account;


    public GoogleDriveUser(String account) {
        this.account = account;
    }

}

