<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="dto.SlotDetailDTO"%>
<%@page import="model.Zone"%>
<%@page import="dto.ProductVariantDTO"%>
<%@taglib tagdir="/WEB-INF/tags/" prefix="t"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<%
    List<SlotDetailDTO> slots = (List<SlotDetailDTO>) request.getAttribute("slots");
    Zone zone = (Zone) request.getAttribute("zone");
    Long warehouseId = (Long) request.getAttribute("warehouseId");
    List<ProductVariantDTO> variants = (List<ProductVariantDTO>) request.getAttribute("variants");
    
    // Compute maxRow and maxCol based on slot data
    int maxRow = 0;
    int maxCol = 0;
    
    if (slots != null && !slots.isEmpty()) {
        for (SlotDetailDTO slot : slots) {
            if (slot.getRowNo() != null && slot.getRowNo() > maxRow) {
                maxRow = slot.getRowNo();
            }
            if (slot.getColNo() != null && slot.getColNo() > maxCol) {
                maxCol = slot.getColNo();
            }
        }
    }
    
    // Set to request attributes so JSTL can use them
    request.setAttribute("maxRow", maxRow);
    request.setAttribute("maxCol", maxCol);
%>

<t:layout title="Zone Layout - ${zone.code}">
    <style>
    .slot-grid-container {
        margin: 20px 0;
        padding: 20px;
        background-color: #f8f9fa;
        border-radius: 8px;
    }
    
    .screen-indicator {
        background-color: #000;
        color: white;
        text-align: center;
        padding: 10px;
        margin-bottom: 20px;
        border-radius: 4px;
        font-weight: bold;
        letter-spacing: 2px;
    }
    
    .slot-grid {
        display: grid;
        gap: 8px;
        justify-content: center;
    }
    
    .slot-item {
        width: 80px;
        height: 80px;
        border: 2px solid #dee2e6;
        border-radius: 8px;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        transition: all 0.2s;
        position: relative;
        font-size: 11px;
        text-align: center;
        padding: 4px;
    }
    
    .slot-item:hover {
        transform: scale(1.05);
        box-shadow: 0 4px 8px rgba(0,0,0,0.2);
        z-index: 10;
    }
    
    /* Empty slot - highlighted */
    .slot-item.empty {
        background-color: #d1ecf1;
        border-color: #0dcaf0;
        animation: pulse 2s infinite;
        box-shadow: 0 0 10px rgba(13, 202, 240, 0.5);
    }
    
    @keyframes pulse {
        0%, 100% { box-shadow: 0 0 10px rgba(13, 202, 240, 0.5); }
        50% { box-shadow: 0 0 20px rgba(13, 202, 240, 0.8); }
    }
    
    /* Slot has inventory */
    .slot-item.occupied {
        background-color: #d4edda;
        border-color: #28a745;
        color: #155724;
    }
    
    /* Slot is blocked */
    .slot-item.blocked {
        background-color: #f8d7da;
        border-color: #dc3545;
        color: #721c24;
        cursor: not-allowed;
        opacity: 0.6;
    }
    
    /* Slot is full */
    .slot-item.full {
        background-color: #fff3cd;
        border-color: #ffc107;
        color: #856404;
    }
    
    .slot-code {
        font-weight: bold;
        font-size: 10px;
        margin-bottom: 2px;
    }
    
    .slot-info {
        font-size: 9px;
        color: #666;
    }
    
    .row-label {
        display: flex;
        align-items: center;
        justify-content: center;
        font-weight: bold;
        color: #495057;
        min-width: 30px;
    }
    
    .col-header {
        display: flex;
        align-items: center;
        justify-content: center;
        font-weight: bold;
        color: #495057;
        min-height: 30px;
    }
    
    .legend {
        display: flex;
        gap: 20px;
        flex-wrap: wrap;
        margin: 20px 0;
    }
    
    .legend-item {
        display: flex;
        align-items: center;
        gap: 8px;
    }
    
    .legend-color {
        width: 30px;
        height: 30px;
        border-radius: 4px;
        border: 2px solid;
    }
    
    .instructions {
        background-color: #e7f3ff;
        border-left: 4px solid #0d6efd;
        padding: 15px;
        margin: 20px 0;
        border-radius: 4px;
    }
