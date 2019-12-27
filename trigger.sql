--create triggers

-- returns true if two users are friends, regardless of the order in which their friendship is stored in the friend relation
create or replace function are_friends(u1 integer, u2 integer)
    returns boolean AS
    $$
    declare
        are_friends boolean := false;
    begin
        select (count(*)>0) into are_friends
            from friend f
            where (f.userID1 = $1 and f.userid2 = $2) or (f.userid1 = $2 and f.userid2 = $1);
        return are_friends;
    end;
    $$ language plpgsql;

-- Given a userID, returns a table of the userIDs of their friends
-- @param u An integer representing the userID to fetch friends for
create or replace function getfriends(u int) returns table(userId int) as
$$
    begin
        return query select * from (
                              (select f.userID1 from friend f where f.userID2 = u)
                              union
                              (select f2.userID2 from friend f2 where f2.userID1 = u)
                      ) ret;
    end;
$$ language plpgsql;

-- returns true if a pending friend request exists from u1 to u2
create or replace function isPending(u1 integer, u2 integer)
    returns boolean AS
    $$
    declare
        isPending boolean := false;
    begin
        select (count(*)>0) into isPending
            from pendingFriend pf
            where (pf.fromid = $1 and pf.toid = $2);
        return isPending;
    end;
    $$ language plpgsql;

create or replace function add_friends()
returns trigger as
    $$
    begin
        if (not(are_friends(new.userid1, new.userid2)))
            then return new;
        else
            raise exception 'userid1 and userid2 are already friends';
        end if;
    end;
    $$ language plpgsql;


drop trigger if exists friendTrig on friend;
create trigger friendTrig
    before insert
    on friend
    for each row
    execute procedure add_friends();

create or replace function add_pendingFriends()
returns trigger as
    $$
    declare exception text default '';
    begin
        if (not(are_friends(new.fromID, new.toID)) and not(isPending(new.toID, new.fromID)))
            then return new;
        elseif (isPending(new.toid, new.fromid))
            then
                insert into friend values (new.toid, new.fromid, current_timestamp, new.message);
                delete from pendingfriend where fromid = new.toid and toid = new.fromid;
                return null;
        else if(are_friends(new.fromid, new.toid))
            then raise exception 'Those users are already friends';
        end if;
        end if;
    END;
    $$ language plpgsql;


drop trigger if exists pendingFriendTrig on pendingFriend;
create trigger pendingFriendTrig
    before insert
    on pendingFriend
    for each row
    execute procedure add_pendingFriends();

create or replace function func_create_msg_Recipient() returns trigger as
$$
begin
        if new.toUserId is not null then
         insert into messageRecipient values(new.msgID, new.toUserID);
        else
         create or replace view group_msg as
         select a.msgId, b.userId
         from messageInfo a join groupMember b on a.toGroupId = b.gID;
         insert into messageRecipient select * from group_msg where msgId = new.msgID;
        end if;
    return new;
end;
$$ language plpgsql;

drop trigger if exists tri_messageRecipient on messageInfo;
create trigger tri_messageRecipient
    AFTER
        insert
    on messageInfo
    for each row
    execute procedure func_create_msg_Recipient();

create or replace function check_group_limit() returns trigger as
    $$
    declare
        group_size integer = (select count(userID) from groupMember where gID = new.gID) + 1;
    begin
        --count how many groupmmebers the group has
        if ( group_size > (select size from groupInfo where gID = new.gID))
            then raise exception 'cannot add to the group because the group is at maximum capacity';
        else
            return new;
        end if;
        --stop the insert from happening if there is not enough space for the new guy
    end;
    $$ language plpgsql;

drop trigger if exists tri_group_Limit on groupMember;
Create trigger tri_group_Limit
    Before
        insert
    on GroupMember
    for each row
execute procedure check_group_limit();

