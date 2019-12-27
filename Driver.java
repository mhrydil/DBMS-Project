/* 
 * Matthew Hrydil
 *
 * to compile on mac running java 8+: javac -cp postgresql-42.2.8.jar Driver.java PittSocial.java
 * to run on mac running java 8+: java -cp postgresql-42.2.8.jar:. Driver
 *
 * to compile on windows running java 8+: javac -cp "postgresql-42.2.8.jar;." Driver.java
 * to run on windows running java 8+: java -cp "postgresql-42.2.8.jar;." Driver
 *
 */


/*
 * @class	Driver
 * @brief	Driver for the PittSocial social network simulation
 */

import java.util.*;
import java.sql.*;

public class Driver{

	private static PittSocial ps = new PittSocial();
	private static Random random = new Random();
	private static int numTestsIssued;
	private static int numTestsPassed;

	/* Constants for printing relations */
	enum relation{
		FRIEND, GROUPINFO, GROUPMEMBER, MESSAGEINFO, MESSAGERECIPIENT, 
		PENDINGFRIEND, PENDINGGROUPMEMBER, PROFILE, NUM_RELATIONS
	};

public static void main(String[] args) throws SQLException, ClassNotFoundException
	{
        ps.set_sc();
        ps.openConnection();

		// Run unit tests on the PittSocial class
		if(args.length > 0){
			if(args[0].equals("--test")){
				test_me();
			}
			else{
				System.out.println("Unknown argument: " + args[0]);
			}
		}

        // for(relation r : relation.values()){
        // 	showTableContents(r);
        // }
		//Drive Program
		ps.run();

	}


public static void test_me() throws SQLException, ClassNotFoundException
	{

		//Run Unit Tests
		System.out.println("\n*******************************************************************************\nTesting Login...\n*******************************************************************************\n");
		testLogin();
		System.out.println("\n*******************************************************************************\nTesting Search For User...\n*******************************************************************************\n");
		testSearchForUser();
		System.out.println("\n*******************************************************************************\nTesting Three Degrees...\n*******************************************************************************\n");
		testThreeDegrees();

		System.out.println("\n*******************************************************************************\nTesting Create Group...\n*******************************************************************************\n");
		showTableContents(relation.GROUPINFO);
		testCreateGroup();
		showTableContents(relation.GROUPINFO);

		System.out.println("\n*******************************************************************************\nTesting Join Group...\n*******************************************************************************\n");
		showTableContents(relation.PENDINGGROUPMEMBER);
		testInitiateAddingGroup();
		showTableContents(relation.PENDINGGROUPMEMBER);

		System.out.println("\n*******************************************************************************\nTesting Group Message...\n*******************************************************************************\n");
		showTableContents(relation.MESSAGEINFO);
		testSendMessageToGroup();
		showTableContents(relation.MESSAGEINFO);

		System.out.println("\n*******************************************************************************\nTesting Create User...\n*******************************************************************************\n");
		showTableContents(relation.PROFILE);
		test_createUser();
		showTableContents(relation.PROFILE);

		System.out.println("\n*******************************************************************************\nTesting Send Message...\n*******************************************************************************\n");
		showTableContents(relation.MESSAGEINFO);
		test_sendMessageToUser();
		showTableContents(relation.MESSAGEINFO);

		System.out.println("\n*******************************************************************************\nTesting Top Messages...\n*******************************************************************************\n");
		test_topMessages();

		System.out.println("\n*******************************************************************************\nTesting Friend Request...\n*******************************************************************************\n");
		showTableContents(relation.PENDINGFRIEND);
		testInitiateFriendship();
		showTableContents(relation.PENDINGFRIEND);

		System.out.println("\n*******************************************************************************\nTesting Display Messages...\n*******************************************************************************\n");
		testDisplayMessages();

		System.out.println("\n*******************************************************************************\nTesting Display New Messages...\n*******************************************************************************\n");
		testDisplayNewMessages();

		System.out.println("\n*******************************************************************************\nTesting Display Friends...\n*******************************************************************************\n");
		testDisplayFriends();

		System.out.println("\n*******************************************************************************\nTesting Confirm Requests...\n*******************************************************************************\n");
		showTableContents(relation.PENDINGFRIEND);
		showTableContents(relation.PENDINGGROUPMEMBER);
		showTableContents(relation.FRIEND);
		showTableContents(relation.GROUPMEMBER);
		testConfirmRequests();
		showTableContents(relation.PENDINGFRIEND);
		showTableContents(relation.PENDINGGROUPMEMBER);
		showTableContents(relation.FRIEND);
		showTableContents(relation.GROUPMEMBER);

		System.out.println("\n*******************************************************************************\nTesting Logout...\n*******************************************************************************\n");
		testLogout();

		System.out.println("\n*******************************************************************************\nTesting Drop User...\n*******************************************************************************\n");
		showTableContents(relation.PROFILE);
		test_dropUser();
		showTableContents(relation.PROFILE);

		System.out.println("\n*******************************************************************************\nTesting Results\n*******************************************************************************\n");
		testReport();
	}

/******************************************************************************
 * Test Functions
 *****************************************************************************/