</style>

    <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
            <h4 class="mb-0">
                Zone Layout: <span class="badge bg-info"><c:out value="${zone.code}" /></span>
                <c:out value="${zone.name}" />
            </h4>
            <small class="text-muted">Type: <c:out value="${zone.zoneType}" /></small>
        </div>
        <a href="${pageContext.request.contextPath}/warehouse-layout" 
           class="btn btn-secondary">Back</a>
    </div>

    <!-- Legend -->
    <div class="legend">
        <div class="legend-item">
            <div class="legend-color empty" style="background-color: #d1ecf1; border-color: #0dcaf0;"></div>
            <span>Empty Slot (Available)</span>
        </div>
        <div class="legend-item">
            <div class="legend-color occupied" style="background-color: #d4edda; border-color: #28a745;"></div>
            <span>Occupied Slot</span>
        </div>
        <div class="legend-item">
            <div class="legend-color full" style="background-color: #fff3cd; border-color: #ffc107;"></div>
            <span>Full Slot</span>
        </div>
        <div class="legend-item">
            <div class="legend-color blocked" style="background-color: #f8d7da; border-color: #dc3545;"></div>
            <span>Blocked Slot</span>
        </div>
    </div>

    <!-- Instructions -->
    <div class="instructions">
        <strong>Instructions:</strong>
        <ul class="mb-0">
            <li>Click on a slot to view details and manage products in that slot</li>
            <li>Empty slots (light blue) will be highlighted for easy recognition</li>
            <li>Occupied slots are green, full slots are yellow, blocked slots are red</li>
        </ul>
    </div>

    <!-- Slot Grid -->
    <div class="slot-grid-container">
        <div class="screen-indicator">OPERATION AREA</div>
        
        <c:if test="${empty slots}">
            <div style="text-align: center; padding: 40px; color: #6c757d; background-color: #f8f9fa; border-radius: 8px; margin: 20px 0;">
                <h5>No slots in this zone</h5>
                <p>Please go back and create slots for this zone first.</p>
                <a href="${pageContext.request.contextPath}/warehouse-layout" class="btn btn-primary">Back</a>
            </div>
        </c:if>
        
        <c:if test="${not empty slots}">
        <c:set var="maxColValue" value="${maxCol > 0 ? maxCol : 10}" />
        <c:set var="maxRowValue" value="${maxRow > 0 ? maxRow : 8}" />
        <div class="slot-grid" id="slotGrid" style="grid-template-columns: 30px repeat(${maxColValue}, 80px); grid-template-rows: 30px repeat(${maxRowValue}, 80px);">
            <!-- Header row -->
            <div></div>
            <c:if test="${maxCol > 0}">
            <c:forEach var="col" begin="1" end="${maxCol}">
            <div class="col-header">${col}</div>
            </c:forEach>
            </c:if>
            
            <!-- Slot rows -->
            <c:if test="${maxRow > 0}">
            <c:forEach var="row" begin="1" end="${maxRow}">
                <c:set var="rowCharIndex" value="${row - 1}" />
                <c:set var="rowChar" value="" />
                <c:choose>
                    <c:when test="${rowCharIndex == 0}"><c:set var="rowChar" value="A" /></c:when>
                    <c:when test="${rowCharIndex == 1}"><c:set var="rowChar" value="B" /></c:when>
                    <c:when test="${rowCharIndex == 2}"><c:set var="rowChar" value="C" /></c:when>
                    <c:when test="${rowCharIndex == 3}"><c:set var="rowChar" value="D" /></c:when>
                    <c:when test="${rowCharIndex == 4}"><c:set var="rowChar" value="E" /></c:when>
                    <c:when test="${rowCharIndex == 5}"><c:set var="rowChar" value="F" /></c:when>
                    <c:when test="${rowCharIndex == 6}"><c:set var="rowChar" value="G" /></c:when>
                    <c:when test="${rowCharIndex == 7}"><c:set var="rowChar" value="H" /></c:when>
                    <c:when test="${rowCharIndex == 8}"><c:set var="rowChar" value="I" /></c:when>
                    <c:when test="${rowCharIndex == 9}"><c:set var="rowChar" value="J" /></c:when>
                    <c:when test="${rowCharIndex == 10}"><c:set var="rowChar" value="K" /></c:when>
                    <c:when test="${rowCharIndex == 11}"><c:set var="rowChar" value="L" /></c:when>
                    <c:when test="${rowCharIndex == 12}"><c:set var="rowChar" value="M" /></c:when>
                    <c:when test="${rowCharIndex == 13}"><c:set var="rowChar" value="N" /></c:when>
                    <c:when test="${rowCharIndex == 14}"><c:set var="rowChar" value="O" /></c:when>
                    <c:when test="${rowCharIndex == 15}"><c:set var="rowChar" value="P" /></c:when>
                    <c:when test="${rowCharIndex == 16}"><c:set var="rowChar" value="Q" /></c:when>
                    <c:when test="${rowCharIndex == 17}"><c:set var="rowChar" value="R" /></c:when>
                    <c:when test="${rowCharIndex == 18}"><c:set var="rowChar" value="S" /></c:when>
                    <c:when test="${rowCharIndex == 19}"><c:set var="rowChar" value="T" /></c:when>
                    <c:when test="${rowCharIndex == 20}"><c:set var="rowChar" value="U" /></c:when>
                    <c:when test="${rowCharIndex == 21}"><c:set var="rowChar" value="V" /></c:when>
                    <c:when test="${rowCharIndex == 22}"><c:set var="rowChar" value="W" /></c:when>
                    <c:when test="${rowCharIndex == 23}"><c:set var="rowChar" value="X" /></c:when>
                    <c:when test="${rowCharIndex == 24}"><c:set var="rowChar" value="Y" /></c:when>
                    <c:otherwise><c:set var="rowChar" value="Z" /></c:otherwise>
                </c:choose>
                <div class="row-label">${rowChar}</div>
                <c:forEach var="col" begin="1" end="${maxCol}">
                    <c:set var="slot" value="${null}" />
                    <c:forEach items="${slots}" var="s">
                        <c:if test="${s.rowNo == row && s.colNo == col}">
                            <c:set var="slot" value="${s}" />
                        </c:if>
                    </c:forEach>
                    <c:choose>
                        <c:when test="${slot != null}">
                            <c:set var="slotClass" value="" />
                            <c:choose>
                                <c:when test="${slot.status == 'BLOCKED'}">
                                    <c:set var="slotClass" value="blocked" />
                                </c:when>
                                <c:when test="${slot.isEmpty}">
                                    <c:set var="slotClass" value="empty" />
                                </c:when>
                                <c:when test="${slot.maxCapacity != null && slot.usedCapacity >= slot.maxCapacity}">
                                    <c:set var="slotClass" value="full" />
                                </c:when>
                                <c:otherwise>
                                    <c:set var="slotClass" value="occupied" />
                                </c:otherwise>
                            </c:choose>
                            <div class="slot-item ${slotClass}"
                                 data-slot-id="${slot.slotId}"
                                 onclick="openSlotDetail(${slot.slotId})">
                                <div class="slot-code"><c:out value="${slot.slotCode}" /></div>
                                <c:choose>
                                    <c:when test="${!slot.isEmpty}">
                                        <div class="slot-info">
                                            ${fn:length(slot.products)} items<br>
                                            <c:choose>
                                                <c:when test="${slot.usedCapacity != null}">
                                                    <fmt:formatNumber value="${slot.usedCapacity}" maxFractionDigits="0" />
                                                </c:when>
                                                <c:otherwise>0</c:otherwise>
                                            </c:choose>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="slot-info">Empty</div>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div style="width: 80px; height: 80px;"></div>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>
            </c:forEach>
            </c:if>
        </div>
        </c:if>
    </div>

    <!-- Slot detail modal -->
    <div class="modal fade" id="slotDetailModal" tabindex="-1" aria-labelledby="slotDetailModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="slotDetailModalLabel">Slot Details</h5>
                    <button type="button" class="btn-close-modal" onclick="closeSlotDetailModal()" aria-label="Close" style="background: none; border: none; font-size: 24px; color: #dc3545; cursor: pointer; padding: 0; width: 30px; height: 30px; line-height: 30px; text-align: center;">×</button>
                </div>
            <div class="modal-body" id="slotDetailContent">
                <div class="text-center">
                    <div class="spinner-border" role="status">
                        <span class="visually-hidden">Loading...</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

    <script>
    const zoneId = ${zone.zoneId};
    const variants = [
        <c:forEach items="${variants}" var="v" varStatus="status">
        <c:if test="${status.index > 0}">,</c:if>
        {
            variantId: ${v.variantId},
            variantSku: "<c:out value='${v.variantSku}' escapeXml='false' />",
            productName: "<c:out value='${v.productName}' escapeXml='false' />"
        }
        </c:forEach>
    ];

