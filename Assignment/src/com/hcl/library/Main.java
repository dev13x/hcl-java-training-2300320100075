package com.hcl.library;

import com.hcl.library.exception.*;
import com.hcl.library.model.*;
import com.hcl.library.service.LibraryService;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final LibraryService libraryService = new LibraryService();

    public static void main(String[] args) {
        boolean running = true;
        while (running) {
            displayMenu();
            System.out.print("Choice: ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            int choice;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid choice. Please enter a number.");
                continue;
            }

            try {
                switch (choice) {
                    case 1:
                        addItem();
                        break;
                    case 2:
                        registerMember();
                        break;
                    case 3:
                        borrowItem();
                        break;
                    case 4:
                        returnItem();
                        break;
                    case 5:
                        viewWaitlist();
                        break;
                    case 6:
                        listItems();
                        break;
                    case 7:
                        searchItem();
                        break;
                    case 8:
                        exportReport();
                        break;
                    case 0:
                        System.out.println("Exiting system. Goodbye!");
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid option. Please choose between 0 and 8.");
                }
            } catch (ItemNotFoundException e) {
                System.out.println("ERROR: " + e.getMessage());
            } catch (ItemNotAvailableException e) {
                System.out.println("ERROR: " + e.getMessage());
            } catch (BorrowLimitExceededException e) {
                System.out.println("ERROR: " + e.getMessage());
            } catch (LibraryException e) {
                System.out.println("ERROR: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println("ERROR: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            } finally {
                System.out.println();
            }
        }
    }

    private static void displayMenu() {
        System.out.println("======== LIBRARY MENU ========");
        System.out.println("1. Add item        2. Register member     3. Borrow        4. Return");
        System.out.println("5. Waitlist        6. List items          7. Search        8. Export report    0. Exit");
    }

    private static void addItem() {
        System.out.println("Select Type: 1. Book  2. Magazine  3. DVD");
        System.out.print("Type: ");
        int type = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("Title: ");
        String title = scanner.nextLine().trim();

        System.out.print("Year: ");
        int year = Integer.parseInt(scanner.nextLine().trim());

        if (type == 1) {
            System.out.print("Author: ");
            String author = scanner.nextLine().trim();
            System.out.print("ISBN: ");
            String isbn = scanner.nextLine().trim();
            Book book = new Book(title, year, author, isbn);
            libraryService.addItem(book);
            System.out.println("Book added successfully (Item ID: " + book.getId() + ").");
        } else if (type == 2) {
            System.out.print("Issue Number: ");
            int issue = Integer.parseInt(scanner.nextLine().trim());
            Magazine mag = new Magazine(title, year, issue);
            libraryService.addItem(mag);
            System.out.println("Magazine added successfully (Item ID: " + mag.getId() + ").");
        } else if (type == 3) {
            System.out.print("Duration (minutes): ");
            int duration = Integer.parseInt(scanner.nextLine().trim());
            DVD dvd = new DVD(title, year, duration);
            libraryService.addItem(dvd);
            System.out.println("DVD added successfully (Item ID: " + dvd.getId() + ").");
        } else {
            System.out.println("Invalid item type.");
        }
    }

    private static void registerMember() {
        System.out.print("Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        Member member = new Member(name, email);
        libraryService.registerMember(member);
        System.out.println("Registered member: " + member.getName() + " (ID: " + member.getId() + ")");
    }

    private static void borrowItem() {
        System.out.print("Member id: ");
        int memberId = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("Item id: ");
        int itemId = Integer.parseInt(scanner.nextLine().trim());

        try {
            LibraryItem item = libraryService.borrowItem(memberId, itemId);
            System.out.println("Issued: " + item);
        } catch (ItemNotAvailableException e) {
            System.out.print("ERROR: Item " + itemId + " is not available. Added to waitlist? (y/n) ");
            String answer = scanner.nextLine().trim();
            if (answer.equalsIgnoreCase("y")) {
                libraryService.addToWaitlist(itemId, memberId);
                System.out.println("Added to waitlist for item " + itemId + ".");
            }
        }
    }

    private static void returnItem() {
        System.out.print("Item id: ");
        int itemId = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("Late by (days): ");
        int daysLate = Integer.parseInt(scanner.nextLine().trim());

        double fee = libraryService.returnItem(itemId, daysLate);
        System.out.println("Late fee: " + fee);
    }

    private static void viewWaitlist() {
        Map<Integer, Queue<Member>> waitlists = libraryService.getWaitlists();
        if (waitlists.isEmpty()) {
            System.out.println("No active waitlists.");
            return;
        }
        for (Map.Entry<Integer, Queue<Member>> entry : waitlists.entrySet()) {
            System.out.print("Item " + entry.getKey() + " waitlist: ");
            for (Member m : entry.getValue()) {
                System.out.print(m.getName() + " (ID: " + m.getId() + ") ");
            }
            System.out.println();
        }
    }

    private static void listItems() {
        System.out.println("1. Sort by Title (Natural Order)");
        System.out.println("2. Sort by Year Descending, then Title");
        System.out.print("Option: ");
        int opt = Integer.parseInt(scanner.nextLine().trim());

        List<LibraryItem> items;
        if (opt == 1) {
            items = libraryService.getItemsSortedByTitle();
        } else {
            items = libraryService.getItemsSortedByYearDescThenTitle();
        }

        System.out.printf("%-6s | %-10s | %-25s | %-6s | %-10s\n", "ID", "TYPE", "TITLE", "YEAR", "STATUS");
        System.out.println("------------------------------------------------------------------");
        for (LibraryItem item : items) {
            String status = item.isAvailable() ? "Available" : "Borrowed";
            System.out.printf("%-6d | %-10s | %-25s | %-6d | %-10s\n",
                    item.getId(), item.getClass().getSimpleName(), item.getTitle(), item.getYear(), status);
        }

        Map<String, Integer> stats = libraryService.getItemTypeStatistics();
        System.out.println("\n--- Item Statistics ---");
        System.out.println("Books:     " + stats.getOrDefault("Book", 0));
        System.out.println("Magazines: " + stats.getOrDefault("Magazine", 0));
        System.out.println("DVDs:      " + stats.getOrDefault("DVD", 0));
    }

    private static void searchItem() {
        System.out.print("Enter title to search: ");
        String query = scanner.nextLine().trim();

        List<LibraryItem> results = libraryService.searchByTitle(query);
        if (results.isEmpty()) {
            System.out.println("No matching items found.");
        } else {
            for (LibraryItem item : results) {
                System.out.println("- " + item);
            }
        }
    }

    private static void exportReport() {
        System.out.print("Enter file path [default: library_report.txt]: ");
        String path = scanner.nextLine().trim();
        if (path.isEmpty()) {
            path = "library_report.txt";
        }
        libraryService.exportReport(path);
        System.out.println("Report exported successfully to " + path);
    }
}
