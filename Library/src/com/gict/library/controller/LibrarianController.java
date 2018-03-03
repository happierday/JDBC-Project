/**
 * 
 */
package com.gict.library.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import com.gcit.library.main.Main;
import com.gcit.library.model.Book;
import com.gcit.library.model.Branch;
import com.gcit.library.model.Librarian;

/**
 * @author jianwu
 *
 */
public class LibrarianController {
	
	private Librarian librarian;
	private LinkedList<Branch> branchList = new LinkedList<Branch>();
	private LinkedList<Book> bookList = new LinkedList<Book>();
	private int choice;
	private String url = Main.getUrl();
	private String driver = Main.getDriver();
	private String username = Main.getUsername();
	private String pwd = Main.getPwd();
	@SuppressWarnings("finally")
	public void LibrarianMainMenu() throws SQLException {
		librarian = new Librarian();
		System.out.println("1) Enter Branch you manage\n" + "2) go back\n");
		//choice = Main.sc.nextInt();
		while(true) {
			switch(Main.sc.nextInt()) {
				case 1: 
					try {
						LibrarianOp1();
					} catch (SQLException e) {
						e.printStackTrace();
					}
					break;
				case 2:
					//if go back, clean up librarian
					librarian = null;
					Main.mainMenu();
					break;
				default:
					System.out.println("You must enter a number from 1 to 2!");
			}
		}
	}
	/**
	 * Select statement(Mysql), no need to call cnn.rollback()
	 * @throws SQLException
	 */
	
	public void LibrarianOp1() throws SQLException {
		getLibraryBranch();
		int size = branchList.size();
		
		while(true) {
			choice = Main.sc.nextInt();
			if(choice > 0 && choice <= size) {
				librarian.setBranchIndex(choice);
				LibrarianOp2();
				break;
			}else if(choice == size+1){
				//if choose to go back, clean up list and librarian
				librarian = null;
				branchList.clear();
				LibrarianMainMenu();
				break;
			}else {
				System.out.println("Please enter a number between 1 and " + (size+1)+ "!");
			}
		}
	}
	
	public void getLibraryBranch() throws SQLException {
		Connection cnn = null;
		branchList.clear();
		try {
			Class.forName(driver);
			cnn = DriverManager.getConnection(url,username,pwd);
			PreparedStatement pstate = cnn.prepareStatement("select * from tbl_library_branch");
			ResultSet rs = pstate.executeQuery();
			Branch branch;
			while(rs.next()) {
				branch = new Branch();
				branch.setId(rs.getInt(1));
				branch.setBranchName(rs.getString(2));
				branch.setAddress(rs.getString(3));
				System.out.println(branch.toString());
				branchList.add(branch);
			}
			System.out.println((branchList.size()+1) + ") go back");
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		} finally {
			if(cnn != null) {
				cnn.close();
				System.out.println("DB connection closed");
			}
		}
	}
	