let slotDetailModal = null;

function openSlotDetail(slotId) {
    const modalElement = document.getElementById('slotDetailModal');
    if (!slotDetailModal) {
        slotDetailModal = new bootstrap.Modal(modalElement, {
            backdrop: true,
            keyboard: true
        });
    }
    const modalContent = document.getElementById('slotDetailContent');
    modalContent.innerHTML = '<div class="text-center"><div class="spinner-border" role="status"><span class="visually-hidden">Loading...</span></div></div>';
    slotDetailModal.show();
    
    loadSlotDetail(slotId);
}

function closeSlotDetailModal() {
    if (slotDetailModal) {
        slotDetailModal.hide();
    } else {
        const modalElement = document.getElementById('slotDetailModal');
        if (modalElement) {
            const modal = bootstrap.Modal.getInstance(modalElement);
            if (modal) {
                modal.hide();
            }
        }
    }
}

function loadSlotDetail(slotId) {
    const modalContent = document.getElementById('slotDetailContent');
    
    fetch('${pageContext.request.contextPath}/warehouse-layout?action=slot-detail&slotId=' + slotId)
        .then(response => response.json())
        .then(data => {
            displaySlotDetail(data, slotId);
        })
        .catch(error => {
            modalContent.innerHTML = '<div class="alert alert-danger">Error: ' + error.message + '</div>';
        });
}

