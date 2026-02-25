package io.github.yuzhiang.qxb.db.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import io.github.yuzhiang.qxb.db.room.bean.Lnm;
import io.github.yuzhiang.qxb.db.room.dao.LnmDao;

@Database(entities = {Lnm.class}, version = 15)
@TypeConverters({Converters.class})

public abstract class AppDatabase extends RoomDatabase {
    //数据库名称
    private static final String DB_NAME = "ldr.db";
    private static volatile AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = create(context);
        }
        return instance;
    }

    //创建数据库
    private static AppDatabase create(final Context context) {


        return Room.databaseBuilder(
                        context,
                        AppDatabase.class, DB_NAME)
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .addMigrations(MIGRATION_2_3)
                .addMigrations(MIGRATION_3_4)
                .addMigrations(MIGRATION_4_5)
                .addMigrations(MIGRATION_5_6)
                .addMigrations(MIGRATION_6_7)
                .addMigrations(MIGRATION_7_8)
                .addMigrations(MIGRATION_8_9)
                .addMigrations(MIGRATION_9_10)
                .addMigrations(MIGRATION_10_11)
                .addMigrations(MIGRATION_11_12)
                .addMigrations(MIGRATION_12_13)
                .addMigrations(MIGRATION_13_14)
                .build();

    }

    public abstract LnmDao lnmDao();


    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {

//                                为旧表添加新的字段
            database.execSQL("ALTER TABLE Course ADD COLUMN remarks TEXT");
//                创建新的数据表
//            database.execSQL("CREATE TABLE `Course` (`id` INTEGER, " + "`name` TEXT, PRIMARY KEY(`id`))");

        }
    };

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // NOT NULL空，PRIMARY KEY(`id`)：自增
            database.execSQL("CREATE TABLE Fee (id INTEGER NOT NULL, date INTEGER, fee REAL NOT NULL, PRIMARY KEY(`id`))");

            database.execSQL("CREATE TABLE Score (id INTEGER NOT NULL, year INTEGER  NOT NULL, term TEXT, selected INTEGER NOT NULL, " +
                    "courseName TEXT, courseProperty TEXT, generalCommentString TEXT, " +
                    "dailyPerformance REAL NOT NULL, terminal REAL NOT NULL, midterm REAL NOT NULL, generalComment REAL NOT NULL, credit REAL NOT NULL, " +
                    "PRIMARY KEY(`id`))");

            database.execSQL("CREATE TABLE Lnm (id INTEGER NOT NULL, startTime INTEGER, planTime INTEGER, stopTime INTEGER, time INTEGER NOT NULL, successLearn INTEGER NOT NULL, PRIMARY KEY(`id`))");

        }
    };

    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // NOT NULL空，PRIMARY KEY(`id`)：自增
            database.execSQL("DROP TABLE Lnm");

            database.execSQL("CREATE TABLE StudyMode (id INTEGER NOT NULL, startTime INTEGER, planTime INTEGER, stopTime INTEGER, successLearn INTEGER NOT NULL, PRIMARY KEY(`id`))");

        }
    };

    private static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // NOT NULL空，PRIMARY KEY(`id`)：自增
            database.execSQL("ALTER TABLE Course ADD COLUMN courseId TEXT");

        }
    };

    private static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // NOT NULL空，PRIMARY KEY(`id`)：自增

            database.execSQL("CREATE TABLE LzuNews (id INTEGER NOT NULL, time INTEGER, title TEXT, url TEXT, type TEXT, PRIMARY KEY(`id`))");

        }
    };

    private static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // NOT NULL空，PRIMARY KEY(`id`)：自增
            database.execSQL("ALTER TABLE Score ADD COLUMN courseId TEXT");

        }
    };


    private static final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // NOT NULL空，PRIMARY KEY(`id`)：自增

            database.execSQL("CREATE TABLE Bill (id INTEGER NOT NULL, date INTEGER, money REAL NOT NULL, autoImport INTEGER  NOT NULL, type TEXT, md5 TEXT, remark TEXT, PRIMARY KEY(`id`))");

        }
    };

    private static final Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // NOT NULL空，PRIMARY KEY(`id`)：自增

            database.execSQL("drop table if exists LzuNews");

        }
    };
    private static final Migration MIGRATION_9_10 = new Migration(9, 10) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // NOT NULL空，PRIMARY KEY(`id`)：自增

            database.execSQL("CREATE TABLE HomeWork (id INTEGER NOT NULL, createdDate INTEGER, endDate INTEGER, advanceDay INTEGER NOT NULL, courseMd5 TEXT, content TEXT, picUrl TEXT, courseName TEXT, PRIMARY KEY(`id`))");

        }
    };
    private static final Migration MIGRATION_10_11 = new Migration(10, 11) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // NOT NULL空，PRIMARY KEY(`id`)：自增
            database.execSQL("ALTER TABLE HomeWork ADD COLUMN complete INTEGER");
            database.execSQL("ALTER TABLE HomeWork ADD COLUMN completeDate INTEGER");

        }
    };
    private static final Migration MIGRATION_11_12 = new Migration(11, 12) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // NOT NULL空，PRIMARY KEY(`id`)：自增
            database.execSQL("ALTER TABLE Score ADD COLUMN modify INTEGER NOT NULL DEFAULT 0");
        }
    };
    private static final Migration MIGRATION_12_13 = new Migration(12, 13) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // NOT NULL空，PRIMARY KEY(`id`)：自增
            database.execSQL("ALTER TABLE Course ADD COLUMN num0 TEXT");
            database.execSQL("ALTER TABLE Course ADD COLUMN num1 TEXT");
            database.execSQL("ALTER TABLE Course ADD COLUMN type TEXT");
        }
    };
    private static final Migration MIGRATION_13_14 = new Migration(13, 14) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // NOT NULL空，PRIMARY KEY(`id`)：自增
            database.execSQL("ALTER TABLE Course ADD COLUMN version INTEGER NOT NULL DEFAULT 0");
        }
    };
}
