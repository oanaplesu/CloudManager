package db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;


@Dao
public interface DropboxUserDao {

    @Insert(onConflict = OnConflictStrategy.FAIL)
    void addUser(DropboxUser user);

    @Query("select account from dropboxuser")
    public List<String> getAllAccounts();

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateUser(DropboxUser user);

    @Query("delete from dropboxuser")
    void removeAllUsers();

    @Query("select token from dropboxuser where account like :account")
    public String getTokenForAccount(String account);
}