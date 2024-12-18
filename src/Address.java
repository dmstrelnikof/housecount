import java.util.Objects;

public class Address {
    private String city;
    private String street;
    private int house;
    private int floors;
    
    public Address(String city, String street, int house, int floors) {
        this.city = city;
        this.street = street;
        this.house = house;
        this.floors = floors;
    }
    
    public String getCity() {
        return city;
    }
    
    public int getFloors() {
        return floors;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return house == address.house &&
               floors == address.floors &&
               Objects.equals(city, address.city) &&
               Objects.equals(street, address.street);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(city, street, house, floors);
    }
    
    @Override
    public String toString() {
        return String.format("%s, ул. %s, д. %d, этажей: %d", 
            city, street, house, floors);
    }
} 