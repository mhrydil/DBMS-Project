/* CS1555 Fall 2019
 * Team 7
 *
 * Elena DeJaco
 * Stephen Longofono
 * Matthew Hrydil
 *
 * to compile on mac running java 8+: javac -cp postgresql-42.2.8.jar PittSocial.java
 * to run on mac running java 8+: java -cp postgresql-42.2.8.jar:. PittSocial
 *
 * to compile on windows running java 8+: javac -cp "postgresql-42.2.8.jar;." PittSocial.java
 * to run on windows running java 8+: java -cp "postgresql-42.2.8.jar;." PittSocial
 *
 */

import java.io.*;
import java.util.*;
import java.sql.*;
import java.time.*;
//import java.DateFormat*; 

	


/*
 * @class	PittSocial
 * @brief	Driver for the PittSocial social network simulation
 */
public class PittSocial{
    private static Connection conn; // make Connection a global variable to access it all of the methods
	
	private static boolean loggedIn;
	private static User activeUser;
	public static User testUser;
	private static Scanner sc;
	private static int numMsgs = -1;
	private static int numUsers = -1;

	public static Connection get_conn(){
		return conn;
	}

	public static Scanner get_sc(){
		return sc;
	}

	public static boolean get_loggedIn(){
		return loggedIn;
	}

	public static User get_activeUser(){
		return activeUser;
	}

	public static void set_sc(){
		sc = new Scanner(System.in);
	}
	


	/* Constants for user menu choices */
	enum choice{
		LOGIN, CREATEUSER, EXIT, NOCHOICE, INITIATEFRIENDSHIP,
		CREATEGROUP, INITIATEADDINGGROUP, CONFIRMREQUESTS,
		SENDMESSAGETOUSER, SENDMESSAGETOGROUP, DISPLAYMESSAGES, DISPLAYNEWMESSAGES,
		DISPLAYFRIENDS, SEARCHFORUSER, THREEDEGREES,
		TOPMESSAGES,LOGOUT,DROPUSER
	};

	/*
	 * @func	createGroup
	 * @brief	Given a group name, optional description, a user limit, and
	 * 			the administrator ID, add the group to the database.
	 * @param name	A String representing the name of the new group
	 * @param desc	A String representing the description of the new group.
	 * 				May be empty.
	 * @param limit	An integer representing the maximum number of users in the
	 * 				new group.  Must be positive.
	 * @param admin	An integer representing the userID of the group
	 * 				creator/administrator.
	 */
	public static boolean createGroup(String name, String desc, int limit, int admin) throws SQLException{
		if(name.length() < 1){
			System.out.println("[ error ] The new group name can't be empty!");
			return false;
		}
		else if(name.length() > 50){
			System.out.println("[ error ] The new group name can't be longer than 50 characters!");
			return false;
		}
		else if(desc.length() > 200){
			System.out.println("[ error ] The new group description can't be longer than 200 characters!");
			return false;
		}
		else if(limit < 1){
			System.out.println("[ error ] Group limits must be positive!");
			return false;
		}
		else if (!loggedIn){
			System.out.println("[ error ] You mustbe logged in to do that!");
			return false;
		}
		try{
			PreparedStatement st = conn.prepareStatement("select * from profile where userID=?;");
			st.setInt(1, admin);
			ResultSet result = st.executeQuery();
			if(result.next()){
				PreparedStatement st2 = conn.prepareStatement("select * from groupInfo where name=?;");
				st2.setString(1, name);
				ResultSet r2 = st2.executeQuery();
				if(r2.next()){
					System.out.println("[ error ] A group by this name already exists!");
					return false;
				}
				else{
					conn.setAutoCommit(false);
					conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
					PreparedStatement st3 = conn.prepareStatement("select gID from groupInfo order by gID desc limit 1;");
					ResultSet r3 = st3.executeQuery();
					r3.next();
					int gID = 1 + r3.getInt("gID");
					System.out.printf("Creating new group as (%d, %s, %s, %d, %d)\n", gID, name, desc, limit, admin);
					PreparedStatement st4 = conn.prepareStatement("insert into groupInfo values (?,?,?,?);");
					st4.setInt(1, gID);
					st4.setString(2, name);
					st4.setInt(3, limit);
					st4.setString(4, desc);
					int rows = st4.executeUpdate();
					System.out.printf("Changed %d rows in groupInfo...\n", rows);
					PreparedStatement st5 = conn.prepareStatement("insert into groupMember values (?,?,'manager');");
					st5.setInt(1, gID);
					st5.setInt(2, admin);
					rows = st5.executeUpdate();
					System.out.printf("Changed %d rows in groupMember...\n", rows);
					conn.commit();
					conn.setAutoCommit(true);
				}
			}
			else{
				System.out.println("[ error ] This user does not exist!");
				return false;
			}
		}
		catch(SQLException e){
			printSQLError(e);
			return false;
		}
		return true;
	}

	/*
	 * @func	getInput
	 * @brief	Helper function to retrieve a string input to facilitate
	 * 			testing
	 * @param prompt	A string to tell the user what to enter
	 * @return	A string, the  next line of input from the CLI
	 */
	public static String getInput(String prompt){
        System.out.println(prompt);
 		String ret = sc.nextLine();
		return ret;
	}


	/*
	 * @func	getInputMultiLine
	 * @brief	Helper function to retrieve a string input which may span
	 * 			multiple lines.
	 * @param prompt	A string to tell the user what to enter
	 * @return	A string, the complete input
	 */
	public static String getInputMultiLine(String prompt){
		boolean notDone = true;
		String ret = getInput(prompt);
		System.out.printf("Got a line: %s\n", ret);
		while(notDone){
			System.out.println("Do you have more text to enter? (y/n):");
			if('y' == sc.next().charAt(0)){
				sc.nextLine();	// flush
				ret += getInput(prompt);
			}
			else{
				notDone = false;
			}
		}
		return ret;
	}


	/*
	 * @func	initiateAddingGroup
	 * @brief	Given a group ID and a message, allow the logged in user to
	 * 			create a request for group membership.
	 * @param groupID	An integer representing the group to request
	 * 					membership for
	 * @param message	A String message to accompany the request
	 * @return	True on success, false otherwise
	 */
	public static boolean initiateAddingGroup(int groupID, String message) throws SQLException{
		if(!loggedIn){
			System.out.println("[ error ] You must be logged in to do that!");
			return false;
		}
		else if(message.length() < 1){
			System.out.println("[ error ] You must provide a message to accompany a group membership request!");
			return false;
		}
		else if(message.length() > 200){
			System.out.println("[ error ] The message cannot be longer than 200 characters!");
			return false;
		}
		PreparedStatement s1 = conn.prepareStatement("select * from groupInfo where gID=?;");
		s1.setInt(1, groupID);
		try{
			ResultSet r1 = s1.executeQuery();
			if(r1.next()){
				PreparedStatement s2 = conn.prepareStatement("select * from groupMember where userID=? and gID=?;");
				s2.setInt(1, activeUser.userID);
				s2.setInt(2, groupID);
				ResultSet r2 = s2.executeQuery();
				if(r2.next()){
					System.out.println("[ error ] This user already belongs to this group!");
					return false;
				}
				else{
					PreparedStatement s3 = conn.prepareStatement("insert into pendingGroupMember values(?,?,?);");
					s3.setInt(1, groupID);
					s3.setInt(2, activeUser.userID);
					s3.setString(3, message);
					int rows = s3.executeUpdate();
					System.out.printf("initiateAddingGroup changed %d rows...\n", rows);
				}
			}
			else{
				System.out.println("[ error ] No group exists with this group ID!");
				return false;
			}
		}
		catch(SQLException e){
			printSQLError(e);
			return false;
		}
		return true;

	} // end initiateAddingGroup

	
	public static int getID(String email){
		try {
			PreparedStatement a = conn.prepareStatement("Select * from profile where email = ?");
			a.setString(1,email);
			ResultSet result = a.executeQuery();
			if (result.next()){
				return result.getInt("UserID");
			}
			else {throw new IllegalArgumentException("User not found in the database. Cannot send a message to this person.");}
		}
		catch(SQLException e){
			printSQLError(e);
			return -1;
		}
	}
	
