insert into profile (userID, name, email, password, date_of_birth, lastlogin) values (1, 'Shenoda', 'shg@pitt.edu', 'shpwd', '1997-10-13', '2019-10-09 15:00:00');
insert into profile (userID, name, email, password, date_of_birth, lastlogin) values (2, 'Lory', 'lra@pitt.edu', 'lpwd', '1996-03-08', '2019-10-09 16:00:00');
insert into profile (userID, name, email, password, date_of_birth, lastlogin) values (3, 'Peter', 'pdj@pitt.edu', 'ppwd', '1994-01-09', '2019-10-10 15:00:00');
insert into profile (userID, name, email, password, date_of_birth, lastlogin) values (4, 'Alexandrie', 'alx@pitt.edu', 'apwd', '1995-08-21', '2019-10-10 16:00:00');
insert into profile (userID, name, email, password, date_of_birth, lastlogin) values (5, 'Panickos', 'pnk@pitt.edu', 'kpwd', '1997-09-08', '2019-10-11 15:00:00');
insert into profile (userID, name, email, password, date_of_birth, lastlogin) values (6, 'Socratis', 'soc@pitt.edu', 'spwd', '1991-05-17', '2019-10-11 16:00:00');
insert into profile (userID, name, email, password, date_of_birth, lastlogin) values (7, 'Yaw', 'yaw@pitt.edu', 'ypwd', '1997-02-27', '2019-10-12 15:00:00');

insert into friend (userID1, userID2, JDate, message) values (1,2, '2019-01-06', 'Hey, it is me  Shenoda!' );
insert into friend (userID1, userID2, JDate, message) values (1,5, '2019-01-15', 'Hey, it is me  Shenoda!');
insert into friend (userID1, userID2, JDate, message) values (2,3, '2019-08-23', 'Hey, it is me  Lory!');
insert into friend (userID1, userID2, JDate, message) values (2,4, '2019-02-17', 'Hey, it is me  Lory!');
insert into friend (userID1, userID2, JDate, message) values (3,4, '2019-09-16', 'Hey, it is me  Peter!');
insert into friend (userID1, userID2, JDate, message) values (4,6, '2019-10-06', 'Hey, it is me  Alexandrie!');
insert into friend (userID1, userID2, JDate, message) values (6,7, '2019-09-13', 'Hey, it is me  Socratis!');

insert into pendingFriend (fromID, toID, message) values (7,4, 'Hey, it is me Yaw');
insert into pendingFriend (fromID, toID, message) values (5,2, 'Hey, it is me Panickos');
insert into pendingFriend (fromID, toID, message) values (2,6, 'Hey, it is me Lory');

insert into groupInfo (gID, name, size, description) values (1, 'Grads at CS', 100, 'list of all graduate students');
insert into groupInfo (gID, name, size, description) values (2, 'DB Group', 10, 'members of the ADMT Lab');

insert into groupMember (gID, userID, role) values (1,1, 'manager');
insert into groupMember (gID, userID, role) values (1,2, 'member');
insert into groupMember (gID, userID, role) values (1,3, 'member');
insert into groupMember (gID, userID, role) values (1,4, 'member');
insert into groupMember (gID, userID, role) values (1,5, 'member');
insert into groupMember (gID, userID, role) values (1,6, 'member');
insert into groupMember (gID, userID, role) values (1,7, 'member');
insert into groupMember (gID, userID, role) values (2,1, 'manager');
insert into groupMember (gID, userID, role) values (2,2, 'member');
insert into groupMember (gID, userID, role) values (2,5, 'member');

insert into messageInfo (msgID, fromID, message, ToUserID, ToGroupID, timeSent) values (1,1, 'Are we meeting tomorrow for the project?', 2, NULL, '2019-12-01 15:00:00');
insert into messageInfo (msgID, fromID, message, ToUserID, ToGroupID, timeSent) values (2,1, 'Peters pub tomorrow?', 5, NULL, '2019-11-01 16:00:00');
insert into messageInfo (msgID, fromID, message, ToUserID, ToGroupID, timeSent) values (3,2, 'Please join our DB Group forum tomorrow', NULL, 1, '2019-10-10 15:00:00');
insert into messageInfo (msgID, fromID, message, ToUserID, ToGroupID, timeSent) values (4,5, 'Here is the paper I will present tomorrow', NULL, 2, '2019-10-10 16:00:00');

insert into messageRecipient (msgID, userID) values (1,2);
insert into messageRecipient (msgID, userID) values (2,5);
insert into messageRecipient (msgID, userID) values (3,1);
insert into messageRecipient (msgID, userID) values (3,2);
insert into messageRecipient (msgID, userID) values (3,3);
insert into messageRecipient (msgID, userID) values (3,4);
insert into messageRecipient (msgID, userID) values (3,5);
insert into messageRecipient (msgID, userID) values (3,6);
insert into messageRecipient (msgID, userID) values (3,7);
insert into messageRecipient (msgID, userID) values (4,1);
insert into messageRecipient (msgID, userID) values (4,2);
insert into messageRecipient (msgID, userID) values (4,5);

