In MySQL, date and time handling is done using several built-in data types and functions. Here are some key concepts and examples:

### Data Types

1. **DATE**: Stores date values in `YYYY-MM-DD` format.
2. **DATETIME**: Stores date and time values in `YYYY-MM-DD HH:MI:SS` format.
3. **TIMESTAMP**: Stores date and time values with timezone information.
4. **TIME**: Stores time values in `HH:MI:SS` format.
5. **YEAR**: Stores year values in `YYYY` format.

### Basic Operations

#### Creating Tables with Date/Time Columns

```sql
CREATE TABLE events (
    event_id INT AUTO_INCREMENT PRIMARY KEY,
    event_name VARCHAR(100),
    event_date DATE,
    event_time TIME,
    event_datetime DATETIME
);
```

#### Inserting Data

```sql
INSERT INTO events (event_name, event_date, event_time, event_datetime)
VALUES ('Conference', '2024-07-10', '14:30:00', '2024-07-10 14:30:00');
```

#### Querying Data

```sql
SELECT * FROM events WHERE event_date = '2024-07-10';
SELECT * FROM events WHERE event_datetime BETWEEN '2024-07-10 00:00:00' AND '2024-07-10 23:59:59';
```

### Functions

#### Date and Time Functions

1. **NOW()**: Returns the current date and time.
   ```sql
   SELECT NOW();
   ```

2. **CURDATE()**: Returns the current date.
   ```sql
   SELECT CURDATE();
   ```

3. **CURTIME()**: Returns the current time.
   ```sql
   SELECT CURTIME();
   ```

4. **DATE()**: Extracts the date part of a datetime.
   ```sql
   SELECT DATE('2024-07-10 14:30:00');
   ```

5. **TIME()**: Extracts the time part of a datetime.
   ```sql
   SELECT TIME('2024-07-10 14:30:00');
   ```

6. **YEAR(), MONTH(), DAY()**: Extracts the year, month, or day from a date.
   ```sql
   SELECT YEAR('2024-07-10'), MONTH('2024-07-10'), DAY('2024-07-10');
   ```

7. **HOUR(), MINUTE(), SECOND()**: Extracts the hour, minute, or second from a time.
   ```sql
   SELECT HOUR('14:30:00'), MINUTE('14:30:00'), SECOND('14:30:00');
   ```

8. **ADDDATE(), ADDTIME()**: Adds a specified interval to a date or time.
   ```sql
   SELECT ADDDATE('2024-07-10', INTERVAL 5 DAY);
   SELECT ADDTIME('14:30:00', '02:00:00');
   ```

9. **DATEDIFF()**: Returns the number of days between two dates.
   ```sql
   SELECT DATEDIFF('2024-07-15', '2024-07-10');
   ```

10. **TIMEDIFF()**: Returns the difference between two times.
    ```sql
    SELECT TIMEDIFF('16:30:00', '14:30:00');
    ```

#### Formatting Dates and Times

1. **DATE_FORMAT()**: Formats a date according to the specified format.
   ```sql
   SELECT DATE_FORMAT('2024-07-10 14:30:00', '%W, %M %d, %Y');
   ```

2. **TIME_FORMAT()**: Formats a time according to the specified format.
   ```sql
   SELECT TIME_FORMAT('14:30:00', '%h:%i %p');
   ```

### Example Use Case

Suppose you have an `events` table and you want to find events that occur in the current month:

```sql
SELECT * FROM events
WHERE YEAR(event_date) = YEAR(CURDATE())
AND MONTH(event_date) = MONTH(CURDATE());
```

To find all events that occur on weekends:

```sql
SELECT * FROM events
WHERE DAYOFWEEK(event_date) IN (1, 7);
```

These examples cover the basics of handling date and time in MySQL. You can combine these functions and concepts to perform more complex queries and manipulations as needed.