	 public static void test_createUser(){
		 String email;
		 String password;
		 String dob;
		 String name;
		 boolean success;
		 email = "kiw" +Integer.toString(random.nextInt(1000))+"@pitt.edu";;
		 password = "compliant";
		 dob = "1998-07-30";
		 name = "LENA";
		
		 try {
		 	 numTestsIssued++;
			 email = "aep" +Integer.toString(random.nextInt(1000))+"@pitt.edu";
			 password = Integer.toString(random.nextInt()); //assumption that this random password will create a unique user each time. 
			 dob = "1998-07-30";
			 name = Integer.toString(random.nextInt());

			 if (ps.createUser(name,email,dob,password)){
				 numTestsPassed++;
				 System.out.println("test_createUser : PASSED simple ps.createUser");
			 }
			 else {
				 System.out.println("test_createUser : FAILED simple createUser");
			 }

		 }
		 catch(Exception e) {
			System.out.println("test_createUser : FAILED simple createUser");
		 }

		 numTestsIssued++;
		 try {
			 email = "kiw" +Integer.toString(random.nextInt(1000))+"@pitt.edu";;
			 password = "compliant";
			 dob = "1998-07-30";
			 name = "LENA";

			 if (ps.createUser(name,email,dob,password)){
				 numTestsPassed++;
				 System.out.println("test_createUser : PASSED second simple createUser");
			 }
			 else {
				 System.out.println("test_createUser : FAILED second simple createUser");
			 }

		 }
		 catch(Exception e) {
			System.out.println("test_createUser : FAILED second simple createUser");
		 }

		// Test that bad dob fails
		numTestsIssued++;
		try{
			success = ps.createUser("welp", "nope@mail.com", "1999", "password");
			if(success){
				System.out.println("testCreateUser: FAILED bad dob");
			}
			else{
				numTestsPassed++;
				System.out.println("testCreateUser: PASSED bad dob");
			}
		}
		catch(Exception e){
			System.out.println("testCreateUser: FAILED bad dob");
		}

		// Test that empty name fails
		numTestsIssued++;
		try{
			success = ps.createUser("", "nope@mail.com", "1999-09-09", "password");
			if(success){
				System.out.println("testCreateUser: FAILED empty name");
			}
			else{
				numTestsPassed++;
				System.out.println("testCreateUser: PASSED empty name");
			}
		}
		catch(Exception e){
			System.out.println("testCreateUser: FAILED empty name");
		}

		// Test that empty email fails
		numTestsIssued++;
		try{
			success = ps.createUser("empty_email", "", "1999-09-09", "password");
			if(success){
				System.out.println("testCreateUser: FAILED empty email");
			}
			else{
				numTestsPassed++;
				System.out.println("testCreateUser: PASSED empty email");
			}
		}
		catch(Exception e){
			System.out.println("testCreateUser: FAILED empty email");
		}

		// Test that empty name fails
		numTestsIssued++;
		try{
			success = ps.createUser("empty_password", "nope@mail.com", "1999-09-09", "");
			if(success){
				System.out.println("testCreateUser: FAILED empty password");
			}
			else{
				numTestsPassed++;
				System.out.println("testCreateUser: PASSED empty password");
			}
		}
		catch(Exception e){
			System.out.println("testCreateUser: FAILED empty password");
		}


		// Test that user already exists fails
		numTestsIssued++;
		try{
			success = ps.createUser(name,email,dob,password);
			if(success){
				System.out.println("test_CreateUser: FAILED user already exists");
			}
			else{
				numTestsPassed++;
				System.out.println("test_CreateUser: PASSED user already exists");
			}
		}
		catch(Exception e){
			System.out.println("test_CreateUser: FAILED user already exists");
		}

		// Test that name too long fails
		numTestsIssued++;
		try{
			success = ps.createUser("reallyreallyreallyreallyreallyreallyreallyreallyreallyreallyreallyreallylongname", email,dob, password);
			if(success){
				System.out.println("test_CreateUser: FAILED name too long");
			}
			else{
				numTestsPassed++;
				System.out.println("test_CreateUser: PASSED name too long");
			}
		}
		catch(Exception e){
			System.out.println("test_CreateUser: FAILED name too long");
		}

		// Test that email too long fails
		numTestsIssued++;
		try{
			success = ps.createUser(name, "reallyreallyreallyreallyreallyreallyreallyreallyreallyreallyreallyreallylongname@mail.com", dob, password);
			if(success){
				System.out.println("test_CreateUser: FAILED email too long");
			}
			else{
				numTestsPassed++;
				System.out.println("test_CreateUser: PASSED email too long");
			}
		}
		catch(Exception e){
			System.out.println("test_CreateUser: FAILED email too long");
		}

		// Test that password too long fails
		numTestsIssued++;
		try{
			success = ps.createUser(name, email, dob, "reallyreallyreallyreallyreallyreallyreallyreallyreallyreallyreallyreallylongPASSWORD");
			if(success){
				System.out.println("test_CreateUser: FAILED password too long");
			}
			else{
				numTestsPassed++;
				System.out.println("test_CreateUser: PASSED password too long");
			}
		}
		catch(Exception e){
			System.out.println("test_CreateUser: FAILED password too long");
		}

     }

	 public static void test_sendMessageToUser(){
	 	boolean success;
	 	//adding users to db if they are not already inside it
	 	try {
	 		ps.createUser("Amanda", "lena@mail.com", "1998-07-30", "password");
	 		ps.createUser("Alex", "dejaco@mail.com", "1998-07-30", "pass");
	 	}
	 	catch (Exception e) {
	 		System.out.print(""); // fake just to fill the syntax.
	 	}

	 	numTestsIssued++;
	 	try{
	 		if (ps.get_loggedIn()) {ps.logout(ps.get_activeUser().getUserID());}
	 	}
	 	catch(Exception e){
	 		System.out.println("test_sendMessageToUser : FAILED to logout user");
	 	}

	 	try{
	 		if (ps.sendMessageToUser(0,"Hello")) {
	 			System.out.println("test_sendMessageToUser : FAILED login required");
	 		}
	 		else {
	 			numTestsPassed++;
	 			System.out.println("test_sendMessageToUser : PASSED login required");
	 		}
	 	}
	 	catch (Exception e){
	 		numTestsPassed++;
	 		System.out.println("test_sendMessageToUser : PASSED login required");
	 	}

		 numTestsIssued++;
		 try{
		 	 ps.login("wifiinvolving@mail.com", "patrick");
			 boolean message_sent = ps.sendMessageToUser(20, "Testing1");
			 if (message_sent) {
			 	numTestsPassed++;
			 	System.out.println("test_sendMessageToUser : PASSED simple message");
			}
			else{
				System.out.println("test_sendMessageToUser : FAILED simple message");
			}
		 }
		 catch (Exception e) {
			 System.out.println("test_sendMessageToUser : FAILED simple Message");
		 }

		 // Test that bad user id fails
		numTestsIssued++;
		try{
			success = ps.sendMessageToUser(-1, "Testing 2");
			if(success){
				System.out.println("test_SendMessageToUser: FAILED bad userID");
			}
			else{
				numTestsPassed++;
				System.out.println("test_SendMessageToUser: PASSED bad userID");
			}
		}
		catch(Exception e){
				System.out.println("test_SendMessageTouser: FAILED bad userID");
		}

		// Test that empty message fails
		numTestsIssued++;
		try{
			success = ps.sendMessageToUser(20, "");
			if(success){
				System.out.println("test_SendMessageToUser: FAILED empty message");
			}
			else{
				numTestsPassed++;
				System.out.println("test_SendMessageToUser: PASSED empty message");
			}
		}
		catch(Exception e){
				System.out.println("test_SendMessageToUser: FAILED empty message");
		}

		// Test that message too long fails
		numTestsIssued++;
		try{
			success = ps.sendMessageToUser(20,"thisisalongmessage!thisisalongmessage!thisisalongmessage!thisisalongmessage!thisisalongmessage!thisisalongmessage!thisisalongmessage!thisisalongmessage!thisisalongmessage!thisisalongmessage!thisisalongmessage!thisisalongmessage!thisisalongmessage!thisisalongmessage!thisisalongmessage!thisisalongmessage!thisisalongmessage!thisisalongmessage!thisisalongmessage!thisisalongmessage!thisisalongmessage!");
			if(success){
				System.out.println("testSendMessageToUser: FAILED message too long");
			}
			else{
				numTestsPassed++;
				System.out.println("testSendMessageToUser: PASSED message too long");
			}
		}
		catch(Exception e){
				System.out.println("testSendMessageToUser: FAILED message too long");
		}

		// Test that known good call succeeds
		numTestsIssued++;
		try{
			success = ps.sendMessageToUser(20, "Testing sendMessageToUser");
			if(success){
				numTestsPassed++;
				System.out.println("testSendMessageToUser: PASSED known good call");
			}
			else{
				System.out.println("testSendMessageToUser: FAILED known good call");
			}
		}
		catch(Exception e){
				System.out.println("testSendMessageToUser: FAILED known good call");
		}

     }

