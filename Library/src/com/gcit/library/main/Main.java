/**
 * 
 */
package com.gcit.library.main;
import java.sql.SQLException;
import java.util.Scanner;

import com.gict.library.controller.LibrarianController;

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
	private static final String driver = "com.mysql.cj.jdbc.Driver";
	private static final String url = "jdbc:mysql://localhost/library";
	private static final String username = "root";
	private static final String pwd = "wu134679";
	
	public static void main(String[] args) {
		mainMenu();
	}
	
	public static void mainMenu() {
		System.out.println("Welcome to the GCIT Library Management System. Which category of a user are you?");
		System.out.println("1) Librarian\n" + "2) Administrator\n" + "3) Borrower\n");
		while(true) {
			switch(sc.nextInt()) {
			case 1: 
				libirarianController.LibrarianMainMenu();
				break;
			case 2:
				System.out.println("Administrator");
				break;
			case 3:
				System.out.println("Borrower");
				break;
			default:
				System.out.println("You must enter a number from 1 to 3");
			}
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