	/*
	* @func 	get_new_userID
	* @brief 	auto-generate userID and make sure it is unused.
				sorry i can't handle long functions and CreateUser was getting too large.
	* @return 	new userID 
	*/
	public static int get_new_userID() {
		if ( numUsers != -1 ) {
			try {
			while(true){
				PreparedStatement st1 = conn.prepareStatement("Select * from profile where userID = ?;");
				st1.setInt(1, numUsers);
				ResultSet res = st1.executeQuery();
				if (!res.next()){
					return numUsers++;
				}
				numUsers++;
			}
			} catch(SQLException e){
				System.out.println("fatal error found");
				exit();
			}
			
		}
		try {
			PreparedStatement st = conn.prepareStatement("Select count(*) from profile;");
			ResultSet result = st.executeQuery();
			if (result.next()){
				numUsers = result.getInt(1)+1;
				while(true) {
					PreparedStatement st1 = conn.prepareStatement("Select * from profile where userID = ?;");
					st1.setInt(1, numUsers);
					ResultSet res = st1.executeQuery();
					if (!res.next()){
						return numUsers++;
					}
				numUsers++;
				}
			}
			else {
				System.out.println("[ error ] cannot count how many profiles have been made");
			}
		}catch(SQLException e){
			printSQLError(e);
			return -1; //what can we return to not get an error when try to use this number? 
		}
		return -1;
	}
	
	/*
	* @func 	get_new_msgID
	* @brief 	auto-generate userID and make sure it is unused.
	* @return 	new userID 
	*/
	public static int get_new_msgID(){
		//int numMsgs;
		if ( numMsgs != -1 ) {
			try {
			while(true){
				PreparedStatement st1 = conn.prepareStatement("Select * from messageInfo where msgID = ?;");
				st1.setInt(1, numMsgs);
				ResultSet res = st1.executeQuery();
				if (!res.next()){
					return numMsgs++;
				}
				numMsgs++;
			}
			}
		   catch(SQLException e){
				System.out.println("fatal error found");
				exit();
			}
		}
		try {
			PreparedStatement st = conn.prepareStatement("Select count(*) from messageInfo;");
			ResultSet result = st.executeQuery();
			if (result.next()){
				numMsgs = result.getInt(1) + 1;
				while(true){
					PreparedStatement st1 = conn.prepareStatement("Select * from messageInfo where msgID = ?;");
					st1.setInt(1, numMsgs);
					ResultSet res = st1.executeQuery();
					if (!res.next()){
						return numMsgs++;
				}
				numMsgs++;
				}
			}
			else {
				System.out.println("[ error ] cannot count how many messages have been sent");
			}
		}catch(SQLException e){
			printSQLError(e);
			return -1; //what can we return to not get an error when try to use this number? 
		}
		return -1;
	}
	
	/*
	* @func 	insert_new_profile
	* @brief 	query sql part of createUser. 
				sorry i can't handle long functions and CreateUser was getting too large.
	* @return 	True if insertion worked. False if it did not. 
	*/
	public static boolean insert_new_profile(int userID, String name, String email, String dob, String password)
	{
		
		try{
			PreparedStatement st = conn.prepareStatement("Insert into Profile values(?,?,?,?,?,?)");
			st.setInt(1,userID);
			st.setString(2,name);
			st.setString(3,email);
			st.setString(4,password);
			java.sql.Date date_of_birth = java.sql.Date.valueOf(dob);
			st.setDate(5, date_of_birth);
			java.sql.Date last_login = new java.sql.Date(System.currentTimeMillis());
			st.setDate(6, last_login);
			int i = st.executeUpdate();
			if (i<0) {return false;}
		}
		catch(SQLException e){
			printSQLError(e);
			return false;
		}
		return true;
	}


	/*
	 * @func 	createUser
	 * @brief 	Given a name, email address, and date of birth, add a new user to the system by inserting a
     * 			new entry into the profile relation. userIDs should be auto-generated.
     * @return 	valid_insertion if the insertion worked True, False if it did not work. 
     */

	public static boolean createUser(String name, String email, String dob, String password)
	{
		try
        {	
        	int year = Integer.parseInt(dob.substring(0,4));
        	int month = Integer.parseInt(dob.substring(5,7));
        	int day = Integer.parseInt(dob.substring(8,10));
            LocalDate.of(year, month, day);
        }
        catch(Exception em)
        {
        	System.out.println("Please use correct dob format [YYYY-MM-DD])");
            return false;
        }
        
		boolean valid_insertion = false;
		if (name.length()>49 || name.length()==0){ 
			System.out.println("Illegal name length"); 
			return false;	
		}
		if (email.length()>49 || email.length()==0){
			System.out.println("Illegal email length"); 
			return false;
		}
		if (password.length()>49 || password.length()==0){
			System.out.println("Illegal password length"); 
			return false;
		}
		if (!email.matches("[a-zA-Z0-9.]+@[a-zA-Z0-9.]+")) {
			System.out.println("Illegal email format"); 
			return false;
		}

		try{
			PreparedStatement a = conn.prepareStatement("Select * from profile where email = ? and password = ?");
			a.setString(1, email);
			a.setString(2, password);
			ResultSet result = a.executeQuery();
			if (result.next() == false){ //don't want it to pick up a null
				int userID = get_new_userID();
				if (userID==-1){return false;}
				valid_insertion = insert_new_profile(userID,name, email,dob,password);
			}
			else{

				System.out.println("User already Created. Please choose a new email or login using the existing account.");
				return false;
        	}
		}
		catch(SQLException e){
			printSQLError(e);
			return false;
		}
		return valid_insertion;
	}
	
