package dao;

import database.DatabaseConnection;
import model.Room;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

    private static final String COLUMNS = "room_id, room_name, building, capacity, room_type";

    // CREATE
    public boolean addRoom(Room room) throws SQLException {
        String sql = "INSERT INTO rooms (room_name, building, capacity, room_type) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, room.getRoomName());
            ps.setString(2, room.getBuilding());
            ps.setInt(3, room.getCapacity());
            ps.setString(4, room.getRoomType());
            return ps.executeUpdate() > 0;
        }
    }

    // READ
    public List<Room> getAllRooms() throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM rooms ORDER BY room_name";
        List<Room> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    // SEARCH
    public List<Room> searchRooms(String keyword) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM rooms "
                   + "WHERE UPPER(room_name) LIKE ? OR UPPER(building) LIKE ? OR UPPER(room_type) LIKE ? "
                   + "ORDER BY room_name";
        String like = "%" + keyword.trim().toUpperCase() + "%";
        List<Room> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    // UPDATE
    public boolean updateRoom(Room room) throws SQLException {
        String sql = "UPDATE rooms SET room_name = ?, building = ?, capacity = ?, room_type = ? "
                   + "WHERE room_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, room.getRoomName());
            ps.setString(2, room.getBuilding());
            ps.setInt(3, room.getCapacity());
            ps.setString(4, room.getRoomType());
            ps.setInt(5, room.getRoomId());
            return ps.executeUpdate() > 0;
        }
    }

    // DELETE 
    public boolean deleteRoom(int roomId) throws SQLException {
        String sql = "DELETE FROM rooms WHERE room_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            return ps.executeUpdate() > 0;
        }
    }

    private Room map(ResultSet rs) throws SQLException {
        return new Room(rs.getInt("room_id"), rs.getString("room_name"),
                rs.getString("building"), rs.getInt("capacity"), rs.getString("room_type"));
    }
}