package com.hcl.library.service;

import com.hcl.library.exception.*;
import com.hcl.library.model.*;
import com.hcl.library.repository.InMemoryRepository;
import com.hcl.library.util.SearchCache;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class LibraryService {
    public static final int MAX_BORROW_LIMIT = 3;

    private final InMemoryRepository<LibraryItem, Integer> itemRepository;
    private final InMemoryRepository<Member, Integer> memberRepository;
    private final Set<String> registeredIsbns;
    private final Map<Integer, Queue<Member>> waitlists;
    private final SearchCache searchCache;

    public LibraryService() {
        this.itemRepository = new InMemoryRepository<>(LibraryItem::getId);
        this.memberRepository = new InMemoryRepository<>(Member::getId);
        this.registeredIsbns = new HashSet<>();
        this.waitlists = new HashMap<>();
        this.searchCache = new SearchCache();
    }

    public void addItem(LibraryItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }
        if (item instanceof Book) {
            Book book = (Book) item;
            if (registeredIsbns.contains(book.getIsbn())) {
                throw new LibraryException("Book with ISBN " + book.getIsbn() + " already exists");
            }
            registeredIsbns.add(book.getIsbn());
        }
        itemRepository.save(item);
    }

    public void registerMember(Member member) {
        if (member == null) {
            throw new IllegalArgumentException("Member cannot be null");
        }
        memberRepository.save(member);
    }

    public LibraryItem borrowItem(int memberId, int itemId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member with ID " + memberId + " not found"));

        LibraryItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item with ID " + itemId + " not found"));

        if (member.getBorrowedItems().size() >= MAX_BORROW_LIMIT) {
            throw new BorrowLimitExceededException("Member " + memberId + " has reached the maximum borrowing limit of 3 items");
        }

        if (!item.isAvailable()) {
            throw new ItemNotAvailableException("Item " + itemId + " is not available");
        }

        item.borrow();
        member.addBorrowedItem(item);
        return item;
    }

    public double returnItem(int itemId, int daysLate) {
        LibraryItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item with ID " + itemId + " not found"));

        if (item.isAvailable()) {
            throw new LibraryException("Item " + itemId + " is not currently borrowed");
        }

        for (Member member : memberRepository.findAll()) {
            if (member.getBorrowedItems().contains(item)) {
                member.removeBorrowedItem(item);
                break;
            }
        }

        item.giveBack();
        double fee = item.lateFee(daysLate);

        Queue<Member> queue = waitlists.get(itemId);
        if (queue != null && !queue.isEmpty()) {
            Member next = queue.poll();
            System.out.println("Notification: Member " + next.getId() + " (" + next.getName() + ") is next in the waitlist for " + itemId + ".");
            if (queue.isEmpty()) {
                waitlists.remove(itemId);
            }
        }

        return fee;
    }

    public void addToWaitlist(int itemId, int memberId) {
        if (!itemRepository.findById(itemId).isPresent()) {
            throw new ItemNotFoundException("Item with ID " + itemId + " not found");
        }
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member with ID " + memberId + " not found"));

        Queue<Member> queue = waitlists.computeIfAbsent(itemId, k -> new ArrayDeque<>());
        if (!queue.contains(member)) {
            queue.offer(member);
        }
    }

    public Map<Integer, Queue<Member>> getWaitlists() {
        return Collections.unmodifiableMap(waitlists);
    }

    public List<LibraryItem> getItemsSortedByTitle() {
        List<LibraryItem> items = new ArrayList<>(itemRepository.findAll());
        Collections.sort(items);
        return Collections.unmodifiableList(items);
    }

    public List<LibraryItem> getItemsSortedByYearDescThenTitle() {
        List<LibraryItem> items = new ArrayList<>(itemRepository.findAll());
        items.sort(LibraryItem.BY_YEAR_DESC_THEN_TITLE);
        return Collections.unmodifiableList(items);
    }

    public List<LibraryItem> searchByTitle(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        query = query.trim();
        Optional<List<LibraryItem>> cached = searchCache.get(query);
        if (cached.isPresent()) {
            System.out.println("[Found in SearchCache]");
            return cached.get();
        }

        List<LibraryItem> results = new ArrayList<>();
        for (LibraryItem item : itemRepository.findAll()) {
            if (item.getTitle().toLowerCase().contains(query.toLowerCase())) {
                results.add(item);
            }
        }
        searchCache.put(query, results);
        return results;
    }

    public int removeOldItems(int beforeYear) {
        int count = 0;
        List<LibraryItem> items = new ArrayList<>(itemRepository.findAll());
        Iterator<LibraryItem> it = items.iterator();
        while (it.hasNext()) {
            LibraryItem item = it.next();
            if (item.getYear() < beforeYear) {
                if (item instanceof Book) {
                    registeredIsbns.remove(((Book) item).getIsbn());
                }
                itemRepository.deleteById(item.getId());
                waitlists.remove(item.getId());
                count++;
            }
        }
        return count;
    }

    public Map<String, Integer> getItemTypeStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        for (LibraryItem item : itemRepository.findAll()) {
            stats.merge(item.getClass().getSimpleName(), 1, Integer::sum);
        }
        return Collections.unmodifiableMap(stats);
    }

    public void exportReport(String path) {
        List<LibraryItem> items = itemRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("======== LIBRARY INVENTORY REPORT ========\n");
        sb.append(String.format("%-6s | %-10s | %-25s | %-6s | %-12s\n", "ID", "TYPE", "TITLE", "YEAR", "STATUS"));
        sb.append("----------------------------------------------------------\n");

        for (LibraryItem item : items) {
            String status = item.isAvailable() ? "Available" : "Borrowed";
            sb.append(String.format("%-6d | %-10s | %-25s | %-6d | %-12s\n",
                    item.getId(),
                    item.getClass().getSimpleName(),
                    item.getTitle(),
                    item.getYear(),
                    status));
        }

        sb.append("----------------------------------------------------------\n");
        sb.append("Total Items: ").append(items.size()).append("\n");

        Map<String, Integer> stats = getItemTypeStatistics();
        sb.append("Statistics: Books=").append(stats.getOrDefault("Book", 0))
                .append(", Magazines=").append(stats.getOrDefault("Magazine", 0))
                .append(", DVDs=").append(stats.getOrDefault("DVD", 0)).append("\n");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            writer.write(sb.toString());
        } catch (IOException e) {
            throw new LibraryException("Failed to export report to " + path, e);
        }
    }
}
