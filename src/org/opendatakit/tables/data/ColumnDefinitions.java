/*
 * Copyright (C) 2012 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.tables.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Holds the immutable column properties. In essence this represents the 
 * backing table in the database. This should be acted upon via 
 * {@link ColumnDefinitions}.
 * <p>
 * This is the column analogue to TableDefinitions. 
 * @author sudar.sam@gmail.com
 *
 */
public class ColumnDefinitions {
  
  private static final String TAG = "ColumnDefinitions";
  
  private static final String DB_BACKING_NAME = "column_definitions";
  
  /***********************************
   *  These are the columns that exist in the DB_TABLENAME tabe.
   ***********************************/ 
  public static final String DB_TABLE_ID = "table_id";
  public static final String DB_ELEMENT_KEY = 
      "element_key";// (was DB_DB_COLUMN_NAME)
  public static final String  DB_ELEMENT_NAME = "element_name";
  public static final String DB_ELEMENT_TYPE = 
      "element_type"; // (was DB_COL_TYPE)
  public static final String  DB_LIST_CHILD_ELEMENT_KEYS = 
      "list_child_element_keys";
  public static final String  DB_IS_PERSISTED = 
      "is_persisted"; // default: 1 (true)
  public static final String DB_JOINS = "joins";
  
  /***********************************
   *  Default values for those columns which require them.
   ***********************************/
  public static final int DEFAULT_DB_IS_PERSISTED = 1;
  public static final ColumnType DEFAULT_DB_ELEMENT_TYPE =
      ColumnType.NONE;
  public static final String DEFAULT_DB_JOINS = null;
  
  // A set of all the column names in this table.
  public static final Set<String> columnNames;
  
  // Populate the set.
  static {
    columnNames = new HashSet<String>();
    columnNames.add(DB_TABLE_ID);
    columnNames.add(DB_ELEMENT_KEY);
    columnNames.add(DB_ELEMENT_NAME);
    columnNames.add(DB_ELEMENT_TYPE);
    columnNames.add(DB_LIST_CHILD_ELEMENT_KEYS);
    columnNames.add(DB_IS_PERSISTED);
    columnNames.add(DB_JOINS);
  }
  
  /*
   * Get the where sql clause for the table id and the given element key.
   */
  private static final String WHERE_SQL_FOR_ELEMENT = 
      DB_TABLE_ID + " = ? AND " + DB_ELEMENT_KEY + " = ?";
  
  private static final String WHERE_SQL_FOR_TABLE_IS_PERSISTED = 
      DB_TABLE_ID + " = ? AND " + DB_IS_PERSISTED + " = ?";
  
  private static final String WHERE_SQL_FOR_TABLE = 
      DB_TABLE_ID + " = ?";
  
  /**
   * Return an unmodifiable set that holds all the names for the columns in 
   * DB_TABLENAME. Note that these are the column names of the actual table 
   * defined in ColumnDefinitions, NOT the user-defined column names that are
   * defined in the ColumnDefinitions table.
   * @return
   */
  public static Set<String> getColumnNames() {
    return Collections.unmodifiableSet(columnNames);
  }
  
  /**
   * Return all the persisted element keys for the given table. Since element
   * keys are the unique identifier of the column, this essentially gets all
   * the columns for the table id. The lone caveat here is that this is only
   * the persisted columns. Persisted columns are basically those that are 
   * used by ODK Tables.
   * <p>
   * Does not close the passed in database.
   * @param tableId
   * @param db
   * @return
   */
  public static List<String> getPersistedElementKeysForTable(String tableId,
      SQLiteDatabase db) {
    Cursor c = null;
    List<String> elementKeys = new ArrayList<String>();
    try {
      c = db.query(DB_BACKING_NAME, 
          new String[] {DB_ELEMENT_KEY}, // we only want the element key column
          WHERE_SQL_FOR_TABLE_IS_PERSISTED,
          new String[] {tableId, "1"}, null, null, null);
      int dbElementKeyIndex = c.getColumnIndexOrThrow(DB_ELEMENT_KEY);
      c.moveToFirst();
      int j = 0;
      while (j < c.getCount()) {
        elementKeys.add(c.getString(dbElementKeyIndex));
        c.moveToNext();
        j++;
      }
    } finally {
      try {
        if (c != null && !c.isClosed()) {
          c.close();
        }
      } finally {
        if (db != null) {
          // TODO: fix the when to close problem
//          db.close();
        }
      }
      
    }
    return elementKeys;
  }
  