    public static void test_topMessages(){
    	if (ps.get_loggedIn()) {ps.logout(ps.get_activeUser().getUserID());}
    	//test must be logged in
    	numTestsIssued++;
    	try{
    		if (ps.topMessages(3, 1)) {
    			System.out.println("test_topMessages : FAILED login required");
    		}
    		else {
    			numTestsPassed++;
    			System.out.println("test_topMessages : PASSED login required");
    		}
    	}
    	catch (Exception e){
    		numTestsPassed++;
    		System.out.println("test_topMessages : PASSED login required");
    	}


    	//simple test
    	numTestsIssued++;
    	try{
    		ps.login("hourtherapy@mail.com", "compliant");
    		if(ps.topMessages(3, 1)){
    			numTestsPassed++;
    			System.out.println("test_topMessages : PASSED simple sort");
    		}
    		else {
    			System.out.println("test_topMessages : FAILED simple sort");
    		}
    	}
    	catch (Exception e){
    		System.out.println("test_topMessages : FAILED simple sort");
    	}
    	//test bad month number
    	numTestsIssued++;
    	try{
    		if (ps.topMessages(3, -1)) {
    			System.out.println("test_topMessages : FAILED bad month number");
    		}
    		else {
    			numTestsPassed++;
    			System.out.println("test_topMessages : PASSED bad month number");
    		}
    	}
    	catch (Exception e){
    		numTestsPassed++;
    		System.out.println("test_topMessages : PASSED bad month number");
    	}
    	//test bad row number
    	numTestsIssued++;
    	try{
    		if (ps.topMessages(-1, 1)) {
    			System.out.println("test_topMessages : FAILED bad row number");
    		}
    		else {
    			numTestsPassed++;
    			System.out.println("test_topMessages : PASSED bad row number");    		
    		}
    	}
    	catch (Exception e){
    		numTestsPassed++;
    		System.out.println("test_topMessages : PASSED bad row number");
    	}
    	//test long month range
    	numTestsIssued++;
    	try{
    		if (ps.topMessages(1, 31)) {
    			numTestsPassed++;
    			System.out.println("test_topMessages : PASSED long month range");
    		}
    		else {
    			System.out.println("test_topMessages : FAILED long month range");
    		}
    	}
    	catch (Exception e){
    		System.out.println("test_topMessages : FAILED long month range");
    	}

    }

    public static void test_dropUser(){
    	boolean worked;
    	numTestsIssued++;
    	String email = "dropUser@mail.com";
		String password = "fleeting";
		String dob = "1979-11-04";
		String name = "Hue";
		ps.createUser(name,email,dob, password);
		if (ps.get_loggedIn()){ ps.logout(ps.get_activeUser().userID);}
		try {
			ps.login(email, password);
		}
		catch (Exception ex){
			System.out.println("test_dropUser: FAILED to create user to drop.");
			return;
		}

    	try{
    		worked = ps.dropUser();
    		if(worked) {
    			numTestsPassed++;
    			System.out.println("test_dropUser: PASSED drop activeUser");
    		}
    		else {System.out.println("test_dropUser: FAILED drop activeUser");}
    	}
    	catch(Exception e){
    		System.out.println("test_dropUser: FAILED drop activeUser");
    	}

    	//Test user in group
    	numTestsIssued++;
    	email = "rogerssurname@pitt.edu" ;
    	password = "detection";
    	try {
    		ps.login(email, password);
    	}catch (Exception ex){
			System.out.println("test_dropUser: FAILED to login user to drop in group.");
			return;
		}
		try{
    		worked = ps.dropUser();
    		if(worked) {
    			numTestsPassed++;
    			System.out.println("test_dropUser: PASSED drop user in group");
    		}
    		else {System.out.println("test_dropUser: FAILED drop user in group");}
    	}
    	catch(Exception e){
    		System.out.println("test_dropUser: FAILED drop user in group");
    	}

    	//Test user in message and pending group 
    	numTestsIssued++;
    	email = "oughtbriefing@yahoo.com" ;
    	password = "quarterly";
    	try {
    		ps.login(email, password);
    	}catch (Exception ex){
			System.out.println("test_dropUser: FAILED to login user to drop in messageInfo.");
			return;
		}
		try{
    		worked = ps.dropUser();
    		if(worked) {
    			numTestsPassed++;
    			System.out.println("test_dropUser: PASSED drop user in messageInfo");
    		}
    		else {System.out.println("test_dropUser: FAILED drop user in messageInfo");}
    	}
    	catch(Exception e){
    		System.out.println("test_dropUser: FAILED drop user in messageInfo");
    	}

    	//Test user in friendship 
    	numTestsIssued++;
    	email = "particlesespn@outlook.com" ;
    	password = "laugh";
    	try {
    		ps.login(email, password);
    	}catch (Exception ex){
			System.out.println("test_dropUser: FAILED to login user to drop in friend.");
			return;
		}
		try{
    		worked = ps.dropUser();
    		if(worked) {
    			numTestsPassed++;
    			System.out.println("test_dropUser: PASSED drop user in friend");
    		}
    		else {System.out.println("test_dropUser: FAILED drop user in friend");}
    	}
    	catch(Exception e){
    		System.out.println("test_dropUser: FAILED drop user in friend");
    	}

    	//Test user in pending friend
    	numTestsIssued++;
    	email = "phonesherbal@yahoo.com" ;
    	password = "helen";
    	try {
    		ps.login(email, password);
    		//if (!ps.initiateFriendship(5, 55, "WILL YOU BE MINE?")) return;
    	}catch (Exception ex){
			System.out.println("test_dropUser: FAILED to login user to drop in pending friend.");
			return; 
		}
		try{
    		worked = ps.dropUser();
    		if(worked) {
    			numTestsPassed++;
    			System.out.println("test_dropUser: PASSED drop user in pending friend");
    		}
    		else {System.out.println("test_dropUser: FAILED drop user in pending friend");}
    	}
    	catch(Exception e){
    		System.out.println("test_dropUser: FAILED drop user in pending friend");
    	}
    }

