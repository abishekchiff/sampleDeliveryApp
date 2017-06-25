package com.skyskew.sampledeliveryapp;

/**
 * Created by Chiffon on 25/06/17.
 */
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ConsigneeDataSource {

    // Database fields
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_NAME ,
            MySQLiteHelper.COLUMN_LAT,
            MySQLiteHelper.COLUMN_LON ,
            MySQLiteHelper.COLUMN_ISCOMPLETED};

    public ConsigneeDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public ConsigneeDetail createConsignee(String name,double lat,double lon) {

        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_NAME, name);
        values.put(MySQLiteHelper.COLUMN_LAT, lat);
        values.put(MySQLiteHelper.COLUMN_LON, lon);
        values.put(MySQLiteHelper.COLUMN_ISCOMPLETED, 0);

        long insertId = database.insert(MySQLiteHelper.TABLE_CONSIGNEE, null,
                values);

        Cursor cursor = database.query(MySQLiteHelper.TABLE_CONSIGNEE,
                allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        ConsigneeDetail newConsignee = cursorToConsignee(cursor);
        cursor.close();
        return newConsignee;
    }

    public void deleteConsignee(ConsigneeDetail consigneeDetail) {
        long id = consigneeDetail.getId();

        System.out.println("Consignee deleted with id: " + id);
        database.delete(MySQLiteHelper.TABLE_CONSIGNEE, MySQLiteHelper.COLUMN_ID
                + " = " + id, null);
    }

    public ArrayList<ConsigneeDetail> getAllComments() {
        ArrayList<ConsigneeDetail> consingees = new ArrayList<ConsigneeDetail>();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_CONSIGNEE,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {

            ConsigneeDetail comment = cursorToConsignee(cursor);
            Log.d("cursor get",comment.getName());
            consingees.add(comment);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return consingees;
    }

    private ConsigneeDetail cursorToConsignee(Cursor cursor) {
        ConsigneeDetail consigneeDetail = new ConsigneeDetail();
        consigneeDetail.setId(cursor.getLong(0));
        consigneeDetail.setName(cursor.getString(1));
        consigneeDetail.setLat(cursor.getDouble(2));
        consigneeDetail.setLon(cursor.getDouble(3));
        consigneeDetail.setDistance(5);
        consigneeDetail.setCompleted(cursor.getInt(4)==1);
        return consigneeDetail;
    }


    public int updateConsignee(ConsigneeDetail consigneeDetail) {


        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_NAME, consigneeDetail.getName());
        values.put(MySQLiteHelper.COLUMN_LAT, consigneeDetail.getLat());
        values.put(MySQLiteHelper.COLUMN_LON, consigneeDetail.getLon());
        values.put(MySQLiteHelper.COLUMN_ISCOMPLETED, consigneeDetail.isCompleted() == true ? 1 : 0);


        // updating row
        return database.update(MySQLiteHelper.TABLE_CONSIGNEE, values, MySQLiteHelper.COLUMN_ID + " = ?",
                new String[] { String.valueOf(consigneeDetail.getId()) });
    }

    public int getConsigneeCount() {
        String countQuery = "SELECT  * FROM " + MySQLiteHelper.TABLE_CONSIGNEE;

        Cursor cursor = database.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }
}