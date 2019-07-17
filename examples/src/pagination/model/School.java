package ru.yandex.ydb.examples.pagination.model;

/**
 * @author Sergey Polovko
 */
public class School {

    private final String city;
    private final int number;
    private final String address;

    public School(String city, int number, String address) {
        this.city = city;
        this.number = number;
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public int getNumber() {
        return number;
    }

    public String getAddress() {
        return address;
    }

    public Key toKey() {
        return new Key(city, number);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        School school = (School) o;

        if (number != school.number) return false;
        if (!city.equals(school.city)) return false;
        return address.equals(school.address);
    }

    @Override
    public int hashCode() {
        int result = city.hashCode();
        result = 31 * result + number;
        result = 31 * result + address.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "School{" +
            "city='" + city + '\'' +
            ", number=" + number +
            ", address='" + address + '\'' +
            '}';
    }

    /**
     * SCHOOL KEY
     */
    public static final class Key {
        private final String city;
        private final int number;

        public Key(String city, int number) {
            this.city = city;
            this.number = number;
        }

        public String getCity() {
            return city;
        }

        public int getNumber() {
            return number;
        }
    }
}