	public static boolean sendMessageToUser(int toUserID, String message) { //assuming fromUserID has to exist for her to send a message. 
		if(!loggedIn){
			System.out.println("[ error ] You must be logged in to do that!");
			return false;
		}
		if (message.length()==0) return false;
		boolean msg_sent = false;
		int msgID = -1;
		int fromID = activeUser.userID;
		
		//Display name of Recipient. 
		try {
			PreparedStatement a = conn.prepareStatement("Select name from profile where userID = ?");
			a.setInt(1,toUserID);
			ResultSet result = a.executeQuery();
			if (result.next()){
				PreparedStatement validFriend = conn.prepareStatement("Select are_friends(?, ?)");
				validFriend.setInt(1, fromID);
				validFriend.setInt(2, toUserID);
				ResultSet rs = validFriend.executeQuery();
				boolean areFriends;
				if(rs.next()) areFriends = rs.getBoolean(1);
    			else areFriends = false;

				if(areFriends){
					String userName = result.getString("name");
					System.out.println("Sending Message To: " + userName);
    			}
    			else{
    				System.out.println("You are not friends with that person.");
    				return false;
    			}
			}
			else {
				System.out.println("UserID is not found in database.");
				return false;
			}
		}
		catch(SQLException e){
			printSQLError(e);
			return false;
		}
		
		msgID = get_new_msgID();
		if (msgID==-1) return false;
		//insert into MessageInfo
		try {
			PreparedStatement st = conn.prepareStatement("Insert into messageInfo values(?,?,?,?,?,?)");
			st.setInt(1,msgID);
			st.setInt(2,fromID);
			st.setString(3,message);
			st.setInt(4,toUserID);
			st.setNull(5,Types.INTEGER);
			java.sql.Date time_sent = new java.sql.Date(System.currentTimeMillis());
			st.setDate(6, time_sent);
			int i = st.executeUpdate();
			if (i>=0) {
				System.out.println("Message Sent");
				return true;
			}
				
			else {
				System.out.println("Message FAILED to Send");
				return false;
			}
		}
		catch (SQLException e){
			printSQLError(e);
			return false;
		}
	}


		/*
	 * @func 	topMessages
	 * @brief	Displays information on the top k users for the last x months,
	 * 			where users are ranked by the number of messages sent to the
	 * 			actively logged in user
	 * @param k	An integer representing the maximum number of users to display
	 * @param x	An integer representing how many months in the past to pull
	 * 			messages from
	 * @return	True if successfully queried, false otherwise
	 */
	public static boolean topMessages(int k, int x){
		try {
			if (!loggedIn) {
				System.out.println("[ error ] You must be loggedIn to do that.");
				return false; 
			}
			if (x<0) {
				System.out.println("Number of Months cannot be less than 0");
				return false;
			}
			if (k<0){
				System.out.println("Number of topMessages cannot be less than 0");
				return false;
			}
			//get the messages from userID 
			PreparedStatement ps = conn.prepareStatement(
				"select A.name as \"User Name\", A.userID as \"User ID\", A.countFrom as \"Count of Messages Recevied\", B.countTo as \"Count of Messages To\" " +
				"from ( select p.name, p.userID, count(*) as countFrom " +
                "       from profile p join messageInfo m on p.userID = m.fromID " +
                "       where m.toUserID = ? and m.timeSent >= now() - ( ? || ' MONTH')::INTERVAL " +
                "       group by p.name, p.userID " +
                "       order by countFrom desc " +
                "       limit ? ) A " +
				"     full outer join " +
                "     ( select p.name, p.userID, count(*) as countTo " +
                "       from profile p join messageInfo m on m.fromID = ? " +
                "       where p.userID = m.toUserID " +
                "       group by p.name, p.userID ) B " +
                "     on A.userID = B.userID " +
        		"where A.userID is not null and A.countFrom is not null " +
        		"order by A.countFrom desc;"
			);
			ps.setInt(1, activeUser.userID);
			ps.setInt(2, x);
			ps.setInt(3, k);
			ps.setInt(4, activeUser.userID);
		
			ResultSet result = ps.executeQuery();
			if(result.next()){
				boolean notDone = true;
				do{
					String s = result.getString(1);
					int i1 = result.getInt(2);
					int i2 = result.getInt(3);
					int i3 = result.getInt(4);
					System.out.printf("Name: %s\n\tUser ID: %d\n\tMessages Received From: %d\n\tMessages Sent To: %d\n\n", s, i1, i2, i3);
					if(result.next()){ }
					else{ notDone = false; }
				} while(notDone);
			}
			else{
				System.out.println("No messages found.  Try being nicer to make some friends.");
			}
		}catch (SQLException e){
			printSQLError(e);
			return false;
		}
		return true;
	}

	
	 /* @func	login
	 * @brief	Collects a user email and password, and populates a new user
	 * 			object with that user's details if it exists
	 * @return	A new User object on success, null otherwise
	 */
    public static User login(String email, String password) throws SQLException, ClassNotFoundException
    {
		if(loggedIn){
			System.out.println("[ error ] A User is already logged in!");
			return null;
		}
        PreparedStatement st = conn.prepareStatement("Select * from profile where email=? and password=?"); //use prepared statement to avoid injection
        st.setString(1, email);
        st.setString(2, password);
		try{
        	ResultSet result = st.executeQuery();
        	if(result.next()){ // result.next() will be false if the query didn't return anything
            	int uID = result.getInt("userID");
            	String name = result.getString("name");
            	String dob = result.getString("date_of_birth");
				loggedIn = true;
				User ret = new User(uID, name, email, password, dob);
				activeUser = ret;
				return ret;
        	}
        	else{
				System.out.println("Incorrect email or password.");
				return null;
        	}
		}
		catch(SQLException e){
			printSQLError(e);
			return null;
		}
    }

    /*
	 * @func	initiateFriendship
	 * @brief	Given a fromID, toID, and message, add to table pending friend values(fromID, toID, message).
	 * @param fromID	An integer representing the user sending the request
	 * @param toID		An integer representing the user to whom a request is being sent
	 * @param message 	A String representing the message being sent for the request
	 * @return	True on success, false otherwise
	 */
    public static boolean initiateFriendship(int fromID, int toID, String message) throws SQLException, ClassNotFoundException, IllegalArgumentException
    {
    	PreparedStatement st = conn.prepareStatement("insert into pendingFriend values(?, ?, ?)");
    	st.setInt(1, fromID);
    	st.setInt(2, toID);
    	st.setString(3, message);
    	try{
    		int result = st.executeUpdate();
    		if(result<0) return false;
    		else return true;
    	}
    	catch(SQLException e){
    		printSQLError(e);
    		return false;
    	}
    }	

    private static String getName(int ID) throws SQLException
	{
		PreparedStatement st = conn.prepareStatement("select name from profile where userid = ?");
		st.setInt(1, ID);
		ResultSet name = st.executeQuery();
		if(name.next()){
			String result = name.getString(1);
			return result;
		}
		else{
			return "";
		}
	}

