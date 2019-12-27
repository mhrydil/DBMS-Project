# Helpers

Helper scripts and text dictionaries for generating random data.

## dict.txt
A list of the 10k most common English words, with words shorter than 3 and
longer than 10 removed.  [Credit to Google's Trillion Word Corpus](https://github.com/first20hours/google-10000-english)

## firstnames.txt
A list of 2000 random first names.
[Credit to this course website,](http://www.ics.uci.edu/~harris/python/femalenames.txt)
[Credit to this course website,](http://www.ics.uci.edu/~harris/python/malenames.txt)

## surnames.txt
A list of 1000 random last names.
[Credit to this course website,](http://www.ics.uci.edu/~harris/python/surnames.txt)

## makeInserts.py
A Python 3 script which uses the above dictionaries to generate valid data for
the database: 100 users, 200 friend relationships, and 300 messages, of which
200 were used to initiate friendships.

## inserts.sql
A text file with data inputs for our database.  Note that these are all valid,
and do not include our test cases for verifying semantics.