  /**
   * Return all the element keys for the given table. Element keys are 
   * the unique identifiers for the columns, so this gets ALL the element keys.
   * <p>
   * Does not close the passed in database.
   * @param tableId
   * @param db
   * @return
   */
  public static List<String> getAllElementKeysForTable(String tableId,
      SQLiteDatabase db) {
    Cursor c = null;
    List<String> elementKeys = new ArrayList<String>();
    try {
      c = db.query(DB_BACKING_NAME, 
          new String[] {DB_ELEMENT_KEY}, // we only want the element key column
          WHERE_SQL_FOR_TABLE,
          new String[] {tableId}, null, null, null);
      int dbElementKeyIndex = c.getColumnIndexOrThrow(DB_ELEMENT_KEY);
      c.moveToFirst();
      int j = 0;
      while (j < c.getCount()) {
        elementKeys.add(c.getString(dbElementKeyIndex));
        c.moveToNext();
        j++;
      }
    } finally {
      try {
        if (c != null && !c.isClosed()) {
          c.close();
        }
      } finally {
        if (db != null) {
          // TODO: fix the when to close problem
//          db.close();
        }
      }
      
    }
    return elementKeys;
  }
  
  /**
   * Return all the element names for the given table. 
   * <p>
   * Does not close the passed in database.
   * @param tableId
   * @param db
   * @return
   */
  public static List<String> getAllElementNamesForTable(String tableId,
      SQLiteDatabase db) {
    Cursor c = null;
    List<String> elementKeys = new ArrayList<String>();
    try {
      c = db.query(DB_BACKING_NAME, 
          new String[] {DB_ELEMENT_NAME},
          WHERE_SQL_FOR_TABLE,
          new String[] {tableId}, null, null, null);
      int dbElementNameIndex = c.getColumnIndexOrThrow(DB_ELEMENT_NAME);
      c.moveToFirst();
      int j = 0;
      while (j < c.getCount()) {
        elementKeys.add(c.getString(dbElementNameIndex));
        c.moveToNext();
        j++;
      }
    } finally {
      try {
        if (c != null && !c.isClosed()) {
          c.close();
        }
      } finally {
        if (db != null) {
          // TODO: fix the when to close problem
//          db.close();
        }
      }
      
    }
    return elementKeys;
  }
  
  /**
   * Return a map of columnName->Value for the row with the given table id and
   * elementKey.
   * TODO: perhaps this should become columnName->TypevValuePair like the rest
   * of these maps.
   * @param tableId
   * @param db
   * @return
   */
  public static Map<String, String> getFields(String tableId, 
      String elementKey, SQLiteDatabase db) {
    Cursor c = null;
    Map<String, String> columnDefMap = new HashMap<String, String>();
    try {
      String[] arrColumnNames = new String[columnNames.size()];
      int i = 0;
      for (String colName : columnNames) {
        arrColumnNames[i] = colName;
        i++;
      }
      c = db.query(DB_BACKING_NAME, arrColumnNames, WHERE_SQL_FOR_ELEMENT, 
          new String[] {tableId, elementKey}, null, null, null);
      int dbTableIdIndex = c.getColumnIndexOrThrow(DB_TABLE_ID);
      int dbElementKeyIndex = c.getColumnIndexOrThrow(DB_ELEMENT_KEY);
      int dbElementNameIndex = c.getColumnIndexOrThrow(DB_ELEMENT_NAME);
      int dbElementTypeIndex = c.getColumnIndexOrThrow(DB_ELEMENT_TYPE);
      int dbListChildElementKeysIndex = 
          c.getColumnIndexOrThrow(DB_LIST_CHILD_ELEMENT_KEYS);
      int dbIsPersistedIndex = c.getColumnIndexOrThrow(DB_IS_PERSISTED);
      int dbJoinsIndex = c.getColumnIndexOrThrow(DB_JOINS);
      
      if (c.getCount() > 1) {
        Log.e(TAG, "query for tableId: " + tableId + "and elementKey: " +
            elementKey + " returned >1 row in" +
            "ColumnDefinitions");
      } 
      if (c.getCount() < 1) {
        Log.e(TAG, "query for tableId: " + tableId + "and elementKey: " +
            elementKey + " returned <1 row in" +
            "ColumnDefinitions");     
      }     
      
      c.moveToFirst();
      int j = 0;
      while (j < c.getCount()) {
        columnDefMap.put(DB_TABLE_ID, c.getString(dbTableIdIndex));
        columnDefMap.put(DB_ELEMENT_KEY, c.getString(dbElementKeyIndex));
        columnDefMap.put(DB_ELEMENT_NAME, c.getString(dbElementNameIndex));
        columnDefMap.put(DB_ELEMENT_TYPE, c.getString(dbElementTypeIndex));
        columnDefMap.put(DB_LIST_CHILD_ELEMENT_KEYS, 
            c.getString(dbListChildElementKeysIndex));
        columnDefMap.put(DB_IS_PERSISTED, Integer.toString(
            c.getInt(dbIsPersistedIndex)));
        columnDefMap.put(DB_JOINS, c.getString(dbJoinsIndex));
        c.moveToNext();
        j++;
      }
    } finally {
      try {
        if (c != null && !c.isClosed()) {
          c.close();
        }
      } finally {
        if (db != null) {
          // TODO: fix the when to close problem
//          db.close();
        }
      }
      
    }
    return columnDefMap;   
  }
  
