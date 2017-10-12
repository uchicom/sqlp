package com.uchicom.sqlp;

import java.util.List;

import org.junit.Test;

import com.uchicom.sqlp.dto.Crud;

public class SqlParserTest {

	@Test
	public void parseString() {
		SqlParser parser = new SqlParser();
		System.out.println(parser.parseString("SELECt a, b from tablea"));
		System.out.println(parser.parseString("INSERt INtO tableb(a,b,c) values('a\"','\"b','c'); "));
		System.out.println(parser.parseString("update from tableb set a='a', b='b' where a='a'; "));
		System.out.println(parser.parseString("delete from tableb where a=\"'a'\""));
		System.out.println(parser.parseString("delete from tableb where a=(select * from a)"));
		System.out.println(parser.parseString("delete from tableb where a=(select * from a)"));
		System.out.println(parser.parseString("insert into tableb as select * from tablea where a=(select * from tablec tabc inner join proceda('b') proa on tabc.id = proa.id)"));
	}
	
	@Test
	public void parse() {
		SqlParser parser = new SqlParser();
		List<Crud> crudList = parser.parse("insert into tableb as select * from tablea where a=(select * from tablec tabc inner join proceda('b') proa on tabc.id = proa.id)");
		System.out.println(crudList);
	}

}
