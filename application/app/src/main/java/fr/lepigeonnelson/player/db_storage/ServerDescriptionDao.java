package fr.lepigeonnelson.player.db_storage;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ServerDescriptionDao {
        @Query("SELECT * FROM server_description")
        List<ServerDescriptionEntity> getAll();

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        Long insertDescription(ServerDescriptionEntity description);

        @Delete
        void delete(ServerDescriptionEntity serverDescriptionEntity);

        @Query("DELETE FROM server_description WHERE url = :url")
        void deleteByURL(String url);
}
