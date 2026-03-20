/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/**
 *
 * @author HungTran
 */
public class SaleOrderHeaderDTO {

    private Long soId;
    private String soNumber;
    private Long customerId;
    private String customerCode;
    private String customerName;
    private String customerEmail;
    private String customerAddress;
    private String customerPhone;
    private String shipToAddress;
    private LocalDate requestedShipDate;
    private String status;
    private Long importedBy;
    private String importedByUsername;
    private LocalDateTime importedAt;
}