    /*
    *	@func displayMessages
    *	@brief display all messages sent to a user
    *	@param userID the user who messages were sent to
    *	
    */
    public static boolean displayMessages(int userID) throws SQLException, IllegalArgumentException
    {
    	if(!loggedIn){
    		System.out.println("[ error ] You mustbe logged in to do that!");
			return false;
    	}

    	try{
    		// check if valid userID
	    	PreparedStatement st = conn.prepareStatement("select * from profile where userID=?");
	    	st.setInt(1, userID);
	    	ResultSet res = st.executeQuery();
	    	if(!res.next()) throw new IllegalArgumentException();

	    	// it's a valid userID
	    	PreparedStatement st2 = conn.prepareStatement("select * from messageinfo where toUserID=?");
	    	st2.setInt(1, userID);
	    	ResultSet result = st2.executeQuery();
	    	int msgID, fromID, toUserID, toGroupID;
	    	Timestamp timeSent;
	    	String message;
	    	System.out.println("Messages from friends:");
	    	while(result.next()){
	    		msgID = result.getInt(1);
	    		fromID = result.getInt(2);
	    		message = result.getString(3);
	    		toUserID = result.getInt(4);
	    		toGroupID = result.getInt(5);
	    		timeSent = result.getTimestamp(6);
	    		System.out.println("messageID: " + msgID + "\tfromID: " + fromID + "\ttoUserID: " + toUserID + "\ttoGroupID: " + toGroupID + "\tTime Sent: " + timeSent);
	    		System.out.println("Message Contents: " + message + "\n");
	    	}
	    	System.out.println("\nMessages sent to groups where you are a member: ");
	    	PreparedStatement st3 = conn.prepareStatement("select gID from groupmember where userID = ?");
	    	st3.setInt(1, userID);
	    	ResultSet res3 = st3.executeQuery();
	    	while(res3.next()){
	    		int currGroupID = res3.getInt(1);
	    		PreparedStatement st4 = conn.prepareStatement("select * from messageinfo where toGroupID=?");
	    		st4.setInt(1, currGroupID);
	    		ResultSet res4 = st4.executeQuery();
	    		while(res4.next()){
	    			msgID = res4.getInt(1);
		    		fromID = res4.getInt(2);
		    		message = res4.getString(3);
		    		toUserID = res4.getInt(4);
		    		toGroupID = res4.getInt(5);
		    		timeSent = res4.getTimestamp(6);
		    		System.out.println("messageID: " + msgID + "\tfromID: " + fromID + "\ttoUserID: " + toUserID + "\ttoGroupID: " + toGroupID + "\tTime Sent: " + timeSent);
		    		System.out.println("Message Contents: " + message + "\n");
	    		}

	    	}
	    	return true;
	    }
	    catch(SQLException e){
	    	//printSQLError(e);
			return false;
	    }
    }

    /*
    *	@func displayNewMessages
    *	@brief display all messages sent to a user after their last login
    *	@param userID the user who messages were sent to
    *	
    */
    public static boolean displayNewMessages(int userID) throws SQLException, IllegalArgumentException
    {
    	if(!loggedIn){
    		System.out.println("[ error ] You mustbe logged in to do that!");
			return false;
    	}

    	try{
	    	// check if valid userID and get last login time
	    	PreparedStatement st = conn.prepareStatement("select * from profile where userID=?");
	    	st.setInt(1, userID);
	    	ResultSet res = st.executeQuery();
	    	Timestamp lastLoginTime;
	    	if(!res.next()) throw new IllegalArgumentException();
	    	else{
	    		lastLoginTime = res.getTimestamp(6);
	    	}

	    	// get user's last login time

	    	// it's a valid userID
	    	PreparedStatement st2 = conn.prepareStatement("select * from messageinfo where toUserID=? and timesent>?");
	    	st2.setInt(1, userID);
	    	st2.setTimestamp(2, lastLoginTime);
	    	ResultSet result = st2.executeQuery();

	    	int msgID, fromID, toUserID, toGroupID;
	    	Timestamp timeSent;
	    	String message;
	    	System.out.println("New Messages from friends:");

	    	while(result.next()){
	    		msgID = result.getInt(1);
	    		fromID = result.getInt(2);
	    		message = result.getString(3);
	    		toUserID = result.getInt(4);
	    		toGroupID = result.getInt(5);
	    		timeSent = result.getTimestamp(6);
	    		System.out.println("messageID: " + msgID + "\tfromID: " + fromID + "\ttoUserID: " + toUserID + "\ttoGroupID: " + toGroupID + "\tTime Sent: " + timeSent);
	    		System.out.println("Message Contents: " + message + "\n\n");
	    	}
	    	System.out.println("\nNew Messages sent to groups where you are a member: ");
	    	PreparedStatement st3 = conn.prepareStatement("select gID from groupmember where userID = ?");
	    	st3.setInt(1, userID);
	    	ResultSet res3 = st3.executeQuery();
	    	while(res3.next()){
	    		int currGroupID = res3.getInt(1);
	    		PreparedStatement st4 = conn.prepareStatement("select * from messageinfo where toGroupID=? and timesent>?");
	    		st4.setInt(1, currGroupID);
	    		st4.setTimestamp(2, lastLoginTime);
	    		ResultSet res4 = st4.executeQuery();
	    		while(res4.next()){
	    			msgID = res4.getInt(1);
		    		fromID = res4.getInt(2);
		    		message = res4.getString(3);
		    		toUserID = res4.getInt(4);
		    		toGroupID = res4.getInt(5);
		    		timeSent = res4.getTimestamp(6);
		    		System.out.println("messageID: " + msgID + "\tfromID: " + fromID + "\ttoUserID: " + toUserID + "\ttoGroupID: " + toGroupID + "\tTime Sent: " + timeSent);
		    		System.out.println("Message Contents: " + message + "\n");
	    		}

	    	}

	    }
	    catch(SQLException e){
	    	//printSQLError(e);
			return false;
	    }
	    return true;
    }

    public static boolean displayFriends(int userID) throws SQLException, IllegalArgumentException
    {
    	if(!loggedIn){
    		System.out.println("[ error ] You must be logged in to do that!");
			return false;
    	}

    	try{
    		if(!displayAllFriends(userID)){
    			return false;
    		}
    		return true;
    	}
    	catch(Exception e){
			printSQLError((SQLException)e);
    		return false;
    	}
    }

    private static boolean displayProfile(int userRequested){
    	try{
	    	PreparedStatement st = conn.prepareStatement("select * from profile where userid=?");
	    	st.setInt(1, userRequested);
	    	ResultSet result = st.executeQuery();
			while(result.next()){
				System.out.println();
				int id = result.getInt(1);
				String name = result.getString(2);
				String email = result.getString(3);
				Timestamp dob = result.getTimestamp(5);
				Timestamp lastLogin = result.getTimestamp(6);
				System.out.print("User ID: " + id + " Name: " + name + " Email: " + email + " DOB: " + dob + " Last Login: " + lastLogin + "\n\n");
			}
			return false;
		}
		catch(Exception e){
			return false;
		}
    	
    }

    private static boolean displayAllFriends(int userID){
    	try{
    		PreparedStatement st = conn.prepareStatement("select userid2, name from friend " + 
    			"join profile on friend.userid2 = profile.userid where friend.userid1 = ?" + 
				" union select userid1, name from friend join profile on friend.userid1 = profile.userid where friend.userid2 = ?");
    		st.setInt(1, userID);
    		st.setInt(2, userID);
    		ResultSet result = st.executeQuery();
    		int uID;
    		String name;
    		System.out.println("Here's a list of your current friends:\n");
    		while(result.next()){
    			uID = result.getInt(1);
    			name = result.getString(2);
    			System.out.println("User ID: " + uID + "\tName: " + name);
    		}
    		return true;

    	}
    	catch(SQLException e){
			//printSQLError(e);
    		return false;
    	}
    }

    private static boolean removePendingFriendRequest(int fromID, int toID) throws SQLException, IllegalArgumentException
    {
    	PreparedStatement st = conn.prepareStatement("delete from pendingFriend where fromid = ? and toid = ?");
    	st.setInt(1, fromID);
    	st.setInt(2, toID);
    	try{
    		st.executeUpdate();
    		return true;
    	}
    	catch(Exception e){
    		System.out.println(e.getMessage());
    		System.out.println("Delete from pendingFriend failed.");
    		return false;
    	}

    }

