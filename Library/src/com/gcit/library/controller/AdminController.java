/**
 * 
 */
package com.gcit.library.controller;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.LinkedList;

import com.gcit.library.helper.Helper;
import com.gcit.library.main.Main;
import com.gcit.library.model.Author;
import com.gcit.library.model.Book;
import com.gcit.library.model.Borrower;
import com.gcit.library.model.Branch;
import com.gcit.library.model.Genre;
import com.gcit.library.model.Loan;
import com.gcit.library.model.Publisher;

/**
 * @author jianwu
 *
 */
public class AdminController {
	private String url = Main.getUrl();
	private String driver = Main.getDriver();
	private String username = Main.getUsername();
	private String pwd = Main.getPwd();
	
	private int choice = 0;
	private Loan loan;
	private Borrower borrower;
	private Publisher pub;
	private Branch branch;
	private Book book;
	
	private Helper helper = new Helper();
	private LinkedList<Loan> loanList = new LinkedList<Loan>();
	private LinkedList<Borrower> borrowerList = new LinkedList<Borrower>();
	private LinkedList<Publisher> pubList = new LinkedList<Publisher>();
	private LinkedList<Branch> branchList = new LinkedList<Branch>();
	private LinkedList<Book> bookList = new LinkedList<Book>();
	private LinkedList<Genre> gList = new LinkedList<Genre>();
	private LinkedList<Author> authorList = new LinkedList<Author>();
	public void AdminMainMenu() throws SQLException {
		while (true) {
			choice = 0;
			System.out.println("1) Add/Update/Delete Book and Author\n" + "2) Add/Update/Delete Publishers\n"
					+ "3) Add/Update/Delete Library Branches\n" + "4) Add/Update/Delete Borrowers\n"
					+ "5) Over-ride Due Date for a Book Loan\n" + "6) go back\n");
			while (true) {
				if(choice != 0) {
					break;
				}
				switch (Main.sc.next()) {
					case "1":
						choice = 1;
						break;
					case "2":
						choice = 2;
						break;
					case "3":
						choice = 3;
						break;
					case "4":
						choice = 4;
						break;
					case "5":
						choice = 5;
						break;
					case "6":
						return;
					default:
						System.out.println("Please enter a number between 1 to 6");
					}
			}
			switch(choice) {
				case 1:;
					bookAuthorAction();
					break;
				case 2:
					publisherAction();
					break;
				case 3:
					branchAction();
					break;
				case 4:
					borrowerAction();
					break;
				case 5:
					loanAction();
					break;
			}
		}
	}

	/** selection statement, don't need to commit and rollback
	 * get lsit of loans for user to edit due date
	 * @throws SQLException 
	 */
	public void getLoanList() throws SQLException {
		Connection cnn = null;
		ResultSet rs = null;
		loanList.clear();
		try {
			Class.forName(driver);
			cnn = DriverManager.getConnection(url,username,pwd);
			String sql = "select loans.bookId,loans.branchId,loans.cardNo, book.title, borrower.name,branch.branchName, loans.dueDate from tbl_book_loans loans\n" + 
					"join tbl_book book on book.bookId = loans.bookId\n" + 
					"join tbl_library_branch branch on branch.branchId = loans.branchId\n" + 
					"join tbl_borrower borrower on borrower.cardNo = loans.cardNo\n"+
					"where loans.dateIn is null; ";
			PreparedStatement pstate = cnn.prepareStatement(sql);
			rs = pstate.executeQuery();
			Loan loan;
			int index = 0;
			while(rs.next()) {
				index++;
				loan = new Loan();
				loan.setBookId(rs.getInt(1));
				loan.setBranchId(rs.getInt(2));
				loan.setCardNo(rs.getInt(3));
				loan.setBookTitle(rs.getString(4));
				loan.setBorrowerName(rs.getString(5));
				loan.setBranchName(rs.getString(6));
				loan.setDueDate(rs.getDate(7).toLocalDate());
				System.out.println(index + ") Book title " + loan.getBookTitle() + " at " +
						loan.getBranchName() + " branch was borrowed by " + loan.getBorrowerName() +
						", the due date is " + loan.getDueDate());
				loanList.add(loan);
			}
			System.out.println((index+1) + ") go back ");
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			System.out.println("DB error, move back!");
		} finally {
			if(cnn != null) {
				cnn.close();
				System.out.println("DB connection closed");
			}
		}
	}
	
