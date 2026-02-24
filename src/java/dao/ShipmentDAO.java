package dao;

import context.DBContext;
import model.Shipment;
import model.Carrier;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ShipmentDAO extends DBContext {

    public List<dto.ShipmentListDTO> getFilteredShipments(String shipmentNumber, Long carrierId, String status,
            String shipmentType,
            String sortField, String sortOrder, int limit, int offset) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                    SELECT s.shipment_id, s.shipment_number, s.status, s.created_at,
                           s.shipment_type, c.name as carrier_name, gdn.gdn_number, s.tracking_code
                    FROM shipment s
                    LEFT JOIN carrier c ON s.carrier_id = c.carrier_id
                    LEFT JOIN goods_delivery_note gdn ON s.gdn_id = gdn.gdn_id
                    WHERE 1=1
                """);

        if (shipmentNumber != null && !shipmentNumber.isBlank()) {
            sql.append(" AND s.shipment_number LIKE ?");
        }
        if (carrierId != null) {
            sql.append(" AND s.carrier_id = ?");
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND s.status = ?");
        }
        if (shipmentType != null && !shipmentType.isBlank()) {
            sql.append(" AND s.shipment_type = ?");
        }

        // Validate sortField and sortOrder
        String validSortField = "s.shipment_id";
        if ("shipment_number".equals(sortField)) {
            validSortField = "s.shipment_number";
        } else if ("carrier_name".equals(sortField)) {
            validSortField = "c.name";
        } else if ("status".equals(sortField)) {
            validSortField = "s.status";
        } else if ("created_at".equals(sortField)) {
            validSortField = "s.created_at";
        }

        String validSortOrder = "DESC";
        if ("ASC".equalsIgnoreCase(sortOrder)) {
            validSortOrder = "ASC";
        }

        sql.append(" ORDER BY ").append(validSortField).append(" ").append(validSortOrder);
        sql.append(" LIMIT ? OFFSET ?");

        List<dto.ShipmentListDTO> list = new ArrayList<>();
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIdx = 1;
            if (shipmentNumber != null && !shipmentNumber.isBlank()) {
                ps.setString(paramIdx++, "%" + shipmentNumber + "%");
            }
            if (carrierId != null) {
                ps.setLong(paramIdx++, carrierId);
            }
            if (status != null && !status.isBlank()) {
                ps.setString(paramIdx++, status);
            }
            if (shipmentType != null && !shipmentType.isBlank()) {
                ps.setString(paramIdx++, shipmentType);
            }
            ps.setInt(paramIdx++, limit);
            ps.setInt(paramIdx++, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dto.ShipmentListDTO item = new dto.ShipmentListDTO();
                    item.setShipmentId(rs.getLong("shipment_id"));
                    item.setShipmentNumber(rs.getString("shipment_number"));
                    item.setCarrierName(rs.getString("carrier_name"));
                    item.setStatus(rs.getString("status"));
                    item.setShipmentType(rs.getString("shipment_type"));
                    item.setGdnNumber(rs.getString("gdn_number"));
                    item.setTrackingCode(rs.getString("tracking_code"));
                    Timestamp createdAtTs = rs.getTimestamp("created_at");
                    if (createdAtTs != null) {
                        item.setCreatedAt(createdAtTs.toLocalDateTime());
                    }
                    list.add(item);
                }
            }
        }
        return list;
    }

    public int countFilteredShipments(String shipmentNumber, Long carrierId, String status, String shipmentType)
            throws SQLException {
        StringBuilder sql = new StringBuilder("""
                    SELECT COUNT(*)
                    FROM shipment s
                    WHERE 1=1
                """);

        if (shipmentNumber != null && !shipmentNumber.isBlank()) {
            sql.append(" AND s.shipment_number LIKE ?");
        }
        if (carrierId != null) {
            sql.append(" AND s.carrier_id = ?");
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND s.status = ?");
        }
        if (shipmentType != null && !shipmentType.isBlank()) {
            sql.append(" AND s.shipment_type = ?");
        }

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIdx = 1;
            if (shipmentNumber != null && !shipmentNumber.isBlank()) {
                ps.setString(paramIdx++, "%" + shipmentNumber + "%");
            }
            if (carrierId != null) {
                ps.setLong(paramIdx++, carrierId);
            }
            if (status != null && !status.isBlank()) {
                ps.setString(paramIdx++, status);
            }
            if (shipmentType != null && !shipmentType.isBlank()) {
                ps.setString(paramIdx++, shipmentType);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public long createShipment(Shipment shipment) throws SQLException {
        String sql = """
                    INSERT INTO shipment (shipment_number, gdn_id, carrier_id, shipment_type, status, created_at, tracking_code, note)
                    VALUES (?, ?, ?, ?, 'CREATED', NOW(), ?, ?)
                """;
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, shipment.getShipmentNumber());
            ps.setObject(2, shipment.getGdnId());
            ps.setObject(3, shipment.getCarrierId());
            ps.setString(4, shipment.getShipmentType());
            ps.setString(5, shipment.getTrackingCode());
            ps.setString(6, shipment.getNote());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                } else {
                    throw new SQLException("Creating shipment failed, no ID obtained.");
                }
            }
        }
    }

    public Shipment getById(Long id) throws SQLException {
        String sql = """
                    SELECT s.*, c.name as carrier_name, gdn.gdn_number
                    FROM shipment s
                    LEFT JOIN carrier c ON s.carrier_id = c.carrier_id
                    LEFT JOIN goods_delivery_note gdn ON s.gdn_id = gdn.gdn_id
                    WHERE s.shipment_id = ?
                """;
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Shipment s = new Shipment();
                    s.setShipmentId(rs.getLong("shipment_id"));
                    s.setShipmentNumber(rs.getString("shipment_number"));
                    s.setGdnId(rs.getObject("gdn_id") != null ? rs.getLong("gdn_id") : null);
                    s.setCarrierId(rs.getObject("carrier_id") != null ? rs.getLong("carrier_id") : null);
                    s.setShipmentType(rs.getString("shipment_type"));
                    s.setStatus(rs.getString("status"));
                    s.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    Timestamp pickedTs = rs.getTimestamp("picked_up_at");
                    if (pickedTs != null)
                        s.setPickedUpAt(pickedTs.toLocalDateTime());
                    Timestamp deliveredTs = rs.getTimestamp("delivered_at");
                    if (deliveredTs != null)
                        s.setDeliveredAt(deliveredTs.toLocalDateTime());
                    s.setTrackingCode(rs.getString("tracking_code"));
                    s.setCarrierName(rs.getString("carrier_name")); // Added mapping
                    s.setGdnNumber(rs.getString("gdn_number")); // Added mapping
                    s.setNote(rs.getString("note"));
                    return s;
                }
            }
        }
        return null;
    }

    public boolean updateShipment(Shipment shipment) throws SQLException {
        String sql = """
                    UPDATE shipment
                    SET carrier_id = ?, status = ?, tracking_code = ?, note = ?,
                        picked_up_at = ?, delivered_at = ?
                    WHERE shipment_id = ?
                """;
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, shipment.getCarrierId());
            ps.setString(2, shipment.getStatus());
            ps.setString(3, shipment.getTrackingCode());
            ps.setString(4, shipment.getNote());
            ps.setTimestamp(5, shipment.getPickedUpAt() != null ? Timestamp.valueOf(shipment.getPickedUpAt()) : null);
            ps.setTimestamp(6, shipment.getDeliveredAt() != null ? Timestamp.valueOf(shipment.getDeliveredAt()) : null);
            ps.setLong(7, shipment.getShipmentId());
            return ps.executeUpdate() > 0;
        }
    }

    public List<Carrier> getAllCarriers() throws SQLException {
        String sql = "SELECT * FROM carrier ORDER BY name";
        List<Carrier> list = new ArrayList<>();
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Carrier c = new Carrier();
                c.setCarrierId(rs.getLong("carrier_id"));
                c.setName(rs.getString("name"));
                c.setCarrierType(rs.getString("carrier_type"));
                c.setPhone(rs.getString("phone"));
                c.setNote(rs.getString("note"));
                list.add(c);
            }
        }
        return list;
    }

    public List<model.GoodsDeliveryNote> getAvailableGDNs() throws SQLException {
        String sql = """
                    SELECT * FROM goods_delivery_note
                    WHERE status <> 'CANCELLED'
                    AND gdn_id NOT IN (SELECT gdn_id FROM shipment WHERE gdn_id IS NOT NULL)
                    ORDER BY gdn_id DESC
                """;
        List<model.GoodsDeliveryNote> list = new ArrayList<>();
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                model.GoodsDeliveryNote gdn = new model.GoodsDeliveryNote();
                gdn.setGdnId(rs.getLong("gdn_id"));
                gdn.setGdnNumber(rs.getString("gdn_number"));
                list.add(gdn);
            }
        }
        return list;
    }

    public String getNextShipmentNumber() throws SQLException {
        String sql = "SELECT MAX(shipment_id) FROM shipment";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            long nextId = 1;
            if (rs.next()) {
                nextId = rs.getLong(1) + 1;
            }
            return String.format("SHIP-%05d", nextId);
        }
    }
}
