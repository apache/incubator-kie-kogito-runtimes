// Copyright IBM Corp. 2025.

package org.acme;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CandidateData {

    private String name;

    private String lastName;

    private String email;

    private Integer experience;

    private List<String> skills;

    public CandidateData() {
    }

    public CandidateData(String name, String lastName, String email, Integer experience, List<String> skills) {
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.experience = experience;
        this.skills = skills;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getExperience() {
        return experience;
    }

    public void setExperience(Integer experience) {
        this.experience = experience;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    @JsonIgnore
    public String getFullName() {
        return name + " " + lastName;
    }

    @java.lang.Override
    public String toString() {
        return "CandidateData{" +
                "name='" + name + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", experience=" + experience +
                ", skills=" + skills +
                '}';
    }
}
