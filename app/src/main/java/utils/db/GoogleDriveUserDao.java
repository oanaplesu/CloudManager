package utils.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;


@Dao
public interface GoogleDriveUserDao {

    @Insert(onConflict = OnConflictStrategy.FAIL)
    void addUser(GoogleDriveUser user);

    @Query("select account from googledriveuser")
    public List<String> getAllAccounts();

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateUser(GoogleDriveUser user);

    @Query("delete from GoogleDriveUser")
    void removeAllUsers();

    @Query("delete from GoogleDriveUser where account like :account")
    void removeUser(String account);

    @Query("select * from googledriveuser where account like :account")
    public GoogleDriveUser getUserByAccount(String account);


}