	/**
	 * when input is 5 from admin main menu
	 * @throws SQLException
	 */
	public void loanAction() throws SQLException {
		getLoanList();
		loan = helper.getChoices(loanList);
		if(loan == null) {
			return;
		}
		editDueDate();
	}

	public void editDueDate() throws SQLException {
		System.out.println("1) Extend the due date?");
		System.out.println("2) Shorten a deadline?");
		choice = 0;
		while(true) {
			if(choice != 0) {
				break;
			}
			switch(Main.sc.next()){
				case "1":
					choice = 1;
					break;
				case "2":
					choice = 2;
					break;
				default:
					System.out.println("Please enter either 1 or 2");
			}
		}
		System.out.println("How many?");
		LocalDate ld = LocalDate.now();
		int n = 0;
		//validate input for shorten due date
		while(true) {
			if(n != 0) {
				break;
			}
			if(Main.sc.hasNextInt()) {
				n = Main.sc.nextInt();
				if(loan.getDueDate().minusDays(n).compareTo(ld) < 0) {
					System.out.println("You can't set due date to be early then today!\n"+
								"Enter a new number: \n");
					n = 0;
					Main.sc.next();
				}
			}else {
				System.out.println("Please enter a number!");
				Main.sc.next();
			}
		}
		Connection cnn = null;
		try {
			Class.forName(driver);
			cnn = DriverManager.getConnection(url,username,pwd);
			cnn.setAutoCommit(false);
			String sql = "update tbl_book_loans set dueDate = ? " + 
						" where bookId = ? and branchId = ? and cardNo = ?";
			PreparedStatement pstate = cnn.prepareStatement(sql);
			if(choice == 1) {
				pstate.setDate(1, Date.valueOf(loan.getDueDate().plusWeeks(n)));
			}else {
				pstate.setDate(1, Date.valueOf(loan.getDueDate().minusDays(n)));
			}
			pstate.setInt(2, loan.getBookId());
			pstate.setInt(3, loan.getBranchId());
			pstate.setInt(4, loan.getCardNo());
			pstate.executeUpdate();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			if(cnn != null)
				cnn.rollback();
			System.out.println("DB error, move back!");
		} finally {
			if(cnn != null) {
				cnn.commit();
				System.out.println("Updated!");
				cnn.close();
				System.out.println("DB closed!");
			}
		}
	}

	public void borrowerAction() throws SQLException {
		System.out.println("This is borrower control panel. \n"+
							"1) Add \n"+
							"2) Edit \n"+
							"3) Delete \n"+
							"4) Go back \n");
		choice = 0;
		while(true) {
			if(choice != 0) {
				break;
			}
			switch(Main.sc.next()) {
				case "1":
					choice = 1;
					break;
				case "2":
					choice = 2;
					break;
				case "3":
					choice = 3;
					break;
				case "4":
					return;
				default:
					System.out.println("Please enter number from 1 to 4!");
			}
		}
		if(choice == 1) {
			addBorrower();
		}else if(choice == 2) {
			editBorrower();
		}else {
			deleteBorrower();
		}
	}