	/*
	 * @func	testCreateGroup
	 * @brief	Unit tests for the createGroup function
	 * @return	void
	 */
	public static void testCreateGroup() throws SQLException{
		boolean success;

		System.out.println("Logged in:");
		System.out.println(ps.get_loggedIn());

		// Test that login is required
		numTestsIssued++;
		try{
			success = ps.createGroup("deleteme213", "sdfdsf", 1, 30);
			if(success){
				System.out.println("testCreateGroup: FAILED login required");
			}
			else{
				numTestsPassed++;
				System.out.println("testCreateGroup: PASSED login required");
			}
		}
		catch(Exception e){
			System.out.println("testCreateGroup: FAILED login required");
		}

		// Test that bad user fails
		numTestsIssued++;
		try{
			ps.login("hourtherapy@mail.com", "compliant");
			success = ps.createGroup("welp", "nope", 1, -1);
			if(success){
				System.out.println("testCreateGroup: FAILED bad userID");
			}
			else{
				numTestsPassed++;
				System.out.println("testCreateGroup: PASSED bad userID");
			}
		}
		catch(Exception e){
			System.out.println("testCreateGroup: FAILED bad userID");
		}

		// Test that group already exists fails
		numTestsIssued++;
		try{
			success = ps.createGroup("pitt", "pittness", 1, 30);
			if(success){
				System.out.println("testCreateGroup: FAILED group already exists");
			}
			else{
				numTestsPassed++;
				System.out.println("testCreateGroup: PASSED group already exists");
			}
		}
		catch(Exception e){
			System.out.println("testCreateGroup: FAILED group already exists");
		}

		// Test that bad limit fails
		numTestsIssued++;
		try{
			success = ps.createGroup("badgroup", "badlimit", -1, 30);
			if(success){
				System.out.println("testCreateGroup: FAILED bad limit");
			}
			else{
				numTestsPassed++;
				System.out.println("testCreateGroup: PASSED bad limit");
			}
		}
		catch(Exception e){
			System.out.println("testCreateGroup: FAILED bad limit");
		}

		// Test that name empty fails
		numTestsIssued++;
		try{
			success = ps.createGroup("", "emptyname", 1, 30);
			if(success){
				System.out.println("testCreateGroup: FAILED empty name");
			}
			else{
				numTestsPassed++;
				System.out.println("testCreateGroup: PASSED empty name");
			}
		}
		catch(Exception e){
			System.out.println("testCreateGroup: FAILED empty name");
		}

		// Test that name too long fails
		numTestsIssued++;
		try{
			success = ps.createGroup("reallyreallyreallyreallyreallyreallyreallyreallyreallyreallyreallyreallylongname", "blah", 1, 30);
			if(success){
				System.out.println("testCreateGroup: FAILED name too long");
			}
			else{
				numTestsPassed++;
				System.out.println("testCreateGroup: PASSED name too long");
			}
		}
		catch(Exception e){
			System.out.println("testCreateGroup: FAILED name too long");
		}

		// Test that desc too long fails
		numTestsIssued++;
		try{
			success = ps.createGroup("blah", "theabsolutelongestdescriptionintheknownuniverse!!!theabsolutelongestdescriptionintheknownuniverse!!!theabsolutelongestdescriptionintheknownuniverse!!!theabsolutelongestdescriptionintheknownuniverse!!!theabsolutelongestdescriptionintheknownuniverse!!!",1, 30);
			if(success){
				System.out.println("testCreateGroup: FAILED desc too long");
			}
			else{
				numTestsPassed++;
				System.out.println("testCreateGroup: PASSED desc too long");
			}
		}
		catch(Exception e){
			System.out.println("testCreateGroup: FAILED desc too long");
		}

		// Test that desc empty is ok
		numTestsIssued++;
		try{
			success = ps.createGroup("deleteme1", "", 1, 30);
			if(success){
				numTestsPassed++;
				System.out.println("testCreateGroup: PASSED empty description");
			}
			else{
				System.out.println("testCreateGroup: FAILED empty description");
			}
		}
		catch(Exception e){
			System.out.println("testCreateGroup: FAILED empty description");
		}
		ps.logout(30);

		// cleanup DB
		try{
			PreparedStatement s = ps.get_conn().prepareStatement("delete from groupMember where gID > 12;");
			PreparedStatement s2 = ps.get_conn().prepareStatement("delete from groupInfo where gID > 12;");
			s.executeUpdate();
			s2.executeUpdate();
		}
		catch(SQLException e){
			ps.printSQLError(e);
			System.out.println("[ error ] Database error, check groups...");
		}

	} // end testCreateGroup


