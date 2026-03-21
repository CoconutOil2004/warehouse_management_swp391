<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@taglib tagdir="/WEB-INF/tags/" prefix="t" %>
        <%@taglib uri="jakarta.tags.core" prefix="c" %>
            <%@taglib uri="jakarta.tags.functions" prefix="fn" %>
                <t:layout title="Goods Receipt Note">
                    <!-- SweetAlert2 -->
                    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
                    <div class="container-fluid py-4">
                        <!-- Back Link -->
                        <div class="mb-3">
                            <a href="${pageContext.request.contextPath}/goods-receipt?action=list"
                                class="text-decoration-none text-muted">
                                <i class="fas fa-arrow-left me-1"></i> Back to List
                            </a>
                        </div>

                        <form action="${pageContext.request.contextPath}/goods-receipt" method="post" id="grnForm">
                            <input type="hidden" name="action" value="save" />
                            <input type="hidden" name="grnId" value="${grnId}" />
                            <input type="hidden" name="warehouseId" id="warehouseIdHidden" value="${sessionScope.USER.warehouseId}" />
                            <input type="hidden" name="grnNumber" id="grnNumberHidden"
                                value="${oldGrnNumber != null ? oldGrnNumber : ''}" />

                            <div class="row">
                                <!-- Header Section -->
                                <div class="col-lg-12">
                                    <div class="card shadow-sm border-0 mb-4">
                                        <div class="card-header bg-primary text-white py-3">
                                            <h5 class="card-title mb-0"><i class="fas fa-file-invoice me-2"></i>
                                                Header Information</h5>
                                        </div>
                                        <div class="card-body">
                                            <div class="row g-3">
                                                <div class="col-md-4">
                                                    <label class="form-label fw-bold">Purchase Order</label>
                                                    <div class="position-relative" id="poCombobox">
                                                        <c:set var="initialPoNumber" value="" />
                                                        <c:if test="${not empty oldPoId}">
                                                            <c:forEach var="po" items="${purchaseOrders}">
                                                                <c:if test="${po.poId == oldPoId}">
                                                                    <c:set var="initialPoNumber"
                                                                        value="${po.poNumber}" />
                                                                </c:if>
                                                            </c:forEach>
                                                        </c:if>
                                                        <div class="input-group">
                                                            <span class="input-group-text bg-light"><i
                                                                    class="fas fa-shopping-cart"></i></span>
                                                            <input type="text" id="poSearchInput"
                                                                class="form-control ${not empty fieldErrors.poId ? 'is-invalid' : ''}"
                                                                placeholder="-- Choose or Search PO --"
                                                                autocomplete="off" value="${initialPoNumber}">
                                                            <button
                                                                class="btn btn-outline-secondary dropdown-toggle dropdown-toggle-split"
                                                                type="button" id="poToggleBtn"></button>
                                                        </div>
                                                        <input type="hidden" name="poId" id="poIdHidden"
                                                            value="${oldPoId != null ? oldPoId : ''}" required>
                                                        <input type="hidden" name="nextStep" id="nextStep" value="putaway">
                                                        <div id="poDropdownList"
                                                            class="dropdown-menu w-100 shadow-sm overflow-auto"
                                                            style="max-height: 400px; display: none; z-index: 1050;">
                                                            <c:forEach var="po" items="${purchaseOrders}">
                                                                <button type="button" class="dropdown-item py-2"
                                                                    data-id="${po.poId}" data-number="${po.poNumber}">
                                                                    <span class="fw-bold">${po.poNumber}</span> <span
                                                                        class="text-muted small ml-1">(#${po.poId})</span>
                                                                </button>
                                                            </c:forEach>
                                                            <div id="noPoMessage"
                                                                class="dropdown-item disabled text-muted text-center py-2"
                                                                style="display: none;">No matching orders</div>
                                                        </div>
                                                    </div>
                                                    <c:if test="${not empty fieldErrors.poId}">
                                                        <div class="text-danger small mt-1">${fieldErrors.poId}</div>
                                                    </c:if>
                                                </div>
                                                <div class="col-md-4">
                                                    <label class="form-label fw-bold">GRN Number</label>
                                                    <div class="input-group">
                                                        <span class="input-group-text bg-light"><i
                                                                class="fas fa-hashtag"></i></span>
                                                        <input type="text" class="form-control bg-light"
                                                            id="grnNumberDisplay"
                                                            value="${oldGrnNumber != null ? oldGrnNumber : ''}"
                                                            readonly>
                                                    </div>
                                                </div>
                                                <div class="col-md-4">
                                                    <label class="form-label fw-bold">Supplier</label>
                                                    <div class="input-group">
                                                        <span class="input-group-text bg-light"><i
                                                                class="fas fa-truck"></i></span>
                                                        <select class="form-control" name="supplierId"
                                                            id="supplierSelect" disabled>
                                                            <option value="">-- Auto-filled from PO --</option>
                                                            <c:forEach var="s" items="${suppliers}">
                                                                <option value="${s.supplierId}"
                                                                    ${oldSupplierId==s.supplierId ? 'selected' : '' }>
                                                                    ${s.name}</option>
                                                            </c:forEach>
                                                        </select>
                                                    </div>
                                                </div>
                                                <div class="col-12">
                                                    <label class="form-label fw-bold">Internal Note</label>
                                                    <textarea class="form-control" name="note" rows="2"
                                                        placeholder="Any special instructions or observations...">${oldNote != null ? oldNote : ''}</textarea>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <!-- Lines Section -->
                                <div class="col-lg-12">
                                    <div class="card shadow-sm border-0">
                                        <div
                                            class="card-header d-flex justify-content-between align-items-center bg-dark text-white py-3">
                                            <h5 class="card-title mb-0"><i class="fas fa-boxes me-2"></i>SKU Details
                                            </h5>
                                            <c:if test="${not empty fieldErrors.lines}">
                                                <span class="badge bg-danger">${fieldErrors.lines}</span>
                                            </c:if>
                                        </div>
                                        <div class="card-body p-0">
                                            <div class="table-responsive">
                                                <table class="table table-hover align-middle mb-0" id="linesTable">
                                                    <thead
                                                        class="table-light text-center text-secondary text-uppercase small">
                                                        <tr>
                                                            <th style="min-width: 200px;">Product</th>
                                                            <th style="width: 100px;">Price</th>
                                                            <th style="width: 100px;">Ordered</th>
                                                            <th style="width: 130px; border-left: 2px solid #dee2e6;">Good (Actual)</th>
                                                            <th style="width: 130px;">Damaged (Actual)</th>
                                                            <th style="width: 120px; border-left: 2px solid #dee2e6;">Extra (good)</th>
                                                            <th style="width: 120px;">Extra (dmg)</th>
                                                            <th style="width: 120px;">Missing</th>
                                                            <th>Note</th>
                                                        </tr>
                                                    </thead>
                                                    <tbody></tbody>
                                                </table>
                                            </div>
                                        </div>
                                        <div class="card-footer bg-light py-3 d-flex justify-content-end gap-2">
                                            <a href="${pageContext.request.contextPath}/goods-receipt?action=list"
                                                class="btn btn-outline-secondary px-4">Cancel</a>
                                            <button class="btn btn-primary px-5 shadow-sm" type="submit"
                                                style="background-color: #4e73df; border-color: #4e73df;">
                                                <i class="fas fa-cart-arrow-down me-2"></i>Go to Putaway
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </form>
                    </div>
                </t:layout>

                <style>
                    .input-group-text {
                        border-right: none;
                    }

                    .form-control:focus+.input-group-text {
                        border-color: #86b7fe;
                    }

                    .table th {
                        font-weight: 600;
                    }

                    .card {
                        border-radius: 0.5rem;
                        overflow: hidden;
                    }

                    .error-message {
                        color: #dc3545;
                        font-size: 0.75rem;
                        margin-top: 0.25rem;
                        display: none;
                    }

                    .is-invalid-field {
                        border-color: #dc3545 !important;
                    }

                    #poDropdownList {
                        top: 100%;
                        left: 0;
                        margin-top: 2px;
                    }

                    #poDropdownList .dropdown-item {
                        cursor: pointer;
                        transition: all 0.2s;
                    }

                    #poDropdownList .dropdown-item:hover {
                        background-color: #f8f9fa;
                        padding-left: 1.5rem;
                    }

                    /* Quantity Input colors */
                    .bg-good {
                        background-color: #e8f5e9 !important;
                        /* Light green */
                        border-color: #c8e6c9 !important;
                    }

                    .bg-damaged {
                        background-color: #ffebee !important;
                        /* Light red */
                        border-color: #ffcdd2 !important;
                    }

                    .bg-missing {
                        background-color: #fffde7 !important;
                        /* Light yellow */
                        border-color: #fff9c4 !important;
                    }
                    
                    .bg-excess {
                        background-color: #e3f2fd !important;
                        /* Light blue */
                        border-color: #bbdefb !important;
                    }

                    .bg-good:focus {
                        background-color: #f1f8f1 !important;
                        box-shadow: 0 0 0 0.25rem rgba(40, 167, 69, 0.1) !important;
                    }

                    .bg-damaged:focus {
                        background-color: #fff5f5 !important;
                        box-shadow: 0 0 0 0.25rem rgba(220, 53, 69, 0.1) !important;
                    }

                    .bg-missing:focus {
                        background-color: #fffeee !important;
                        box-shadow: 0 0 0 0.25rem rgba(255, 193, 7, 0.1) !important;
                    }

                    .bg-excess:focus {
                        background-color: #f5faff !important;
                        box-shadow: 0 0 0 0.25rem rgba(13, 110, 253, 0.1) !important;
                    }

                    .bg-extra-damaged {
                        background-color: #fce4ec !important;
                        border-color: #f8bbd9 !important;
                    }

                    .bg-extra-damaged:focus {
                        background-color: #fff8fa !important;
                        box-shadow: 0 0 0 0.25rem rgba(233, 30, 99, 0.12) !important;
                    }
                </style>

                <%-- Variants data: dùng data-attributes thay vì JSON để tránh quote-escaping --%>
                    <div id="variantsData" style="display:none">
                        <c:forEach var="v" items="${variants}">
                            <span data-id="${v.variantId}" data-sku="<c:out value='${v.variantSku}'/>"
                                data-name="<c:out value='${v.productName}'/>"></span>
                        </c:forEach>
                    </div>

                    <script id="oldLinesData" type="application/json">
                        ${not empty oldLinesJson ? oldLinesJson : '[]'}
                    </script>

                    <script>
                        let idx = 0;
                        const variants = Array.from(
                            document.querySelectorAll('#variantsData span')
                        ).map(el => ({
                            id: el.dataset.id,
                            sku: el.dataset.sku,
                            name: el.dataset.name
                        }));

                        function updateBalance(row) {
                            const qExpInput = row.querySelector('.qty-expected');
                            if (!qExpInput) return;
                            const qExp = parseFloat(qExpInput.value) || 0;
                            const qGoodPhys = parseFloat(row.querySelector('.phys-good').value) || 0;
                            const qDamagedPhys = parseFloat(row.querySelector('.phys-damaged').value) || 0;
                            const qExtraGoodPhys = parseFloat(row.querySelector('.phys-extra-good')?.value) || 0;
                            const qExtraDamagedPhys = parseFloat(row.querySelector('.phys-extra-damaged')?.value) || 0;
                            
                            // Shortage vs PO: good + damaged (on line) + extra good; extra damaged does not cover ordered qty
                            const receivedAgainstPo = qGoodPhys + qDamagedPhys + qExtraGoodPhys;
                            const onPoGoodDamaged = qGoodPhys + qDamagedPhys;
                            const finalGood = qGoodPhys;
                            const finalDamaged = qDamagedPhys;
                            const finalMissing = Math.max(0, qExp - receivedAgainstPo);
                            
                            row.querySelector('.server-good').value = finalGood;
                            row.querySelector('.server-damaged').value = finalDamaged;
                            row.querySelector('.server-extra-good').value = qExtraGoodPhys;
                            row.querySelector('.server-extra-damaged').value = qExtraDamagedPhys;
                            row.querySelector('.server-missing').value = finalMissing;
                            
                            row.querySelector('.display-missing').value = finalMissing;

                            const errorRow = row.nextElementSibling;
                            const errorDiv = errorRow && errorRow.querySelector('.error-message');
                            if (errorDiv) {
                                if (onPoGoodDamaged > qExp) {
                                    errorDiv.style.display = 'block';
                                    errorDiv.textContent = 'Good + Damaged (thực tế) không được vượt số đặt (' + qExp + '). Phần vượt nhập vào Extra.';
                                    row.querySelector('.phys-good')?.classList.add('is-invalid-field');
                                    row.querySelector('.phys-damaged')?.classList.add('is-invalid-field');
                                    row.querySelector('.phys-extra-good')?.classList.remove('is-invalid-field');
                                    row.querySelector('.phys-extra-damaged')?.classList.remove('is-invalid-field');
                                } else if ((qExtraGoodPhys > 0 || qExtraDamagedPhys > 0) && (onPoGoodDamaged < qExp)) {
                                    errorDiv.style.display = 'block';
                                    errorDiv.textContent = 'Please fulfill the ordered quantity (' + qExp + ') in Good/Damaged columns before using the Extra columns.';
                                    row.querySelector('.phys-extra-good')?.classList.add('is-invalid-field');
                                    row.querySelector('.phys-extra-damaged')?.classList.add('is-invalid-field');
                                    row.querySelector('.phys-good')?.classList.remove('is-invalid-field');
                                    row.querySelector('.phys-damaged')?.classList.remove('is-invalid-field');
                                } else {
                                    errorDiv.style.display = 'none';
                                    errorDiv.textContent = '';
                                    row.querySelector('.phys-good')?.classList.remove('is-invalid-field');
                                    row.querySelector('.phys-damaged')?.classList.remove('is-invalid-field');
                                    row.querySelector('.phys-extra-good')?.classList.remove('is-invalid-field');
                                    row.querySelector('.phys-extra-damaged')?.classList.remove('is-invalid-field');
                                }
                            }
                        }

                        function addLine(data = null) {
                            const tbody = document.querySelector("#linesTable tbody");
                            const tr = document.createElement("tr");
                            tr.className = "line-row";

                            let productName = "Unknown Product";
                            if (data && data.variantId) {
                                const v = variants.find(v => v.id == data.variantId);
                                if (v) productName = v.sku + " - " + v.name;
                            }

                            tr.innerHTML = `
<td style="min-width: 200px;">
    <input type="hidden" name="lines[\${idx}].poLineId" value="\${data ? data.poLineId : ''}">
    <input type="hidden" name="lines[\${idx}].variantId" value="\${data ? data.variantId : ''}">
    <span class="small fw-semibold">\${productName}</span>
</td>
<td style="width: 100px;">
    <input type="number" class="form-control form-control-sm text-center bg-light" value="\${data ? Number(data.unitPrice).toFixed(2) : '0.00'}" readonly title="Unit Price">
</td>
<td style="width: 100px;">
    <input type="number" class="form-control form-control-sm text-center bg-light qty-expected" name="lines[\${idx}].qtyExpected" value="\${data ? Math.floor(data.qtyExpected) : 0}" readonly title="Ordered Qty">
</td>
<td style="width: 130px; border-left: 2px solid #dee2e6;">
    <input type="number" min="0" step="1" class="form-control form-control-sm text-center bg-good phys-good" 
           value="\${data && data.qtyGood ? Math.floor(data.qtyGood) : 0}" 
           oninput="this.value = Math.abs(Math.floor(this.value)); updateBalance(this.closest('.line-row'));"
           onfocus="if(this.value=='0') this.value='';" onblur="if(this.value=='') this.value='0';" title="Good condition counted vs ordered qty (with damaged on line)">
    <input type="hidden" class="server-good" name="lines[\${idx}].qtyGood" value="\${data && data.qtyGood ? Math.floor(data.qtyGood) : 0}">
</td>
<td style="width: 130px;">
    <input type="number" min="0" step="1" class="form-control form-control-sm text-center bg-damaged phys-damaged" 
           value="\${data && data.qtyDamaged ? Math.floor(data.qtyDamaged) : 0}" 
           oninput="this.value = Math.abs(Math.floor(this.value)); updateBalance(this.closest('.line-row'));"
           onfocus="if(this.value=='0') this.value='';" onblur="if(this.value=='') this.value='0';" title="Actual Damaged received">
    <input type="hidden" class="server-damaged" name="lines[\${idx}].qtyDamaged" value="\${data && data.qtyDamaged ? Math.floor(data.qtyDamaged) : 0}">
</td>
<td style="width: 120px; border-left: 2px solid #dee2e6;">
    <input type="number" min="0" step="1" class="form-control form-control-sm text-center bg-excess phys-extra-good" 
           value="\${data && data.qtyExtraGood ? Math.floor(data.qtyExtraGood) : 0}" 
           oninput="this.value = Math.abs(Math.floor(this.value)); updateBalance(this.closest('.line-row'));"
           onfocus="if(this.value=='0') this.value='';" onblur="if(this.value=='') this.value='0';" title="Extra good: counts vs ordered for shortage; putaway → EXCESS zone">
    <input type="hidden" class="server-extra-good" name="lines[\${idx}].qtyExtraGood" value="\${data && data.qtyExtraGood ? Math.floor(data.qtyExtraGood) : 0}">
</td>
<td style="width: 120px;">
    <input type="number" min="0" step="1" class="form-control form-control-sm text-center bg-extra-damaged phys-extra-damaged" 
           value="\${data && data.qtyExtraDamaged ? Math.floor(data.qtyExtraDamaged) : 0}" 
           oninput="this.value = Math.abs(Math.floor(this.value)); updateBalance(this.closest('.line-row'));"
           onfocus="if(this.value=='0') this.value='';" onblur="if(this.value=='') this.value='0';" title="Surplus damaged units (DAMAGE zone)">
    <input type="hidden" class="server-extra-damaged" name="lines[\${idx}].qtyExtraDamaged" value="\${data && data.qtyExtraDamaged ? Math.floor(data.qtyExtraDamaged) : 0}">
</td>
<td style="width: 120px;">
    <input type="number" class="form-control form-control-sm text-center bg-missing display-missing" 
           value="\${data && data.qtyMissing ? Math.floor(data.qtyMissing) : 0}" readonly title="Missing vs PO = Ordered − (Good + Damaged + Extra good)">
    <input type="hidden" class="server-missing" name="lines[\${idx}].qtyMissing" value="\${data && data.qtyMissing ? Math.floor(data.qtyMissing) : 0}">
</td>
<td>
    <input type="text" class="form-control form-control-sm" name="lines[\${idx}].note" placeholder="Remark" value="\${data ? data.note : ''}">
</td>
`;
                            tbody.appendChild(tr);

                            const trError = document.createElement("tr");
                            trError.innerHTML = `
<td colspan="9" class="border-0 py-0 ps-3">
    <div class="error-message mb-2" id="error_\${idx}"></div>
</td>
`;
                            tbody.appendChild(trError);
                            
                            if (data && data.fromOld) {
                                updateBalance(tr);
                            }
                            
                            idx++;
                        }

                        document.addEventListener("DOMContentLoaded", function () {
                            const grnForm = document.getElementById('grnForm');
                            const oldLinesDataTag = document.getElementById('oldLinesData');
                            const oldLines = oldLinesDataTag ? JSON.parse(oldLinesDataTag.textContent) : [];

                            if (oldLines && oldLines.length > 0) {
                                oldLines.forEach(line => {
                                    line.fromOld = true;
                                    addLine(line);
                                });
                            }

                            const poCombobox = document.getElementById('poCombobox');
                            const poSearchInput = document.getElementById('poSearchInput');
                            const poToggleBtn = document.getElementById('poToggleBtn');
                            const poDropdownList = document.getElementById('poDropdownList');
                            const poIdHidden = document.getElementById('poIdHidden');
                            const noPoMessage = document.getElementById('noPoMessage');
                            const dropdownItems = poDropdownList.querySelectorAll('.dropdown-item:not(#noPoMessage)');
                            const grnNumberDisplay = document.getElementById('grnNumberDisplay');
                            const grnNumberHidden = document.getElementById('grnNumberHidden');

                            function showDropdown() { poDropdownList.style.display = 'block'; }
                            function hideDropdown() { poDropdownList.style.display = 'none'; }

                            function filterPO() {
                                if(!poSearchInput) return;
                                const term = poSearchInput.value.toLowerCase().trim();
                                let count = 0;
                                dropdownItems.forEach(item => {
                                    const text = item.getAttribute('data-number').toLowerCase();
                                    if (text.includes(term)) {
                                        item.style.display = 'block';
                                        count++;
                                    } else {
                                        item.style.display = 'none';
                                    }
                                });
                                if(noPoMessage) noPoMessage.style.display = count === 0 ? 'block' : 'none';
                            }

                            if(poSearchInput) {
                                poSearchInput.addEventListener('focus', showDropdown);
                                poSearchInput.addEventListener('click', showDropdown);
                                poSearchInput.addEventListener('input', () => {
                                    showDropdown();
                                    filterPO();
                                    if (poSearchInput.value.trim() === "") {
                                        poIdHidden.value = "";
                                        fetchPoDetails("");
                                    }
                                });
                            }

                            if(poToggleBtn) {
                                poToggleBtn.addEventListener('click', (e) => {
                                    e.stopPropagation();
                                    if (poDropdownList.style.display === 'none') {
                                        showDropdown();
                                        poSearchInput.focus();
                                    } else {
                                        hideDropdown();
                                    }
                                });
                            }

                            dropdownItems.forEach(item => {
                                item.addEventListener('click', function () {
                                    const poId = this.getAttribute('data-id');
                                    const poNumber = this.getAttribute('data-number');
                                    poSearchInput.value = poNumber;
                                    poIdHidden.value = poId;
                                    hideDropdown();
                                    fetchPoDetails(poId);
                                });
                            });

                            document.addEventListener('click', (e) => {
                                if (poCombobox && !poCombobox.contains(e.target)) hideDropdown();
                            });

                            async function fetchPoDetails(poId) {
                                if (!poId) {
                                    const tb = document.querySelector("#linesTable tbody");
                                    if(tb) tb.innerHTML = '';
                                    const sup = document.getElementById('supplierSelect');
                                    if(sup) sup.value = '';
                                    if (grnNumberDisplay) grnNumberDisplay.value = '';
                                    if (grnNumberHidden) grnNumberHidden.value = '';
                                    return;
                                }
                                try {
                                    const resp = await fetch(`${pageContext.request.contextPath}/goods-receipt?action=getPoDetails&poId=\${poId}`);
                                    if (!resp.ok) throw new Error("Failed to fetch PO details");
                                    const data = await resp.json();
                                    if (data.grnNumber) {
                                        if (grnNumberDisplay) grnNumberDisplay.value = data.grnNumber;
                                        if (grnNumberHidden) grnNumberHidden.value = data.grnNumber;
                                    }
                                    const sup = document.getElementById('supplierSelect');
                                    if (data.supplierId && sup) {
                                        sup.disabled = false;
                                        sup.value = data.supplierId;
                                        sup.disabled = true;
                                    }
                                    const tbody = document.querySelector("#linesTable tbody");
                                    if(tbody) {
                                        tbody.innerHTML = '';
                                        idx = 0;
                                        data.lines.forEach(l => {
                                            addLine({
                                                poLineId: l.poLineId,
                                                variantId: l.variantId,
                                                unitPrice: l.unitPrice,
                                                qtyExpected: l.orderedQty,
                                                qtyGood: 0, qtyDamaged: 0, qtyExtraGood: 0, qtyExtraDamaged: 0, qtyMissing: l.orderedQty,
                                                note: '', fromOld: false
                                            });
                                        });
                                    }
                                } catch (err) {
                                    console.error(err);
                                    alert("Error fetching PO information: " + err.message);
                                }
                            }

                            if(grnForm) {
                                grnForm.addEventListener('submit', async function (e) {
                                    e.preventDefault();
                                    const rows = document.querySelectorAll('.line-row');
                                    if (rows.length === 0) {
                                        Swal.fire({ icon: 'warning', title: 'No Items', text: 'Please select a Purchase Order.' });
                                        return;
                                    }

                                    rows.forEach(row => updateBalance(row));
                                    for (let r = 0; r < rows.length; r++) {
                                        const row = rows[r];
                                        const qExp = parseFloat(row.querySelector('.qty-expected')?.value) || 0;
                                        const g = parseFloat(row.querySelector('.phys-good')?.value) || 0;
                                        const d = parseFloat(row.querySelector('.phys-damaged')?.value) || 0;
                                        const eg = parseFloat(row.querySelector('.phys-extra-good')?.value) || 0;
                                        const ed = parseFloat(row.querySelector('.phys-extra-damaged')?.value) || 0;
                                        if (g + d > qExp) {
                                            Swal.fire({
                                                icon: 'error',
                                                title: 'Số lượng không hợp lệ',
                                                text: 'Good + Damaged (thực tế) không được vượt số đặt. Phần vượt hãy nhập vào Extra (good) hoặc Extra (dmg).'
                                            });
                                            row.scrollIntoView({ behavior: 'smooth', block: 'center' });
                                            return;
                                        }

                                        if ((eg > 0 || ed > 0) && (g + d < qExp)) {
                                            Swal.fire({
                                                icon: 'error',
                                                title: 'Invalid Entry Logic',
                                                text: 'You cannot enter extra quantities when the ordered quantity (' + qExp + ') has not been fully accounted for in Good/Damaged columns.'
                                            });
                                            row.scrollIntoView({ behavior: 'smooth', block: 'center' });
                                            return;
                                        }
                                    }

                                    const warehouseId = document.getElementById('warehouseIdHidden').value;
                                    if (warehouseId) {
                                        // Tính tổng số lượng từ các dòng
                                        let totalGood = 0, totalDamaged = 0, totalExtra = 0;
                                        rows.forEach(row => {
                                            totalGood    += parseFloat(row.querySelector('.server-good').value)    || 0;
                                            totalDamaged += parseFloat(row.querySelector('.server-damaged').value) || 0;
                                            totalDamaged += parseFloat(row.querySelector('.server-extra-damaged').value) || 0;
                                            totalExtra   += parseFloat(row.querySelector('.server-extra-good').value)   || 0;
                                        });

                                        try {
                                            const params = new URLSearchParams({
                                                action: 'checkCapacity',
                                                warehouseId: warehouseId,
                                                totalGood: totalGood,
                                                totalDamaged: totalDamaged,
                                                totalExtra: totalExtra
                                            });
                                            const resp = await fetch(`${pageContext.request.contextPath}/goods-receipt?` + params.toString());
                                            if (resp.ok) {
                                                const data = await resp.json();
                                                if (!data.sufficient) {
                                                    // Not enough space → redirect to warehouse layout
                                                    let html = '<div class="text-start small">';
                                                    if (data.details.good && !data.details.good.isSufficient) {
                                                        html += '<p class="text-danger mb-1"><i class="fas fa-exclamation-triangle me-1"></i><b>GOOD:</b> Need ' + data.details.good.required + ', available ' + data.details.good.available + '</p>';
                                                    }
                                                    if (data.details.damaged && !data.details.damaged.isSufficient) {
                                                        html += '<p class="text-danger mb-1"><i class="fas fa-exclamation-triangle me-1"></i><b>DAMAGED:</b> Need ' + data.details.damaged.required + ', available ' + data.details.damaged.available + '</p>';
                                                    }
                                                    if (data.details.excess && !data.details.excess.isSufficient) {
                                                        html += '<p class="text-danger mb-1"><i class="fas fa-exclamation-triangle me-1"></i><b>EXCESS:</b> Need ' + data.details.excess.required + ', available ' + data.details.excess.available + '</p>';
                                                    }
                                                    html += '</div><hr><p class="mb-0">Please create more slots in <b>Warehouse Layout</b> before continuing.</p>';

                                                    Swal.fire({
                                                        title: 'Insufficient Warehouse Capacity!',
                                                        html: html,
                                                        icon: 'warning',
                                                        showCancelButton: true,
                                                        confirmButtonText: '<i class="fas fa-th me-1"></i> Save & Go to Layout',
                                                        cancelButtonText: 'Cancel',
                                                        confirmButtonColor: '#0d6efd',
                                                        cancelButtonColor: '#6e7881'
                                                    }).then((result) => {
                                                        if (result.isConfirmed) {
                                                            document.getElementById('nextStep').value = 'layout';
                                                            const sup = document.getElementById('supplierSelect');
                                                            if (sup) sup.disabled = false;
                                                            document.getElementById('grnForm').submit();
                                                        }
                                                    });
                                                    return; // STOP automatic submission below, we use the Swal confirmation instead
                                                }
                                            }
                                            // resp không ok (400, 500,...) hoặc đủ chỗ → submit bình thường
                                        } catch (err) {
                                            console.error('Capacity check error:', err);
                                            // Nếu check lỗi → vẫn cho submit
                                        }
                                    }

                                    const sup = document.getElementById('supplierSelect');
                                    if(sup) sup.disabled = false;
                                    this.submit();
                                });
                            }
                        });
                    </script>