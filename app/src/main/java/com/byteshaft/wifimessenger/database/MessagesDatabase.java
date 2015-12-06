package com.byteshaft.wifimessenger.database;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
//sqlitedatabase is the class import u
// ...need to use while working with databases

public class MessagesDatabase extends SQLiteOpenHelper {

    public MessagesDatabase(Context context) {
        super(context, DatabaseConstants.DATABASE_NAME, null, DatabaseConstants.DATABASE_VERSION);
    }

    //call dbconstants class(refer this class to see what are contents of that table)
    // to create "table_create_main"
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DatabaseConstants.TABLE_CREATE_MAIN);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS" + DatabaseConstants.TABLE_CREATE_MAIN);
        onCreate(db);
    }
//table name is chatIndex
    private void updateTableIndex(SQLiteDatabase db, String name,
                                  String lastMsg, String lastMsgTime) {

        String query = "SELECT * FROM "
                + DatabaseConstants.TABLE_NAME
                + " WHERE "
                + DatabaseConstants.TABLES
                + "="
                + String.format("'%s'", name);
        Cursor cursor = db.rawQuery(query, null);
        //cursor is like a pointer tat points to derived rows from sql query,it points to first row,
        // if there are no rows in table,cursor points to null
        cursor.moveToFirst();
        //get id of column
        int id = cursor.getInt(cursor.getColumnIndex(DatabaseConstants.ID_COLUMN));

        ContentValues values = new ContentValues();

        //contentvalues is used to update table,here we create a ref
        // and store last msg n last time in 'values' and update tat in the table
        values.put(DatabaseConstants.LAST_MESSAGE, lastMsg);
        values.put(DatabaseConstants.LAST_MESSAGE_TIME, lastMsgTime);
        db.update(DatabaseConstants.TABLE_NAME, values, "ID=" + id, null);
        cursor.close();
    }

    //this is to add another row to existing table

    private void addNewTableIndex(SQLiteDatabase db, String name,
                                  String lastMsg, String lastMsgTime) {

        ContentValues values = new ContentValues();
        System.out.println("Adding: " + name);
        values.put(DatabaseConstants.TABLES, name);
        values.put(DatabaseConstants.LAST_MESSAGE, lastMsg);
        values.put(DatabaseConstants.LAST_MESSAGE_TIME, lastMsgTime);
        db.insert(DatabaseConstants.TABLE_NAME, null, values);
    }

    //a boolaen method to check if table exists

    private boolean doesTableExist(SQLiteDatabase db, String name) {
        boolean exists = false;
        Cursor cursor = db.rawQuery(
                "select DISTINCT tbl_name from sqlite_master where tbl_name = '"+name+"'", null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                exists = true;
            }
            cursor.close();
        }
        return exists;
    }

    //this is called when table for storing messages for a contact is not created

    private void createNewThreadTableIfNotExists(SQLiteDatabase db, String tableName,
                                                 String lastMsg, String lastMsgTime) {

        db.execSQL(DatabaseConstants.getThreadDefinition(tableName));
        addNewTableIndex(db, tableName, lastMsg, lastMsgTime);
    }


    //this is to add a message to the contacts message thread,ex u send 5 msgs,n
    // ur gonna send another one,the new msg needs to be added to ur msg thread

    public void addNewMessageToThread(String uniqueId, String body, String direction, String time) {
        SQLiteDatabase db = getWritableDatabase();
//get ref to writable database ie permissions to write to database
        if (doesTableExist(db, uniqueId)) {
            //check if user has already sent msgs,if yes just create another row,update its id
            updateTableIndex(db, uniqueId, body, time);
        } else {
            //first msg being sent,so create a table
            createNewThreadTableIfNotExists(db, uniqueId, body, time);
        }
//loggin the message operation for debuggin
        System.out.println("Saving to table: " + uniqueId);
//create a contentvalues ref,add data to it,n then insert that ref into database
        ContentValues values = new ContentValues();
        values.put(DatabaseConstants.BODY, body);
        values.put(DatabaseConstants.DIRECTION, direction);
        values.put(DatabaseConstants.TIME, time);
        db.insert(uniqueId, null, values);
        db.close();
    }

    //this method obtins the messages stored in the name of a certain contact
    public ArrayList<HashMap> getAllTablesIndexData() {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM " + DatabaseConstants.TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);
        ArrayList<HashMap> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            int unique_id = cursor.getInt(
                    cursor.getColumnIndex(DatabaseConstants.ID_COLUMN));
            String tableName = cursor.getString(
                    cursor.getColumnIndex(DatabaseConstants.TABLES));
            String body = cursor.getString(
                    cursor.getColumnIndex(DatabaseConstants.LAST_MESSAGE));
            String time = cursor.getString(
                    cursor.getColumnIndex(DatabaseConstants.LAST_MESSAGE_TIME));
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("unique_id", String.valueOf(unique_id));
            hashMap.put("table_name", tableName);
            hashMap.put("body", body);
            hashMap.put("time_stamp", time);
            list.add(hashMap);
        }
        db.close();
        cursor.close();
        return list;
    }

    public ArrayList<HashMap> getMessagesForContact(String tableName) {
        SQLiteDatabase db = getReadableDatabase();
        //need to make a ref to SQLiteDatabase
        // and execute getReadableDatabase for reading permissions to be set
        String query = "SELECT * FROM " + tableName;
        Cursor cursor = db.rawQuery(query, null);
//hashmap is created to store data in (unique_id,body,direction,time_Stamp) format
        ArrayList<HashMap> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            //to display msgs onto screen,we need a list that keeps populating messages
            // under the contact name so ,we define a list of type hashmap
            int unique_id = cursor.getInt(
                    cursor.getColumnIndex(DatabaseConstants.ID_COLUMN));
            String messageBody = cursor.getString(
                    cursor.getColumnIndex(DatabaseConstants.BODY));
            String messageDirection = cursor.getString(
                    cursor.getColumnIndex(DatabaseConstants.DIRECTION));
            String messageTime = cursor.getString(
                    cursor.getColumnIndex(DatabaseConstants.TIME));
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("unique_id", String.valueOf(unique_id));
            hashMap.put("body", messageBody);
            hashMap.put("direction", messageDirection);
            hashMap.put("time_stamp", messageTime);
            list.add(hashMap);
        }
        db.close();
        cursor.close();
        //this list contains messages
        return list;
    }
}
