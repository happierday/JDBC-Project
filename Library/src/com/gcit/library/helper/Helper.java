/**
 * 
 */
package com.gcit.library.helper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import com.gcit.library.main.Main;
import com.gcit.library.model.Author;
import com.gcit.library.model.Book;
import com.gcit.library.model.Borrower;
import com.gcit.library.model.Branch;
import com.gcit.library.model.Genre;
import com.gcit.library.model.Publisher;

/**
 * @author jianwu
 *
 */
public class Helper {

	private String url = Main.getUrl();
	private String driver = Main.getDriver();
	private String username = Main.getUsername();
	private String pwd = Main.getPwd();
	
	public <T> int getChoiceIndex(LinkedList<T> list) {
		int size = list.size();
		int choice = 0;
		while(true) {
			if(choice > 0) {
				break;
			}
			if(Main.sc.hasNextInt()) {
				choice = Main.sc.nextInt();
				if(choice > 0 && choice <= size+1) {
					return choice;
				}else {
					choice = 0;
					System.out.println("Please enter a number between 1 and " + (size+1)+ "!");
				}
			}else {
				System.out.println("Please enter a number!");
				Main.sc.next();
			}
		}
		return choice;
	}
	
	/**
	 * get user choices
	 * @param list
	 * @return
	 */
	public <T> T getChoices(LinkedList<T> list){
		int size = list.size();
		int choice = 0;
		while(true) {
			if(choice > 0) {
				break;
			}
			if(Main.sc.hasNextInt()) {
				choice = Main.sc.nextInt();
				if(choice > 0 && choice <= size) {
					return list.get(choice-1);
				}else if(choice == size+1){
					//if choose to go back, clean up list and librarian
					list.clear();
					break;
				}else {
					choice = 0;
					System.out.println("Please enter a number between 1 and " + (size+1)+ "!");
				}
			}else {
				System.out.println("Please enter a number!");
				Main.sc.next();
			}
		}
		return null;
	}
	
	/**
	 * formate plain numebr string into phone formate
	 * @param s
	 * @return
	 */
	public String formatePhone(String s) {
		return "(" + s.substring(0,3) + ")" + s.substring(3,6) + "-" + s.substring(6);
	}
	
	/**
	 * get branch list
	 * @return
	 * @throws SQLException
	 */
	public LinkedList<Branch> getBranchList() throws SQLException {
		LinkedList<Branch> list = new LinkedList<Branch>();
		Connection cnn = null;
		try {
			Class.forName(driver);
			cnn = DriverManager.getConnection(url,username,pwd);
			cnn.setAutoCommit(false);
			String sql = "select * from tbl_library_branch;";
			PreparedStatement pstate = cnn.prepareStatement(sql);
			ResultSet rs = pstate.executeQuery();
			Branch temp;
			int index = 0;
			while(rs.next()) {
				index++;
				temp = new Branch();
				temp.setId(rs.getInt(1));
				temp.setBranchName(rs.getString(2));
				temp.setAddress(rs.getString(3));
				list.add(temp);
				System.out.println(index + ") " + temp.getBranchName() +
									", address: " + temp.getAddress());
			}
			System.out.println((index+1)+") go back" );
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			if(cnn != null) {
				System.out.println("DB Roll Back due to error! Back to previous Menu!");
			}
		} finally {
			if(cnn != null) {
				cnn.close();
				System.out.println("DB connection closed");
			}
		}
		return list;
	}
	
	/**
	 * get book list
	 * @return list
	 * @throws SQLException
	 */
	public LinkedList<Book> getBookAuthorList() throws SQLException {
		LinkedList<Book> list = new LinkedList<Book>();
		Connection cnn = null;
		try {
			Class.forName(driver);
			cnn = DriverManager.getConnection(url,username,pwd);
			String sql = "select * from tbl_book";
			PreparedStatement pstate = cnn.prepareStatement(sql);
			ResultSet rs = pstate.executeQuery();
			int index = 0;
			Book temp;
			while(rs.next()) {
				index++;
				temp = new Book();
				temp.setBookId(rs.getInt(1));
				temp.setTitle(rs.getString(2));
				list.add(temp);
				System.out.println(index + ") " + temp.getTitle());
			}
			System.out.println((index+1)+") go back" );
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			if(cnn != null) {
				cnn.rollback();
				System.out.println("DB Roll Back due to error! Back to previous Menu!");
			}
		} finally {
			if(cnn != null) {
				cnn.close();
				System.out.println("DB connection closed");
			}
		}
		return list;
	}
	
