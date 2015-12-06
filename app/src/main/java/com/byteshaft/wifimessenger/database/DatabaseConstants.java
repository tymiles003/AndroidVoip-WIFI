package com.byteshaft.wifimessenger.database;

public class DatabaseConstants {


    //defining constants like version of db,db_name,table name,columns
    public static final String DATABASE_NAME = "ChatDatabase.db";
    public static final int DATABASE_VERSION = 1;
    private static final String OPENING_BRACE = "(";
    private static final String CLOSING_BRACE = ")";
    /* Structure for the main table, that holds all other tables */
    public static final String TABLE_NAME = "ChatsIndex";
    public static final String TABLES = "tables_index";
    public static final String LAST_MESSAGE = "last_message";
    public static final String LAST_MESSAGE_TIME = "last_message_time";
    public static final String ID_COLUMN = "ID";

    //create table tat stores last message(latest) with the time

    public static final String TABLE_CREATE_MAIN = "CREATE TABLE "
            + TABLE_NAME
            + OPENING_BRACE
            + ID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + TABLES + " TEXT,"
            + LAST_MESSAGE + " TEXT,"
            + LAST_MESSAGE_TIME + " TEXT"
            + CLOSING_BRACE;

    /* Structure of contact specific table */
    public static final String BODY = "body";
    public static final String DIRECTION = "direction";
    public static final String TIME = "time";

    //this is to create a table that stores msg(body)
    // ,time,direction(most probably msg comin frm whome goin to whome) the name of table being created is sent as a parameter

    public static String getThreadDefinition(String NAME) {
        return "CREATE TABLE IF NOT EXISTS "
                + NAME
                + OPENING_BRACE
                + ID_COLUMN + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + BODY + " TEXT,"
                + DIRECTION + " TEXT,"
                + TIME + " TEXT"
                + CLOSING_BRACE;
    }
}