	public void getBorrowerList() throws SQLException {
		Connection cnn = null;
		borrowerList.clear();
		try {
			Class.forName(driver);
			cnn = DriverManager.getConnection(url,username,pwd);
			cnn.setAutoCommit(false);
			String sql = "select * from tbl_borrower;";
			PreparedStatement pstate = cnn.prepareStatement(sql);
			ResultSet rs = pstate.executeQuery();
			Borrower tempBorr;
			int index = 0;
			while(rs.next()) {
				index++;
				tempBorr = new Borrower();
				tempBorr.setBorrowerId(rs.getInt(1));
				tempBorr.setName(rs.getString(2));
				tempBorr.setAddress(rs.getString(3));
				tempBorr.setPhoneNum(rs.getString(4));
				borrowerList.add(tempBorr);
				System.out.println(index + ") " + tempBorr.getName() + 
									", address: " + tempBorr.getAddress() + 
									", phone: " + tempBorr.getPhoneNum());
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
	}
	
	private void deleteBorrower() throws SQLException {
		getBorrowerList();
		borrower = helper.getChoices(borrowerList);
		if(borrower == null) {
			return;
		}
		deleteBorrUpdate();
	}
	
	private boolean hasUnreturnBook() throws SQLException {
		Connection cnn = null;
		try {
			Class.forName(driver);
			cnn = DriverManager.getConnection(url,username,pwd);
			String sql = "select * from tbl_book_loans where cardNo = ? and dateIn is null";
			PreparedStatement pstate = cnn.prepareStatement(sql);
			pstate.setInt(1, borrower.getBorrowerId());
			ResultSet rs = pstate.executeQuery();
			if(rs.next()) {
				return true;
			}else {
				return false;
			}
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
		return false;
			
	}
	/**
	 * when delete a borrower, need to delete borrower table and loan table
	 * @throws SQLException 
	 */
	private void deleteBorrUpdate() throws SQLException {
		if(hasUnreturnBook()) {
			System.out.println("This borrower has unreturn book, can't delete!");
			return;
		}
		Connection cnn = null;
		try {
			Class.forName(driver);
			cnn = DriverManager.getConnection(url,username,pwd);
			cnn.setAutoCommit(false);
			String sql1 = "delete from tbl_borrower where cardNo = ?;",
					sql2 = "delete from tbl_book_loans where cardNo = ?;";
			PreparedStatement pstate = cnn.prepareStatement(sql1);
			pstate.setInt(1, borrower.getBorrowerId());
			pstate.execute();
			pstate = cnn.prepareStatement(sql2);
			pstate.setInt(1, borrower.getBorrowerId());
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
	 * when admin decide to edit borrower
	 * @throws SQLException
	 */
	private void editBorrower() throws SQLException {
		System.out.println("Which one you want to edit?");
		getBorrowerList();
		borrower = helper.getChoices(borrowerList);
		if(borrower == null) return;
		System.out.println( "You are editing " + borrower.getName()+ "\n"+
							"1) edit name \n"+
							"2) edit address \n"+
							"3) edit phone \n"+
							"4) go back \n");
		choice = 0;
		while(true) {
			if(choice != 0) {
				break;
			}
			switch(Main.sc.next()) {
				case "1":
					choice = 1;
					break;
				case "2":
					choice = 2;
					break;
				case "3":
					choice = 3;
					break;
				case "4":
					choice = 0;
					return;
				default:
					System.out.println("Please enter a number from 1 to 4");
			}
		}
		String phoneRegEx = "^([0-9]){10,10}$",s = "";
		Main.sc.nextLine();
		if(choice == 1) {
			System.out.println("Enter Full name seperate by space!");
			while(true) {
				s = Main.sc.nextLine();
				if(s.contains(" ")) break;
				else System.out.println("Try again!");
			}
			helper.updateTable("tbl_borrower", "name", s, "cardNo", borrower.getBorrowerId());
		}else if(choice == 2) {
			System.out.println("Enter Address!");
			s = Main.sc.nextLine();
			helper.updateTable("tbl_borrower", "address", s, "cardNo", borrower.getBorrowerId());
		}else{
			System.out.println("Enter 10 digit phone number without dash and space!");
			while(true) {
				s = Main.sc.nextLine();
				if(s.matches(phoneRegEx)) break;
				else System.out.println("Try again!");
			}
			//formate phone number
			helper.updateTable("tbl_borrower", "phone", helper.formatePhone(s), "cardNo", borrower.getBorrowerId());
		}
	}
	
	private void addBorrower() throws SQLException {
		String name,address,phone, phoneRegEx = "^([0-9]){10,10}$";
		Main.sc.nextLine();
		System.out.println("Enter Full name seperate by space!");
		while(true) {
			name = Main.sc.nextLine();
			if(name.contains(" ")) break;
			else System.out.println("Try again!");
		}
		System.out.println("Enter an address");
		address = Main.sc.nextLine();
		System.out.println("Enter 10 digits phone number without dash and space!");
		while(true) {
			phone = Main.sc.nextLine();
			if(phone.matches(phoneRegEx)) break;
			else System.out.println("Try again!");
		}
		addBorrowerUpdate(name,address,helper.formatePhone(phone));
	}
	/**
	 * update the borrower table with the new entry(insert)
	 * @param name
	 * @param address
	 * @param phone
	 * @throws SQLException 
	 */
	private void addBorrowerUpdate(String name, String address, String phone) throws SQLException {
		Connection cnn = null;
		try {
			Class.forName(driver);
			cnn = DriverManager.getConnection(url,username,pwd);
			cnn.setAutoCommit(false);
			String sql = "insert into tbl_borrower (name,address,phone) value(?,?,?)";
			PreparedStatement pstate = cnn.prepareStatement(sql);
			pstate.setString(1, name);
			pstate.setString(2, address);
			pstate.setString(3, phone);
			pstate.execute();	
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			if(cnn!=null) {
				cnn.rollback();
				System.out.println("DB eror, roll back!");
			}
		} finally {
			if(cnn != null) {
				cnn.commit();
				System.out.println("Borrower table inserted!");
				cnn.close();
				System.out.println("DB closed!");
			}
		}
	}
	
	/**
	 * when user press 2
	 * @throws SQLException 
	 */
	public void publisherAction() throws SQLException {
		System.out.println("This is publisher control panel. \n"+
				"1) Add \n"+
				"2) Edit \n"+
				"3) Delete \n"+
				"4) Go back \n");
		choice = 0;
		while(true) {
			if(choice != 0) {
				break;
			}
			switch(Main.sc.next()) {
				case "1":
					choice = 1;
					break;
				case "2":
					choice = 2;
					break;
				case "3":
					choice = 3;
					break;
				case "4":
					return;
				default:
					System.out.println("Please enter number from 1 to 4!");
			}
		}
		if(choice == 1) {
			addPublisher();
		}else if(choice == 2){
			editPublisher();
		}else {
			deletePublisher();
		}
	}
	
	/**
	 * delete publihser
	 * @throws SQLException
	 */
	private void deletePublisher() throws SQLException {
		System.out.println("Which one you want to delete?");
		pubList = helper.getPublisherList();
		pub = helper.getChoices(pubList);
		if(pub == null) return;
		System.out.println( "You are deleting " + pub.getPublisherName()+"!");
		helper.deleteRowFromTable("tbl_publisher", "publisherId", pub.getPublisherId());
	}

	/**
	 * edit publisher when press 2
	 * @throws SQLException
	 */
	private void editPublisher() throws SQLException{
		System.out.println("Which one you want to edit?");
		pubList = helper.getPublisherList();
		pub = helper.getChoices(pubList);
		if(pub == null) return;
		System.out.println( "You are editing " + pub.getPublisherName()+ "\n"+
							"1) edit name \n"+
							"2) edit address \n"+
							"3) edit phone \n"+
							"4) go back \n");
		choice = 0;
		while(true) {
			if(choice != 0) {
				break;
			}
			switch(Main.sc.next()) {
				case "1":
					choice = 1;
					break;
				case "2":
					choice = 2;
					break;
				case "3":
					choice = 3;
					break;
				case "4":
					choice = 0;
					return;
				default:
					System.out.println("Please enter a number from 1 to 4");
			}
		}
		String phoneRegEx = "^([0-9]){10,10}$",s = "";
		Main.sc.nextLine();
		if(choice == 1) {
			System.out.println("Enter new publisher name!");
			s = Main.sc.nextLine();
			helper.updateTable("tbl_publisher", "publisherName", s, "publisherId", pub.getPublisherId());
		}else if(choice == 2) {
			System.out.println("Enter new Address!");
			s = Main.sc.nextLine();
			helper.updateTable("tbl_publisher", "publisherAddress", s, "publisherId", pub.getPublisherId());
		}else{
			System.out.println("Enter 10 digit new phone number without dash and space!");
			while(true) {
				s = Main.sc.nextLine();
				if(s.matches(phoneRegEx)) break;
				else System.out.println("Try again!");
			}
			//formate phone number
			helper.updateTable("tbl_publisher", "publisherPhone", helper.formatePhone(s), "publisherId", pub.getPublisherId());
		}
	}
	
	/**
	 * add publisher action
	 * @throws SQLException
	 */
	private void addPublisher() throws SQLException {
		String name,address,phone, phoneRegEx = "^([0-9]){10,10}$";
		Main.sc.nextLine();
		System.out.println("Enter publisher name!");
		name = Main.sc.nextLine();
		System.out.println("Enter an address");
		address = Main.sc.nextLine();
		System.out.println("Enter 10 digits phone number without dash and space!");
		while(true) {
			phone = Main.sc.nextLine();
			if(phone.matches(phoneRegEx)) break;
			else System.out.println("Try again!");
		}
		addPulisherUpdate(name,address,helper.formatePhone(phone));
	}
	/**
	 * insert date into publisher table
	 * @param name
	 * @param address
	 * @param phone
	 * @throws SQLException
	 */
	private void addPulisherUpdate(String name, String address, String phone) throws SQLException {
		Connection cnn = null;
		try {
			Class.forName(driver);
			cnn = DriverManager.getConnection(url,username,pwd);
			cnn.setAutoCommit(false);
			String sql = "insert into tbl_publisher (publisherName,publisherAddress,publisherPhone) value(?,?,?)";
			PreparedStatement pstate = cnn.prepareStatement(sql);
			pstate.setString(1, name);
			pstate.setString(2, address);
			pstate.setString(3, phone);
			pstate.execute();	
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			if(cnn!=null) {
				cnn.rollback();
				System.out.println("DB eror, roll back!");
			}
		} finally {
			if(cnn != null) {
				cnn.commit();
				System.out.println("Borrower table inserted!");
				cnn.close();
				System.out.println("DB closed!");
			}
		}
	}

	/**
	 * when press 3
	 * @throws SQLException 
	 */
	public void branchAction() throws SQLException {
		System.out.println("This is branch control panel. \n"+
				"1) Add \n"+
				"2) Edit \n"+
				"3) Delete \n"+
				"4) Go back \n");
		choice = 0;
		while(true) {
			if(choice != 0) {
				break;
			}
			switch(Main.sc.next()) {
				case "1":
					choice = 1;
					break;
				case "2":
					choice = 2;
					break;
				case "3":
					choice = 3;
					break;
				case "4":
					return;
				default:
					System.out.println("Please enter number from 1 to 4!");
			}
		}
		if(choice == 1) {
			addBranch();
		}else if(choice == 2){
			editBranch();
		}else {
			deleteBranch();
		}
	}

	/**
	 * delete branch from table and update
	 * @throws SQLException
	 */
	private void deleteBranch() throws SQLException {
		branchList = helper.getBranchList();
		System.out.println("Which one you want to delet? ");
		branch = helper.getChoices(branchList);
		if(branch == null) {
			return;
		}
		helper.deleteRowFromTable("tbl_library_branch", "branchId", branch.getId());
	}
	
	/**
	 * edit branch
	 * @throws SQLException
	 */
	private void editBranch() throws SQLException {
		System.out.println("Which one you want to edit?");
		branchList = helper.getBranchList();
		branch = helper.getChoices(branchList);
		if(branch == null) return;
		System.out.println( "You are editing " + branch.getBranchName()+ "\n"+
							"1) edit name \n"+
							"2) edit address \n"+
							"3) go back \n");
		choice = 0;
		while(true) {
			if(choice != 0) {
				break;
			}
			switch(Main.sc.next()) {
				case "1":
					choice = 1;
					break;
				case "2":
					choice = 2;
					break;
				case "3":
					choice = 0;
					return;
				default:
					System.out.println("Please enter a number from 1 to 4");
			}
		}
		Main.sc.nextLine();
		String s = "";
		if(choice == 1) {
			System.out.println("Enter new branch name!");
			s = Main.sc.nextLine();
			helper.updateTable("tbl_library_branch", "branchName", s, "branchId",branch.getId());
		}else {
			System.out.println("Enter new Address!");
			s = Main.sc.nextLine();
			helper.updateTable("tbl_library_branch", "branchAddress", s, "branchId",branch.getId());;
		}
	}

	/**
	 * add new branch
	 * @throws SQLException
	 */
	private void addBranch() throws SQLException {
		String name,address;
		Main.sc.nextLine();
		System.out.println("Enter branch name!");
		name = Main.sc.nextLine();
		System.out.println("Enter branch address");
		address = Main.sc.nextLine();
		addBranchUpdate(name,address);
	}
	
	/**
	 * insert into branch table and update
	 * @param name
	 * @param address
	 * @throws SQLException
	 */
	private void addBranchUpdate(String name, String address) throws SQLException {
		Connection cnn = null;
		try {
			Class.forName(driver);
			cnn = DriverManager.getConnection(url,username,pwd);
			cnn.setAutoCommit(false);
			String sql = "insert into tbl_library_branch (branchName,branchAddress) value(?,?)";
			PreparedStatement pstate = cnn.prepareStatement(sql);
			pstate.setString(1, name);
			pstate.setString(2, address);
			pstate.execute();	
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			if(cnn!=null) {
				cnn.rollback();
				System.out.println("DB eror, roll back!");
			}
		} finally {
			if(cnn != null) {
				cnn.commit();
				System.out.println("Branch table inserted!");
				cnn.close();
				System.out.println("DB closed!");
			}
		}
	}

	public void bookAuthorAction() throws SQLException {
		System.out.println("This is book/author control panel. \n"+
				"1) Add book/author \n"+
				"2) Edit book/author \n"+
				"3) Delete book/author \n"+
				"4) Go back book/author \n");
		choice = 0;
		while(true) {
			if(choice != 0) {
				break;
			}
			switch(Main.sc.next()) {
				case "1":
					choice = 1;
					break;
				case "2":
					choice = 2;
					break;
				case "3":
					choice = 3;
					break;
				case "4":
					return;
				default:
					System.out.println("Please enter number from 1 to 4!");
			}
		}
		if(choice == 1) {
			addBookAuthor();
		}else if(choice == 2){
			editBookAuthor();
		}else {
			deleteBookAuthor();
		}
	}

	private void deleteBookAuthor() throws SQLException {
		System.out.println("1) delete book \n" + "2) delete author\n");
		choice = 0;
		while(true) {
			if(choice!=0) {
				break;
			}
			switch(Main.sc.next()) {
				case "1":
					choice = 1;
					break;
				case "2":
					choice = 2;
					break;
				default:
					System.out.println("Please enter a number from 1 to 2");
			}
		}
		if(choice == 1) {
			bookList = helper.getBookAuthorList();
			System.out.println("Which one you want to delete? ");
			book = helper.getChoices(bookList);
			if(book == null) {
				return;
			}
			helper.deleteRowFromTable("tbl_book", "bookId", book.getBookId());
		}else {
			
		}
	}

	private void editBookAuthor() {
		// TODO Auto-generated method stub
	}

	private void addBookAuthor() throws SQLException {
		// TODO Auto-generated method stub
		System.out.println("1) add book \n" + "2) add author\n");
		choice = 0;
		while(true) {
			if(choice!=0) {
				break;
			}
			switch(Main.sc.next()) {
				case "1":
					choice = 1;
					break;
				case "2":
					choice = 2;
					break;
				default:
					System.out.println("Please enter a number from 1 to 2");
			}
		}
		if(choice == 1) {
			addBook();
		}else {
			addAuthor();
		}
	}

	private void addAuthor() throws SQLException {
		String author;
		Main.sc.nextLine();
		System.out.println("What is the name of the author? ");
		author = Main.sc.nextLine();
		helper.insertAuthor(author);
	}

	public void addGenre() throws SQLException	{
		String genre;
		Main.sc.nextLine();
		System.out.println("What is the new genre? ");
		genre = Main.sc.nextLine();
		helper.insertGenre(genre);
	}
	
	private void addBook() throws SQLException {
		Book tempBook = new Book();
		Main.sc.nextLine();
		System.out.println("What is the title of the book? ");
		tempBook.setTitle(Main.sc.nextLine());
		//System.out.println(book.getTitle());
		choice = 0;
		while(true) {
			if(tempBook.getGenreId() > 0) {
				break;
			}
			gList = helper.getGenreList();
			System.out.println("Choosing from existing genre or make a new one: ");
			choice = helper.getChoiceIndex(gList);
			if(choice <= gList.size()) {
				tempBook.setGenreId(gList.get(choice-1).getId());
			}else {
				addGenre();
			}
		}
		gList.clear();
		System.out.println("Choosing from existing author or make a new one: ");
		while (true) {
			if(tempBook.getAuthorId() > 0) {
				break;
			}
			authorList = helper.getAuthorList();
			choice = helper.getChoiceIndex(authorList);
			if (choice <= authorList.size()) {
				tempBook.setAuthorId(authorList.get(choice - 1).getId());
			} else {
				addAuthor();
			} 
		}
		authorList.clear();
		while (true) {
			if(tempBook.getPubId() > 0) {
				break;
			}
			pubList = helper.getPublisherList();
			System.out.println("Choosing from existing publisher or make a new one: ");
			choice = helper.getChoiceIndex(pubList);
			if (choice <= pubList.size()) {
				tempBook.setPubId(pubList.get(choice - 1).getPublisherId());
			} else {
				addPublisher();
			} 
		}
		pubList.clear();
		while (true) {
			if(tempBook.getBranchId() > 0) {
				break;
			}
			branchList = helper.getBranchList();
			System.out.println("Choosing from existing branch or make a new one: ");
			choice = helper.getChoiceIndex(branchList);
			if (choice <= branchList.size()) {
				tempBook.setBranchId(branchList.get(choice - 1).getId());
			} else {
				addBranch();
			} 
		}
		branchList.clear();
		//Main.sc.nextLine();
		System.out.println("How many copies does it have? ");
		int n = 0;
		while(true) {
			if(Main.sc.hasNextInt()) {
				n = Main.sc.nextInt();
				if(n > 0) {
					tempBook.setNumOfCopies(n);
					break;
				} else {
					System.out.println("Please enter a positive integer!");
				}
			}else {
				System.out.println("Please enter a number!");
				Main.sc.next();
			}
		}
		/*
		 * Ready to insert book
		 */
		//get book id from the insertino
		book = helper.insertBook(tempBook);
		if(book != null) {
			if(helper.insertBookAuthorTable(book)) {
				if(helper.insertBookCopyTable(book)) {
					if(helper.insertBookGenreTable(book)) {
						
					}
				}
			}
		}
	}
}