    private static boolean confirmFriendRequest(int fromID, int toID) throws SQLException, IllegalArgumentException
    {
    	PreparedStatement getMessage = conn.prepareStatement("select message from pendingFriend where fromID = ? and toID = ?");
    	getMessage.setInt(1, toID);
    	getMessage.setInt(2, fromID);
    	StringBuilder msg = new StringBuilder();
    	ResultSet result = getMessage.executeQuery();
    	if(result.next()){
    		msg.append(result.getString(1));
    	}
    	PreparedStatement st = conn.prepareStatement("insert into pendingFriend values (?, ?, ?)");
    	st.setInt(1, fromID);
    	st.setInt(2, toID);
    	st.setString(3, msg.toString());
    	try{
    		st.executeUpdate();
    		return true;
    	}
    	catch(Exception e){
    		System.out.println(e.getMessage());
    		System.out.println("Insert failed.");
    		return false;
    	}
    }

    private static int promptForConfirm()
    {
    	boolean validID = false;
    	int id = -1;
    	String idString;
    	do{
    		idString = getInput("Enter the number of the request you would like to confirm. Enter 0 to accept all requests. Enter -1 to decline all requests.");
    		try{
    			id = Integer.parseInt(idString);
    			validID = true;
    		}
    		catch(Exception e){
    			System.out.println("Please enter an integer.");
    		}
    	} while(!validID);
    	return id;
    }

    /*
    *
    *
    *
    */
    private static int showAllFriendRequests(int userID, HashMap<Integer, Integer> map) throws SQLException, IllegalArgumentException
    {
    	System.out.println("Pending friend requests:");
    	PreparedStatement st = conn.prepareStatement("Select pendingFriend.fromID, profile.name, pendingFriend.message from pendingFriend join profile on fromid = userid where pendingFriend.toID=?");
    	st.setInt(1, userID);
    	ResultSet friends = st.executeQuery();
    	int senderID;
    	String userName;
    	String message;
    	int currRequest = 1;
    	while(friends.next()){
    		System.out.print("Friend Request #" + currRequest + ":\t");
    		senderID = friends.getInt(1);
    		userName = friends.getString(2);
    		message = friends.getString(3);
    		map.put(currRequest, senderID);
    		currRequest++;
    		System.out.println("Sender ID: " + senderID + "\tName: " + userName + "\tMessage: " + message);
    	}
    	return currRequest - 1;
    }

    /*
    *	@func showAllGroupRequests
    *	@brief lists all pending group requests where the passed in user is an admin starting at the number following the number of friend requests that exist to that user
    		Inserts each group request to the hashmap, associating a userID and groupID with each number that the user can select to confirm a request
    *	@param userID - the user who is an admin over a group
    *	@param numFriendRequests - Used to print the correct starting value for the user to select
    *	@param map - a hashmap associating the listed numbers with the userIDs (the user will see 1-n as options to confirm, and associates each index with a userID)
    */
    private static int showAllGroupRequests(int userID, int numFriendRequests, HashMap<Integer, List<Integer>> map) throws SQLException, IllegalArgumentException
    {
    	int currRequest = numFriendRequests+1;
    	int groupRequests = 0;
    	System.out.println("Pending group requests for groups where you are an administrator:");
    	PreparedStatement st = conn.prepareStatement("Select gid from groupmember where userID = ? and role = 'manager'"); //returns the group ids where current user is admin
    	st.setInt(1, userID);
    	ResultSet groupsWhereAdmin = st.executeQuery();
    	while(groupsWhereAdmin.next()){
    		PreparedStatement st2 = conn.prepareStatement("Select pendingGroupMember.userID, profile.name, pendingGroupMember.message from pendingGroupMember " +
				"join profile on pendingGroupMember.userid = profile.userid " + 
    			"where pendingGroupMember.gid = ?");
    		int groupID = groupsWhereAdmin.getInt(1);
    		st2.setInt(1, groupID);
    		ResultSet pendingRequests = st2.executeQuery(); // gets all of the pending requests for each group 
    		while(pendingRequests.next()){
    			ArrayList<Integer> userGroup = new ArrayList<>();
    			int requestorID = pendingRequests.getInt(1);
    			String requestorName = pendingRequests.getString(2);
    			String requestorMessage = pendingRequests.getString(3);
    			userGroup.add(requestorID);
    			userGroup.add(groupID);
    			map.put(currRequest, userGroup);
    			System.out.print("Group Request #" + currRequest + ":\t");
    			groupRequests++;
    			currRequest++;
    			System.out.println("Sender ID: " + requestorID + "\tName: " + requestorName + "\tGroup: " + groupID + "\tMessage: " + requestorMessage);
    		}
    	}
    	return groupRequests;
    }


     /*
    *	@func confirmGroupRequest
    *	@brief given a list containing a userID and groupID, add them to that group
    *	@param userGroup - a list with a userID at index 0 and groupID at index 1
    */
    private static boolean confirmGroupRequest(List<Integer> userGroup) throws SQLException, IllegalArgumentException
    {
    	int userID = userGroup.get(0);
    	int groupID = userGroup.get(1);
    	PreparedStatement getMsg = conn.prepareStatement("select message from pendingGroupMember where userid = ? and gid = ?");
    	getMsg.setInt(1, userID);
    	getMsg.setInt(2, groupID);
    	ResultSet msgResult = getMsg.executeQuery();
    	if(msgResult.next()){
	    	String message = msgResult.getString(1);
	    	PreparedStatement st = conn.prepareStatement("insert into groupMember values (?, ?, ?)");
	    	st.setInt(1, groupID);
	    	st.setInt(2, userID);
	    	st.setString(3, message);
	    	try{
	    		st.executeUpdate();
	    		removePendingGroupMember(userGroup);
	    		return true;
	    	}
	    	catch(Exception e){
	    		System.out.println(e.getMessage());
	    		System.out.println("Insert failed.");
	    		return false;
	    	}
	    }
	    return false;
    }

    /*
    *	@func removePendingGroupMember
    *	@brief removes the entry from pendingGroupMember for the user/group that is passed in
    *	@param userGroup - a list with a userID at index 0 and a groupID at index 1
    */
    private static boolean removePendingGroupMember(List<Integer> userGroup) throws SQLException, IllegalArgumentException
    {
    	int userID = userGroup.get(0);
    	int groupID = userGroup.get(1);
    	PreparedStatement st = conn.prepareStatement("delete from pendingGroupMember where gid = ? and userid = ?");
    	st.setInt(1, groupID);
    	st.setInt(2, userID);
    	try{
    		st.executeUpdate();
    		return true;
    	}
    	catch(Exception e){
    		System.out.println(e.getMessage());
    		System.out.println("Delete from pendingGroupMember failed.");
    		return false;
    	}
    }

    /*
	 * @func	logout
	 * @brief	Sets the last login time for the given user to now
	 * @return	True on success, False otherwise
	 */
	public static boolean logout(int userID){
		if(!loggedIn){
			System.out.println("[ error ] No user is logged in!");
			return false;
		}
		try{
			System.out.println("Logging out user " + userID);
			PreparedStatement st = conn.prepareStatement(
				"update profile set lastlogin = now() where userID = ?;"
			);
			st.setInt(1, userID);
			st.executeUpdate();
			System.out.println("Logged out user " + userID);
			loggedIn = false;
			activeUser = null;
		}
		catch(SQLException e){
			printSQLError(e);
			return false;
		}
		return true;
	}

