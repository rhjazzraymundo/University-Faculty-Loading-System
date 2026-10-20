package model;

public class Room {
    private int roomId;
    private String roomName;
    private String building;
    private int capacity;
    private String roomType;     

    public Room() { }

    public Room(int roomId, String roomName, String building, int capacity, String roomType) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.building = building;
        this.capacity = capacity;
        this.roomType = roomType;
    }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    @Override
    public String toString() { return roomName; }
}