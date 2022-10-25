package com.example.demo.controller;

import com.fasterxml.jackson.annotation.JsonAlias;

public class Order {
    public Integer getOrder_id() {
        return order_id;
    }

    Integer order_id=null;
    int team_size;
    String members;
    int building_id;
    boolean gender;
    int state;

    public int getTeam_size() {
        return team_size;
    }
    @JsonAlias({"size","team_size"})
    public void setTeam_size(int team_size) {
        this.team_size = team_size;
    }

    public String getMembers() {
        return members;
    }

    public void setMembers(String members) {
        this.members = members;
    }

    public int getBuilding_id() {
        return building_id;
    }

    public void setBuilding_id(int building_id) {
        this.building_id = building_id;
    }

    public boolean isGender() {
        return gender;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