	public static boolean dropUser(){
		if (!loggedIn) { 
			System.out.println("Cannot Drop User if User is Not Logged In");
			return false;
		}
		int dropID = activeUser.userID;
		logout(activeUser.userID);
		try {
			PreparedStatement st = conn.prepareStatement("delete from profile where userID = ?;");
			st.setInt(1, dropID);
			int i = st.executeUpdate();
			if (i<0) {return false;}
			else return true;
		}
		catch (SQLException e){
			printSQLError(e);
			return false;
		}
	}

    public static void exit(){
    	sc.close();
    	System.out.println("Goodbye!");
    	if(loggedIn) {logout(activeUser.userID);}   
    	System.exit(0);
    }

     /*
    *	@func confirmRequests
    *	@brief shows the user all pending friend requests and group requests where they are an admin.
    *		Allows the user to confirm individual requests or confirm/decline all requests.
    *	@param userID - the userID of the user who has been sent requests
    *	
    *	Uses several helper methods.
    */
    public static boolean confirmRequests(int userID)
    {
    	if(!loggedIn){
    		System.out.println("[ error ] You must be logged in to do that!");
			return false;
    	}
    	if(userID < 0){
    		System.out.println("[ error ] Invalid userID");
    		return false;
    	}

    	try{
    		HashMap<Integer, Integer> friendRequestToID = new HashMap<>(); // maps the request number to a userID
    		HashMap<Integer, List<Integer>> groupRequestToIDs = new HashMap<>(); // maps the request number to a list that contains the userID and the group they'd like to join

    		int numFriendRequests = showAllFriendRequests(userID, friendRequestToID); // returns the number of pending friend requests
    		if(numFriendRequests == 0){
    			System.out.println("You have no pending friend requests.");
    			//return true;
    		}
    		int numGroupRequests = showAllGroupRequests(userID, numFriendRequests, groupRequestToIDs);
    		if(numGroupRequests == 0){
    			System.out.println("You have no group requests.");
    			if(numFriendRequests == 0){
    				return true;
    			}
    		}
    		int whoToConfirm = promptForConfirm();
    		boolean done = false;
    		while(!done){ // loop while they confirm individual requests
    			while(whoToConfirm > numFriendRequests+numGroupRequests+1 || whoToConfirm < -1){ // loop when they try to confirm a request for a number that is not valid
    				System.out.println("Invalid entry. Please try again.");
    				whoToConfirm = promptForConfirm();
    			}
    			if(whoToConfirm == 0){ // confirm all requests
	    			for(Integer user : friendRequestToID.keySet()){
	    				confirmFriendRequest(userID, friendRequestToID.get(user));
	    			}
	    			for(Integer user : groupRequestToIDs.keySet()){
	    				confirmGroupRequest(groupRequestToIDs.get(user));
	    			}
	    			done = true;
	    		}
				else if(whoToConfirm == -1){ //delete all requests
	    			for(Integer user : friendRequestToID.keySet()){
	    				removePendingFriendRequest(friendRequestToID.get(user), userID);
	    			}
	    			for(Integer user : groupRequestToIDs.keySet()){
	    				List<Integer> curr = groupRequestToIDs.get(user);
	    				removePendingGroupMember(curr);
	    			}
	    			done = true;
	    		}

    			else if(whoToConfirm > numFriendRequests){ // the user is confirming a group request
    				List<Integer> userGroup = groupRequestToIDs.get(whoToConfirm);
    				confirmGroupRequest(userGroup);
    				groupRequestToIDs.clear();
    			}
    			else{ //the user is confirming a friend request  or it's an invalid entry
	    			confirmFriendRequest(userID, friendRequestToID.get(whoToConfirm)); //confirm request for given user
	    			friendRequestToID.clear(); // clear the hashmap before refilling it. This is dumb and slow but should be safe
	    		}
    			numFriendRequests = showAllFriendRequests(userID, friendRequestToID);
    			numGroupRequests = showAllGroupRequests(userID, numFriendRequests, groupRequestToIDs);
    			if(numFriendRequests==0 && numGroupRequests == 0) return true;
    			whoToConfirm = promptForConfirm();
    		}

    		return true;
    	}
    	catch(Exception e){
    		System.out.println(e.getMessage());
    		return false;
    	}
    }


	public static void main(String[] args) throws SQLException, ClassNotFoundException 
	{
        //openConnection();
        //sc = new Scanner(System.in);
		//sc.nextLine();

		run();

    	//sc.close();
    	exit();
	}


	/*
	 * @func	openConection
	 * @brief	Establishes a connection to the PostgreSQL database for the
	 * 			PittSocial application
	 * @return	void
	 */
    public static void openConnection() throws SQLException, ClassNotFoundException
    {
        Class.forName("org.postgresql.Driver");
        String url = "jdbc:postgresql://localhost/postgres";
        Properties props = new Properties();
        props.setProperty("user", "postgres");
        props.setProperty("password", "postgres"); // Your password for postgres
		try{
        	conn = DriverManager.getConnection(url, props);
		}
		catch(SQLException e){
			printSQLError(e);
			System.exit(-1);
		}
    }

	/*
	 * @func	printMainMenu
	 * @brief	Displays options and collects commands at the second-level UI
	 * @return	A choice enum representing the user's choice of action
	 */
	public static choice printMainMenu(){
		choice ret =choice.NOCHOICE;
		int inp = 3;
		boolean notDone = true;
		System.out.println(
			"\n*******************************************************************************" +
			"\nWelcome to the PittSocial Social Network\n" +
			"*******************************************************************************\n" +
			"\nPlease select an option from the following:\n" +
			"\t1)\tMake a friend request\n" +
			"\t2)\tCreate a new group\n" +
			"\t3)\tMake a request to join a group\n" +
			"\t4)\tConfirm my friend requests\n" +
			"\t5)\tSend a message to a friend\n" +
			"\t6)\tSend a message to a group\n" +
			"\t7)\tDisplay my messages\n" +
			"\t8)\tDisplay my new messages(since last login)\n" +
			"\t9)\tDisplay my friends\n" +
			"\t10)\tSearch for a user\n" +
			"\t11)\tSearch for a user within three degrees of separation\n" +
			"\t12)\tDisplay my top K messages\n" +
			"\t13)\tLog out\n" +
			"\t14)\tDelete my user account\n" +
			"\t15)\tExit\n");
		while(notDone){
			try{
				System.out.printf("Your choice: ");
				inp = sc.nextInt();
				if(inp > 0 && inp < 16){
					notDone = false;
				}
				else{
					System.out.println("That is not a valid option, please input a number from 1 to 15 inclusive and press enter!");
				}
			}
			catch(InputMismatchException e){
				System.out.println("That is not a valid option, please input a number from 1 to 15 inclusive and press enter!");
				sc.nextLine();
			}
		}
		switch(inp){
			case 1: ret = choice.INITIATEFRIENDSHIP; break;
			case 2: ret = choice.CREATEGROUP; break;
			case 3: ret = choice.INITIATEADDINGGROUP; break;
			case 4: ret = choice.CONFIRMREQUESTS; break;
			case 5: ret = choice.SENDMESSAGETOUSER; break;
			case 6: ret = choice.SENDMESSAGETOGROUP; break;
			case 7: ret = choice.DISPLAYMESSAGES; break;
			case 8: ret = choice.DISPLAYNEWMESSAGES; break;
			case 9: ret = choice.DISPLAYFRIENDS; break;
			case 10: ret = choice.SEARCHFORUSER; break;
			case 11: ret = choice.THREEDEGREES; break;
			case 12: ret = choice.TOPMESSAGES; break;
			case 13: ret = choice.LOGOUT; break;
			case 14: ret = choice.DROPUSER; break;
			case 15: ret = choice.EXIT; break;
		}
		sc.nextLine();
		return ret;
}

