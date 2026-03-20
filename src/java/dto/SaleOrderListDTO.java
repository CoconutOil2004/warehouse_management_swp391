/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaleOrderListDTO {
    private Long soId;
    private String soNumber;
    private String customerName;
    private LocalDate requestedShipDate;
    private String shipToAddress;
    private String status;
    private String importedByUsername; // hoặc full_name
}
