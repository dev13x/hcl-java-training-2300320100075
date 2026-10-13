# Assessment 2: Library Management System

**HCL Tech Training | Java Full Stack Development**  
**Content © ABES Engineering College**

---

## 1. Student Information
- **Name:** Dev Awasthi
- **Roll Number:** 2300320100075
- **Batch:** Java Full Stack Development (Assessment 2 - Java API)
- **Date:** 13 October 2026

---

## 2. How to Compile and Run

### Compile
Run this command from the project root:
```powershell
javac -d Assignment/bin (Get-ChildItem -Recurse Assignment/src/*.java | Select-Object -ExpandProperty FullName)
```

Or standard javac:
```bash
javac -d Assignment/bin Assignment/src/com/hcl/library/model/*.java Assignment/src/com/hcl/library/exception/*.java Assignment/src/com/hcl/library/repository/*.java Assignment/src/com/hcl/library/util/*.java Assignment/src/com/hcl/library/service/*.java Assignment/src/com/hcl/library/Main.java
```

### Run
```powershell
java -cp Assignment/bin com.hcl.library.Main
```

---

## 3. Design Note & Class Relationships

### Classes and Interfaces:
1. **`Borrowable` (interface):** Declares `isAvailable()`, `borrow()`, and `giveBack()`.
2. **`LibraryItem` (abstract class):** Implements `Borrowable` and `Comparable<LibraryItem>`. Contains base fields (`id`, `title`, `year`, `available`), abstract `lateFee(int daysLate)`, and comparator `BY_YEAR_DESC_THEN_TITLE`.
3. **`Book` (class):** Extends `LibraryItem`. Adds `author` and `isbn`. Late fee: 2.0 per day.
4. **`Magazine` (class):** Extends `LibraryItem`. Adds `issueNumber`. Late fee: 1.0 per day.
5. **`DVD` (class):** Extends `LibraryItem`. Adds `durationMinutes`. Late fee: 5.0 per day.
6. **`Member` (class):** Contains `id`, `name`, `email` (validates `@`), and `borrowedItems`.
7. **`LibraryException` (class):** Base runtime exception extending `RuntimeException`.
   - `ItemNotFoundException` extends `LibraryException`.
   - `ItemNotAvailableException` extends `LibraryException`.
   - `BorrowLimitExceededException` extends `LibraryException`.
8. **`Repository<T, ID>` (generic interface):** Defines `save`, `findById`, `findAll`, `deleteById`.
9. **`InMemoryRepository<T, ID>` (class):** Implements `Repository<T, ID>` using a `HashMap` and an ID extractor function.
10. **`CollectionUtils` (utility class):** Generic `maxOf(List<T>)` and `totalFees(List<? extends LibraryItem>, int daysLate)` with bounded wildcards.
11. **`SearchCache` (utility class):** LRU cache using `LinkedHashMap` overriding `removeEldestEntry` with a maximum capacity of 5.
12. **`LibraryService` (service class):** Handles all business logic, stores items and members in repositories, enforces ISBN uniqueness, manages waitlists using `ArrayDeque`, and exports reports.
13. **`Main` (class):** Console menu loop with `Scanner` and `try-catch` exception handling.

---

## 4. Five Test Cases

### Test Case 1: Successful Borrow
- **Input:**
  - Register member: Alice (ID: 1)
  - Add Book: Clean Code, 2008, Robert Martin, ISBN 978-0132350884 (ID: 1001)
  - Menu 3 (Borrow) -> Member ID: 1, Item ID: 1001
- **Expected Output:**
  `Issued: Book[1001, Clean Code, 2008, Robert Martin]`

---

### Test Case 2: Borrowing an Unavailable Item (Waitlist)
- **Input:**
  - Register member: Ravi (ID: 2)
  - Menu 3 (Borrow) -> Member ID: 2, Item ID: 1001 (already borrowed by Alice)
  - Added to waitlist? `y`
- **Expected Output:**
  `ERROR: Item 1001 is not available. Added to waitlist? (y/n) y`

---

### Test Case 3: Exceeding the Borrow Limit
- **Input:**
  - Member 1 already has 3 items borrowed (items 1001, 1002, 1003).
  - Menu 3 (Borrow) -> Member ID: 1, Item ID: 1004.
- **Expected Output:**
  `ERROR: Member 1 has reached the maximum borrowing limit of 3 items`

---

### Test Case 4: Invalid Item ID
- **Input:**
  - Menu 3 (Borrow) -> Member ID: 1, Item ID: 9999 (non-existent item).
- **Expected Output:**
  `ERROR: Item with ID 9999 not found`

---

### Test Case 5: Return with Late Fee & Waitlist Notification
- **Input:**
  - Menu 4 (Return) -> Item ID: 1001, Late by (days): 3
- **Expected Output:**
  `Late fee: 6.0`  
  `Notification: Member 2 (Ravi) is next in the waitlist for 1001.`

---

## 5. LRU Cache and Memory Leaks (3 to 4 Lines)

An unbounded `HashMap` keeps every key-value pair in memory indefinitely as strong references for the entire lifespan of the application. In a search feature where users continuously enter different search terms, an unbounded map will never let old entries get garbage collected, steadily consuming heap memory until an `OutOfMemoryError` occurs. The LRU `SearchCache` solves this by using `LinkedHashMap` with `removeEldestEntry` bounded at 5 items, automatically evicting the least recently accessed entries whenever the size exceeds 5.

---

## 6. Challenges Faced and What Was Learned

1. **Handling Collections Concurrent Modification:** When removing older items in `removeOldItems`, modifying a collection while looping with a for-each causes `ConcurrentModificationException`. Learned to use `Iterator.remove()` to safely remove items during iteration.
2. **Generics and Wildcards:** Learned that generics are invariant, so `List<? extends LibraryItem>` is required in `CollectionUtils.totalFees` to allow passing `List<Book>` or `List<DVD>` polymorphically.
3. **Queue Waitlist Management:** Learned to use `ArrayDeque` inside a `Map<Integer, Queue<Member>>` to maintain first-come, first-served order for waitlisted members and notify them upon returns.
