# SQLCipher
-keep class net.zetetic.** { *; }
-keep class net.zetetic.database.sqlcipher.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
