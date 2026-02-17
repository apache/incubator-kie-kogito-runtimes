// Copyright IBM Corp. 2025.

package org.acme;

public class Offer {

    private String category;

    private Integer salary;

    public Offer() {
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getSalary() {
        return salary;
    }

    public void setSalary(Integer salary) {
        this.salary = salary;
    }

    @java.lang.Override
    public String toString() {
        return "Offer{" +
                "category='" + category + '\'' +
                ", salary=" + salary +
                '}';
    }
}
