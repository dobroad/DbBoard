package com.lee.board;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.lee.board.Article;
import com.lee.board.util.DBUtil;
import com.lee.board.util.SecSql;

public class App {
	public void run() {
		Scanner input = new Scanner(System.in);

		Connection conn = null;

		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			String url = "jdbc:mysql://127.0.0.1:3306/text_board?useUnicode=true&characterEncoding=utf8&autoReconnect=true&serverTimezone=UTC&useOldAliasMetadataBehavior=true&zeroDateTimeNehavior=convertToNull";

			conn = DriverManager.getConnection(url, "root", "");

			while (true) {
				System.out.printf("명령어) ");
				String cmd = input.nextLine();
				cmd = cmd.trim(); // 공백제거

				int actionResult = doAction(conn, input, cmd);
				if (actionResult == -1) {
					break;
				}

			}

		} catch (ClassNotFoundException e) {
			System.out.println("드라이버 로딩 실패");
		} catch (SQLException e) {
			System.out.println("에러: " + e);
		} finally {
			try {
				if (conn != null && !conn.isClosed()) {
					conn.close(); // 연결 종료
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private static int doAction(Connection conn, Scanner input, String cmd) {
		if (cmd.equals("article write")) { // cmd.equals => 문장비교

			String title;
			String body;

			System.out.println("== 게시글 작성 ==");
			System.out.printf("제목: ");
			title = input.nextLine();
			System.out.printf("내용: ");
			body = input.nextLine();

			SecSql sql = new SecSql();
			sql.append("INSERT INTO article");
			sql.append("SET regDate = NOW()");
			sql.append(", updateDate = NOW()");
			sql.append(", title = ?", title);
			sql.append(", body = ?", body);

			int id = DBUtil.insert(conn, sql);

			System.out.printf("%d번 게시물이 생성되었습니다.\n", id);

		} else if (cmd.equals("article list")) { // cmd.equals => 문장비교
			System.out.println("== 게시글 목록 ==");

			List<Article> articles = new ArrayList<>(); // 출력용

			SecSql sql = new SecSql();
			sql.append("SELECT * FROM article");
			sql.append("ORDER BY id DESC");

			List<Map<String, Object>> articleListMap = DBUtil.selectRows(conn, sql);
			for (Map<String, Object> articleMap : articleListMap) {
				articles.add(new Article(articleMap)); // 틀에 담아(articleMap) 운반차에 싣고(Article) 보관함에 저장(articles)
			}

			if (articles.size() == 0) {
				System.out.println("게시물이 존재하지 않습니다.");
				return 0;
			}

			System.out.println("번호 / 제목");
			for (Article article : articles) { // 운반객체 article 안에 있는 articles 처음부터 끝까지 조회
				System.out.printf("%d / %s\n", article.id, article.title);
			}

		} else if (cmd.startsWith("article modify")) { // cmd.startsWith => 시작하는 문장 대조

			int id = Integer.parseInt(cmd.split(" ")[2].trim()); // cmd.split(" "); => article modify 명령어를 공백을 기준으로
																	// 나눠서 배열로 저장
																	// [2] => 공백을 기준으로 배열로 나누면 id가 들어가는 자리의 index
																	// .trim() => 공백 제거
			String title;
			String body;

			System.out.println("== 게시글 수정 ==");
			System.out.printf("새 제목: ");
			title = input.nextLine();
			System.out.printf("새 내용: ");
			body = input.nextLine();

			SecSql sql = new SecSql();
			sql.append("UPDATE article");
			sql.append("SET regDate = NOW()");
			sql.append(", updateDate = NOW()");
			sql.append(", title = ?", title);
			sql.append(", body = ?", body);
			sql.append("WHERE id = ?", id);

			DBUtil.update(conn, sql);

			System.out.printf("%d번 글이 수정되었습니다.\n", id);

		} else if (cmd.startsWith("article delete")) { // cmd.startsWith => 시작하는 문장 대조

			int id = Integer.parseInt(cmd.split(" ")[2].trim());
			
			System.out.println("== 게시글 삭제 ==");

			SecSql sql = new SecSql();
			sql.append("DELETE FROM article");
			sql.append("WHERE id = ?", id);

			DBUtil.delete(conn, sql);

			System.out.printf("%d번 글이 삭제되었습니다.\n", id);

		} else if (cmd.equals("system exit")) {
			System.out.println("프로그램을 종료합니다.");
			return -1;

		} else {
			System.out.println("잘못된 명령어입니다.");
		}

		return 0;
	}
}