	/*
	 * @func	testInitiateAddingGroup
	 * @brief	Unit tests for the initiateAddingGroup function
	 * @return 	void
	 */
	public static void testInitiateAddingGroup(){
		boolean success;

		// Test that user must be logged in
		numTestsIssued++;
		try{
			success = ps.initiateAddingGroup(1, "Can I join your teaparty?");
			if(success){
				System.out.println("testInitiateGroup: FAILED login required");
			}
			else{
				numTestsPassed++;
				System.out.println("testInitiateGroup: PASSED login required");
			}
		}
		catch(Exception e){
			System.out.println("testInitiateGroup: FAILED login required");
		}

		try{
			ps.login("wifiinvolving@mail.com", "patrick");
		}
		catch(Exception e){
			System.out.println("[ error ] Check database, known login failed..");
			System.exit(-1);
		}

		// Test that message can't be empty
		numTestsIssued++;
		try{
			success = ps.initiateAddingGroup(1, "");
			if(success){
				System.out.println("testInitiateGroup: FAILED empty message");
			}
			else{
				numTestsPassed++;
				System.out.println("testInitiateGroup: PASSED empty message");
			}
		}
		catch(Exception e){
			System.out.println("testInitiateGroup: FAILED empty message");
		}

		// Test that message can't be too long
		numTestsIssued++;
		try{
			success = ps.initiateAddingGroup(1, "theabsolutelongestdescriptionintheknownuniverse!!!theabsolutelongestdescriptionintheknownuniverse!!!theabsolutelongestdescriptionintheknownuniverse!!!theabsolutelongestdescriptionintheknownuniverse!!!theabsolutelongestdescriptionintheknownuniverse!!!");
			if(success){
				System.out.println("testInitiateGroup: FAILED message too long");
			}
			else{
				numTestsPassed++;
				System.out.println("testInitiateGroup: PASSED message too long");
			}
		}
		catch(Exception e){
			System.out.println("testInitiateGroup: FAILED message too long");
		}

		// Test that group exists
		numTestsIssued++;
		try{
			success = ps.initiateAddingGroup(9000, "Its over 9000!");
			if(success){
				System.out.println("testInitiateGroup: FAILED bad group ID");
			}
			else{
				numTestsPassed++;
				System.out.println("testInitiateGroup: PASSED bad group ID");
			}
		}
		catch(Exception e){
			System.out.println("testInitiateGroup: FAILED bad group ID");
		}

		// Test known good query works
		numTestsIssued++;
		try{
			success = ps.initiateAddingGroup(1, "Can I join your teaparty please?");
			if(success){
				numTestsPassed++;
				System.out.println("testInitiateGroup: PASSED known good request");
			}
			else{
				System.out.println("testInitiateGroup: FAILED known good request");
			}
		}
		catch(Exception e){
			System.out.println("testInitiateGroup: FAILED known good request");
		}

		// Test that can't join a group as a member
		numTestsIssued++;
		try{
			success = ps.initiateAddingGroup(1, "Gimme some damn tea");
			if(success){
				System.out.println("testInitiateGroup: FAILED already in group");
			}
			else{
				numTestsPassed++;
				System.out.println("testInitiateGroup: PASSED already in group");
			}
		}
		catch(Exception e){
			System.out.println("testInitiateGroup: FAILED already in group");
		}

		// clean up DB
		try{
			PreparedStatement q = ps.get_conn().prepareStatement("delete from pendingGroupMember where userID=?");
			q.setInt(1, ps.get_activeUser().userID);
			q.executeUpdate();
		}
		catch(Exception e){
			System.out.println("[ error ] Database error, check pendingGroupMember");
		}
		ps.logout(0);


	} // end initiateAddingGroup


	/*
	 * @func	testLogin
	 * @brief	Unit tests for the login function
	 * @return	void
	 */
	public static void testLogin(){
		String email;
		String password;
		PittSocial.User u;

		//Test that empty password fails
		numTestsIssued++;
		try{
			email = "hourtherapy@mail.com";
			password = "";
			u = ps.login(email, password);
			if(u != null){
				System.out.println("testLogin: FAILED empty password");
			}
			else{
				numTestsPassed++;
				System.out.println("testLogin: PASSED empty password");
			}
		}
		catch(Exception e){
			System.out.println("testLogin: FAILED empty password");
		}

		// Test that empty email fails
		numTestsIssued++;
		try{
			email = "";
			password = "compliant";
			u = ps.login(email, password);
			if(u != null){
				System.out.println("testLogin: FAILED empty email");
			}
			else{
				numTestsPassed++;
				System.out.println("testLogin: PASSED empty email");
			}
		}
		catch(Exception e){
			System.out.println("testLogin: FAILED empty email");
		}

		// Test that known good credentials work
		numTestsIssued++;
		try{
			email = "hourtherapy@mail.com";
			password = "compliant";
			u = ps.login(email, password);
			if(u != null){
				numTestsPassed++;
				System.out.println("testLogin: PASSED good credentials");
				ps.logout(u.userID);
			}
		}
		catch(Exception e){
			System.out.println("testLogin: FAILED good credentials");
		}

		// Test that known bad credentials fail
		numTestsIssued++;
		try{
			email = "harryPotter@mail.com";
			password = "dobby";
			u = ps.login(email, password);
			if(u != null){
				System.out.println("testLogin: FAILED bad credentials");
			}
			else{
				numTestsPassed++;
				System.out.println("testLogin: PASSED bad credentials");
			}
		}
		catch(Exception e){
			System.out.println("testLogin: FAILED bad credentials");
		}

		// Test that cannot log in while already logged in...
		numTestsIssued++;
		try{
			email = "hourtherapy@mail.com";
			password = "compliant";
			u = ps.login(email, password);
			if(u != null){
				PittSocial.User u2 = ps.login(email, password);
				if(u2 != null){
					System.out.println("testLogin: FAILED double log in");
				}
				else{
					System.out.println("testLogin: PASSED double log in");
					numTestsPassed++;
				}
				ps.logout(u.userID);
			}
		}
		catch(Exception e){
			System.out.println("testLogin: FAILED double log in");
		}
	} // end testLogin


	/*
	 * @func	testLogout
	 * @brief	Unit tests for the logout function
	 * @return	void
	 */
	public static void testLogout(){
		int u;
		boolean success;
		if (ps.get_loggedIn()) {ps.logout(ps.get_activeUser().getUserID());}
		// Test that user is logged in before logging out
		numTestsIssued++;
		try{
			success = ps.logout(99);
			if(success){
				System.out.println("testLogout: FAILED logout without login");
			}
			else{
				System.out.println("testLogout: PASSED logout without login");
				numTestsPassed++;
			}
		}
		catch(Exception e){
			System.out.println("testLogout: FAILED logout without login");
		}

		// Test that bad userID fails
		numTestsIssued++;
		try{
			u = -1;
			success = ps.logout(u);
			if(success){
				System.out.println("testLogout: FAILED bad userID");
			}
			else{
				numTestsPassed++;
				System.out.println("testLogout: PASSED bad userID");
			}
		}
		catch(Exception e){
			System.out.println("testLogout: FAILED bad userID");
		}

		// Test that good userID succeeds
		numTestsIssued++;
		try{
			PittSocial.User u1 = ps.login("hourtherapy@mail.com", "compliant");
			success = ps.logout(u1.userID);
			if(success){
				System.out.println("testLogout: PASSED good userID");
				numTestsPassed++;
			}
			else{
				System.out.println("testLogout: FAILED good userID");
			}
		}
		catch(Exception e){
			System.out.println("testLogout: FAILED good userID");
		 }
	} // end testLogout


