package com.uchicom.sqlp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.uchicom.sqlp.dto.Crud;

public class SqlParser {

	private Set<String> keywordSet = new HashSet<>();

	private void initProperties() {

		try {
			List<String> list = Files.readAllLines(new File("conf/keyword.properties").toPath());
			for (String keyword : list) {
				keywordSet.add(keyword);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public SqlParser() {
		initProperties();
	}

	public List<Crud> parse(String sql) {
		List<Crud> list = new ArrayList<>();
		List<String> stringList = parseString(sql);
		// System.out.println("stringList>>>>" );
		// stringList.forEach((a)->{
		// System.out.println(a);
		// });
		// C(Insert),R(Select),U(update),D(delete)
		FOR:for (int i = 0; i < stringList.size(); i++) {
			String word = stringList.get(i);
			switch (word.toLowerCase()) {
			case "select":
				for (String table : searchAll(stringList, i + 1, "from", "join")) {
					if (table.endsWith("(")) {
						list.add(new Crud("P", getToken(table.substring(0, table.length() - 1))));
					} else {
						list.add(new Crud("R", getToken(table)));
					}
				}
				// 後のfrom,join検索
				break FOR;
			case "update":// update from table or update table
				list.add(new Crud("U", searchOr(stringList, i + 1, "from")));
				for (String table : searchAll(stringList, i + 2, "from", "join")) {
					if (table.endsWith("(")) {
						list.add(new Crud("P", getToken(table.substring(0, table.length() - 1))));
					} else {
						list.add(new Crud("R", getToken(table)));
					}

				}
				break FOR;
			case "delete":// delete from table or delete table
				list.add(new Crud("D", searchOr(stringList, i + 1, "from")));
				for (String table : searchAll(stringList, i + 2, "from", "join")) {
					if (table.endsWith("(")) {
						list.add(new Crud("P", getToken(table.substring(0, table.length() - 1))));
					} else {
						list.add(new Crud("R", getToken(table)));
					}
				}
				break FOR;
			case "insert":// insert into table
				String insert = searchMust(stringList, i + 1, "into");
				if (insert.endsWith("(")) {
					list.add(new Crud("C", getToken(insert.substring(0, insert.length() - 1))));
				} else {
					list.add(new Crud("C", getToken(insert)));
				}
				for (String table : searchAll(stringList, i + 2, "from", "join")) {
					if (table.endsWith("(")) {
						list.add(new Crud("P", getToken(table.substring(0, table.length() - 1))));
					} else {
						list.add(new Crud("R", getToken(table)));
					}
				}
				break FOR;
			}
		}
		return list;
	}

	public List<String> searchAll(List<String> stringList, int fromIndex, String... previousKeywords) {
		List<String> searchList = new ArrayList<>();
		int max = stringList.size();
		for (int i = fromIndex; i < max; i++) {
			if (i == stringList.size() - 1)
				break;
			String value = stringList.get(i + 1);
			if (!value.equals("(")) {
				for (String previousKeyword : previousKeywords) { // from join
					if (previousKeyword.equals(stringList.get(i).toLowerCase())) {
						if (keywordSet.contains(value.toUpperCase())) {
							searchList.add(stringList.get(i + 2));// JOIN
																	// LATELALなど
						} else {
							searchList.add(value);
						}
					}
				}
			}
		}
		return searchList;

	}

	public String searchOr(List<String> stringList, int fromIndex, String previousKeyword) {
		String string = null;
		for (int i = fromIndex; i < stringList.size(); i++) {
			if (!previousKeyword.equals(stringList.get(i).toLowerCase())) {
				return stringList.get(i);
			}
		}
		return string;
	}

	public String searchMust(List<String> stringList, int fromIndex, String previousKeyword) {
		String string = null;
		for (int i = fromIndex; i < stringList.size(); i++) {
			if (previousKeyword.equals(stringList.get(i).toLowerCase())) {
				if (!stringList.get(i + 1).equals("(")) {
					return stringList.get(i + 1);
				}
			}
		}
		return string;
	}

	public List<String> parseString(String sql) {
		String trimed = sql.trim();
		List<String> stringList = new ArrayList<>();
		StringBuffer strBuff = new StringBuffer();
		char[] ch = trimed.toCharArray();
		boolean escapeDouble = false;
		boolean escapeSingle = false;
		for (int i = 0; i < ch.length; i++) {
			if (!escapeSingle && ch[i] == '"') {
				escapeDouble = !escapeDouble;
			} else if (!escapeDouble && ch[i] == '\'') {
				escapeSingle = !escapeSingle;
			} else {
				if (checkOr(ch[i], '\t', ' ', '\n')) {
					if (strBuff.length() != 0) {
						stringList.add(strBuff.toString());
						strBuff.setLength(0);
					}
					continue;
				} else if (checkOr(ch[i], ',', '(', ')', ';', '+', '/', '*', '-', '%', '?', '!', '=', '<', '>', '^',
						'~', '[', ']', ':')) {
					// function や プロシージャチェック
					if (ch[i] == '(') {
						if (strBuff.length() != 0) {
							if (!keywordSet.contains(strBuff.toString().toUpperCase())) {
								// ファンクションプロシージャ
								strBuff.append(ch[i]);
								stringList.add(strBuff.toString());
								strBuff.setLength(0);
								continue;
							}
						} else if (stringList.size() > 0) {
							int last = stringList.size() - 1;
							String tmp = stringList.get(last);
							stringList.remove(last);
							stringList.add(tmp + ch[i]);
							continue;
						}
					}
					if (strBuff.length() != 0) {
						stringList.add(strBuff.toString());
						strBuff.setLength(0);
					}
					stringList.add(new String(new char[] { ch[i] }));
					continue;
				} else if (checkOr(ch[i], '.')) {
					strBuff.append(stringList.get(stringList.size() - 1));
					stringList.remove(stringList.size() - 1);
				}
			}
			strBuff.append(ch[i]);
		}
		if (ch.length > 0) {
			stringList.add(strBuff.toString());
		}
		return stringList;
	}

	public boolean checkOr(char target, char... checks) {
		for (char check : checks) {
			if (target == check) {
				return true;
			}
		}
		return false;
	}
	public String getToken(String string) {
		return string.replaceAll("\\\"",  "");
	}
}