create or replace function remove_profile() returns trigger as
    $$
    begin
        --get msgIDs where the toUser is is the old.userID
        delete from messageRecipient where userID = old.userID;
        delete from messageRecipient where msgid in (select msgid from messageInfo where fromID = old.userID);
        update messageInfo set toUserID = null where toUserID = old.userID;
        update messageInfo set fromID = null where fromID = old.userID;
        delete from pendingfriend where fromID = old.userID;
        delete from pendingfriend where toID = old.userID;
        delete from groupMember where userID = old.userID;
        delete from pendingGroupmember where userID = old.userID;
        delete from friend where userID1 = old.userID;
        delete from friend where userID2 = old.userID;
        --stop the insert from happening if there is not enough space for the new guy
        return old;
    end;
    $$ language plpgsql;

-- Returns true if the user is already associated with the group in the groupInfo
-- table
-- @param u_ID int  The userID of the inserted tuple on pendingGroupMember
-- @param g_ID int  The gID of the inserted tuple on pendingGroupMember
create or replace function isInGroup(u_ID int, g_ID int) returns boolean as
    $$
    declare
        in_group boolean := false;
    begin
        select (count(*) > 0) into in_group
        from groupMember g
        where g.gID = g_ID and g.userID = u_ID;
        return in_group;
    end;
    $$ language plpgsql;

-- Trigger-style function to check that a user is not in a group before adding
-- it to the pendingGroupMember table
create or replace function addGroup() returns trigger as
    $$
    begin
        if(not(isInGroup(new.userID, new.gID))) then
            return new;
        end if;
        raise exception 'The user already belongs to this group!';
    end;
    $$ language plpgsql;

-- Trigger to validate that a user is not already in a group before adding an
-- entry into the pendingGroupmember table
drop trigger if exists groupTrig on pendingGroupMember;
create trigger groupTrig
    before insert on pendingGroupMember
    for each row
    execute procedure  addGroup();


drop trigger if exists tri_remove_profile on profile;
create trigger tri_remove_profile
    before delete
    on profile
    for each row
    execute procedure remove_profile();


-- Check that a user was born before a given date
create or replace function bornBefore(d date, uid integer) returns boolean as
$$
declare
    dob date;
begin
    select date_of_birth into dob
    from profile
    where userID = uid;
    return (dob < d);
end;
$$ language plpgsql;


-- Check that two users became friends after both were born
create or replace function saneDates() returns trigger as
$$
begin
    if( bornBefore(new.JDate, new.userID1) and bornBefore(new.JDate, new.userID2)) then
        return new;
    end if;
    raise exception 'Date error: one of these users was not born on the date of this friendship.';
end;
$$ language plpgsql;

drop trigger if exists tri_check_friend_dates on friend;
create trigger tri_check_friend_dates
before insert on friend
for each row
    execute procedure saneDates();


-- Searches the profile table for any names or emails that match any of the search strings provided
-- @param patterns  An array of varchar(50) types representing valid LIKE argument strings
create or replace function search_profile( patterns varchar(50)[] ) returns refcursor as
$$
    declare
        ret refcursor;
    begin
        OPEN ret FOR(
            (
                SELECT *
                FROM profile p
                WHERE p.email LIKE ANY (patterns)
            ) UNION
            (
                SELECT *
                FROM profile p2
                WHERE p2.name LIKE ANY (patterns)
            )
        );
        return ret;
    end;
$$ language plpgsql;


-- Given a userID, returns a table of the userIDs of their friends
-- @param u	An integer representing the userID to fetch friends for
create or replace function get_friends(u int) returns table(userId int) as
$$
    begin
        return query select * from (
                              (select f.userID1 from friend f where f.userID2 = u)
                              union
                              (select f2.userID2 from friend f2 where f2.userID1 = u)
                      ) ret;
    end;
$$ language plpgsql;


