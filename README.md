

## Database Setup
Set up the Database in postgresSQL by running the schema, trigger, and insert files.
```shell
postgres -h localhost -U postgres
postgres=# \i schema.sql
postgres=# \i trigger.sql
postgres=# \i insert.sql
```

## Building the Driver
Compile Driver.java.
### Windows
```shell
javac -cp "postgresql-42.2.8.jar;." Driver.java
```
### Mac
```shell
javac -cp postgresql-42.2.8.jar Driver.java Pittsocial.java
```

## Running the Driver
### Windows
```shell
java -cp "postgresql-42.2.8.jar;." Driver
```
### Mac
```shell
java -cp postgresql-42.2.8.jar:. Driver
```

## Unit Testing
Invoke the driver with the argument "--test" to run the unit tests.  This must
be run on a clean database setup, i.e. after running the instructions in the
Database Setup section above.
### Windows
```
java -cp "postgresql-42.2.8.jar;." Driver --test
```

### Mac
```shell
java -cp postgresql-42.2.8.jar:. Driver --test
```


