package com.example.demo.controller;

public class Room {
    public String room_id;
    public int building_id;
    public int remain_beds;
    public boolean gender;

    public String getRoom_id() {
        return room_id;
    }

    public void setRoom_id(String room_id) {
        this.room_id = room_id;
    }

    public int getBuilding_id() {
        return building_id;
    }

    public void setBuilding_id(int building_id) {
        this.building_id = building_id;
    }

    public int getRemain_beds() {
        return remain_beds;
    }

    public void setRemain_beds(int remain_beds) {
        this.remain_beds = remain_beds;
    }

    public boolean isGender() {
        return gender;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    @Override
    public String toString() {
        String g=new String();
        if(!gender)
            g="男";
        else
            g="女";
        return "房间号: "+room_id+"\n楼号: "+building_id+"\n剩余床位: "+remain_beds+"\n性别: "+g;
    }
}
