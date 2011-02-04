package yoonsung.odk.spreadsheet.Database;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBIO {
	//public static final String DB_PATH = "/data/data/com.yoonsung.spreadsheetsms/databases/";
	public static final String DB_PATH = "/sdcard/";
	
	public static final String DB_NAME = "my_tables";
	//public static final int DB_VERSION = 1;
		
	// Constructor
	public DBIO() {
		SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(DB_PATH + DB_NAME, null);
		//db.beginTransaction();
		// Create data table
		/*
    	db.execSQL("CREATE TABLE IF NOT EXISTS " + DB_DATA_TABLE_NAME + " ("
                + Data.DATA_ROWID + " INTEGER PRIMARY KEY,"
                + Data.DATA_PHONE_NUMBER_IN + " TEXT,"
                + Data.DATA_TIMESTAMP + " TIMESTAMP"
                + ");");
    	*/
		
		// Create tableList table
    	db.execSQL("CREATE TABLE IF NOT EXISTS " + TableList.TABLE_LIST + " ("
                + TableList.TABLE_ID + " INTEGER PRIMARY KEY,"
                + TableList.TABLE_NAME + " TEXT"
                + ");");
    	
    	// Create tableProperty table
    	db.execSQL("CREATE TABLE IF NOT EXISTS " + TableProperty.TABLE_PROPERTY + " ("
    			+ TableProperty.TABLE_PROPERTY_TABLE_ID + " INTEGER PRIMARY KEY,"
                + TableProperty.TABLE_PROPERTY_PRIME + " TEXT,"
                + TableProperty.TABLE_PROPERTY_COLUMN_ORDER + " TEXT,"
                + TableProperty.TABLE_PROPERTY_SORT_BY + " TIMESTAMP"
                + ");");
    	
    	// Create columnProperty table
    	db.execSQL("CREATE TABLE IF NOT EXISTS " + ColumnProperty.COLUMN_PROPERTY + " ("
    			+ ColumnProperty.COLUMN_PROPERTY_TABLE_ID + " INTEGER,"
    			+ ColumnProperty.COLUMN_PROPERTY_NAME + " TEXT,"
                + ColumnProperty.COLUMN_PROPERTY_ABRV + " TEXT,"
                + ColumnProperty.COLUMN_PROPERTY_TYPE + " TEXT,"
                + ColumnProperty.COLUMN_PROPERTY_SMSIN + " TEXT,"
                + ColumnProperty.COLUMN_PROPERTY_SMSOUT + " TEXT,"
                + ColumnProperty.COLUMN_PROPERTY_FOOTER_MODE+ " TEXT"
                + ");");
    	//db.endTransaction();
    	db.close();
	}
	
	public SQLiteDatabase getConn() {
		SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(DB_PATH + DB_NAME, null);
		return db;
	}
	
	public String toSafeSqlString(String input) {
		if (isInteger(input) || isDouble(input))
			return input;
		else
			return "'" + input + "'";
	}
	
	private boolean isInteger(String str) {
		try {
			Integer.parseInt(str);
		} catch(NumberFormatException e) {
			return false;
		} 
		return true;
	}
	
	private boolean isDouble(String str) {
		try {
			Double.parseDouble(str);
		} catch(NumberFormatException e) {
			return false;
		} 
		return true;
	}
	
	public String toSafeSqlColumn(String input, boolean as, String func) {
		if (as && func != null && func.trim().length() != 0)
			return func + "(" + input + ")" + " AS `" + input + "`"; 
		else if (as)
			return "`" + input + "` AS `" + input + "`";
		else 
			return "`" + input + "`";
	}
	
	public String listColumns(ArrayList<String> columns, boolean as, String func, String funcCol) {
		String result = "";
		for (int i = 0; i < columns.size(); i++) {
			String col = columns.get(i);
			// First & End
			if (i == 0) 
				result += " " + listColumnWithFunc(col, as, func, funcCol) + " ";
			else 
				result += ", " + listColumnWithFunc(col, as, func, funcCol) + " ";
		}
		return result;
	}
	
	public String listColumns(HashMap<String, String> colMapFunc, boolean as) {
		String result = "";
		boolean start = true;
		for (String col : colMapFunc.keySet()) {
			String func = colMapFunc.get(col);
			// First & Other
			if (start) {
				result += " " + listColumnWithFunc(col, as, func, col) + " ";
				start = false;
			} else { 
				result += ", " + listColumnWithFunc(col, as, func, col) + " ";
			}
		}
		return result;
	}
	
	// Add Function
	private String listColumnWithFunc(String col, boolean as, String func, String funCol) {
		if (col.equals(funCol)) 
			return toSafeSqlColumn(col, as, func);
		else
			return toSafeSqlColumn(col, as, null);
	}
	
}