	/*
	 * @func	testSearchForUser
	 * @brief	Unit tests for the searchForUser function
	 * @return	void
	 */
	public static void testSearchForUser(){
		boolean success;
		// Test that empty string fails
		numTestsIssued++;
		try{
			success = ps.searchForUser("");
			if(success){
				System.out.println("testSearchForUser: FAILED empty search");
			}
			else{
				System.out.println("testSearchForUser: PASSED empty search");
				numTestsPassed++;
			}
		}
		catch(Exception e){
			System.out.println("testSearchForUser: FAILED empty search");
		}

		// Test that substrings that are too long fail
		numTestsIssued++;
		try{
			success = ps.searchForUser("thisisareallylongsearchstringanditsuckssooooooooobad");
			if(success){
				System.out.println("testSearchForUser: FAILED long search query");
			}
			else{
				System.out.println("testSearchForUser: PASSED long search query");
				numTestsPassed++;
			}
		}
		catch(Exception e){
			System.out.println("testSearchForUser: FAILED long search query");
		}


		// Test that known bad substrings fail
		numTestsIssued++;
		try{
			success = ps.searchForUser("ABC XYZ");
			if(success){
				System.out.println("testSearchForUser: FAILED known bad search");
			}
			else{
				System.out.println("testSearchForUser: PASSED known bad search");
				numTestsPassed++;
			}
		}
		catch(Exception e){
			System.out.println("testSearchForUser: FAILED known bad search");
		}

		// Test that known good substrings pass
		numTestsIssued++;
		try{
			success = ps.searchForUser("FRED ANTO therapy");
			if(success){
				numTestsPassed++;
				System.out.println("testSearchForUser: PASSED known good search");
			}
			else{
				System.out.println("testSearchForUser: FAILED known good search");
			}
		}
		catch(Exception e){
			System.out.println("testSearchForUser: FAILED known good search");
		}
	} // end testSearchForUser


	/*
	 * @func	testSendMessageToGroup
	 * @brief	Unit tests for the sendMessageToGroup function
	 * @return	void
	 */
	public static void testSendMessageToGroup() throws SQLException{
		boolean success;

		// Test that login is required
		numTestsIssued++;
		try{
			success = ps.sendMessageToGroup(11, "Testing 1");
			if(success){
				System.out.println("testSendMessageToGroup: FAILED login required");
			}
			else{
				numTestsPassed++;
				System.out.println("testSendMessageToGroup: PASSED login required");
			}
		}
		catch(Exception e){
				System.out.println("testSendMessageToGroup: FAILED login required");
		}

		try{
			ps.login("rogerssurname@pitt.edu", "detection");
		}
		catch(Exception e){
			System.out.println("[ error ] Check database, known login failed..");
			System.exit(-1);
		}

		// Test that bad group id fails
		numTestsIssued++;
		try{
			success = ps.sendMessageToGroup(-1, "Testing 2");
			if(success){
				System.out.println("testSendMessageToGroup: FAILED bad groupID");
			}
			else{
				numTestsPassed++;
				System.out.println("testSendMessageToGroup: PASSED bad groupID");
			}
		}
		catch(Exception e){
				System.out.println("testSendMessageToGroup: FAILED bad groupID");
		}

		// Test that empty message fails
		numTestsIssued++;
		try{
			success = ps.sendMessageToGroup(11, "");
			if(success){
				System.out.println("testSendMessageToGroup: FAILED empty message");
			}
			else{
				numTestsPassed++;
				System.out.println("testSendMessageToGroup: PASSED empty message");
			}
		}
		catch(Exception e){
				System.out.println("testSendMessageToGroup: FAILED empty message");
		}

		// Test that message too long fails
		numTestsIssued++;
		try{
			success = ps.sendMessageToGroup(11,"thisisalongmessage!thisisalongmessage!thisisalongmessage!thisisalongmessage!thisisalongmessage!thisisalongmessage!thisisalongmessage!thisisalongmessage!thisisalongmessage!thisisalongmessage!thisisalongmessage!thisisalongmessage!thisisalongmessage!thisisalongmessage!thisisalongmessage!thisisalongmessage!thisisalongmessage!thisisalongmessage!thisisalongmessage!thisisalongmessage!thisisalongmessage!");
			if(success){
				System.out.println("testSendMessageToGroup: FAILED message too long");
			}
			else{
				numTestsPassed++;
				System.out.println("testSendMessageToGroup: PASSED message too long");
			}
		}
		catch(Exception e){
				System.out.println("testSendMessageToGroup: FAILED message too long");
		}

		// Test that known good call succeeds
		numTestsIssued++;
		try{
			success = ps.sendMessageToGroup(11, "Testing sendMessageToGroup");
			if(success){
				numTestsPassed++;
				System.out.println("testSendMessageToGroup: PASSED known good call");
			}
			else{
				System.out.println("testSendMessageToGroup: FAILED known good call");
			}
		}
		catch(Exception e){
				System.out.println("testSendMessageToGroup: FAILED known good call");
		}

		// Test that cannot send message to group without membership
		numTestsIssued++;
		try{
			success = ps.sendMessageToGroup(2, "Testing sendMessageToGroup");
			if(success){
				System.out.println("testSendMessageToGroup: FAILED must be group member");
			}
			else{
				numTestsPassed++;
				System.out.println("testSendMessageToGroup: PASSED must be group member");
			}
		}
		catch(Exception e){
				System.out.println("testSendMessageToGroup: FAILED must be a group member");
		}

		// clean up DB
		try{
			PreparedStatement s1 = ps.get_conn().prepareStatement("select msgID from messageInfo where message=?;");
			s1.setString(1, "Testing sendMessageToGroup");
			ResultSet r1 = s1.executeQuery();
			r1.next();
			int msgID = r1.getInt("msgID");
			PreparedStatement s2 = ps.get_conn().prepareStatement("delete from messageRecipient where msgID=?;");
			s2.setInt(1, msgID);
			PreparedStatement s3 = ps.get_conn().prepareStatement("delete from messageInfo where msgID=?;");
			s3.setInt(1, msgID);
			s2.executeUpdate();
			s3.executeUpdate();
		}
		catch(SQLException e){
			ps.printSQLError(e);
			System.out.println("[ error ] Database error, check that messageRecipient cleaned up...");
			System.exit(-1);
		}
		ps.logout(1);

	} // end testSendMessageToGroup

