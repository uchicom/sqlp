package com.uchicom.sqlp.dto;

public class Crud {

	public static final String SELECT = "SELECT";//R
	public static final String INSERT = "INSERT";//C
	public static final String UPDATE = "UPDATE";//U
	public static final String DELETE = "DELETE";//D
	private String type;
	private String tableName;
	public Crud(String type, String tableName) {
		this.type = type;
		this.tableName = tableName;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	@Override
	public String toString() {
		return "Crud [type=" + type + ", tableName=" + tableName + "]";
	}
}
