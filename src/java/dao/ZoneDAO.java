package dao;

import context.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import model.Zone;

public class ZoneDAO extends DBContext {

  public List<Zone> getZonesByWarehouseId(Long warehouseId) throws Exception {
    List<Zone> list = new ArrayList<>();

    String sql = """
          SELECT
              zone_id,
              warehouse_id,
              code,
              name,
              zone_type,
              status
          FROM zone
          WHERE warehouse_id = ?
          ORDER BY zone_id
      """;

    try (
      Connection con = DBContext.getConnection();
      PreparedStatement ps = con.prepareStatement(sql)
    ) {
      ps.setLong(1, warehouseId);

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          Zone z = new Zone();
          z.setZoneId(rs.getLong("zone_id"));
          z.setWarehouseId(rs.getLong("warehouse_id"));
          z.setCode(rs.getString("code"));
          z.setName(rs.getString("name"));
          z.setZoneType(rs.getString("zone_type"));
          z.setStatus(rs.getString("status"));

          list.add(z);
        }
      }
    }

    return list;
  }

  public Zone getZoneById(Long zoneId) throws Exception {
    String sql = """
          SELECT
              zone_id,
              warehouse_id,
              code,
              name,
              zone_type,
              status
          FROM zone
          WHERE zone_id = ?
      """;

    try (
      Connection con = DBContext.getConnection();
      PreparedStatement ps = con.prepareStatement(sql)
    ) {
      ps.setLong(1, zoneId);

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          Zone z = new Zone();
          z.setZoneId(rs.getLong("zone_id"));
          z.setWarehouseId(rs.getLong("warehouse_id"));
          z.setCode(rs.getString("code"));
          z.setName(rs.getString("name"));
          z.setZoneType(rs.getString("zone_type"));
          z.setStatus(rs.getString("status"));

          return z;
        }
      }
    }
    return null;
  }

  /**
   * Get all zones in the system.
   * @return List of all zones
   */
  public List<Zone> getAllZones() throws Exception {
    List<Zone> list = new ArrayList<>();
    String sql = """
          SELECT
              zone_id,
              warehouse_id,
              code,
              name,
              zone_type,
              status
          FROM zone
          ORDER BY code
      """;

    try (
      Connection con = DBContext.getConnection();
      PreparedStatement ps = con.prepareStatement(sql);
      ResultSet rs = ps.executeQuery()
    ) {
      while (rs.next()) {
        Zone z = new Zone();
        z.setZoneId(rs.getLong("zone_id"));
        z.setWarehouseId(rs.getLong("warehouse_id"));
        z.setCode(rs.getString("code"));
        z.setName(rs.getString("name"));
        z.setZoneType(rs.getString("zone_type"));
        z.setStatus(rs.getString("status"));
        list.add(z);
      }
    }
    return list;
  }

  /**
   * Kiểm tra xem zone code đã tồn tại trong warehouse này chưa
   */
  public boolean zoneCodeExistsInWarehouse(Long warehouseId, String code)
    throws Exception {
    String sql = """
          SELECT COUNT(*) as count
          FROM zone
          WHERE warehouse_id = ? AND code = ?
      """;

    try (
      Connection con = DBContext.getConnection();
      PreparedStatement ps = con.prepareStatement(sql)
    ) {
      ps.setLong(1, warehouseId);
      ps.setString(2, code);

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getInt("count") > 0;
        }
      }
    }

    return false;
  }

  /**
   * Kiểm tra xem zone code đã tồn tại trong toàn bộ database chưa
   * (Nếu unique constraint là global)
   */
  public boolean zoneCodeExists(String code) throws Exception {
    String sql = """
          SELECT COUNT(*) as count
          FROM zone
          WHERE code = ?
      """;

    try (
      Connection con = DBContext.getConnection();
      PreparedStatement ps = con.prepareStatement(sql)
    ) {
      ps.setString(1, code);

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getInt("count") > 0;
        }
      }
    }

    return false;
  }

  public Long createZone(
    Long warehouseId,
    String code,
    String name,
    String zoneType
  ) throws Exception {
    // Normalize code: trim và uppercase
    if (code == null || code.isBlank()) {
      throw new Exception("Zone code cannot be empty");
    }
    code = code.trim().toUpperCase();

    // Validate code không được chứa ký tự đặc biệt
    if (!code.matches("^[A-Z0-9_-]+$")) {
      throw new Exception(
        "Zone code can only contain letters, numbers, hyphens and underscores"
      );
    }

    // Kiểm tra code đã tồn tại trong warehouse này chưa
    if (zoneCodeExistsInWarehouse(warehouseId, code)) {
      throw new Exception(
        "Zone code '" +
          code +
          "' already exists in this warehouse. Please use a different code"
      );
    }

    // Kiểm tra code đã tồn tại trong toàn bộ database chưa (nếu unique constraint là global)
    if (zoneCodeExists(code)) {
      throw new Exception(
        "Zone code '" +
          code +
          "' already exists in the system. Please use a different code"
      );
    }

    String sql = """
          INSERT INTO zone (warehouse_id, code, name, zone_type, status)
          VALUES (?, ?, ?, ?, 'ACTIVE')
      """;

    try (
      Connection con = DBContext.getConnection();
      PreparedStatement ps = con.prepareStatement(
        sql,
        PreparedStatement.RETURN_GENERATED_KEYS
      )
    ) {
      ps.setLong(1, warehouseId);
      ps.setString(2, code);
      ps.setString(3, name);
      ps.setString(4, zoneType);

      ps.executeUpdate();

      try (ResultSet rs = ps.getGeneratedKeys()) {
        if (rs.next()) {
          return rs.getLong(1);
        }
      }
    } catch (java.sql.SQLIntegrityConstraintViolationException e) {
      throw new Exception(
        "Zone code '" +
          code +
          "' already exists in the system. Please use a different code"
      );
    }

    return null;
  }
}