	public static void testConfirmRequests(){
		int userID;

		System.out.println();
		numTestsIssued++;
		try{
			userID = 10;
			if(ps.confirmRequests(userID)){
				numTestsPassed++;
				System.out.println("testConfirmRequests: PASSED valid userID");
			}
			else{
				System.out.println("testConfirmRequests: FAILED valid userID");
			}
		}
		catch(Exception e){
			System.out.println("testConfirmRequests: FAILED valid userID");
		}

		System.out.println();
		numTestsIssued++;
		try{
			userID = 2; // 2 is an admin for group 2.
			if(ps.confirmRequests(userID)){
				numTestsPassed++;
				System.out.println("testConfirmRequests: PASSED valid group admin.");
			}
			else{
				System.out.println("testConfirmRequests: FAILED valid userID");
			}
		}
		catch(Exception e){
			System.out.println(e.getMessage());
			System.out.println("testConfirmReqeusts: FAILED valid userID");
		}

		System.out.println();
		numTestsIssued++;
		try{
			userID = -5;
			if(ps.confirmRequests(userID)){
				System.out.println("testConfirmRequests: FAILED invalid group admin.");
			}
			else{
				numTestsPassed++;
				System.out.println("testConfirmRequests: PASSED invalid userID");
			}
		}
		catch(Exception e){
			numTestsPassed++;
			//System.out.println(e.getMessage());
			System.out.println("testConfirmReqeusts: PASSED invalid userID");
		}
	}
	/*
	* 	@func testDisplayMessages
	*	@brief Unit tests for the testDisplayMessages function
	*	@return void
	*
	*/
	public static void testDisplayMessages(){
		int userID1;
		try { if (!ps.get_loggedIn()) {ps.login("rogerssurname@pitt.edu", "detection");} }
		catch (Exception e) {System.out.println("testDisplayMessages: FAILED TO INITIATE TESTS");}
		// test displaying messages for invalid userID
		numTestsIssued++;
		try{
			userID1 = -5;
			if(!ps.displayMessages(userID1)){
				System.out.println("testDisplayMessages: PASSED invalid userID");
			}
			else System.out.println("testDisplayMessages: FAILED invalid userID");
		}
		catch(Exception e){
			numTestsPassed++;
			System.out.println("testDisplayMessages: PASSED invalid userID");
		}

		// test displaying messages for a valid userID
		numTestsIssued++;
		try{
			userID1 = 17;
			ps.displayMessages(userID1);
			System.out.println("testDisplayMessages: PASSED valid userID");
			numTestsPassed++;
		}
		catch(Exception e){
			System.out.println("testDisplayMessages: FAILED valid userID");
		}
	}

	/*
	*
	*
	*
	*/
	public static void testDisplayNewMessages(){
		int userID1;
		try { if (!ps.get_loggedIn()) {ps.login("rogerssurname@pitt.edu", "detection");} }
		catch (Exception e) {System.out.println("testDisplayNewMessages: FAILED TO INITIATE TESTS");}
		// test displaying messages for invalid userID
		numTestsIssued++;
		try{
			userID1 = -5;
			if(ps.displayNewMessages(userID1)){
				System.out.println("testDisplayNewMessages: FAILED invalid userID");
			}
			else{
				numTestsPassed++;
				System.out.println("testDisplayNewMessages: PASSED invalid userID");
			}
		}
		catch(Exception e){
			numTestsPassed++;
			System.out.println("testDisplayNewMessages: PASSED invalid userID");
		}

		// test displaying messages for a valid userID
		numTestsIssued++;
		try{
			userID1 = 17;
			ps.displayNewMessages(userID1);
			System.out.println("testDisplayNewMessages: PASSED valid userID");
			numTestsPassed++;
		}
		catch(Exception e){
			ps.printSQLError((SQLException) e);
			System.out.println("testDisplayNewMessages: FAILED valid userID");
		}
	}
	/*
	* @func 	testInitiateFriendship()
	* @brief	Unit tests for the testInitiateFriendship function
	* @return 	void
	*/
	public static void testInitiateFriendship(){
		int userID1;
		int userID2;
		String message;
		try { if (!ps.get_loggedIn()) {ps.login("rogerssurname@pitt.edu", "detection");} }
		catch (Exception e) {System.out.println("testInitiateFriendShip: FAILED TO INITIATE TESTS");}
		// test that adding friendship between two users who are already friends fails
		numTestsIssued++;
		try{
			userID1 = 10;
			userID2	= 91;
			message = "Let's be friends.";
			if(!ps.initiateFriendship(userID1, userID2, message)){
				numTestsPassed++;
				System.out.println("testInitiateFriendship: PASSED already friends");
			}
			else System.out.println("testInitiateFriendship: FAILED already friends");
		}
		catch(Exception e){
			numTestsPassed++;
			System.out.println("testInitiateFriendship: PASSED already friends");
		}

		// Test adding friend who doesn't exist
		numTestsIssued++;
		try{
			userID1 = 10;
			userID2	= -5;
			message = "Let's be friends, buddy.";
			if(!ps.initiateFriendship(userID1, userID2, message)){
				numTestsPassed++;
				System.out.println("testInitiateFriendship: PASSED friend doesn't exist");
			}
			else System.out.println("testInitiateFriendship: FAILED friend doesn't exist");
		}
		catch(Exception e){
			numTestsPassed++;
			System.out.println("testInitiateFriendship: PASSED friend doesn't exist");
		}

		// Test adding friend who doesn't exist
		numTestsIssued++;
		try{
			userID1 = 10;
			userID2	= 20;
			message = "Let's be friends.";
			ps.initiateFriendship(userID1, userID2, message);
			System.out.println("testInitiateFriendship: PASSED valid insert");
			numTestsPassed++;

		}
		catch(Exception e){
			System.out.println("testInitiateFriendship: FAILED valid insert");
		}

	}

	public static void testDisplayFriends(){
		System.out.println("Starting testDisplayFriends");
		int userID;
		try { if (!ps.get_loggedIn()) {ps.login("rogerssurname@pitt.edu", "detection");} }
		catch (Exception e) {System.out.println("testDisplayFriends: FAILED TO INITIATE TESTS");}
		numTestsIssued++;
		try{
			userID = 10;
			if(ps.displayFriends(userID)){
				System.out.println("testDisplayFriends: PASSED valid userID");
				numTestsPassed++;
			}
			else{
				System.out.println("testDisplayFriends: FAILED valid userID");
			}
		}
		catch(Exception e){
			System.out.println(e.getMessage());
			System.out.println("testDisplayFriends: FAILED valid userID");
		}
	}


