--Clean up
drop table if exists profile CASCADE ;
drop table if exists friend CASCADE ;
drop table if exists pendingFriend CASCADE ;
drop table if exists messageInfo cascade;
drop table if exists messageRecipient cascade;
drop table if exists groupMember cascade;
drop table if exists pendingGroupMember cascade;
drop table if exists groupInfo cascade;
drop table if exists "group" cascade;
drop table if exists message cascade;
drop table if exists aGroup cascade;

commit;

create table profile(
    userID integer,
    name varchar(50) not null,
    email varchar(50) unique,
    password varchar(50) not null,
    date_of_birth date not null ,
    lastlogin timestamp not null,
    constraint pk_profile primary key (userID),
	constraint valid_dates check (date_of_birth < lastlogin)
);

create table friend(
    userID1 integer,
    userID2 integer,
    JDate date not null,
    message varchar(200) not null,
    constraint pk_friend primary key (userID1,userID2),
    constraint fk_friend_user1 foreign key (userID1) references profile (userID) deferrable,
    constraint fk_friend_user2 foreign key (userID2) references profile (userID) deferrable
);


create table pendingFriend(
    fromID integer,
    toID integer,
    message varchar(200) not null,
    constraint pk_pendingFriend primary key (fromID, toID),
    constraint fk_fromID foreign key (fromID) references profile(userID) deferrable,
    constraint fk_toID foreign key (toID) references profile(userID) deferrable
);

create table groupInfo
(
    gID integer not null,
    name varchar(50) not null,
    size integer not null,
    description varchar(200) not null,
    Constraint group_PK primary key (gID),
	constraint valid_limit check (size > 0)
);

create table messageInfo
(
    msgID  integer not null,
    fromID integer, --must allow nulls when try to dropUser
    message varchar(200) not null,
    toUserID integer, -- must allow nulls when try to dropUser
    toGroupID integer default null,
    timeSent timestamp not null,
    Constraint message_PK primary key (msgID),
    Constraint message_FK1 foreign key (fromID) references profile (userID) initially immediate deferrable,
    Constraint message_FK2 foreign key (toGroupID) references groupInfo(gID) initially immediate deferrable
);

create table messageRecipient
(
    msgID integer not null,
    userID integer not null,
    Constraint messageRecipient_PK primary key (msgID,userID),
    Constraint messageRecipient_FK1 foreign key (msgID) references messageInfo(msgID) initially immediate deferrable,
    Constraint messageRecipient_FK2 foreign key (UserID) references profile(userID)
);

create table groupMember
(
    gID integer not null,
    userID integer not null,
    role varchar(20) default 'not ADMIN',
    CONSTRAINT groupMember_PK primary key(gID, userID)
);

create table pendingGroupMember
(
    gID integer not null,
    userID integer not null,
    message varchar(200) not null,
    CONSTRAINT pendingGroupMember_PK primary key (gID, userID),
    CONSTRAINT pendingGroupMember_FK1 foreign key (gID) references groupInfo(gID) initially immediate deferrable,
    CONSTRAINT pendingGroupMember_FK2 foreign key (userID) references profile(userID) initially immediate deferrable
);
