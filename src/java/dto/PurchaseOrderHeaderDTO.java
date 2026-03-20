/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dto;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderHeaderDTO {
    private long poId;
    private String poNumber;
    private long supplierId;
    private String supplierCode;
    private String supplierName;
    private Date expectedDeliveryDate;
    private String status;
    private String note;
    private String supplierAddress;
    private String supplierPhone;
    private String supplierEmail;
    // getter/setter...
}