	/*
	 * @func	printWelcomeMenu
	 * @brief	Displays options and collects commands at the top-level UI
	 * @return	A choice enum representing the user's choice of action
	 */
	public static choice printWelcomeMenu(){
		choice ret =choice.NOCHOICE;
		int inp = 3;
		boolean notDone = true;
		System.out.println(
			"\n*******************************************************************************" +
			"\nWelcome to the PittSocial Social Network\n" +
			"*******************************************************************************\n" +
			"\nPlease select an option from the following:\n" +
			"\t1)\tCreate a new user\n" +
			"\t2)\tLog in as an existing user\n" + 
			"\t3)\tExit\n");
		while(notDone){
			try{
				System.out.printf("Your choice: ");
				inp = sc.nextInt();
				if(inp > 0 && inp < 4){
					notDone = false;
				}
				else{
					System.out.println("That is not a valid option, please input a number from 1 to 3 inclusive and press enter!");
				}
			}
			catch(InputMismatchException e){
				System.out.println("That is not a valid option, please input a number from 1 to 3 inclusive and press enter!");
				sc.nextLine();
			}
		}
		switch(inp){
			case 1: ret = choice.CREATEUSER; break;
			case 2: ret = choice.LOGIN; break;
			case 3: ret = choice.EXIT; break;
		}
		sc.nextLine();
		return ret;
	}


	/*
	 * @func	printSQLError
	 * @brief	Pretty-prints exceptions associated with the database
	 * @param e	A SQLException object
	 * @return	void
	 */
	public static void printSQLError(SQLException e){
		System.out.println("[ error ] Database error:");
		while(e != null){
			System.out.println(e.getMessage());
			System.out.println(e.getSQLState());
			System.out.println(e.getErrorCode());
			e = e.getNextException();
		}
	}

	/*
	 * @func	run
	 * @brief	Conducts user interaction loop for a PittSocial instance
	 * @return	void
	 */
	public static void run() throws SQLException, ClassNotFoundException, IllegalArgumentException{
		openConnection();
        sc = new Scanner(System.in);
        String email, password, name;
        String confirm;

		choice curr;
		while (true){
			if(loggedIn){
				curr = printMainMenu();
			}
			else{
				curr = printWelcomeMenu();
			}
			switch(curr){
				case LOGIN:
					email = getInput("Enter your email address:");
					password = getInput("Enter your password:");
					try{
						User temp = login(email, password);
					}
					catch(IllegalArgumentException e){
						System.out.println("[ error ] The email or password were incorrect.  Both are case sensitive!");
					}
					break;
				case CREATEUSER:
					email = getInput("Enter new user email");
			 		password = getInput("Enter new user password");
					String dob = getInput("Enter new user dob in the YYYY-MM-DD format");
			 		name = getInput("Enter new user First and Last Name");
			 		try {
				 		if (createUser(name,email,dob,password)){
					 		System.out.printf("Successfully created the new user %s\n", name);
				 		}
				 		else{ //without this, a user would be automatically logged in if they tried to create another account that already existed
				 				// with the same email and password. 
				 			System.out.println("Failed to create new user.");
				 			break;
				 		}
				 	} catch (Exception e){
				 		System.out.println("[ error ] Failed to create user.");
				 		break;
				 	}
				 	try{
				 		login(email,password);
				 	}catch(Exception e){
				 		System.out.println("[ error ] Failed to log in new user. ");
				 	}
					break;
				case EXIT:
					exit();
					break;
				case INITIATEFRIENDSHIP:
					boolean validID = false;
					String friendIDString;
					int fID = -1;
					do{
						try{
							friendIDString = getInput("Enter the user ID of the friend you would like to add:");
							fID = Integer.parseInt(friendIDString);
							validID = true;
						}
						catch(Exception e){
							System.out.println("Please enter an integer.");
						}
					} while(!validID);
					String friendName = getName(fID);
					if(friendName.length() == 0){
						System.out.println("Invalid User ID");
						break;
					}
					confirm = getInput("Would you like to initiate a friendship with " + friendName + "? (Enter 'y' or 'n')");
					if(!confirm.toLowerCase().equals("y")){
						break;
					}

					String message = getInput("Enter the message you would like to send to " + friendName + ".");
					try{
						if(initiateFriendship(activeUser.userID, fID, message)){
							System.out.println("Successfully initiated a friendship with " + friendName + ".");
						}
					}
					catch(Exception e){
						System.out.println("Friendship initiation was unsuccessful.");
					}

					break;
				case CREATEGROUP:
					name = getInput("Enter the new group name:");
					String description = getInput("Enter the new group description:");
					int limit;
					try{
						limit = Integer.parseInt(getInput("Enter the maximum number of members (>0):"));
						if(createGroup(name, description, limit, activeUser.userID)){
							System.out.printf("Successfully created the new group %s\n", name);
						}
					}
					catch(NumberFormatException e){
						System.out.println("[ error ] Limits must be positive integer values!");
					}
					break;
				case INITIATEADDINGGROUP:
					int GID;
					try{
						GID = Integer.parseInt(getInput("Enter the group ID you wish to join:"));
						String msg = getInput("Enter the message for your request:");
						if(initiateAddingGroup(GID, msg)){
							System.out.printf("Successfully sent a request to join group %d\n", GID);
						}
					}
					catch(NumberFormatException e){
						System.out.println("[ error ] You must enter a nonnegative integer value for the group ID!");
					}
					break;
				case CONFIRMREQUESTS:
					try{
						confirmRequests(activeUser.userID);
					}
					catch(Exception e){
						System.out.println("Confirm Requests failed.");
					}
					break;
				case SENDMESSAGETOUSER:
		 			try{
		 				int sendToID = Integer.parseInt(getInput("Enter the User ID you wish to send a Message To."));
		 				String msg = getInputMultiLine("Enter the message:");
						if (sendMessageToUser(sendToID, msg)) {
						 	System.out.printf("Successfully sent message to user %d\n", sendToID);
						}
						else{
							System.out.println("Unable to send message to user " + sendToID);
						}
					 }
					 catch (NumberFormatException e) {
						 System.out.println("[ error ] You must enter a nonnegative integer value of a valid user that is your friend!");
					 }
					break;
				case SENDMESSAGETOGROUP:
					try{
						int GID2 = Integer.parseInt(getInput("Enter the group ID to send a message to:"));
						String msg = getInputMultiLine("Enter the message:");
						if(sendMessageToGroup(GID2, msg)){
							System.out.printf("Successfully sent a message to group %d\n", GID2);
						}
					}
					catch(NumberFormatException e){
						System.out.println("[ error ] You must enter a nonnegative integer value for the group ID!");
					}
					break;
				case DISPLAYMESSAGES:
					try{
						if(!displayMessages(activeUser.userID)){
							System.out.println("Display Messages failed.");
						}
					}
					catch(Exception e){
						System.out.println("Display Messages failed.");
					}
					break;
				case DISPLAYNEWMESSAGES:
					try{
						if(!displayNewMessages(activeUser.userID)){
							System.out.println("Display New Messages failed.");
						}
					}
					catch(Exception e){
						System.out.println("Display New Messages failed.");
					}
					break;
				case DISPLAYFRIENDS:
					try{
						if(!displayFriends(activeUser.userID)){
							System.out.println("Display Friends failed.");
						}
						else{
							int userRequested;
				    		do{
				    			userRequested = Integer.parseInt(getInput("Enter the User ID of the user you would like to retrieve. Enter 0 to return to the main menu."));
				    			if(userRequested != 0){
				    				PreparedStatement st2 = conn.prepareStatement("select are_friends(?, ?)");
				    				st2.setInt(1, activeUser.userID);
				    				st2.setInt(2, userRequested);
				    				ResultSet res2 = st2.executeQuery();
				    				boolean areFriends;
				    				if(res2.next()) areFriends = res2.getBoolean(1);
					    			else areFriends = false;

				    				if(areFriends){
				    					displayProfile(userRequested);
					    			}
					    			else{
					    				System.out.println("You are not friends with that person.");
					    			}
				    			}
				    		}
				    		while (userRequested != 0);
						}
					}
					catch(Exception e){
						System.out.println("Display Friends failed.");
					}
					break;
				case SEARCHFORUSER:
					String query = getInput("Enter your search query:");
					searchForUser(query);
					break;
				case THREEDEGREES:
					try{
						int targetUser = Integer.parseInt(getInput("Enter the user ID to check:"));
						threeDegrees(targetUser);
					}
					catch(NumberFormatException e){
						System.out.println("[ error ] You must enter a valid user ID!");
					}
					break;
				case TOPMESSAGES:
					try{
						int k = Integer.parseInt(getInput("Enter the number of users you would like to view."));
						int x = Integer.parseInt(getInput("Enter the number of months you would like to view."));
						if (topMessages(k,x)){
							System.out.printf("Successfully viewed top Messages for %d users over the past %d months.", k,x);
						}
					} catch (NumberFormatException e){
						System.out.println("[ error ] You must enter a nonnegative number of users and months. ");
					}
					break;
				case LOGOUT:
					logout(activeUser.userID);
					break;
				case DROPUSER:
					try {
						confirm = getInput("Are you sure you want to drop your user profile from the database? [Y/N]");
						if (confirm.equalsIgnoreCase("Y")){
							if(dropUser()){
								System.out.printf("Successfully removed profile from database.");
							}
						}
						else{
							System.out.println("Okay, taking you back to the main menu. ");
						}
					} catch (Exception e){
						System.out.println("[ error ] unable to remove this profile.");
					}

					break;
				default:
					System.out.println("[ error ] That menu choice is invalid!");
			}
		}
	} // end run


