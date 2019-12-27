"""
makeInserts.py

Helper script to auto-generate randomized inserts for user profiles, friend
relationships, and messages.

Notes:
    I assume everyone joined before 2016, sent friend request messages in 2016,
    became friends in 2017, sent other messages in 2018, and logged in last in
    2019.  Thus all the dates are consistent for having joined before sending
    messages, having sent friend request messages before becoming friends,
    having sent additional messages after friends are established, and all of
    the above before the last login.
"""
import random
from itertools import combinations

fnames = None
lnames = None
passwords = None
dobs = None
words = None
emails = set()
years = [ str(x) for x in range(1975, 2001) ]
months = [ str(x).zfill(2) for x in range(1, 13) ]
days = [ str(x).zfill(2) for x in range(1,28) ]
domains = ['@mail.com','@yahoo.com','@gmail.com','@outlook.com','@pitt.edu']

def getDate(year=None):
    """
    Build a date string in SQL format YYYY-MM-DD
    """
    if(year is None):
        return '-'.join( [random.choice(years), random.choice(months), random.choice(days)] )

    return '-'.join( [year, random.choice(months), random.choice(days)] )

def getEmail():
    """
    Build an email string as two dictionary words and a domain concatenated.
    """
    return ''.join( [random.choice(words), random.choice(words), random.choice(domains)] )

def getMessage():
    """
    Build a message from dictionary words and a template
    """
    templates = [
            f"Hey, I saw {random.choice(words)} {random.choice(words)} and it reminded me of {random.choice(words)}.",
            f"What are you doing tonight?  I found this {random.choice(words)} {random.choice(words)} I want to try!",
            f"Did you get the {random.choice(words)} I sent you?",
            f"Check out the {random.choice(words)}! #{random.choice(words)}{random.choice(words)}",
            f"What do you want for your birthday?  What about {random.choice(words)} {random.choice(words)}?",
            f"My dog just ate {random.choice(words)} {random.choice(words)} and it is all your fault.",
            f"Today in cs1555 we talked about {random.choice(words)}.  In Oracle, we use {random.choice(words)}, but in PostgreSQL we use {random.choice(words)}."
    ]
    return random.choice(templates)

def getName():
    """
    Build a name as the concatenation of a first name and a last name
    """
    return ' '.join( [random.choice(fnames), random.choice(lnames)] )

def getTimeStamp(year):
    """
    Build a timestamp string in year using getDate output
    """
    d = getDate()
    return year + d[4:] + ' 00:00:00'

def genSQLProfile(uid, name, email, password, dob, lastlogin):
    """
    Composes a SQL insert query for the profile table
    """
    return f"insert into profile values({uid},'{name}','{email}','{password}',to_date('{dob}','YYYY-MM-DD'),to_timestamp('{lastlogin}', 'YYYY-MM-DD HH24:MI:SS'));\n"

def genSQLFriend(id1, id2, date, message):
    """
    Composes a SQL insert query for the friend table
    """
    return f"insert into friend values({id1},{id2},to_date('{date}','YYYY-MM-DD'),'{message}');\n"

def genSQLGroup(gid, uid, name, limit, description):
    """
    Composes two SQL queries to create a new group and associate the group
    administrator
    """
    return f"insert into agroup values({gid},'{name}',{limit},'{description}');\ninsert into groupMember values({gid},{uid},'manager');\n"

def genSQLMessage(mid, id1, m, id2, gid, time):
    """
    Composes a SQL insert query for the message table
    """
    return f"insert into message values({mid},{id1},'{m}',{id2},{gid},to_timestamp('{time}', 'YYYY-MM-DD HH24:MI:SS'));\n"

with open('surnames.txt', 'r') as inp:
    lnames = [ s.strip() for s in inp.readlines() ]

with open('firstnames.txt', 'r') as inp:
    fnames = [ s.strip() for s in inp.readlines() ]

with open('dict.txt', 'r') as inp:
    words = [ s.strip() for s in inp.readlines() ]

dobs = [ getDate() for x in range(100) ]
while len(emails) < 100:
    x = getEmail()
    if len(x) < 50:
        emails.add(x)

emailList = list(emails)
profiles = []
interactions = list(combinations(range(100), 2))
random.shuffle(interactions)    # Unique non-ordered userID pairs
friends = []                    # Friend table tuples
messages = []                   # Message table tuples
relationships = []              # Existing friendships for messages
groups = []                     # Groups founded by the first 10 users

# Generate user profiles
for i in range(100):
    profiles.append(
        genSQLProfile(i, getName(), emailList[i], random.choice(words), random.choice(dobs), getTimeStamp('2019'))
    )

# Using the possible combinations of interactions, generate initial messages
# and friendships that are consistent with last logins
for i in range(200):
    m = getMessage()
    # These are not tracked in the messages table per Costas
    #messages.append(
    #    genSQLMessage(i,interactions[i][0],m,interactions[i][1],'NULL',getTimeStamp('2016'))
    #)
    friends.append(
        genSQLFriend( interactions[i][0], interactions[i][1], getDate('2017'),m)
    )
    relationships.append(interactions[i])

# Generate the remaining 100 messages from existing friend relationships
for i in range(300):
    m = getMessage()
    a,b = random.choice(relationships)
    messages.append(
        genSQLMessage(i, a, m, b, 'NULL', getTimeStamp('2018'))
    )

# Generate groups
#def genSQLGroup(gid, uid, name, limit, description):
groups.append(genSQLGroup(1, 1, 'dblovers', 10, 'We love databases.'))
groups.append(genSQLGroup(2, 2, 'catfolk', 10, 'We love cats.'))
groups.append(genSQLGroup(3, 3, 'catssuck', 10, 'We hate cats.'))
groups.append(genSQLGroup(4, 4, 'yinzers', 10, 'Yinz all are welcome here. No jagoffs allowed.'))
groups.append(genSQLGroup(5, 5, 'OracleAllStars', 10, 'Welcome to the dark side of the force.'))
groups.append(genSQLGroup(6, 6, 'PostGres4Lyfe', 10, 'The best DBMS there is.'))
groups.append(genSQLGroup(7, 7, 'SteelersFans', 10, 'Black and yellow.'))
groups.append(genSQLGroup(8, 8, 'PittCS', 10, 'Computer science for Pitt students.'))
groups.append(genSQLGroup(9, 9, 'PittCS Faculty', 10, 'Faculty network for Pitt CS department.'))
groups.append(genSQLGroup(10, 10, 'PennState', 10, 'Place for idiots to argue with one another.'))


# Dump to inserts file
with open('inserts.sql', 'w') as op:
    for p in profiles:
        op.write(p)

    for m in messages:
        op.write(m)

    for f in friends:
        op.write(f)

    for g in groups:
        op.write(g)