function displaySlotDetail(slot, slotId) {
    const modalContent = document.getElementById('slotDetailContent');
    
    // Calculate capacity percentage
    const maxCap = slot.maxCapacity ? parseFloat(slot.maxCapacity) : null;
    const usedCap = slot.usedCapacity ? parseFloat(slot.usedCapacity) : 0;
    const availableCap = slot.availableCapacity ? parseFloat(slot.availableCapacity) : 0;
    const capacityPercent = maxCap && maxCap > 0 ? (usedCap / maxCap * 100).toFixed(1) : null;
    const capacityColor = capacityPercent && capacityPercent >= 90 ? 'danger' : capacityPercent && capacityPercent >= 70 ? 'warning' : 'success';
    
    let html = '';
    
    // First row: Slot Information (full width)
    html += '<div class="row mb-3">';
    
    // Slot Information (including Capacity and Edit Capacity) - Full width
    html += '<div class="col-12">';
    html += '<div class="card">';
    html += '<div class="card-header bg-primary text-white"><h6 class="mb-0"><i class="fas fa-info-circle me-2"></i>Slot Information</h6></div>';
    html += '<div class="card-body">';
    
    // Basic Slot Information
    html += '<div class="mb-3">';
    html += '<table class="table table-sm table-borderless mb-0">';
    html += '<tr><td class="fw-bold" style="width: 40%;">Slot Code:</td><td>' + (slot.slotCode || 'N/A') + '</td></tr>';
    html += '<tr><td class="fw-bold">Status:</td><td><span class="badge bg-' + 
        (slot.status === 'ACTIVE' ? 'success' : slot.status === 'BLOCKED' ? 'danger' : 'secondary') + '">' + 
        (slot.status || 'N/A') + '</span></td></tr>';
    html += '<tr><td class="fw-bold">Empty:</td><td>' + (slot.isEmpty ? '<span class="badge bg-success">Yes</span>' : '<span class="badge bg-warning">No</span>') + '</td></tr>';
    html += '</table>';
    html += '</div>';
    
    // Divider
    html += '<hr>';
    
    // Capacity Information
    html += '<div class="mb-3">';
    html += '<h6 class="text-muted mb-3"><i class="fas fa-cube me-2"></i>Capacity Information</h6>';
    html += '<div class="mb-2">';
    html += '<div class="d-flex justify-content-between mb-1">';
    html += '<span class="small">Used Capacity:</span>';
    html += '<span class="fw-bold">' + Math.floor(usedCap) + '</span>';
    html += '</div>';
    if (maxCap !== null) {
        html += '<div class="progress" style="height: 20px;">';
        html += '<div class="progress-bar bg-' + capacityColor + '" role="progressbar" style="width: ' + (capacityPercent || 0) + '%" aria-valuenow="' + (capacityPercent || 0) + '" aria-valuemin="0" aria-valuemax="100">';
        html += (capacityPercent || 0) + '%';
        html += '</div></div>';
    }
    html += '</div>';
    html += '<div class="mb-2">';
    html += '<div class="d-flex justify-content-between mb-1">';
    html += '<span class="small">Available Capacity:</span>';
    html += '<span class="fw-bold text-success">' + Math.floor(availableCap) + '</span>';
    html += '</div>';
    html += '</div>';
    html += '<div class="mb-2">';
    html += '<div class="d-flex justify-content-between mb-1">';
    html += '<span class="small">Max Capacity:</span>';
    html += '<span class="fw-bold">' + (maxCap !== null ? Math.floor(maxCap) : 'Unlimited') + '</span>';
    html += '</div>';
    html += '</div>';
    html += '</div>';
    
    // Divider
    html += '<hr>';
    
    // Edit Max Capacity Form
    html += '<div>';
    html += '<h6 class="text-muted mb-3"><i class="fas fa-edit me-2"></i>Edit Max Capacity</h6>';
    html += '<form id="updateCapacityForm" onsubmit="updateMaxCapacity(event, ' + slotId + ')">';
    html += '<div class="input-group mb-2">';
    html += '<input type="number" id="maxCapacityInput" class="form-control" step="1" min="0" placeholder="Enter max capacity" value="' + (maxCap !== null ? Math.floor(maxCap) : '') + '">';
    html += '<button class="btn btn-primary" type="submit"><i class="fas fa-save me-1"></i>Update</button>';
    html += '</div>';
    html += '<small class="text-muted">Leave empty for unlimited capacity. Must be >= used capacity (' + Math.floor(usedCap) + ')</small>';
    html += '<div id="capacityUpdateMessage" class="mt-2"></div>';
    html += '</form>';
    html += '</div>';
    
    html += '</div></div>'; // End card body and card
    html += '</div>'; // End column
    html += '</div>'; // End first row
    
    // Second row: Products in Slot (Full width, below)
    html += '<div class="row">';
    html += '<div class="col-12">';
    html += '<div class="card">';
    html += '<div class="card-header bg-success text-white"><h6 class="mb-0"><i class="fas fa-boxes me-2"></i>Products in Slot</h6></div>';
    html += '<div class="card-body p-0">';
    if (slot.products && slot.products.length > 0) {
        html += '<div class="table-responsive">';
        html += '<table class="table table-sm table-hover mb-0">';
        html += '<thead class="table-light"><tr><th>SKU</th><th>Product Name</th><th>Condition</th><th>Quantity (On Hand)</th><th>Quantity (Available)</th></tr></thead><tbody>';
        slot.products.forEach(p => {
            html += '<tr>';
            html += '<td><strong>' + (p.variantSku || '') + '</strong></td>';
            html += '<td>' + (p.productName || '') + '</td>';
            html += '<td><span class="badge bg-' + 
                (p.condition === 'GOOD' ? 'success' : p.condition === 'DAMAGED' ? 'danger' : 'warning') + '">' + 
                (p.condition || '') + '</span></td>';
            html += '<td><strong>' + (p.qtyOnHand ? Math.floor(parseFloat(p.qtyOnHand)) : '0') + '</strong></td>';
            html += '<td><strong class="text-success">' + (p.qtyAvailable ? Math.floor(parseFloat(p.qtyAvailable)) : '0') + '</strong></td>';
            html += '</tr>';
        });
        html += '</tbody></table></div>';
    } else {
        html += '<div class="text-center p-4 text-muted"><i class="fas fa-inbox fa-2x mb-2"></i><p class="mb-0">No products in this slot</p></div>';
    }
    html += '</div></div>';
    html += '</div>'; // End col-12
    html += '</div>'; // End second row
    
    modalContent.innerHTML = html;
}

