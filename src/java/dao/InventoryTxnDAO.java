package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;

import context.DBContext;
import model.InventoryTxn;

public class InventoryTxnDAO extends DBContext {

    /**
     * Inserts a new inventory transaction record.
     * Table structure:
     * CREATE TABLE inventory_txn (
     * txn_id BIGINT PRIMARY KEY AUTO_INCREMENT,
     * txn_at DATETIME DEFAULT CURRENT_TIMESTAMP,
     * txn_type VARCHAR(50),
     * warehouse_id BIGINT,
     * from_slot_id BIGINT,
     * to_slot_id BIGINT,
     * variant_id BIGINT,
     * `condition` VARCHAR(50),
     * qty_delta DECIMAL(18, 2),
     * ref_doc_type VARCHAR(50),
     * ref_doc_id BIGINT,
     * note TEXT,
     * created_by BIGINT
     * );
     */
    public void insertTxn(InventoryTxn txn) throws Exception {
        String sql = """
                    INSERT INTO inventory_txn
                    (txn_type, warehouse_id, from_slot_id, to_slot_id, variant_id, `condition`, qty_delta, ref_doc_type, ref_doc_id, note, created_by, txn_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())
                """;

        try (Connection con = DBContext.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, txn.getTxnType());
            ps.setObject(2, txn.getWarehouseId());
            ps.setObject(3, txn.getFromSlotId());
            ps.setObject(4, txn.getToSlotId());
            ps.setObject(5, txn.getVariantId());
            ps.setString(6, txn.getCondition());
            ps.setBigDecimal(7, txn.getQtyDelta());
            ps.setString(8, txn.getRefDocType());
            ps.setObject(9, txn.getRefDocId());
            ps.setString(10, txn.getNote());
            ps.setObject(11, txn.getCreatedBy());

            ps.executeUpdate();
        }
    }
}
