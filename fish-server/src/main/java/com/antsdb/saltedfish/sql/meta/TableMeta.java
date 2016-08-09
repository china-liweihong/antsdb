/*-------------------------------------------------------------------------------------------------
 _______ __   _ _______ _______ ______  ______
 |_____| | \  |    |    |______ |     \ |_____]
 |     | |  \_|    |    ______| |_____/ |_____]

 Copyright (c) 2016, antsdb.com and/or its affiliates. All rights reserved. *-xguo0<@

 This program is free software: you can redistribute it and/or modify it under the terms of the
 GNU Affero General Public License, version 3, as published by the Free Software Foundation.

 You should have received a copy of the GNU Affero General Public License along with this program.
 If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>
-------------------------------------------------------------------------------------------------*/
package com.antsdb.saltedfish.sql.meta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.antsdb.saltedfish.nosql.SlowRow;
import com.antsdb.saltedfish.sql.Orca;
import com.antsdb.saltedfish.sql.vdm.KeyMaker;
import com.antsdb.saltedfish.sql.vdm.ObjectName;
import com.antsdb.saltedfish.util.BytesUtil;

public class TableMeta extends MetaObject {
    public static final ObjectName SEQ_NAME = new ObjectName(Orca.SYSNS, "tableId");

    SlowRow row;
    PrimaryKeyMeta pk;
    List<ColumnMeta> columns = Collections.emptyList();
    List<IndexMeta> indexes = new ArrayList<IndexMeta>();
    List<ForeignKeyMeta> fks = new ArrayList<>();
    int maxColumnId = 0;
    KeyMaker keyMaker;

    public TableMeta(Orca orca, ObjectName name) {
        int id;
        if (Orca.SYSNS.equals(name.getNamespace())) {
            id = -TableId.valueOf(name.getTableName().toUpperCase()).ordinal();
        }
        else {
            id = (int)orca.getIdentityService().getSequentialId(SEQ_NAME);
        }
        this.row = new SlowRow(id);
        setId(id);
        setNamespace(name.getNamespace());
        setTableName(name.getTableName());
    }
    
    public TableMeta(SlowRow row) {
        super();
        this.row = row;
    }

    public int getId() {
        return (Integer)row.get(ColumnId.systable_id.getId());
    }
    
    public void setId(int id) {
        row.set(ColumnId.systable_id.getId(), id);
    }
    
    public String getNamespace() {
        return (String)row.get(ColumnId.systable_namespace.getId());
    }
    
    public TableMeta setNamespace(String name) {
        row.set(ColumnId.systable_namespace.getId(), name);
        return this;
    }
    
    public String getTableName() {
        return (String)row.get(ColumnId.systable_table_name.getId());
    }
    
    public TableMeta setTableName(String name) {
        row.set(ColumnId.systable_table_name.getId(), name);
        return this;
    }
    
    public byte[] getKey() {
        return this.row.getKey();
    }
    
    static byte[] getKey(ObjectName name) {
        String key = (name.getNamespace() + "." + name.getTableName()).toLowerCase();
        return BytesUtil.toUtf8(key);
    }
    
    public List<ColumnMeta> getColumns() {
        return this.columns;
    }
    
    public void setColumns(List<ColumnMeta> columns) {
    	this.maxColumnId = 0;
    	for (ColumnMeta i:columns) {
    		this.maxColumnId = Math.max(this.maxColumnId, i.getColumnId());
    	}
    	this.columns = Collections.unmodifiableList(columns);
    }
    
    public ColumnMeta getColumn(String columnName) {
        for (ColumnMeta i:this.columns) {
            if (columnName.equalsIgnoreCase(i.getColumnName())) {
                return i;
            }
        }
        return null;
    }
    
    public PrimaryKeyMeta getPrimaryKey() {
        return this.pk;
    }
    
    public void setPrimaryKey(PrimaryKeyMeta pk) {
        this.pk = pk;
    }
    
    public ObjectName getObjectName() {
        return new ObjectName(getNamespace(), getTableName());
    }

    public ColumnMeta getColumn(int columnId) {
        for (ColumnMeta i:this.columns) {
            if (i.getId() == columnId) {
                return i;
            }
        }
        return null;
    }

    public ColumnMeta getColumnByColumnId(int columnId) {
        for (ColumnMeta i:this.columns) {
            if (i.getColumnId() == columnId) {
                return i;
            }
        }
        return null;
    }

    public Collection<ForeignKeyMeta> getForeignKeys() {
        return this.fks;
    }
    
    public Collection<IndexMeta> getIndexes() {
        return this.indexes;
    }

    @Override
    public String toString() {
        return getObjectName().toString();
    }
    
    public ColumnMeta findAutoIncrementColumn() {
    	for (ColumnMeta i:this.columns) {
    		if (i.isAutoIncrement()) {
    			return i;
    		}
    	}
    	return null;
    }

	public int getMaxColumnId() {
		return maxColumnId;
	}
    
    public ObjectName getAutoIncrementSequenceName() {
		ObjectName name = new ObjectName(getNamespace(), getTableName() + "_auto_increment");
    	return name;
    }

	public IndexMeta findIndex(String indexName) {
		for (IndexMeta i:this.indexes) {
			if (i.getName().equalsIgnoreCase(indexName)) {
				return i;
			}
		}
		return null;
	}
	
	public KeyMaker getKeyMaker() {
		return this.keyMaker;
	}
	
	/**
	 * get the external name. external table are the name used in external storage such as HBase
	 * 
	 * @return
	 */
	public String getExternalName() {
        return (String)row.get(ColumnId.systable_ext_name.getId());
	}
	
	public void setExternalName(String value) {
		row.set(ColumnId.systable_ext_name.getId(), value);
	}
}