function updateMaxCapacity(event, slotId) {
    event.preventDefault();
    const maxCapacityInput = document.getElementById('maxCapacityInput');
    const messageDiv = document.getElementById('capacityUpdateMessage');
    const maxCapacity = maxCapacityInput.value.trim();
    
    messageDiv.innerHTML = '<div class="spinner-border spinner-border-sm me-2" role="status"></div>Updating...';
    messageDiv.className = 'mt-2 text-info';
    
    const formData = new URLSearchParams();
    formData.append('action', 'update-max-capacity');
    formData.append('slotId', slotId);
    if (maxCapacity) {
        formData.append('maxCapacity', maxCapacity);
    }
    
    fetch('${pageContext.request.contextPath}/warehouse-layout', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: formData.toString()
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            messageDiv.innerHTML = '<div class="alert alert-success small mb-0"><i class="fas fa-check-circle me-1"></i>' + (data.message || 'Max capacity updated successfully') + '</div>';
            messageDiv.className = 'mt-2';
            // Reload slot detail after 1 second
            setTimeout(() => {
                loadSlotDetail(slotId);
            }, 1000);
        } else {
            messageDiv.innerHTML = '<div class="alert alert-danger small mb-0"><i class="fas fa-exclamation-circle me-1"></i>' + (data.error || 'Failed to update max capacity') + '</div>';
            messageDiv.className = 'mt-2';
        }
    })
    .catch(error => {
        messageDiv.innerHTML = '<div class="alert alert-danger small mb-0"><i class="fas fa-exclamation-circle me-1"></i>Error: ' + error.message + '</div>';
        messageDiv.className = 'mt-2';
    });
}
    </script>
</t:layout>
