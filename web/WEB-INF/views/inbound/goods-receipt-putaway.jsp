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

        <form id="putawayForm" action="${pageContext.request.contextPath}/goods-receipt" method="post">
            <input type="hidden" name="action" value="confirmputaway" />
            <input type="hidden" name="grnId" value="${grn.grnId}" />
            <input type="hidden" name="warehouseId" value="${grn.warehouseId}" />

            <!-- Inventory Summary -->
            <div class="row mb-4">
                <c:forEach var="l" items="${lines}">
                    <div class="col-md-3">
                        <div class="card border-0 shadow-sm h-100 product-summary-card">
                            <div class="card-body py-3">
                                <div class="small fw-bold text-uppercase text-muted mb-2 text-center border-bottom pb-1">
                                    <i class="fas fa-barcode me-1"></i> ${l.sku}
                                </div>

                                <!-- Good Items Summary -->
                                <c:if test="${l.qtyGood > 0}">
                                    <div class="mb-3">
                                        <div class="d-flex justify-content-between align-items-center mb-1">
                                            <span class="badge bg-success-subtle text-success small">GOOD</span>
                                            <span class="small fw-bold" id="summary_received_${l.grnLineId}_STORAGE"
                                                data-total="<fmt:formatNumber value='${l.qtyGood}' pattern='0' />">
                                                <span id="summary_assigned_${l.grnLineId}_STORAGE">
                                                    Remaining: ${l.qtyGood}
                                                </span> /
                                                <fmt:formatNumber value="${l.qtyGood}" pattern="#,##0" />
                                            </span>
                                        </div>
                                        <div class="progress" style="height: 6px;">
                                            <div class="progress-bar bg-success" role="progressbar"
                                                id="progress_${l.grnLineId}_STORAGE" style="width: 0%"></div>
                                        </div>
                                    </div>
                                </c:if>

                                <!-- Damaged Items Summary -->
                                <c:if test="${l.qtyDamaged > 0}">
                                    <div class="mb-1">
                                        <div class="d-flex justify-content-between align-items-center mb-1">
                                            <span class="badge bg-danger-subtle text-danger small">DAMAGED</span>
                                            <span class="small fw-bold" id="summary_received_${l.grnLineId}_DAMAGE"
                                                data-total="<fmt:formatNumber value='${l.qtyDamaged}' pattern='0' />">
                                                <span id="summary_assigned_${l.grnLineId}_DAMAGE">
                                                    Remaining: ${l.qtyDamaged}
                                                </span> /
                                                <fmt:formatNumber value="${l.qtyDamaged}" pattern="#,##0" />
                                            </span>
                                        </div>
                                        <div class="progress" style="height: 6px;">
                                            <div class="progress-bar bg-danger" role="progressbar"
                                                id="progress_${l.grnLineId}_DAMAGE" style="width: 0%"></div>
                                        </div>
                                    </div>
                                </c:if>

                                <!-- Excess Items Summary -->
                                <c:if test="${l.qtyExtra > 0}">
                                    <div class="mb-1 mt-3">
                                        <div class="d-flex justify-content-between align-items-center mb-1">
                                            <span class="badge bg-info-subtle text-info small">EXCESS</span>
                                            <span class="small fw-bold" id="summary_received_${l.grnLineId}_EXCESS"
                                                data-total="<fmt:formatNumber value='${l.qtyExtra}' pattern='0' />">
                                                <span id="summary_assigned_${l.grnLineId}_EXCESS">
                                                    Remaining: ${l.qtyExtra}
                                                </span> /
                                                <fmt:formatNumber value="${l.qtyExtra}" pattern="#,##0" />
                                            </span>
                                        </div>
                                        <div class="progress" style="height: 6px;">
                                            <div class="progress-bar bg-info" role="progressbar"
                                                id="progress_${l.grnLineId}_EXCESS" style="width: 0%"></div>
                                        </div>
                                    </div>
                                </c:if>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>

            <div class="card shadow-sm border-0 mb-4">
                <div class="card-header bg-dark text-white d-flex justify-content-between align-items-center py-3">
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
                                        <tr class="assignment-row storage-row" data-grn-line-id="${l.grnLineId}"
                                            data-sku="${l.sku}"
                                            data-max="<fmt:formatNumber value='${l.qtyGood}' pattern='0' />"
                                            data-type="STORAGE">
                                            <td class="ps-4">
                                                <div class="fw-bold text-primary">${l.sku}</div>
                                                <div class="small text-muted text-truncate" style="max-width: 180px;">
                                                    ${l.productName}</div>
                                            </td>
                                            <td><span
                                                    class="badge bg-success-subtle text-success border border-success-subtle w-100">GOOD
                                                    (STORAGE)</span></td>
                                            <td>
                                                <input type="number" class="form-control text-center qty-input"
                                                    name="qty_${l.grnLineId}_STORAGE[]"
                                                    value="<fmt:formatNumber value='${l.qtyGood}' pattern='0' />" min="0"
                                                    max="<fmt:formatNumber value='${l.qtyGood}' pattern='0' />" step="1">
                                            </td>
                                            <td>
                                                <select name="slotId_${l.grnLineId}_STORAGE[]"
                                                    class="form-select slot-select">
                                                    <option value="">-- Select Storage Slot --
                                                    </option>
                                                    <c:forEach var="slot" items="${storageSlots}">
                                                        <c:set var="prodList">
                                                            <c:forEach var="p" items="${slot.products}"
                                                                varStatus="pst">${p.variantSku}: ${p.qtyOnHand}${not pst.last ? ' &#10; ' : ''}</c:forEach>
                                                        </c:set>
                                                        <option value="${slot.slotId}"
                                                            data-capacity="<fmt:formatNumber value='${slot.availableCapacity != null ? slot.availableCapacity : 0}' pattern='0' />"
                                                            data-max-capacity="<fmt:formatNumber value='${slot.maxCapacity != null ? slot.maxCapacity : 0}' pattern='0' />"
                                                            data-used="<fmt:formatNumber value='${slot.usedCapacity != null ? slot.usedCapacity : 0}' pattern='0' />"
                                                            title='<c:out value="${prodList}"/>'>
                                                            ${slot.slotCode}
                                                        </option>
                                                    </c:forEach>
                                                </select>
                                                <div class="slot-info small mt-1 text-muted px-2">
                                                </div>
                                            </td>
                                            <td class="text-center">
                                                <div class="slot-capacity-display small fw-bold text-muted">
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
                                        <tr class="assignment-row damage-row" data-grn-line-id="${l.grnLineId}"
                                            data-sku="${l.sku}"
                                            data-max="<fmt:formatNumber value='${l.qtyDamaged}' pattern='0' />"
                                            data-type="DAMAGE">
                                            <td class="ps-4">
                                                <div class="fw-bold text-primary">${l.sku}</div>
                                                <div class="small text-muted text-truncate" style="max-width: 180px;">
                                                    ${l.productName}</div>
                                            </td>
                                            <td><span
                                                    class="badge bg-danger-subtle text-danger border border-danger-subtle w-100">DAMAGED
                                                    (DAMAGE)</span></td>
                                            <td>
                                                <input type="number" class="form-control text-center qty-input"
                                                    name="qty_${l.grnLineId}_DAMAGE[]"
                                                    value="<fmt:formatNumber value='${l.qtyDamaged}' pattern='0' />"
                                                    min="0"
                                                    max="<fmt:formatNumber value='${l.qtyDamaged}' pattern='0' />"
                                                    step="1">
                                            </td>
                                            <td>
                                                <select name="slotId_${l.grnLineId}_DAMAGE[]"
                                                    class="form-select slot-select">
                                                    <option value="">-- Select Damage Slot --
                                                    </option>
                                                    <c:forEach var="slot" items="${damageSlots}">
                                                        <c:set var="prodList">
                                                            <c:forEach var="p" items="${slot.products}"
                                                                varStatus="pst">${p.variantSku}: ${p.qtyOnHand}${not pst.last ? ' &#10; ' : ''}</c:forEach>
                                                        </c:set>
                                                        <option value="${slot.slotId}"
                                                            data-capacity="<fmt:formatNumber value='${slot.availableCapacity != null ? slot.availableCapacity : 0}' pattern='0' />"
                                                            data-max-capacity="<fmt:formatNumber value='${slot.maxCapacity != null ? slot.maxCapacity : 0}' pattern='0' />"
                                                            data-used="<fmt:formatNumber value='${slot.usedCapacity != null ? slot.usedCapacity : 0}' pattern='0' />"
                                                            title='<c:out value="${prodList}"/>'>
                                                            ${slot.slotCode}
                                                        </option>
                                                    </c:forEach>
                                                </select>
                                                <div class="slot-info small mt-1 text-muted px-2">
                                                </div>
                                            </td>
                                            <td class="text-center">
                                                <div class="slot-capacity-display small fw-bold text-muted">
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

                                    <!-- Excess Assignment(s) -->
                                    <c:if test="${l.qtyExtra > 0}">
                                        <tr class="assignment-row excess-row" data-grn-line-id="${l.grnLineId}"
                                            data-sku="${l.sku}"
                                            data-max="<fmt:formatNumber value='${l.qtyExtra}' pattern='0' />"
                                            data-type="EXCESS">
                                            <td class="ps-4">
                                                <div class="fw-bold text-primary">${l.sku}</div>
                                                <div class="small text-muted text-truncate" style="max-width: 180px;">
                                                    ${l.productName}</div>
                                            </td>
                                            <td><span
                                                    class="badge bg-info-subtle text-info border border-info-subtle w-100">EXCESS
                                                    (EXCESS)</span></td>
                                            <td>
                                                <input type="number" class="form-control text-center qty-input"
                                                    name="qty_${l.grnLineId}_EXCESS[]"
                                                    value="<fmt:formatNumber value='${l.qtyExtra}' pattern='0' />"
                                                    min="0"
                                                    max="<fmt:formatNumber value='${l.qtyExtra}' pattern='0' />"
                                                    step="1">
                                            </td>
                                            <td>
                                                <select name="slotId_${l.grnLineId}_EXCESS[]"
                                                    class="form-select slot-select">
                                                    <option value="">-- Select Excess Slot --
                                                    </option>
                                                    <c:forEach var="slot" items="${excessSlots}">
                                                        <c:set var="prodList">
                                                            <c:forEach var="p" items="${slot.products}"
                                                                varStatus="pst">${p.variantSku}: ${p.qtyOnHand}${not pst.last ? ' &#10; ' : ''}</c:forEach>
                                                        </c:set>
                                                        <option value="${slot.slotId}"
                                                            data-capacity="<fmt:formatNumber value='${slot.availableCapacity != null ? slot.availableCapacity : 0}' pattern='0' />"
                                                            data-max-capacity="<fmt:formatNumber value='${slot.maxCapacity != null ? slot.maxCapacity : 0}' pattern='0' />"
                                                            data-used="<fmt:formatNumber value='${slot.usedCapacity != null ? slot.usedCapacity : 0}' pattern='0' />"
                                                            title='<c:out value="${prodList}"/>'>
                                                            ${slot.slotCode}
                                                        </option>
                                                    </c:forEach>
                                                </select>
                                                <div class="slot-info small mt-1 text-muted px-2">
                                                </div>
                                            </td>
                                            <td class="text-center">
                                                <div class="slot-capacity-display small fw-bold text-muted">
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
                        <c:if test="${canMutation}">
                            <button type="submit" id="submitBtn" class="btn btn-primary px-5 shadow-sm">
                                <i class="fas fa-check-circle me-2"></i> Confirm Putaway
                            </button>
                        </c:if>
                        <c:if test="${not canMutation}">
                            <span class="badge bg-warning text-dark"><i class="fas fa-info-circle me-1"></i> Read-only
                                mode</span>
                        </c:if>
                    </div>
                </div>
            </div>
        </form>
    </div>

    <script>
        document.addEventListener('DOMContentLoaded', function () {
            var putawayTable = document.getElementById('putawayTable');
            var putawayForm = document.getElementById('putawayForm');
            var submitBtn = document.getElementById('submitBtn');

            function updateSummaryAndLogic() {
                var totals = {};
                var allValid = true;
                var validationMsg = "";

                // Track cumulative used capacity per slot across all rows in this form
                var currentSlotUsage = {};

                // First pass: Calculate cumulative usage
                var allRows = document.querySelectorAll('.assignment-row');
                for (var i = 0; i < allRows.length; i++) {
                    var row = allRows[i];
                    var slotSelect = row.querySelector('.slot-select');
                    var qtyInput = row.querySelector('.qty-input');
                    var qty = parseFloat(qtyInput.value) || 0;

                    if (slotSelect && slotSelect.value) {
                        currentSlotUsage[slotSelect.value] = (currentSlotUsage[slotSelect.value] || 0) + qty;
                    }
                }

                // Second pass: Validate slots and capacity, and calculate per-GRN-line totals
                for (var j = 0; j < allRows.length; j++) {
                    var row_v = allRows[j];
                    var grnLineId = row_v.getAttribute('data-grn-line-id');
                    var sku = row_v.getAttribute('data-sku');
                    var type = row_v.getAttribute('data-type');
                    var qtyInput_v = row_v.querySelector('.qty-input');
                    var qty_v = parseFloat(qtyInput_v.value) || 0;
                    var slotSelect_v = row_v.querySelector('.slot-select');
                    var selectedSlot = slotSelect_v.options[slotSelect_v.selectedIndex];
                    var slotInfo = row_v.querySelector('.slot-info');
                    var capacityDisplay = row_v.querySelector('.slot-capacity-display');

                    if (!totals[grnLineId]) totals[grnLineId] = { STORAGE: 0, DAMAGE: 0, EXCESS: 0 };
                    totals[grnLineId][type] += qty_v;

                    if (selectedSlot && selectedSlot.value) {
                        var origAvail = parseFloat(selectedSlot.getAttribute('data-capacity')) || 0;
                        var maxCap = parseFloat(selectedSlot.getAttribute('data-max-capacity')) || 0;
                        var origUsed = parseFloat(selectedSlot.getAttribute('data-used')) || 0;

                        var totalUsageOfThisSlot = currentSlotUsage[selectedSlot.value] || 0;
                        var remainingAvailForThisSlot = origAvail - totalUsageOfThisSlot + qty_v;

                        var overallUsed = (origUsed + totalUsageOfThisSlot) || 0;
                        var overallAvail = Math.max(0, origAvail - totalUsageOfThisSlot) || 0;
                        
                        var displayContent = '<div class="' + (overallUsed > maxCap && maxCap > 0 ? 'text-danger fw-bold' : 'text-dark') + '">' + 
                                            overallUsed + ' / ' + maxCap + '</div>' +
                                            '<div class="text-muted" style="font-size: 0.7rem;">(Avail: ' + overallAvail + ')</div>';
                        capacityDisplay.innerHTML = displayContent;

                        if (qty_v > remainingAvailForThisSlot + 0.001) {
                            slotInfo.innerHTML = '<i class="fas fa-exclamation-triangle text-danger me-1"></i> Row exceeds available (' + remainingAvailForThisSlot + ')';
                            qtyInput_v.classList.add('is-invalid');
                            allValid = false;
                            if (!validationMsg) validationMsg = "Capacity exceeded for " + sku + " in slot " + selectedSlot.text;
                        } else {
                            slotInfo.innerHTML = '<i class="fas fa-check text-success me-1"></i> Fits in slot';
                            qtyInput_v.classList.remove('is-invalid');
                        }
                    } else {
                        slotInfo.innerHTML = "";
                        capacityDisplay.textContent = '--';
                        qtyInput_v.classList.remove('is-invalid');
                        if (qty_v > 0) {
                            allValid = false;
                            if (!validationMsg) validationMsg = "Missing destination slot for " + sku;
                        }
                    }
                }

                // Third pass: Update summary UI
                var summaries = document.querySelectorAll('[id^="summary_received_"]');
                for (var k = 0; k < summaries.length; k++) {
                    var summaryDiv = summaries[k];
                    var idParts = summaryDiv.id.split('_');
                    var s_grnLineId = idParts[2];
                    var s_type = idParts[3];

                    var totalReceived = parseFloat(summaryDiv.getAttribute('data-total'));
                    var assigned = (totals[s_grnLineId] && totals[s_grnLineId][s_type]) || 0;

                    var assignedSpan = document.getElementById('summary_assigned_' + s_grnLineId + '_' + s_type);
                    var progressBar = document.getElementById('progress_' + s_grnLineId + '_' + s_type);

                    if (assignedSpan && progressBar) {
                        var remaining = totalReceived - assigned;
                        assignedSpan.innerHTML = 'Remaining: <span class="' + (remaining != 0 ? 'text-danger' : 'text-primary') + '">' + remaining + '</span>';

                        var percent = Math.min((assigned / totalReceived) * 100, 100);
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
                }

                var msgEl = document.getElementById('validationMsg');
                if (msgEl) msgEl.textContent = validationMsg;
                if (submitBtn) {
                    submitBtn.disabled = !allValid;
                    submitBtn.title = allValid ? "Ready to confirm" : validationMsg;
                }

                rebalanceSlots(currentSlotUsage);
            }

            function rebalanceSlots(currentUsage) {
                var allSelects = document.querySelectorAll('.slot-select');

                for (var i = 0; i < allSelects.length; i++) {
                    var select = allSelects[i];
                    var currentValue = select.value;
                    var qtyInput = select.closest('tr').querySelector('.qty-input');
                    var currentQtyThisRow = parseFloat(qtyInput.value) || 0;

                    for (var j = 0; j < select.options.length; j++) {
                        var option = select.options[j];
                        if (!option.value) continue;

                        var origAvail = parseFloat(option.getAttribute('data-capacity')) || 0;
                        var usageByOthers = (currentUsage[option.value] || 0) - (option.value === currentValue ? currentQtyThisRow : 0);
                        var effectiveAvail = origAvail - usageByOthers;

                        // UNIQUE CONSTRAINT REMOVED: No longer disabling if selected in another row.
                        // Only disable if the slot is full (capacity <= 0).
                        option.disabled = (effectiveAvail <= 0 && option.value !== currentValue);

                        var baseText = option.text.split(' (')[0];
                        if (effectiveAvail <= 0 && option.value !== currentValue) {
                            option.text = baseText + " (FULL)";
                            option.style.color = "#dc3545";
                        } else {
                            option.text = baseText + " (" + (effectiveAvail < 0 ? 0 : effectiveAvail) + " left)";
                            option.style.color = "";
                        }
                    }
                }
            }

            function addAssignmentRow(sourceRow, initialQty) {
                var grnLineId = sourceRow.getAttribute('data-grn-line-id');
                var type = sourceRow.getAttribute('data-type');

                var sameCategoryRows = document.querySelectorAll('.assignment-row[data-grn-line-id="' + grnLineId + '"][data-type="' + type + '"]');
                var lastRow = sameCategoryRows[sameCategoryRows.length - 1];

                var newRow = sourceRow.cloneNode(true);

                var qtyInput = newRow.querySelector('.qty-input');
                if (qtyInput) {
                    qtyInput.value = initialQty;
                    qtyInput.classList.remove('is-invalid');
                }

                var slotSelect = newRow.querySelector('.slot-select');
                if (slotSelect) slotSelect.value = "";

                var info = newRow.querySelector('.slot-info');
                if (info) info.innerHTML = "";
                var cap = newRow.querySelector('.slot-capacity-display');
                if (cap) cap.innerHTML = "--";

                var actionsCell = newRow.querySelector('td:last-child');
                if (actionsCell) {
                    actionsCell.innerHTML = '<button type="button" class="btn btn-sm btn-outline-danger remove-assignment-btn" title="Remove row">' +
                                          '<i class="fas fa-trash"></i>' +
                                          '</button>';
                }

                if (lastRow && lastRow.parentNode) {
                    lastRow.parentNode.insertBefore(newRow, lastRow.nextSibling);
                }
                updateSummaryAndLogic();
            }

            putawayTable.addEventListener('input', function(e) {
                if (e.target.classList.contains('qty-input')) {
                    var row = e.target.closest('tr');
                    var slotSelect = row.querySelector('.slot-select');
                    var selectedSlot = slotSelect.options[slotSelect.selectedIndex];

                    if (selectedSlot && selectedSlot.value) {
                        var currentQty = parseFloat(e.target.value) || 0;
                        var usageByOthers = 0;
                        var allSelects = document.querySelectorAll('.slot-select');
                        for (var i = 0; i < allSelects.length; i++) {
                            var s = allSelects[i];
                            if (s !== slotSelect && s.value === selectedSlot.value) {
                                usageByOthers += parseFloat(s.closest('tr').querySelector('.qty-input').value) || 0;
                            }
                        }

                        var origAvail = parseFloat(selectedSlot.getAttribute('data-capacity')) || 0;
                        var realAvail = Math.max(0, origAvail - usageByOthers);

                        if (currentQty > realAvail + 0.001) {
                            var overflow = currentQty - realAvail;
                            e.target.value = realAvail;
                            addAssignmentRow(row, overflow);
                        }
                    }
                }
                updateSummaryAndLogic();
            });

            putawayTable.addEventListener('change', function(e) {
                if (e.target.classList.contains('slot-select')) {
                    var row = e.target.closest('tr');
                    var qtyInput = row.querySelector('.qty-input');
                    var selectedSlot = e.target.options[e.target.selectedIndex];

                    if (selectedSlot && selectedSlot.value) {
                        var currentUsage = {};
                        var allSelects = document.querySelectorAll('.slot-select');
                        for (var i = 0; i < allSelects.length; i++) {
                            var s = allSelects[i];
                            if (s !== e.target && s.value) {
                                var q = parseFloat(s.closest('tr').querySelector('.qty-input').value) || 0;
                                currentUsage[s.value] = (currentUsage[s.value] || 0) + q;
                            }
                        }

                        var origAvail = parseFloat(selectedSlot.getAttribute('data-capacity')) || 0;
                        var usedByOthers = currentUsage[selectedSlot.value] || 0;
                        var realAvail = Math.max(0, origAvail - usedByOthers);
                        var currentQty = parseFloat(qtyInput.value) || 0;

                        if (currentQty > realAvail + 0.001) {
                            var overflow = currentQty - realAvail;

                            if (typeof Swal !== 'undefined') {
                                Swal.fire({
                                    icon: realAvail <= 0 ? 'warning' : 'info',
                                    title: realAvail <= 0 ? 'Slot is Full' : 'Slot Capacity Exceeded',
                                    text: realAvail <= 0 
                                        ? 'This slot is full. Moving all ' + overflow + ' items to a new row.'
                                        : 'Moving ' + overflow + ' items to a new row. ' + realAvail + ' will stay here.',
                                    toast: true,
                                    position: 'top-end',
                                    showConfirmButton: false,
                                    timer: 4000
                                });
                            }

                            qtyInput.value = realAvail;
                            addAssignmentRow(row, overflow);
                            if (realAvail <= 0) e.target.value = "";
                        }
                    }
                }
                updateSummaryAndLogic();
            });

            document.addEventListener('click', function(e) {
                var addBtn = e.target.closest('.add-assignment-btn');
                var removeBtn = e.target.closest('.remove-assignment-btn');

                if (addBtn) {
                    e.preventDefault();
                    var row = addBtn.closest('tr');
                    if (!row) return;

                    var grnLineId = row.getAttribute('data-grn-line-id');
                    var type = row.getAttribute('data-type');
                    var totalAssigned = 0;
                    var selector = '.assignment-row[data-grn-line-id="' + grnLineId + '"][data-type="' + type + '"] .qty-input';
                    var categoryInputs = document.querySelectorAll(selector);
                    for (var i = 0; i < categoryInputs.length; i++) {
                        totalAssigned += parseFloat(categoryInputs[i].value) || 0;
                    }

                    var summaryDiv = document.getElementById('summary_received_' + grnLineId + '_' + type);
                    var totalReceived = summaryDiv ? parseFloat(summaryDiv.getAttribute('data-total')) : 0;
                    var remaining = Math.max(0, totalReceived - totalAssigned);

                    addAssignmentRow(row, remaining);
                }

                if (removeBtn) {
                    e.preventDefault();
                    var row_r = removeBtn.closest('tr');
                    if (row_r) {
                        row_r.remove();
                        updateSummaryAndLogic();
                    }
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