	/*
	 * @func	testThreeDegrees
	 * @brief	Unit tests for the threeDegrees function
	 * @return	void
	 */
	public static void testThreeDegrees(){
		boolean success;

		// Test that user must be logged in
		numTestsIssued++;
		try{
			success = ps.threeDegrees(5);
			if(success){
				System.out.println("testThreeDegrees: FAILED login required");
			}
			else{
				numTestsPassed++;
				System.out.println("testThreeDegrees: PASSED login required");
			}
		}
		catch(Exception e){
			System.out.println("testThreeDegrees: FAILED login required");
		}

		try{
			ps.login("wifiinvolving@mail.com", "patrick");
		}
		catch(Exception e){
			System.out.println("[ error ] Check database, known login failed..");
			System.exit(-1);
		}

		// Test that bad target fails
		numTestsIssued++;
		try{
			success = ps.threeDegrees(99999999);
			if(success){
				System.out.println("testThreeDegrees: FAILED bad target");
			}
			else{
				numTestsPassed++;
				System.out.println("testThreeDegrees: PASSED bad target");
			}
		}
		catch(Exception e){
			System.out.println("testThreeDegrees: FAILED bad target");
		}


		// Test that known nonexistent path succeeds
		numTestsIssued++;
		try{
			success = ps.threeDegrees(1);
			if(success){
				numTestsPassed++;
				System.out.println("testThreeDegrees: PASSED nonexistent path");
			}
			else{
				System.out.println("testThreeDegrees: FAILED nonexistent path");
			}
		}
		catch(Exception e){
			System.out.println("testThreeDegrees: FAILED nonexistent path");
		}


		// Test that known path length 1 succeeds
		numTestsIssued++;
		try{
			success = ps.threeDegrees(20);
			if(success){
				numTestsPassed++;
				System.out.println("testThreeDegrees: PASSED known path length 1");
			}
			else{
				System.out.println("testThreeDegrees: ");
			}
		}
		catch(Exception e){
			System.out.println("testThreeDegrees: FAILED known path length 1");
		}


		// Test that known path length 2 succeeds
		numTestsIssued++;
		try{
			success = ps.threeDegrees(18);
			if(success){
				numTestsPassed++;
				System.out.println("testThreeDegrees: PASSED known path length 2");
			}
			else{
				System.out.println("testThreeDegrees: FAILED known path length 2");
			}
		}
		catch(Exception e){
			System.out.println("testThreeDegrees: FAILED known path length 2");
		}


		// Test that known path length 3 succeeds
		numTestsIssued++;
		try{
			success = ps.threeDegrees(14);
			if(success){
				numTestsPassed++;
				System.out.println("testThreeDegrees: PASSED known path length 3");
			}
			else{
				System.out.println("testThreeDegrees: FAILED known path length 3");
			}
		}
		catch(Exception e){
			System.out.println("testThreeDegrees: FAILED known path length 3");
		}

		ps.logout(7);

	} // end testThreeDegrees


	/*
	 * @func 	testReport
	 * @brief	Prints out the results as a percentage of passed tests
	 * @return	void
	 */
	public static void testReport(){
		System.out.println("Unit test results: ");
		if(numTestsIssued > 0){
			System.out.println(
				numTestsPassed + "/" +
				numTestsIssued + " (" +
				(100.0*numTestsPassed)/numTestsIssued +
				"%)"
			);
		}
		else{
			System.out.println("No tests issued...");
		}
	}

	public static void showTableContents(relation r) throws SQLException
	{
		PreparedStatement st;
		ResultSet res;

		switch(r){
			case FRIEND:
				System.out.println("\nFRIEND: \n-------");
				st = ps.get_conn().prepareStatement("select * from friend order by userid1 asc");
				res = st.executeQuery();
				while(res.next()){
					System.out.printf("UserID 1: %s\t UserID2: %s\t Date: %s\n\tMessage: %s\n", res.getString(1), res.getString(2), res.getString(3), res.getString(4));
				}
				break;
			case GROUPINFO:
				System.out.println("\nGROUPINFO: \n------------");
				st = ps.get_conn().prepareStatement("select * from groupinfo order by gid asc");
				res = st.executeQuery();
				while(res.next()){
					System.out.printf("gID: %s\tName: %s\tCapacity: %s\n\tDescription: %s\n", res.getString(1), res.getString(2), res.getString(3), res.getString(4));
				}
				break;
			case GROUPMEMBER:
				System.out.println("\nGROUPMEMBER: \n------------");
				st = ps.get_conn().prepareStatement("select * from groupmember order by gid asc");
				res = st.executeQuery();
				while(res.next()){
					System.out.printf("gID: %s\tuserID: %s\trole: %s\n", res.getString(1), res.getString(2), res.getString(3));
				}
				break;
			case MESSAGEINFO:
				System.out.println("\nMESSAGEINFO: \n------------");
				st = ps.get_conn().prepareStatement("select * from messageinfo order by msgid asc");
				res = st.executeQuery();
				while(res.next()){
					System.out.printf("msgID: %s\tfromID: %s\ttoID: %s\ttoGroupID: %s\tTime Sent: %s\n\tMessage: %s\n", res.getString(1), res.getString(2), res.getString(4), res.getString(5), res.getString(6), res.getString(3));
				}
				break;
			case MESSAGERECIPIENT:
				System.out.println("\nMESSAGERECIPIENT: \n------------_");
				st = ps.get_conn().prepareStatement("select * from messagerecipient order by msgid asc");
				res = st.executeQuery();
				while(res.next()){
					System.out.printf("msgID: %s\tuserID: %s\n", res.getString(1), res.getString(2));
				}
				break;
			case PENDINGFRIEND:
				System.out.println("\nPENDINGFRIEND: \n------------");
				st = ps.get_conn().prepareStatement("select * from pendingfriend order by fromid asc");
				res = st.executeQuery();
				while(res.next()){
					System.out.printf("fromID: %s\t toID: %s\n\t Message: %s\n", res.getString(1), res.getString(2), res.getString(3));
				}
				break;
			case PENDINGGROUPMEMBER:
				System.out.println("\nPENDINGGROUPMEMBER: \n------------");
				st = ps.get_conn().prepareStatement("select * from pendinggroupmember order by gid asc");
				res = st.executeQuery();
				while(res.next()){
					System.out.printf("groupID: %s\t userID: %s\n\t Message: %s\n", res.getString(1), res.getString(2), res.getString(3));
				}
				break;
			case PROFILE:
				System.out.println("\nPROFILE: \n------------");
				st = ps.get_conn().prepareStatement("select * from profile order by userid asc");
				res = st.executeQuery();
				while(res.next()){
					System.out.printf("ID: %s\t name: %s\t email: %s\t password: %s\t DOB: %s\t lastLogin: %s\n", res.getString(1), res.getString(2), res.getString(3), res.getString(4), res.getString(5), res.getString(6));
				}
				break;
		}
	}
}