	/*
	 * @func	searchForUser
	 * @brief	Searches profile table for user names and emails which match
	 * 			the given search string
	 * @param s	A string representing a sequence of space-delimited search
	 * 			strings
	 * @return	True on success, false otherwise
	 * @notes	Substrings which are longer than 50 characters will cause
	 * 			failure, since this is the maximum length of fields we are
	 * 			searching.  Assumes that the user need not be logged in to
	 * 			search for users.
	 */
	public static boolean searchForUser(String s) throws SQLException {
		if(s.length() == 0){
			System.out.println("[ error ] Your search string may not be empty.");
			return false;
		}
		String[] searches = s.split("\\s+");
		for(int i = 0; i < searches.length; i++){
			if(searches[i].length() > 50){
				System.out.println("[ error ] One or more of your search strings is too long.  Each space-separated search word should be <= 50 characters!");
				return false;
			}
			searches[i] = '%' + searches[i] + '%';
			System.out.println("Modified query: " + searches[i]);
		}

		Array sqlArray = conn.createArrayOf("varchar", searches);
		CallableStatement st = conn.prepareCall("{? = call search_profile(?) }");
		st.registerOutParameter(1, Types.OTHER);
		st.setArray(2, sqlArray);
		try{
			conn.setAutoCommit(false);		// cursors only work within transactions
			st.execute();
			ResultSet result = (ResultSet) st.getObject(1);
			if(result.next()){
				System.out.printf("Match: User ID: %d Name: %s Email: %s\n", result.getInt("userID"), result.getString("name"), result.getString("email"));
				while(result.next()){
					System.out.printf("Match: User ID: %d Name: %s Email: %s\n", result.getInt("userID"), result.getString("name"), result.getString("email"));
				}
			}
			else{
				System.out.println("No matches for the given search string!");
				return false;
			}
			conn.commit();
			conn.setAutoCommit(true);
		}
		catch(SQLException e){
			printSQLError(e);
			return false;
		}

		return true;
	}


	/*
	 * @func	sendMessageToGroup
	 * @brief	Given a group ID, send a message to all group members from the
	 * 			currently logged-in user.
	 * @param groupID	An integer representing the group that the logged-in
	 * 					user is a member of
	 * @param message	A string representing the message contents
	 * @return	True on success, false otherwise
	 */
	public static boolean sendMessageToGroup(int groupID, String message) throws SQLException{
		if(!loggedIn){
			System.out.println("[ error ] You must be logged in to do that!");
			return false;
		}
		else if(message.length() < 1){
			System.out.println("[ error ] Your message cannot be empty!");
			return false;
		}
		else if(message.length() > 200){
			System.out.println("[ error ] Your message cannot be longer than 200 characters!");
			return false;
		}
		try{
			// Verify that the user is a member of this group
			PreparedStatement s1 = conn.prepareStatement("select * from groupMember where userID=? and gID=?;");
			s1.setInt(1, activeUser.userID);
			s1.setInt(2, groupID);
			ResultSet r1 = s1.executeQuery();
			if(r1.next()){
				CallableStatement s2 = conn.prepareCall("{call add_group_message(?,?,?) }");
				s2.setInt(1, activeUser.userID);
				s2.setString(2, message);
				s2.setInt(3, groupID);
				s2.execute();
			}
			else{
				System.out.println("[ error ] You must already be a member of a group to send messages!");
				return false;
			}
		}
		catch(SQLException e){
			printSQLError(e);
			return false;
		}
		return true;
	}


	/*
	 * @func	threeDegrees
	 * @param target	An integer representing the userID of the user to
	 * 						find a path to
	 * @return 	True on success, false otherwise
	 */
	public static boolean threeDegrees(int target) throws SQLException{
		if(!loggedIn){
			System.out.println("[ error ] You must be logged in to do that!");
			return false;
		}
		int sourceUser = activeUser.userID;

        PreparedStatement st = conn.prepareStatement("select * from profile where userID=?;");
        st.setInt(1, target);
		try{
        	ResultSet result = st.executeQuery();
	        	if(result.next()){
    	    		System.out.printf("Searching for paths to user %d (%s)\n", target, target);

					CallableStatement c = conn.prepareCall("{? = call path_3_hops(?,?) }");
					c.registerOutParameter(1, Types.VARCHAR);
					c.setInt(2, sourceUser);
					c.setInt(3, target);
					c.execute();
					System.out.println(c.getString(1));
					c.close();
					return true;
				}
    	    	else{
					System.out.println("[ error ] The given userID does not match a known user, cannot find paths!");
					return false;
    	    	}
		}
		catch(SQLException e){
			printSQLError(e);
			return false;
		}
	} // end threeDegrees

	
	public static class User{
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

		public int getUserID(){
			return userID;
		}
	}

}
