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
package org.opendatakit.tables.view.custom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.opendatakit.tables.activities.Controller;
import org.opendatakit.tables.data.ColumnProperties;
import org.opendatakit.tables.data.DbHelper;
import org.opendatakit.tables.data.DbTable;
import org.opendatakit.tables.data.KeyValueStore;
import org.opendatakit.tables.data.Query;
import org.opendatakit.tables.data.Table;
import org.opendatakit.tables.data.TableProperties;
import org.opendatakit.tables.data.UserTable;
import android.content.Context;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;


public abstract class CustomView extends LinearLayout {
    
    protected static WebView webView;
    private static ViewGroup lastParent;
    
    protected CustomView(Context context) {
        super(context);
        initCommonWebView(context);
    }
    
    public static void initCommonWebView(Context context) {
        if (webView != null) {
            return;
        }
        webView = new WebView(context);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {});
    }
    
    protected void initView() {
        if (lastParent != null) {
            lastParent.removeView(webView);
        }
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.FILL_PARENT);
        addView(webView, lp);
        lastParent = this;
    }
    
    protected void load(String url) {
        webView.clearView();
        webView.loadUrl(url);
    }
    
    protected void loadData(String data, String mimeType, String encoding) {
        webView.clearView();
        webView.loadData(data, mimeType, encoding);
    }
    
    /**
     * "Unused" warnings are suppressed because the public methods of this
     * class are meant to be called through the JavaScript interface.
     */
    protected class RowData {
        
        private final TableProperties tp;
        private Map<String, String> data;
        
        RowData(TableProperties tp) {
            this.tp = tp;
        }
        
        RowData(TableProperties tp, Map<String, String> data) {
            this.tp = tp;
            this.data = data;
        }
        
        void set(Map<String, String> data) {
            this.data = data;
        }
        
        public String get(String key) {
            ColumnProperties cp = tp.getColumnByUserLabel(key);
            if (cp == null) {
                return null;
            }
            return data.get(cp.getColumnDbName());
        }
    }
    
    /**
     * "Unused" warnings are suppressed because the public methods of this
     * class are meant to be called through the JavaScript interface.
     */
    protected class TableData {
        
        private final Table rawTable;
        private final UserTable userTable;
        private Map<String, Integer> colMap;
        
        public TableData(TableProperties tp, Table table) {
            rawTable = table;
            userTable = null;
            initColMap(tp);
        }
        
        public TableData(TableProperties tp, UserTable table) {
            rawTable = null;
            userTable = table;
            initColMap(tp);
        }
        
        private void initColMap(TableProperties tp) {
            colMap = new HashMap<String, Integer>();
            ColumnProperties[] cps = tp.getColumns();
            for (int i = 0; i < cps.length; i++) {
                colMap.put(cps[i].getDisplayName(), i);
                String abbr = cps[i].getSmsLabel();
                if (abbr != null) {
                    colMap.put(abbr, i);
                }
            }
        }
        
        public int getCount() {
            if (rawTable == null) {
                return userTable.getHeight();
            } else {
                return rawTable.getHeight();
            }
        }
        
        public String getData(int rowNum, String colName) {
            if (colMap.containsKey(colName)) {
                if (rawTable == null) {
                    return userTable.getData(rowNum, colMap.get(colName));
                } else {
                    return rawTable.getData(rowNum, colMap.get(colName));
                }
            } else {
                return null;
            }
        }
    }
    
    protected class Control {
        
        protected Context context;
        private TableProperties[] allTps;
        private Map<String, TableProperties> tpMap;
        
        public Control(Context context) {
            this.context = context;
        }
        
        private void initTpInfo() {
            if (tpMap != null) {
                return;
            }
            tpMap = new HashMap<String, TableProperties>();
            allTps = TableProperties.getTablePropertiesForAll(
                    DbHelper.getDbHelper(context),
                    KeyValueStore.Type.ACTIVE);
            for (TableProperties tp : allTps) {
                tpMap.put(tp.getDisplayName(), tp);
            }
        }
        
        public boolean openTable(String tableName, String query) {
            initTpInfo();
            if (!tpMap.containsKey(tableName)) {
                return false;
            }
            Controller.launchTableActivity(context, tpMap.get(tableName),
                    query, false);
            return true;
        }
        
        public TableData query(String tableName, String searchText) {
            initTpInfo();
            if (!tpMap.containsKey(tableName)) {
                return null;
            }
            TableProperties tp = tpMap.get(tableName);
            Query query = new Query(allTps, tp);
            query.loadFromUserQuery(searchText);
            DbTable dbt = DbTable.getDbTable(DbHelper.getDbHelper(context),
                    tp.getTableId());
            ArrayList<String> columnOrder = tp.getColumnOrder();
            return new TableData(tp, dbt.getRaw(query, columnOrder.toArray(new String[columnOrder.size()])));
        }
    }
}
