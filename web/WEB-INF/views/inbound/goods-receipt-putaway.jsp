<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@taglib tagdir="/WEB-INF/tags/" prefix="t" %>
        <%@taglib uri="jakarta.tags.core" prefix="c" %>
            <%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

                <t:layout title="Goods Receipt Putaway">
                    <div class="container-fluid py-4">
                        <!-- Back Link -->
                        <div class="mb-3">
                            <a href="${pageContext.request.contextPath}/goods-receipt?action=detail&id=${grn.grnId}"
                                class="text-decoration-none text-muted">
                                <i class="fas fa-arrow-left me-1"></i> Back to Detail
                            </a>
                        </div>

                        <div class="card shadow-sm border-0 mb-4">
                            <div class="card-header bg-primary text-white py-3">
                                <h5 class="card-title mb-0"><i class="fas fa-dolly-flatbed me-2"></i> Confirm Putaway -
                                    ${grn.grnNumber}</h5>
                            </div>
                            <div class="card-body bg-light-subtle">
                                <div class="row text-center mb-0">
                                    <div class="col">
                                        <p class="text-muted small mb-1 text-uppercase fw-bold">Purchase Order</p>
                                        <p class="mb-0 fw-semibold">${grn.poNumber}</p>
                                    </div>
                                    <div class="col border-start">
                                        <p class="text-muted small mb-1 text-uppercase fw-bold">Warehouse</p>
                                        <p class="mb-0 fw-semibold">${warehouseName}</p>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <form action="${pageContext.request.contextPath}/goods-receipt" method="post">
                            <input type="hidden" name="action" value="confirmputaway" />
                            <input type="hidden" name="grnId" value="${grn.grnId}" />
                            <input type="hidden" name="warehouseId" value="${grn.warehouseId}" />

                            <form id="putawayForm" action="${pageContext.request.contextPath}/goods-receipt"
                                method="post">
                                <input type="hidden" name="action" value="confirmputaway" />
                                <input type="hidden" name="grnId" value="${grn.grnId}" />
                                <input type="hidden" name="warehouseId" value="${grn.warehouseId}" />

                                <!-- Inventory Summary -->
                                <div class="row mb-4">
                                    <c:forEach var="l" items="${lines}">
                                        <div class="col-md-3">
                                            <div class="card border-0 shadow-sm h-100 product-summary-card">
                                                <div class="card-body py-3">
                                                    <div
                                                        class="small fw-bold text-uppercase text-muted mb-2 text-center border-bottom pb-1">
                                                        <i class="fas fa-barcode me-1"></i> ${l.sku}
                                                    </div>

                                                    <!-- Good Items Summary -->
                                                    <c:if test="${l.qtyGood > 0}">
                                                        <div class="mb-3">
                                                            <div
                                                                class="d-flex justify-content-between align-items-center mb-1">
                                                                <span
                                                                    class="badge bg-success-subtle text-success small">GOOD</span>
                                                                <span class="small fw-bold"
                                                                    id="summary_received_${l.grnLineId}_STORAGE"
                                                                    data-total="<fmt:formatNumber value='${l.qtyGood}' pattern='0' />">
                                                                    <span id="summary_assigned_${l.grnLineId}_STORAGE">
                                                                        <fmt:formatNumber value="${l.qtyGood}"
                                                                            pattern="#,##0" />
                                                                    </span> /
                                                                    <fmt:formatNumber value="${l.qtyGood}"
                                                                        pattern="#,##0" />
                                                                </span>
                                                            </div>
                                                            <div class="progress" style="height: 6px;">
                                                                <div class="progress-bar bg-success" role="progressbar"
                                                                    id="progress_${l.grnLineId}_STORAGE"
                                                                    style="width: 0%"></div>
                                                            </div>
                                                        </div>
                                                    </c:if>

                                                    <!-- Damaged Items Summary -->
                                                    <c:if test="${l.qtyDamaged > 0}">
                                                        <div class="mb-1">
                                                            <div
                                                                class="d-flex justify-content-between align-items-center mb-1">
                                                                <span
                                                                    class="badge bg-danger-subtle text-danger small">DAMAGED</span>
                                                                <span class="small fw-bold"
                                                                    id="summary_received_${l.grnLineId}_DAMAGE"
                                                                    data-total="<fmt:formatNumber value='${l.qtyDamaged}' pattern='0' />">
                                                                    <span id="summary_assigned_${l.grnLineId}_DAMAGE">
                                                                        <fmt:formatNumber value="${l.qtyDamaged}"
                                                                            pattern="#,##0" />
                                                                    </span> /
                                                                    <fmt:formatNumber value="${l.qtyDamaged}"
                                                                        pattern="#,##0" />
                                                                </span>
                                                            </div>
                                                            <div class="progress" style="height: 6px;">
                                                                <div class="progress-bar bg-danger" role="progressbar"
                                                                    id="progress_${l.grnLineId}_DAMAGE"
                                                                    style="width: 0%"></div>
                                                            </div>
                                                        </div>
                                                    </c:if>
                                                </div>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </div>

                                <div class="card shadow-sm border-0 mb-4">
                                    <div
                                        class="card-header bg-dark text-white d-flex justify-content-between align-items-center py-3">
                                        <h5 class="card-title mb-0"><i class="fas fa-boxes me-2"></i> Allocation Details
                                        </h5>
                                        <div class="small opacity-75">Assign all items to slots to confirm</div>
                                    </div>
                                    <div class="card-body p-0">
                                        <div class="table-responsive">
                                            <table class="table table-bordered align-middle mb-0" id="putawayTable">
                                                <thead class="bg-light text-muted small text-uppercase">
                                                    <tr>
                                                        <th class="ps-4" style="width: 20%;">Product Information</th>
                                                        <th style="width: 12%;">Type</th>
                                                        <th class="text-center" style="width: 12%;">Quantity</th>
                                                        <th style="width: 30%;">Destination Slot</th>
                                                        <th class="text-center" style="width: 15%;">Slot Capacity</th>
                                                        <th class="text-center" style="width: 11%;">Actions</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    <c:forEach var="l" items="${lines}" varStatus="status">
                                                        <!-- Storage Assignment(s) -->
                                                        <c:if test="${l.qtyGood > 0}">
                                                            <tr class="assignment-row storage-row"
                                                                data-grn-line-id="${l.grnLineId}" data-sku="${l.sku}"
                                                                data-max="<fmt:formatNumber value='${l.qtyGood}' pattern='0' />"
                                                                data-type="STORAGE">
                                                                <td class="ps-4">
                                                                    <div class="fw-bold text-primary">${l.sku}</div>
                                                                    <div class="small text-muted text-truncate"
                                                                        style="max-width: 180px;">${l.productName}</div>
                                                                </td>
                                                                <td><span
                                                                        class="badge bg-success-subtle text-success border border-success-subtle w-100">GOOD
                                                                        (Z-STO)</span></td>
                                                                <td>
                                                                    <input type="number"
                                                                        class="form-control text-center qty-input"
                                                                        name="qty_${l.grnLineId}_STORAGE[]"
                                                                        value="<fmt:formatNumber value='${l.qtyGood}' pattern='0' />"
                                                                        min="1"
                                                                        max="<fmt:formatNumber value='${l.qtyGood}' pattern='0' />"
                                                                        step="1">
                                                                </td>
                                                                <td>
                                                                    <select name="slotId_${l.grnLineId}_STORAGE[]"
                                                                        class="form-select slot-select" required>
                                                                        <option value="">-- Select Storage Slot --
                                                                        </option>
                                                                        <c:forEach var="slot" items="${storageSlots}">
                                                                            <option value="${slot.slotId}"
                                                                                data-capacity="<fmt:formatNumber value='${slot.availableCapacity}' pattern='0' />"
                                                                                data-max-capacity="<fmt:formatNumber value='${slot.maxCapacity}' pattern='0' />"
                                                                                data-used="<fmt:formatNumber value='${slot.usedCapacity}' pattern='0' />">
                                                                                ${slot.slotCode}
                                                                            </option>
                                                                        </c:forEach>
                                                                    </select>
                                                                    <div class="slot-info small mt-1 text-muted px-2">
                                                                    </div>
                                                                </td>
                                                                <td class="text-center">
                                                                    <div
                                                                        class="slot-capacity-display small fw-bold text-muted">
                                                                        --</div>
                                                                </td>
                                                                <td class="text-center">
                                                                    <button type="button"
                                                                        class="btn btn-sm btn-outline-primary add-assignment-btn"
                                                                        title="Split into another slot">
                                                                        <i class="fas fa-plus"></i>
                                                                    </button>
                                                                </td>
                                                            </tr>
                                                        </c:if>

                                                        <!-- Damage Assignment(s) -->
                                                        <c:if test="${l.qtyDamaged > 0}">
                                                            <tr class="assignment-row damage-row"
                                                                data-grn-line-id="${l.grnLineId}" data-sku="${l.sku}"
                                                                data-max="<fmt:formatNumber value='${l.qtyDamaged}' pattern='0' />"
                                                                data-type="DAMAGE">
                                                                <td class="ps-4">
                                                                    <div class="fw-bold text-primary">${l.sku}</div>
                                                                    <div class="small text-muted text-truncate"
                                                                        style="max-width: 180px;">${l.productName}</div>
                                                                </td>
                                                                <td><span
                                                                        class="badge bg-danger-subtle text-danger border border-danger-subtle w-100">DAMAGED
                                                                        (Z-DAM)</span></td>
                                                                <td>
                                                                    <input type="number"
                                                                        class="form-control text-center qty-input"
                                                                        name="qty_${l.grnLineId}_DAMAGE[]"
                                                                        value="<fmt:formatNumber value='${l.qtyDamaged}' pattern='0' />"
                                                                        min="1"
                                                                        max="<fmt:formatNumber value='${l.qtyDamaged}' pattern='0' />"
                                                                        step="1">
                                                                </td>
                                                                <td>
                                                                    <select name="slotId_${l.grnLineId}_DAMAGE[]"
                                                                        class="form-select slot-select" required>
                                                                        <option value="">-- Select Damage Slot --
                                                                        </option>
                                                                        <c:forEach var="slot" items="${damageSlots}">
                                                                            <option value="${slot.slotId}"
                                                                                data-capacity="<fmt:formatNumber value='${slot.availableCapacity}' pattern='0' />"
                                                                                data-max-capacity="<fmt:formatNumber value='${slot.maxCapacity}' pattern='0' />"
                                                                                data-used="<fmt:formatNumber value='${slot.usedCapacity}' pattern='0' />">
                                                                                ${slot.slotCode}
                                                                            </option>
                                                                        </c:forEach>
                                                                    </select>
                                                                    <div class="slot-info small mt-1 text-muted px-2">
                                                                    </div>
                                                                </td>
                                                                <td class="text-center">
                                                                    <div
                                                                        class="slot-capacity-display small fw-bold text-muted">
                                                                        --</div>
                                                                </td>
                                                                <td class="text-center">
                                                                    <button type="button"
                                                                        class="btn btn-sm btn-outline-primary add-assignment-btn"
                                                                        title="Split into another slot">
                                                                        <i class="fas fa-plus"></i>
                                                                    </button>
                                                                </td>
                                                            </tr>
                                                        </c:if>
                                                    </c:forEach>
                                                </tbody>
                                            </table>
                                        </div>
                                    </div>
                                    <div class="card-footer bg-white border-top-0 py-3">
                                        <div class="d-flex justify-content-between align-items-center">
                                            <div id="validationMsg" class="text-danger small fw-bold"></div>
                                            <button type="submit" id="submitBtn" class="btn btn-primary px-5 shadow-sm">
                                                <i class="fas fa-check-circle me-2"></i> Confirm Putaway
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            </form>
                    </div>

                    <script>
                        document.addEventListener('DOMContentLoaded', function () {
                            const putawayTable = document.getElementById('putawayTable');
                            const putawayForm = document.getElementById('putawayForm');
                            const submitBtn = document.getElementById('submitBtn');

                            function updateSummaryAndLogic() {
                                const totals = {};
                                let allValid = true;
                                let validationMsg = "";

                                // Track cumulative used capacity per slot across all rows in this form
                                const currentSlotUsage = {};

                                // First pass: Calculate cumulative usage
                                document.querySelectorAll('.assignment-row').forEach(row => {
                                    const slotSelect = row.querySelector('.slot-select');
                                    const qtyInput = row.querySelector('.qty-input');
                                    const qty = parseFloat(qtyInput.value) || 0;
                                    
                                    if (slotSelect.value) {
                                        currentSlotUsage[slotSelect.value] = (currentSlotUsage[slotSelect.value] || 0) + qty;
                                    }
                                });

                                // Second pass: Validate slots and capacity
                                document.querySelectorAll('.assignment-row').forEach(row => {
                                    const grnLineId = row.dataset.grnLineId;
                                    const sku = row.dataset.sku;
                                    const type = row.dataset.type;
                                    const qtyInput = row.querySelector('.qty-input');
                                    const qty = parseFloat(qtyInput.value) || 0;
                                    const slotSelect = row.querySelector('.slot-select');
                                    const selectedSlot = slotSelect.options[slotSelect.selectedIndex];
                                    const slotInfo = row.querySelector('.slot-info');
                                    const capacityDisplay = row.querySelector('.slot-capacity-display');

                                    if (!totals[grnLineId]) totals[grnLineId] = { STORAGE: 0, DAMAGE: 0 };

                                    if (selectedSlot && selectedSlot.value) {
                                        const origAvail = parseFloat(selectedSlot.dataset.capacity) || 0;
                                        const max = parseFloat(selectedSlot.dataset.maxCapacity) || 0;
                                        const origUsed = parseFloat(selectedSlot.dataset.used) || 0;

                                        // Total usage of this slot across all rows
                                        const totalUsageOfThisSlot = currentSlotUsage[selectedSlot.value] || 0;
                                        const remainingAvailForThisSlot = origAvail - totalUsageOfThisSlot + qty; // Avail before this row's contribution

                                        // Display projected state for this SPECIFIC slot (overall)
                                        const overallUsed = origUsed + totalUsageOfThisSlot;
                                        const overallAvail = Math.max(0, origAvail - totalUsageOfThisSlot);
                                        
                                        capacityDisplay.innerHTML = '<div class="fw-bold ' + (overallUsed > max ? 'text-danger' : 'text-dark') + '">' + 
                                            overallUsed + ' / ' + max + '</div>' +
                                            '<div class="text-muted" style="font-size: 0.75rem;">Total Avail: ' + overallAvail + '</div>';

                                        if (qty > remainingAvailForThisSlot + 0.001) {
                                            slotInfo.innerHTML = '<i class="fas fa-exclamation-triangle text-danger me-1"></i> Row exceeds available (' + remainingAvailForThisSlot + ')';
                                            qtyInput.classList.add('is-invalid');
                                            allValid = false;
                                            if (!validationMsg) validationMsg = "Capacity exceeded for " + sku + " in slot " + selectedSlot.text;
                                        } else {
                                            slotInfo.innerHTML = '<i class="fas fa-check text-success me-1"></i> Fits in slot';
                                            qtyInput.classList.remove('is-invalid');
                                            totals[grnLineId][type] += qty;
                                        }
                                    } else {
                                        slotInfo.innerHTML = "";
                                        capacityDisplay.textContent = '--';
                                        qtyInput.classList.remove('is-invalid');
                                        if (qty > 0) {
                                            allValid = false;
                                            if (!validationMsg) validationMsg = "Missing destination slot for " + sku;
                                        }
                                    }
                                });

                                // Third pass: Update summary UI
                                document.querySelectorAll('[id^="summary_received_"]').forEach(summaryDiv => {
                                    const idParts = summaryDiv.id.split('_');
                                    const grnLineId = idParts[2];
                                    const type = idParts[3];

                                    const totalReceived = parseFloat(summaryDiv.dataset.total);
                                    const assigned = (totals[grnLineId] && totals[grnLineId][type]) || 0;

                                    const assignedSpan = document.getElementById('summary_assigned_' + grnLineId + '_' + type);
                                    const progressBar = document.getElementById('progress_' + grnLineId + '_' + type);

                                    if (assignedSpan && progressBar) {
                                        const remaining = totalReceived - assigned;
                                        assignedSpan.innerHTML = 'Remaining: <span class="' + (remaining < 0 ? 'text-danger' : 'text-primary') + '">' + remaining + '</span>';

                                        const percent = Math.min((assigned / totalReceived) * 100, 100);
                                        progressBar.style.width = Math.max(0, percent) + '%';

                                        if (Math.abs(assigned - totalReceived) < 0.001) {
                                            progressBar.className = 'progress-bar bg-success';
                                        } else if (assigned > totalReceived) {
                                            progressBar.className = 'progress-bar bg-danger';
                                            allValid = false;
                                            validationMsg = "Assigned quantity exceeds received quantity.";
                                        } else {
                                            progressBar.className = 'progress-bar bg-warning';
                                            allValid = false;
                                            if (!validationMsg) validationMsg = "Some items are not fully assigned.";
                                        }
                                    }
                                });

                                document.getElementById('validationMsg').textContent = validationMsg;
                                submitBtn.disabled = !allValid;

                                rebalanceSlots(currentSlotUsage);
                            }

                            function rebalanceSlots(currentUsage) {
                                document.querySelectorAll('.slot-select').forEach(select => {
                                    const currentValue = select.value;
                                    const currentQtyThisRow = parseFloat(select.closest('tr').querySelector('.qty-input').value) || 0;

                                    Array.from(select.options).forEach(option => {
                                        if (!option.value) return;

                                        const origAvail = parseFloat(option.dataset.capacity) || 0;
                                        const usageByOthers = (currentUsage[option.value] || 0) - (option.value === currentValue ? currentQtyThisRow : 0);
                                        const effectiveAvail = origAvail - usageByOthers;

                                        // If slot is exhausted by OTHERS, disable it
                                        const isExhausted = effectiveAvail <= 0 && option.value !== currentValue;
                                        
                                        option.disabled = isExhausted;
                                        
                                        // Update text to show real-time available
                                        const baseText = option.text.split(' (')[0];
                                        if (effectiveAvail <= 0) {
                                            option.text = baseText + " (FULL)";
                                            option.style.color = "#dc3545";
                                        } else {
                                            option.text = baseText + " (" + effectiveAvail + " left)";
                                            option.style.color = "";
                                        }
                                    });
                                });
                            }

                            putawayTable.addEventListener('input', updateSummaryAndLogic);
                            
                            putawayTable.addEventListener('change', (e) => {
                                if (e.target.classList.contains('slot-select')) {
                                    const row = e.target.closest('tr');
                                    const qtyInput = row.querySelector('.qty-input');
                                    const selectedSlot = e.target.options[e.target.selectedIndex];

                                    if (selectedSlot && selectedSlot.value) {
                                        // Calculate usage BY OTHERS to determine truly available for this split
                                        const currentUsage = {};
                                        document.querySelectorAll('.slot-select').forEach(s => {
                                            if (s !== e.target && s.value) {
                                                const q = parseFloat(s.closest('tr').querySelector('.qty-input').value) || 0;
                                                currentUsage[s.value] = (currentUsage[s.value] || 0) + q;
                                            }
                                        });

                                        const origAvail = parseFloat(selectedSlot.dataset.capacity) || 0;
                                        const usedByOthers = currentUsage[selectedSlot.value] || 0;
                                        const realAvail = Math.max(0, origAvail - usedByOthers);
                                        const currentQty = parseFloat(qtyInput.value) || 0;

                                        if (currentQty > realAvail) {
                                            const overflow = currentQty - realAvail;
                                            
                                            // Show notification
                                            if (typeof Swal !== 'undefined') {
                                                Swal.fire({
                                                    icon: realAvail === 0 ? 'warning' : 'info',
                                                    title: realAvail === 0 ? 'Slot is Full' : 'Slot Capacity Exceeded',
                                                    text: realAvail === 0 
                                                        ? 'This slot is already full. Created a new row for all ' + overflow + ' items.'
                                                        : 'Moving ' + overflow + ' items to a new row. ' + realAvail + ' items will stay in this slot.',
                                                    toast: true,
                                                    position: 'top-end',
                                                    showConfirmButton: false,
                                                    timer: 4000
                                                });
                                            }

                                            qtyInput.value = realAvail;

                                            // Create new row
                                            const newRow = row.cloneNode(true);
                                            newRow.querySelector('.qty-input').value = overflow;
                                            newRow.querySelector('.slot-select').value = "";
                                            newRow.querySelector('.slot-info').innerHTML = "";
                                            newRow.querySelector('.slot-capacity-display').innerHTML = "--";
                                            newRow.querySelector('.qty-input').classList.remove('is-invalid');

                                            const actionTd = newRow.querySelector('td:last-child');
                                            actionTd.innerHTML = '<button type="button" class="btn btn-sm btn-outline-danger remove-assignment-btn"><i class="fas fa-trash"></i></button>';

                                            row.after(newRow);
                                            
                                            if (realAvail === 0) {
                                                e.target.value = ""; // Reset current row if nothing fits
                                            }
                                        }
                                    }
                                }
                                updateSummaryAndLogic();
                            });

                            putawayTable.addEventListener('click', (e) => {
                                const addBtn = e.target.closest('.add-assignment-btn');
                                const removeBtn = e.target.closest('.remove-assignment-btn');

                                if (addBtn) {
                                    const row = addBtn.closest('tr');
                                    const type = row.dataset.type;
                                    
                                    // Logic check: Is there any slot left in this zone?
                                    const selects = document.querySelectorAll(`.assignment-row[data-type="${type}"] .slot-select`);
                                    const allSlots = Array.from(selects[0].options).filter(opt => opt.value);
                                    
                                    // Calculate total usage of all slots in this zone
                                    const usage = {};
                                    document.querySelectorAll('.slot-select').forEach(s => {
                                        if (s.value) {
                                            const q = parseFloat(s.closest('tr').querySelector('.qty-input').value) || 0;
                                            usage[s.value] = (usage[s.value] || 0) + q;
                                        }
                                    });

                                    const availableSlots = allSlots.filter(opt => (parseFloat(opt.dataset.capacity) || 0) > (usage[opt.value] || 0));

                                    if (availableSlots.length === 0) {
                                        if (typeof Swal !== 'undefined') {
                                            Swal.fire({
                                                icon: 'error',
                                                title: 'Warehouse Full',
                                                text: 'All available slots in this zone are currently filled to capacity.',
                                                confirmButtonColor: '#0d6efd'
                                            });
                                        } else {
                                            alert("Warehouse Full: All available slots in this zone are filled.");
                                        }
                                        return;
                                    }

                                    const newRow = row.cloneNode(true);
                                    newRow.querySelector('.qty-input').value = 0;
                                    newRow.querySelector('.slot-select').value = "";
                                    newRow.querySelector('.slot-info').innerHTML = "";
                                    newRow.querySelector('.slot-capacity-display').innerHTML = "--";
                                    newRow.querySelector('td:last-child').innerHTML = `
                                        <button type="button" class="btn btn-sm btn-outline-danger remove-assignment-btn">
                                            <i class="fas fa-trash"></i>
                                        </button>
                                    `;
                                    row.after(newRow);
                                    updateSummaryAndLogic();
                                }

                                if (removeBtn) {
                                    removeBtn.closest('tr').remove();
                                    updateSummaryAndLogic();
                                }
                            });

                            updateSummaryAndLogic();
                        });
                    </script>

                    <style>
                        .form-select {
                            border-radius: 8px;
                            border-color: #e0e0e0;
                        }

                        .form-select:focus {
                            box-shadow: 0 0 0 0.25rem rgba(13, 110, 253, 0.15);
                        }

                        .badge {
                            font-weight: 600;
                        }

                        .bg-light-subtle {
                            background-color: #f8f9fa;
                        }
                    </style>
                </t:layout>