-- Attempts to find a path from the given source user to the given target user with at most 3 degrees of separation
-- @param src The userID of the source user
-- @param target The userID of the target user
-- @return a String representing the hops on success, or a string indicating failure otherwise.
create or replace function path_3_hops( src int, target int ) returns varchar as
$$
    declare
        -- Store user names along the path
		user1 varchar(50);
        user2 varchar(50);
        user3 varchar(50);
        user4 varchar(50);
		-- Intermediate indices as we traverse nodes
        outerUID integer;
        innerUID integer;
        inner2UID integer;
		-- Track the shortest path as we go
        shortestSeen integer := 10;
        ret varchar(256) := 'No path found for the given users';
		-- parameterized cursors for selecting nodes
        outerC CURSOR (srcouter integer) IS select * from getFriends(srcouter);
        innerC CURSOR (srcinner integer) IS select * from getFriends(srcinner);
        inner2C CURSOR (srcinner2 integer) IS select * from getFriends(srcinner2);
   begin
        -- Pseudocode:
        -- For each of the source's friends f:
        --   if the fi is the target, return source->target
        --   else, for each of the friends of fi f2 (not including fi):
        --      if the f2i is the target, return source->f1i->f2i
        --      else, for each of the friends of f2i f3 (not including f2i):
        --          if the f3i is the target, return source->f1i->f2i->f3i
        -- Modification:  If we track the shortest seen, and only update the
        -- return if we encounter a shorter path, then we will always return
        -- the shortest path if it exists.  However, this will always incur
        -- the worst-case complexity as we will traverse all three degrees.
        OPEN outerC(src);
        LOOP
            fetch outerC into outerUID;
            if not found then exit; end if;

            if outerUID = target then
                -- Case degree 1, output and exit
                select name from profile where userID = src into user1;
                select name from profile where userID = outerUID into user2;
                shortestSeen := 1;
                ret := user1 || '->' || user2;
                raise notice '%', ret;
				-- return ret;  -- short-circuit whenever we find a valid path
            else
                OPEN innerC(outerUID);
                loop
                    fetch innerC into innerUID;
                    if not found then exit; end if;

                    if innerUID = outerUID or innerUID = src then
                        continue;
                    elsif innerUID = target then
                        if shortestSeen > 2 then
                            -- Case degree 2
                            select name from profile where userID = src into user1;
                            select name from profile where userID = outerUID into user2;
                            select name from profile where userID = innerUID into user3;
                            shortestSeen := 2;
                            ret := user1 || '->' || user2 || '->' || user3;
                            raise notice '%', ret;
							-- return ret;  -- short-circuit whenever we find a valid path
                        end if;
                    else
                        OPEN inner2C(innerUID);
                        loop
                            fetch inner2C into inner2UID;
                            if not found then exit; end if;

                            if inner2UID = innerUID or inner2UID = outerUID or inner2UID = src then
                                continue;
                            elsif inner2UID = target then
                                if shortestSeen > 3 then
                                    select name from profile where userID = src into user1;
                                    select name from profile where userID = outerUID into user2;
                                    select name from profile where userID = innerUID into user3;
                                    select name from profile where userID = inner2UID into user4;
                                    shortestSeen := 3;
                                    ret := user1 || '->' || user2 || '->' || user3 || '->' || user4;
                                    raise notice '%', ret;
									-- return ret;  -- short-circuit whenever we find a valid path
                                end if;
                            end if;

                        end loop; -- end second inner loop
                        close inner2C;
                    end if;
                end loop; -- end first inner loop
                close innerC;
            end if;
        end loop; -- end outer loop
        close outerC;
        return ret;
    end;
$$ language plpgsql;

-- Adds a new group message to messageInfo and triggers the messageRecipient
create or replace function add_group_message(fromUser int, msg varchar(200), toGroup int) returns void as
$$
    declare
        newID integer;
    begin
        set transaction read write;
        select msgID from messageInfo order by msgID desc limit 1 into newID;
        newID := newID + 1;
        insert into messageInfo values(newID, fromUser, msg, NULL, toGroup, now());
    end;
$$ language plpgsql;