	public void LibrarianOp2() {
		System.out.println("1) Update the details of the Library \n" + 
				"2) Add copies of Book to the Branch\n" +
				"3) Quit to previous\n");
		while(true) {
			choice = Main.sc.nextInt();
			try {
				switch(choice) {
				case 1:
					updateBranch(branchList.get(librarian.getBranchIndex()-1));
					break;
				case 2:
					manageBooks(branchList.get(librarian.getBranchIndex()-1));
					break;
				case 3:
					LibrarianOp1();
					break;
				default:
					System.out.println("Pleaase enter a number from 1 to 3");
				}
			} catch(SQLException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public void updateBranch(Branch branch) throws SQLException {
		String s = "";
		
		System.out.println("You have chosen to update the Branch with Branch Id: " + branch.getId()
				+" and Branch Name: "+ branch.getBranchName() +".");
		System.out.println("Enter ‘quit’ at any prompt to cancel operation.");
		Main.sc.nextLine();
		System.out.println("Please enter new branch name or enter N/A for no change:" );
		s = Main.sc.nextLine();
		//System.out.println(s);
		if(s.toLowerCase().compareTo("quit") == 0){
			LibrarianOp2();
			return;
		}
		if(s.compareTo("N/A") != 0) {
			branch.setBranchName(s);
		}
		System.out.println("Please enter new branch address or enter N/A for no change: ");
		s = Main.sc.nextLine();
		if(s.toLowerCase().compareTo("quit") == 0){
			LibrarianOp2();
			return;
		}
		if(s.compareTo("N/A") != 0) {
			branch.setAddress(s);
		}
		System.out.println(branch.toString());
		//get all input from sc, ready to update
		Connection cnn = null;
		try {
			Class.forName(driver);
			cnn = DriverManager.getConnection(url,username,pwd);
			cnn.setAutoCommit(false);
			String sql = "update tbl_library_branch set branchName = ?, branchAddress=? where branchId = ?";
			PreparedStatement pstate = cnn.prepareStatement(sql);
			pstate.setString(1, branch.getBranchName());
			pstate.setString(2,branch.getAddress());
			pstate.setInt(3, branch.getId());
			pstate.execute();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			if(cnn != null) {
				cnn.rollback();
				System.out.println("DB Roll Back due to error! Back to previous Menu!");
				LibrarianOp2();
				return;
			}
		} finally {
			if(cnn != null) {
				cnn.commit();
				cnn.close();
				System.out.println("DB connection closed");
				LibrarianOp2();
				return;
			}
		}
	}
	
	
	public void getBooks(Branch branch) throws SQLException {
		Connection cnn = null;
		bookList.clear();
		try {
			Class.forName(driver);
			cnn = DriverManager.getConnection(url,username,pwd);
			cnn.setAutoCommit(false);
			String sql = "select \n" + 
					"	tbl_book_copies.bookId,\n" + 
					"    tbl_book_copies.branchId,\n" + 
					"    tbl_book.pubId,\n" + 
					"    tbl_book_authors.authorId,\n" + 
					"    tbl_book.title,\n" + 
					"    tbl_book_copies.noOfCopies\n" + 
					"from tbl_book_copies\n" + 
					"join tbl_book on tbl_book.bookId = tbl_book_copies.bookId\n" + 
					"join tbl_book_authors on tbl_book.bookId = tbl_book_authors.bookId\n" + 
					"where tbl_book_copies.branchId = ?;";
			PreparedStatement pstate = cnn.prepareStatement(sql);
			pstate.setInt(1, branch.getId());
			ResultSet rs = pstate.executeQuery();
			Book book;
			int index = 0;
			while(rs.next()) {
				index ++;
				book = new Book();
				book.setBookId(rs.getInt(1));
				book.setBranchId(rs.getInt(2));
				book.setPubId(rs.getInt(3));
				book.setAuthorId(rs.getInt(4));
				book.setTitle(rs.getString(5));
				book.setNumOfCopies(rs.getInt(6));
				System.out.println(index + ") " + book.getTitle());
				bookList.add(book);
			}
			System.out.println((bookList.size()+1) + ") go back");
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			if(cnn != null) {
				cnn.rollback();
				System.out.println("DB Roll Back due to error! Back to previous Menu!");
				LibrarianOp2();
				return;
			}
		} finally {
			if(cnn != null) {
				cnn.commit();
				cnn.close();
				System.out.println("DB connection closed");
			}
		}
	}
	
	public void manageBooks(Branch branch) throws SQLException {
		System.out.println("Pick the Book you want to add copies of, to your branch: ");
		getBooks(branch);
		int n = Main.sc.nextInt();
		while(true) {
			if(n <= bookList.size() + 1 && n > 0) {
				if(n == bookList.size() + 1) {
					LibrarianOp2();
					
				}else {
					updateBook(bookList.get(n),n);
				}
				return;
			}else {
				System.out.println("Please enter a number between 1 to " + (bookList.size() + 1));
			}
		}
	}
	
	public void updateBook(Book book,int index) throws SQLException {
		Connection cnn = null;
		System.out.println("Existing number of copies for " + book.getTitle() + ": " +
							book.getNumOfCopies());
		System.out.println("Enter a new number of copies: ");
		int n = Main.sc.nextInt();
		try {
			Class.forName(driver);
			cnn = DriverManager.getConnection(url,username,pwd);
			cnn.setAutoCommit(false);
			String sql = "update tbl_book_copies set noOfCopies = ?\n" + 
					"where bookId = ? and branchId = ?;";
			PreparedStatement pstate = cnn.prepareStatement(sql);
			pstate.setInt(1, n);
			pstate.setInt(2, book.getBookId());
			pstate.setInt(3, book.getBranchId());
			pstate.execute();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			if(cnn != null) {
				cnn.rollback();
				System.out.println("DB Roll Back due to error! Back to previous Menu!");
				manageBooks(branchList.get(librarian.getBranchIndex()-1));
				return;
			}
		} finally {
			if(cnn != null) {
				cnn.commit();
				System.out.println("updated!");
				cnn.close();
				System.out.println("DB connection closed");
			}
			LibrarianOp2();
		}
	}
	public Librarian getLibrarian() {
		return librarian;
	}
	public LinkedList<Branch> getBranchList() {
		return branchList;
	}
	public LinkedList<Book> getBookList() {
		return bookList;
	}
}