	/**
	 * get publisher lsit
	 * @return
	 * @throws SQLException
	 */
	public LinkedList<Publisher> getPublisherList() throws SQLException{
		LinkedList<Publisher> list = new LinkedList<Publisher>();
		Connection cnn = null;
		list.clear();
		try {
			Class.forName(driver);
			cnn = DriverManager.getConnection(url,username,pwd);
			cnn.setAutoCommit(false);
			String sql = "select * from tbl_publisher;";
			PreparedStatement pstate = cnn.prepareStatement(sql);
			ResultSet rs = pstate.executeQuery();
			Publisher temp;
			int index = 0;
			while(rs.next()) {
				index++;
				temp = new Publisher();
				temp.setPublisherId(rs.getInt(1));
				temp.setPublisherName(rs.getString(2));
				temp.setPublisherAddress(rs.getString(3));
				temp.setPublisherPhone(rs.getString(4));
				list.add(temp);
				System.out.println(index + ") " + temp.getPublisherName() +
									", address: " + temp.getPublisherAddress() + 
									", phone: " + temp.getPublisherPhone());
			}
			System.out.println((index+1)+") go back" );
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			if(cnn != null) {
				System.out.println("DB Roll Back due to error! Back to previous Menu!");
			}
		} finally {
			if(cnn != null) {
				cnn.close();
				System.out.println("DB connection closed");
			}
		}
		return list;
	}
	
	/**
	 * general delete query for tables
	 * @param tableName
	 * @param col
	 * @param value
	 * @throws SQLException
	 */
	public void deleteRowFromTable(String tableName,String col,int value) throws SQLException {
		Connection cnn = null;
		try {
			Class.forName(driver);
			cnn = DriverManager.getConnection(url,username,pwd);
			cnn.setAutoCommit(false);
			String sql = "delete from " + tableName+ " where " + col + " = ?;";
			PreparedStatement pstate = cnn.prepareStatement(sql);
			pstate.setInt(1, value);
			pstate.execute();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			if(cnn != null) {
				cnn.rollback();
				System.out.println("DB Roll Back due to error! Back to previous Menu!");
			}
		} finally {
			if(cnn != null) {
				cnn.commit();
				System.out.println("Delete Updated!");
				cnn.close();
				System.out.println("DB connection closed");
			}
		}
	}
	
	/**
	 * general update table after editing
	 * @param tableName
	 * @param col
	 * @param value
	 * @param idCol
	 * @param id
	 * @throws SQLException
	 */
	public void updateTable(String tableName,String col, String value, String idCol, int id) throws SQLException {
		Connection cnn = null;
		try {
			Class.forName(driver);
			cnn = DriverManager.getConnection(url,username,pwd);
			cnn.setAutoCommit(false);
			String sql = "update " + tableName + " set "+ col +" = ? where "+ idCol +"= ?";
			PreparedStatement pstate = cnn.prepareStatement(sql);
			pstate.setString(1, value);
			pstate.setInt(2, id);
			pstate.execute();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			if(cnn != null) {
				cnn.rollback();
				System.out.println("DB Roll Back due to error! Back to previous Menu!");
			}
		} finally {
			if(cnn != null) {
				cnn.commit();
				System.out.println("Updated borrower table!");
				cnn.close();
				System.out.println("DB connection closed");
			}
		}
	}
	
	public boolean insertAuthor(String name) throws SQLException {
		Connection cnn = null;
		try {
			Class.forName(driver);
			cnn = DriverManager.getConnection(url,username,pwd);
			cnn.setAutoCommit(false);
			String sql = "insert into tbl_author (authorName) value(?)";
			PreparedStatement pstate = cnn.prepareStatement(sql);
			pstate.setString(1, name);
			pstate.execute();
			return true;
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			if(cnn != null) {
				cnn.rollback();
				System.out.println("DB Roll Back due to error! Back to previous Menu!");
			}
		} finally {
			if(cnn != null) {
				cnn.commit();
				System.out.println("inserted into author table");
				cnn.close();
				System.out.println("DB connection closed");
			}
		}
		return false;
	}
	
	public Book insertBook(Book book) throws SQLException {
		Connection cnn = null;
		ResultSet rs = null;
		try {
			Class.forName(driver);
			cnn = DriverManager.getConnection(url,username,pwd);
			cnn.setAutoCommit(false);
			String sql = "insert into tbl_book (title,pubId) value(?,?)";
			PreparedStatement pstate = cnn.prepareStatement(sql,PreparedStatement.RETURN_GENERATED_KEYS);;
			pstate.setString(1, book.getTitle());
			pstate.setInt(2, book.getPubId());
			pstate.execute();
			rs = pstate.getGeneratedKeys();
			if(rs.next()) {
				book.setBookId(rs.getInt(1));
			}
			return book;
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			if(cnn != null) {
				cnn.rollback();
				System.out.println("DB Roll Back due to error! Back to previous Menu!");
			}
		} finally {
			if(cnn != null) {
				cnn.commit();
				System.out.println("inserted into genre table");
				cnn.close();
				System.out.println("DB connection closed");
			}
		}
		return null;
	}

