/**
 * 
 */
package com.gcit.library.main;
import java.sql.SQLException;
import java.util.Scanner;

import com.gcit.library.controller.AdminController;
import com.gcit.library.controller.BorrowerController;
import com.gcit.library.controller.LibrarianController;

/**
 * @author jianwu
 *
 */
public class Main {
	/**
	 * @param args
	 */
	public static Scanner sc = new Scanner(System.in);
	public static LibrarianController libirarianController = new LibrarianController();
	public static BorrowerController borrowerController = new BorrowerController();
	public static AdminController adminController = new AdminController();
	
	private static final String driver = "com.mysql.cj.jdbc.Driver";
	private static final String url = "jdbc:mysql://localhost/library?useSSL=false";
	private static final String username = "root";
	private static final String pwd = "wu134679";
	
	public static void main(String[] args) throws SQLException{
		while(true) {
			mainMenu();
		}
	}
	
	public static void mainMenu() throws SQLException {
		System.out.println("Welcome to the GCIT Library Management System. Which category of a user are you?");
		System.out.println("1) Librarian\n" + "2) Administrator\n" + "3) Borrower\n");
		int choice = 0;
		while(true) {
			if(choice != 0) {
				break;
			}
			switch(sc.next()) {
				case "1": 
					choice = 1;
					break;
				case "2":
					choice = 2;
					break;
				case "3":
					choice = 3;
					break;
				default:
					System.out.println("You must enter a number from 1 to 3");
			}
		}
		if(choice == 1) {
			libirarianController.LibrarianMainMenu();		
		}else if(choice == 2){
			adminController.AdminMainMenu();
		}else {
			borrowerController.BorrowerMainMenu();
		}
	}

	public static String getDriver() {
		return driver;
	}

	public static String getUrl() {
		return url;
	}

	public static String getUsername() {
		return username;
	}

	public static String getPwd() {
		return pwd;
	}
	
}
