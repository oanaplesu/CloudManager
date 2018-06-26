package utils.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface OneDriveUserDao {
    @Insert(onConflict = OnConflictStrategy.FAIL)
    void addUser(OneDriveUser user);

    @Query("select account from onedriveuser")
    public List<String> getAllAccounts();

    @Query("select name from onedriveuser where account like :account")
    public String getNameForAccount(String account);

    @Query("UPDATE onedriveuser SET token = :token WHERE account like :account")
	void updateUser(String account, String token);

    @Query("delete from onedriveuser")
    void removeAllUsers();

    @Query("delete from onedriveuser where account like :account")
    void removeUser(String account);

    @Query("select token from onedriveuser where account like :account")
    public String getTokenForAccount(String account);
}