	public boolean insertBookGenreTable(Book book) throws SQLException {
		Connection cnn = null;
		try {
			Class.forName(driver);
			cnn = DriverManager.getConnection(url,username,pwd);
			cnn.setAutoCommit(false);
			String sql = "insert into tbl_book_genres value(?,?)";
			PreparedStatement pstate = cnn.prepareStatement(sql);
			pstate.setInt(1, book.getGenreId());
			pstate.setInt(2, book.getBookId());
			pstate.execute();
			return true;
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			if(cnn != null) {
				cnn.rollback();
				System.out.println("Insert into book genre table failed, back to previous menu!");
			}
		} finally {
			if(cnn != null) {
				cnn.commit();
				System.out.println("inserted into book genre table");
				cnn.close();
				System.out.println("DB connection closed");
			}
		}
		return false;
	}
	
	public boolean insertBookCopyTable(Book book) throws SQLException {
		Connection cnn = null;
		try {
			Class.forName(driver);
			cnn = DriverManager.getConnection(url,username,pwd);
			cnn.setAutoCommit(false);
			String sql = "insert into tbl_book_copies value(?,?,?)";
			PreparedStatement pstate = cnn.prepareStatement(sql);
			pstate.setInt(1, book.getBookId());
			pstate.setInt(2, book.getBranchId());
			pstate.setInt(3, book.getNumOfCopies());
			pstate.execute();
			return true;
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			if(cnn != null) {
				cnn.rollback();
				System.out.println("Insert into book copy table failed, back to previous menu!");
			}
		} finally {
			if(cnn != null) {
				cnn.commit();
				System.out.println("inserted into book copy table");
				cnn.close();
				System.out.println("DB connection closed");
			}
		}
		return false;
	}
	
	public boolean insertBookAuthorTable(Book book) throws SQLException {
		Connection cnn = null;
		try {
			Class.forName(driver);
			cnn = DriverManager.getConnection(url,username,pwd);
			cnn.setAutoCommit(false);
			String sql = "insert into tbl_book_authors value(?,?)";
			PreparedStatement pstate = cnn.prepareStatement(sql);
			pstate.setInt(1, book.getBookId());
			pstate.setInt(2, book.getAuthorId());
			pstate.execute();
			return true;
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			if(cnn != null) {
				cnn.rollback();
				System.out.println("Insert into book author table failed, back to previous menu!");
			}
		} finally {
			if(cnn != null) {
				cnn.commit();
				System.out.println("inserted into book author table");
				cnn.close();
				System.out.println("DB connection closed");
			}
		}
		return false;
	}
	
	public boolean insertGenre(String name) throws SQLException {
		Connection cnn = null;
		try {
			Class.forName(driver);
			cnn = DriverManager.getConnection(url,username,pwd);
			cnn.setAutoCommit(false);
			String sql = "insert into tbl_genre (genre_name) value(?)";
			PreparedStatement pstate = cnn.prepareStatement(sql);
			pstate.setString(1, name);
			pstate.execute();
			return true;
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			if(cnn != null) {
				cnn.rollback();
				System.out.println("DB Roll Back due to error! Back to previous Menu!");
			}
		} finally {
			if(cnn != null) {
				cnn.commit();
				System.out.println("inserted into genre table");
				cnn.close();
				System.out.println("DB connection closed");
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @return genre list
	 * @throws SQLException
	 */
	public LinkedList<Genre> getGenreList() throws SQLException {
		LinkedList<Genre> list = new LinkedList<Genre>();
		Connection cnn = null;
		try {
			Class.forName(driver);
			cnn = DriverManager.getConnection(url,username,pwd);
			cnn.setAutoCommit(false);
			String sql = "select * from tbl_genre;";
			PreparedStatement pstate = cnn.prepareStatement(sql);
			ResultSet rs = pstate.executeQuery();
			Genre temp;
			int index = 0;
			while(rs.next()) {
				index++;
				temp = new Genre();
				temp.setId(rs.getInt(1));
				temp.setName(rs.getString(2));
				list.add(temp);
				System.out.println(index + ") " + temp.getName());
			}
			System.out.println((index+1)+") new genre" );
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			if(cnn != null) {
				System.out.println("DB Roll Back due to error! Back to previous Menu!");
			}
		} finally {
			if(cnn != null) {
				cnn.close();
				System.out.println("DB connection closed");
			}
		}
		return list;
	}
	
	public LinkedList<Author> getAuthorList() throws SQLException {
		LinkedList<Author> list = new LinkedList<Author>();
		Connection cnn = null;
		try {
			Class.forName(driver);
			cnn = DriverManager.getConnection(url,username,pwd);
			cnn.setAutoCommit(false);
			String sql = "select * from tbl_author;";
			PreparedStatement pstate = cnn.prepareStatement(sql);
			ResultSet rs = pstate.executeQuery();
			Author temp;
			int index = 0;
			while(rs.next()) {
				index++;
				temp = new Author();
				temp.setId(rs.getInt(1));
				temp.setName(rs.getString(2));
				list.add(temp);
				System.out.println(index + ") " + temp.getName());
			}
			System.out.println((index+1)+") new author" );
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			if(cnn != null) {
				System.out.println("DB Roll Back due to error! Back to previous Menu!");
			}
		} finally {
			if(cnn != null) {
				cnn.close();
				System.out.println("DB connection closed");
			}
		}
		return list;
	}
}
