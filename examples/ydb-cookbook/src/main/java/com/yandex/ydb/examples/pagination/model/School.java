package com.yandex.ydb.examples.pagination.model;

import java.util.Objects;

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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        School school = (School) o;

        return Objects.equals(number, school.number)
                && Objects.equals(city, school.city)
                && Objects.equals(address, school.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(city, number, address);
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
