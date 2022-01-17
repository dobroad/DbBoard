package com.lee.board;

import java.sql.Connection;
import java.sql.DriverManager;
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

			System.out.printf("%d번 게시글이 생성되었습니다.\n", id);

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
				System.out.println("게시글이 존재하지 않습니다.");
				return 0;
			}

			System.out.println("번호 / 제목");
			for (Article article : articles) { // 운반객체 article 안에 있는 articles 처음부터 끝까지 조회
				System.out.printf("%d / %s\n", article.id, article.title);
			}

		} else if (cmd.startsWith("article modify ")) { // cmd.startsWith => 시작하는 문장 대조

			boolean isInt = cmd.split(" ")[2].matches("-?\\d+"); // .matches("-?\\d+") => 정수인지 문자열인지 판단
			if (!isInt) {
				System.out.println("게시글의 ID를 숫자로 입력해주세요.");
				return 0;
			}

			int id = Integer.parseInt(cmd.split(" ")[2].trim()); // cmd.split(" "); => article modify 명령어를 공백을 기준으로
																	// 나눠서 배열로 저장
																	// [2] => 공백을 기준으로 배열로 나누면 id가 들어가는 자리의 index
																	// .trim() => 공백 제거

			SecSql sql = new SecSql();
			sql.append("SELECT COUNT(*)");
			sql.append("FROM article");
			sql.append("WHERE id = ?", id);

			int articlesCount = DBUtil.selectRowIntValue(conn, sql);
			if (articlesCount == 0) {
				System.out.printf("%d번 게시글이 존재하지 않습니다.\n", id);
				return 0;
			}

			String title;
			String body;

			System.out.println("== 게시글 수정 ==");
			System.out.printf("새 제목: ");
			title = input.nextLine();
			System.out.printf("새 내용: ");
			body = input.nextLine();

			sql = new SecSql();
			sql.append("UPDATE article");
			sql.append("SET regDate = NOW()");
			sql.append(", updateDate = NOW()");
			sql.append(", title = ?", title);
			sql.append(", body = ?", body);
			sql.append("WHERE id = ?", id);

			DBUtil.update(conn, sql);

			System.out.printf("%d번 글이 수정되었습니다.\n", id);

		} else if (cmd.startsWith("article delete ")) { // cmd.startsWith => 시작하는 문장 대조

			boolean isInt = cmd.split(" ")[2].matches("-?\\d+"); // .matches("-?\\d+") => 정수인지 문자열인지 판단
			if (!isInt) {
				System.out.println("게시글의 ID를 숫자로 입력해주세요.");
				return 0;
			}

			int id = Integer.parseInt(cmd.split(" ")[2].trim());

			SecSql sql = new SecSql();
			sql.append("SELECT COUNT(*)");
			sql.append("FROM article");
			sql.append("WHERE id = ?", id);

			int articlesCount = DBUtil.selectRowIntValue(conn, sql);
			if (articlesCount == 0) {
				System.out.printf("%d번 게시글이 존재하지 않습니다.\n", id);
				return 0;
			}

			System.out.println("== 게시글 삭제 ==");

			sql = new SecSql();
			sql.append("DELETE FROM article");
			sql.append("WHERE id = ?", id);

			DBUtil.delete(conn, sql);

			System.out.printf("%d번 글이 삭제되었습니다.\n", id);

		} else if (cmd.startsWith("article detail ")) { // cmd.startsWith => 시작하는 문장 대조

			boolean isInt = cmd.split(" ")[2].matches("-?\\d+"); // .matches("-?\\d+") => 정수인지 문자열인지 판단
			if (!isInt) {
				System.out.println("게시글의 ID를 숫자로 입력해주세요.");
				return 0;
			}

			int id = Integer.parseInt(cmd.split(" ")[2].trim());

			SecSql sql = new SecSql();
			sql.append("SELECT COUNT(*)");
			sql.append("FROM article");
			sql.append("WHERE id = ?", id);

			int articlesCount = DBUtil.selectRowIntValue(conn, sql);
			if (articlesCount == 0) {
				System.out.printf("%d번 게시글이 존재하지 않습니다.\n", id);
				return 0;
			}

			System.out.println("== 게시글 상세내용 ==");

			sql = new SecSql();
			sql.append("SELECT * FROM article");
			sql.append("WHERE id = ?", id);
			sql.append("ORDER BY id DESC");

			Map<String, Object> articleMap = DBUtil.selectRow(conn, sql);

			Article article = new Article(articleMap);
			System.out.printf("번호: %d\n", article.id);
			System.out.printf("등록 날짜: %s", article.regDate);
			System.out.printf("수정 날짜: %s\n", article.updateDate);
			System.out.printf("제목: %s\n", article.title);
			System.out.printf("내용: %s\n", article.body);

		} else if (cmd.equals("member join")) { // cmd.equals => 문장비교
			String loginId;
			String loginPw;
			String loginPwConfirm;
			String name;
			
			System.out.println("== 회원가입 ==");
						
			SecSql sql;
			
			int joinTry = 0;
			
			while(true) {
				sql = new SecSql();
				
				if(joinTry >= 3) {
					System.out.println("회원가입을 다시 시도해주세요.");
					return 0;
				}
				
				System.out.printf("로그인 아이디: ");
				loginId = input.nextLine();
				
				if(loginId.length() == 0) {
					System.out.println("아이디를 입력해주세요.");
					joinTry++;
					continue;
				}
				
				sql.append("SELECT COUNT(*) FROM `member`");
				sql.append("WHERE loginId = ?", loginId);
				
				int memberCnt = DBUtil.selectRowIntValue(conn, sql);
				if(memberCnt > 0) {
					System.out.println("이미 존재하는 아이디입니다.");
					joinTry++;
					continue;
				}
				
				break;
			}
			
			joinTry = 0;
			
			while(true) {
				if(joinTry >= 3) {
					System.out.println("회원가입을 다시 시도해주세요.");
					return 0;
				}
				
				System.out.printf("로그인 비밀번호: ");
				loginPw = input.nextLine();
				
				if(loginPw.length() == 0) {
					System.out.println("비밀번호를 입력해주세요.");
					joinTry++;
					continue;
				}
				
				while(true) {
					System.out.printf("로그인 비밀번호 확인: ");
					loginPwConfirm = input.nextLine();
					if(loginPwConfirm.length() == 0) {
						System.out.println("비밀번호 확인을 입력해주세요.");
						continue;
					}
					
					break;
				}
				
				if(!loginPw.equals(loginPwConfirm)) {
					System.out.println("입력된 비밀번호가 일치하지 않습니다.");
					joinTry++;
					continue;
				}
				
				break;
			}
			
			while(true) {
				System.out.printf("이름: ");
				name = input.nextLine();
				if(name.length() == 0) {
					System.out.println("이름을 입력해주세요.");
					continue;
				}
				
				break;
			}
			
			sql = new SecSql();
			sql.append("INSERT INTO member");
			sql.append("SET regDate = NOW()");
			sql.append(", updateDate = NOW()");
			sql.append(", loginId = ?", loginId);
			sql.append(", loginPw = ?", loginPw);
			sql.append(", name = ?", name);
			
			DBUtil.insert(conn, sql);
			
			System.out.printf("%s님 환영합니다.\n", name);
		
			
		} else if (cmd.equals("member login")) { // cmd.equals => 문장비교
			String loginId;
			String loginPw;
			
			System.out.println("== 로그인 ==");
			
			SecSql sql;
			
			int joinTry = 0;
			
			while(true) {
				sql = new SecSql();
				
				if(joinTry >= 3) {
					System.out.println("로그인을 다시 시도해주세요.");
					return 0;
				}
				
				System.out.printf("로그인 아이디: ");
				loginId = input.nextLine();
				if(loginId.length() == 0) {
					System.out.println("아이디를 입력해주세요.");
					joinTry++;
					continue;
				}

				sql.append("SELECT COUNT(*) FROM member");
				sql.append("WHERE loginId = ?", loginId);
				
				int memberCnt = DBUtil.selectRowIntValue(conn, sql);
				if(memberCnt == 0) {
					System.out.println("아이디가 존재하지 않습니다.");
					joinTry++;
					continue;
				}
				
				break;
			}
			
			joinTry = 0;
			
			while(true) {
				if(joinTry >= 3) {
					System.out.println("로그인을 다시 시도해주세요.");
					return 0;
				}
				
				System.out.printf("로그인 비밀번호: ");
				loginPw = input.nextLine();
				if(loginPw.length() == 0) {
					System.out.println("비밀번호를 입력해주세요.");
					joinTry++;
					continue;
				}
				
				break;
			}
			
			// 데이터를 가져온다
			// 편하게 사용하기 위해 Member 객체 생성
			// Member 객체에서 데이터를 형태에 맞게 조정
			// Member 인스턴스를 만들어서 비밀번호 값 조회
			sql = new SecSql();
			sql.append("SELECT * FROM member");
			sql.append("WHERE loginId = ?", loginId);
			
			Map<String, Object> memberMap = DBUtil.selectRow(conn, sql);
			Member member = new Member(memberMap);
			if(!member.loginPw.equals(loginPw)) {
				System.out.println("비밀번호가 일치하지 않습니다.");
				return 0;
			}
			
			System.out.printf("%s님 환영합니다.\n", member.name);			
						
		} else if (cmd.equals("system exit")) {
			System.out.println("프로그램을 종료합니다.");
			return -1;

		} else {
			System.out.println("잘못된 명령어입니다.");
		}

		return 0;
	}
}