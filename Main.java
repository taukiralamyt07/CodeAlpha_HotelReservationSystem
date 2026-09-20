import java.io.*;
import java.util.*;

public class Main {
    static final Scanner sc = new Scanner(System.in);
    static final String FILE = "bookings.dat";
    static final Hotel hotel = new Hotel();

    public static void main(String[] args) {
        hotel.load(FILE);
        boolean run = true;
        while (run) {
            System.out.println("\n===== CODEALPHA HOTEL RESERVATION SYSTEM =====");
            System.out.println("1. View Rooms");
            System.out.println("2. Search Available Rooms");
            System.out.println("3. Make Reservation");
            System.out.println("4. Cancel Reservation");
            System.out.println("5. View Booking Details");
            System.out.println("6. Save & Exit");
            System.out.print("Choose: ");
            try {
                switch (sc.nextLine()) {
                    case "1" -> hotel.showRooms();
                    case "2" -> search();
                    case "3" -> book();
                    case "4" -> cancel();
                    case "5" -> details();
                    case "6" -> { hotel.save(FILE); run = false; }
                    default -> System.out.println("Invalid choice.");
                }
            } catch (Exception e) { System.out.println("Error: " + e.getMessage()); }
        }
        sc.close();
    }

    static void search() {
        System.out.print("Room category (Standard/Deluxe/Suite): ");
        hotel.searchAvailable(sc.nextLine());
    }

    static void book() {
        System.out.print("Guest name: "); String name = sc.nextLine();
        System.out.print("Phone/email: "); String contact = sc.nextLine();
        System.out.print("Room category: "); String category = sc.nextLine();
        Room room = hotel.findAvailable(category);
        if (room == null) { System.out.println("No room available in this category."); return; }
        System.out.print("Number of nights: "); int nights = Integer.parseInt(sc.nextLine());
        if (nights <= 0) throw new IllegalArgumentException("Nights must be positive.");
        double amount = room.getPrice() * nights;
        String id = hotel.createBooking(name, contact, room, nights, amount);
        System.out.println("\nReservation confirmed!");
        System.out.println("Booking ID: " + id);
        System.out.println("Room: " + room.getNumber() + " (" + room.getCategory() + ")");
        System.out.printf("Amount: ₹%.2f%n", amount);
        System.out.println("Payment Status: PAID (simulated)");
    }

    static void cancel() {
        System.out.print("Booking ID: ");
        System.out.println(hotel.cancelBooking(sc.nextLine()) ? "Reservation cancelled and room released." : "Booking not found.");
    }

    static void details() {
        System.out.print("Booking ID: ");
        Booking b = hotel.getBooking(sc.nextLine());
        if (b == null) System.out.println("Booking not found."); else b.display();
    }
}

class Room implements Serializable {
    private final int number; private final String category; private final double price; private boolean available = true;
    Room(int number, String category, double price) { this.number = number; this.category = category; this.price = price; }
    public int getNumber() { return number; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}

class Booking implements Serializable {
    private final String id, guestName, contact, category; private final int roomNumber, nights; private final double amount; private final Date createdAt;
    Booking(String id, String guestName, String contact, Room room, int nights, double amount) {
        this.id=id; this.guestName=guestName; this.contact=contact; this.roomNumber=room.getNumber(); this.category=room.getCategory(); this.nights=nights; this.amount=amount; this.createdAt=new Date();
    }
    public String getId() { return id; }
    public int getRoomNumber() { return roomNumber; }
    void display() {
        System.out.println("\n--- BOOKING DETAILS ---");
        System.out.println("Booking ID: " + id);
        System.out.println("Guest: " + guestName);
        System.out.println("Contact: " + contact);
        System.out.println("Room: " + roomNumber);
        System.out.println("Category: " + category);
        System.out.println("Nights: " + nights);
        System.out.printf("Total: ₹%.2f%n", amount);
        System.out.println("Payment: PAID (simulated)");
        System.out.println("Created: " + createdAt);
    }
}

class Hotel implements Serializable {
    private final List<Room> rooms = new ArrayList<>();
    private final Map<String, Booking> bookings = new LinkedHashMap<>();
    private int nextId = 1001;

    Hotel() {
        rooms.add(new Room(101,"Standard",2000)); rooms.add(new Room(102,"Standard",2000));
        rooms.add(new Room(201,"Deluxe",3500)); rooms.add(new Room(202,"Deluxe",3500));
        rooms.add(new Room(301,"Suite",6000)); rooms.add(new Room(302,"Suite",6000));
    }
    void showRooms() {
        System.out.printf("%-8s %-12s %-12s %-12s%n", "ROOM", "CATEGORY", "PRICE/NIGHT", "STATUS");
        for (Room r: rooms) System.out.printf("%-8d %-12s ₹%-11.2f %-12s%n", r.getNumber(), r.getCategory(), r.getPrice(), r.isAvailable()?"Available":"Booked");
    }
    void searchAvailable(String category) {
        boolean found=false;
        for (Room r: rooms) if (r.getCategory().equalsIgnoreCase(category) && r.isAvailable()) { System.out.printf("Room %d — %s — ₹%.2f/night%n", r.getNumber(),r.getCategory(),r.getPrice()); found=true; }
        if (!found) System.out.println("No available room found.");
    }
    Room findAvailable(String category) { for (Room r: rooms) if (r.getCategory().equalsIgnoreCase(category) && r.isAvailable()) return r; return null; }
    String createBooking(String name, String contact, Room room, int nights, double amount) {
        String id = "BK" + nextId++;
        room.setAvailable(false); bookings.put(id, new Booking(id,name,contact,room,nights,amount)); return id;
    }
    Booking getBooking(String id) { return bookings.get(id); }
    boolean cancelBooking(String id) {
        Booking b=bookings.remove(id); if (b==null) return false;
        for(Room r:rooms) if(r.getNumber()==b.getRoomNumber()) r.setAvailable(true); return true;
    }
    void save(String file) { try(ObjectOutputStream out=new ObjectOutputStream(new FileOutputStream(file))){out.writeObject(this); System.out.println("Bookings saved.");}catch(IOException e){System.out.println("Could not save data.");} }
    void load(String file) {
        File f=new File(file); if(!f.exists()) return;
        try(ObjectInputStream in=new ObjectInputStream(new FileInputStream(f))){ Hotel h=(Hotel)in.readObject(); rooms.clear();rooms.addAll(h.rooms);bookings.clear();bookings.putAll(h.bookings);nextId=h.nextId;System.out.println("Saved bookings loaded."); }catch(Exception e){System.out.println("Starting with fresh hotel data.");}
    }
}