  /**
   * Set the value for the given column name for the row that matches the 
   * passed in tableId and the passed in element Key.
   * <p>
   * Does not close the database.
   * @param tableId
   * @param elementKey
   * @param columnName
   * @param newValue
   * @param db
   */
  public static void setValue(String tableId, String elementKey, 
      String columnName, String newValue, SQLiteDatabase db) {
    ContentValues values = new ContentValues();
    values.put(columnName, newValue);
    db.update(DB_BACKING_NAME, values, WHERE_SQL_FOR_ELEMENT,
        new String[] {tableId, elementKey});
  }
 
  /**
   * Set the value for the given column name for the row that matches the 
   * passed in tableId and the passed in element Key.
   * <p>
   * Does not close the database.
   * @param tableId
   * @param elementKey
   * @param columnName
   * @param newValue
   * @param db
   */
  public static void setValue(String tableId, String elementKey, 
      String columnName, int newValue, SQLiteDatabase db) {
    ContentValues values = new ContentValues();
    values.put(columnName, newValue);
    db.update(DB_BACKING_NAME, values, WHERE_SQL_FOR_ELEMENT,
        new String[] {tableId, elementKey});
  }

  /**
   * Add the definition of the column to the column_definitions table in
   * the SQLite database. All of the values in {@ColumnDefinitions.columnNames}
   * are added. Those with values not passed in are set to their default values
   * as defined in ColumnDefinitions.
   * <p>
   * Does NOT restructure the user table to add the column. Only adds a column
   * to column_definitions.
   * <p>
   * Returns a Map<String,String> of columnName->value. This is intended to
   * play nicely with {@link ColumnProperties}, which requires construction of
   * a ColumnProperties object with this information during table adding.
   * <p>
   * Does not close the passed in database.
   * TODO: check for redundant names
   * TODO: make this method also create the actual table for the rows.
   * TODO: make this return String->TypeValuePair, not the String represenation
   * of all the types.
   * @param db
   * @param tableId
   * @param elementKey
   * @param elementName
   * @param elementType type of the column. null values will be converted to
   * DEFAULT_DB_ELEMENT_TYPE
   * @param listChild
   * @param isPersisted
   * @param joins
   * @return a map of column names to fields for the new table
   */
  public static Map<String, String> addColumn(SQLiteDatabase db, 
      String tableId, String elementKey, String elementName, 
      ColumnType elementType, String listChild, int isPersisted, 
      String joins) {
    ContentValues values = new ContentValues();
    values.put(DB_TABLE_ID, tableId);
    values.put(DB_ELEMENT_KEY, elementKey);
    values.put(DB_ELEMENT_NAME, elementName);
    if (elementType == null) {
      elementType = DEFAULT_DB_ELEMENT_TYPE;
    }
    values.put(DB_ELEMENT_TYPE, elementType.name());
    values.put(DB_LIST_CHILD_ELEMENT_KEYS, listChild);
    values.put(DB_IS_PERSISTED, isPersisted);
    values.put(DB_JOINS, joins);
    db.insert(DB_BACKING_NAME, null, values);    
    // Take care of the return.
    Map<String, String> valueMap = new HashMap<String, String>();
    valueMap.put(DB_TABLE_ID, tableId);
    valueMap.put(DB_ELEMENT_KEY, elementKey);
    valueMap.put(DB_ELEMENT_NAME, elementName);
    valueMap.put(DB_ELEMENT_TYPE, elementType.name());
    valueMap.put(DB_LIST_CHILD_ELEMENT_KEYS, listChild);
    valueMap.put(DB_IS_PERSISTED, Integer.toString(isPersisted));
    valueMap.put(DB_JOINS, joins);
    return valueMap;
  }
  
  /**
   * Delete the column definition from the column definitions table by using
   * its element key. This truly deletes the column.
   * @param tableId
   * @param elementKey
   * @param db
   * @return
   */
  public static int deleteColumn(String tableId, String elementKey, 
      SQLiteDatabase db) {
    int count = db.delete(DB_BACKING_NAME, WHERE_SQL_FOR_ELEMENT, 
        new String[] {tableId, elementKey});
    if (count != 1) {
      Log.e(TAG, "deleteColumn() deleted " + count + " rows");
    }
    return count;
  }
  
  /**
   * Get the create sql for the column_definitions table.
   * @return
   */
  static String getTableCreateSql() {
    return "CREATE TABLE " + DB_BACKING_NAME + "(" +
                   DB_TABLE_ID + " TEXT NOT NULL" +
            ", " + DB_ELEMENT_KEY + " TEXT NOT NULL" +
            ", " + DB_ELEMENT_NAME + " TEXT NOT NULL" +
            ", " + DB_ELEMENT_TYPE + " TEXT NOT NULL" +
            ", " + DB_LIST_CHILD_ELEMENT_KEYS + " TEXT NULL" +
            ", " + DB_IS_PERSISTED + " INTEGER NOT NULL" +
            ", " + DB_JOINS + " TEXT NULL" +
//            ", " + DB_JOIN_TABLE_ID + " TEXT NULL" +
//            ", " + DB_JOIN_ELEMENT_KEY + " TEXT NULL" +
            ")";
}


}
