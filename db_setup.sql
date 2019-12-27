--Clean up
drop table if exists message cascade;
drop table if exists messageRecipient cascade;
--drop table if exists profile cascade;
drop table if exists groupMember cascade;
drop table if exists pendingGroupMember cascade;
drop table if exists aGroup cascade;

commit;

--Create tables
-- create table profile
-- (
--     userID integer NOT NULL,
--     name varchar(50),
--     email varchar(50),
--     password varchar(50),
--     date_of_birth date,
--     lastlogin timestamp,
--     CONSTRAINT profile_PK primary key (userID)
-- );

create table aGroup
(
    gID integer NOT NULL,
    name varchar(50),
    aLimit integer,
    description varchar(200),
    Constraint group_PK primary key (gID)
);

create table message
(
    msgID  integer NOT NULL,
    fromID integer,
    message varchar(200),
    toUserID integer default NULL,
    toGroupID integer default NULL,
    timeSent timestamp,
    Constraint message_PK primary key (msgID),
    Constraint message_FK1 foreign key (fromID) references profile (userID) initially immediate deferrable,
    Constraint message_FK2 foreign key (toGroupID) references aGroup(gID) initially immediate deferrable
);

create table messageRecipient
(
    msgID integer NOT NULL,
    userID integer,
    Constraint messageRecipient_PK primary key (msgID),
    Constraint messageRecipient_FK1 foreign key (msgID) references message(msgID) initially immediate deferrable
    --Constraint messageRecipient_FK2 foreign key (UserID) references message(toUserID)
);

create table groupMember
(
    gID integer not NULL,
    userID integer not NULL,
    role varchar(20) default 'NOT ADMIN',
    CONSTRAINT groupMember_PK primary key(gID, userID)
);

create table pendingGroupMember
(
    gID integer NOT NULL,
    userID integer NOT NULL,
    message varchar(200),
    CONSTRAINT pendingGroupMember_PK primary key (gID, userID),
    CONSTRAINT pendingGroupMember_FK1 foreign key (gID) references aGroup(gID) initially immediate deferrable,
    CONSTRAINT pendingGroupMember_FK2 foreign key (userID) references profile(userID) initially immediate deferrable
)

--create trigger
create or replace function func_create_msg_Recipient() returns trigger as
$$
begin
    insert into messageRecipient values (new.msgID , new.toUserID);
    return new;
end;
$$ language plpgsql;

drop trigger if exists tri_messageRecipient on message;
CREATE TRIGGER tri_messageRecipient
    AFTER
        INSERT
    ON message
    FOR EACH ROW
execute procedure func_create_msg_Recipient();
