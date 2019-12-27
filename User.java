// Simple class that stores the data for a user.

import java.util.*;

public class User{
	int userID;
	String name;
	String email;
	String password;
	String dateOfBirth;


	public User(int uID, String n, String e, String pw, String dob){
		userID = uID;
		name = n;
		email = e;
		password = pw;
		dateOfBirth = dob;
	}

	public User(){
	}

	public String toString(){
		StringBuilder result = new StringBuilder();
		result.append("userID: " + userID + " name: " + name + " email: " + email + " password: " + password + " DOB: " + dateOfBirth);
		return new String(result);
	}

	public String getName(){
		return name;
	}

	public String getEmail(){
		return email;
	}

	public int getID(){
		return userID;
	}
}
