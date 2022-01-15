package com.lee.board.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JDBCTest {

	public static void main(String[] args) {
		Connection conn = null; // DB 접속 객체

	   	try {
	   		Class.forName("com.mysql.cj.jdbc.Driver"); // Mysql JDBC 드라이버 로딩
	   		String url = "jdbc:mysql://127.0.0.1:3306/text_board?useUnicode=true&characterEncoding=utf8&autoReconnect=true&serverTimezone=Asia/Seoul&useOldAliasMetadataBehavior=true&zeroDateTimeNehavior=convertToNull";
	   		// 연결 문자열 생성 => jdbc:mysql://네트워크 주소:포트 번호/연결 DB명?옵션1&옵션2&옵션3...

	   		conn = DriverManager.getConnection(url, "root", ""); // Mysql JDBC 드라이버 연결 메소드 => (연결 문자열, 데이터베이스 ID, 데이터베이스 PASSWORD)
	   		// Connection 객체를 반환 => 앞으로 conn을 이용해서 쿼리문을 실행할 예정입니다.

	   	} catch (ClassNotFoundException e) {
	   		System.out.println("드라이버 로딩 실패");
	   	} catch (SQLException e) {
	   		System.out.println("에러: " + e);
	   	} finally { // 예외 상황이든 아니든 무조건 마지막에 실행하는 finally
	   		try {
	   			if (conn != null && !conn.isClosed()) {
	   				conn.close(); // 연결 종료
	   			}
	   		} catch (SQLException e) {
	   			e.printStackTrace();
			}
		}
	}

}
