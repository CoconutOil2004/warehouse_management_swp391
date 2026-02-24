<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib tagdir="/WEB-INF/tags/" prefix="t" %>
<%@taglib uri="jakarta.tags.core" prefix="c" %>
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
                                    <div class="input-group">
                                        <span class="input-group-text bg-light"><i
                                                class="fas fa-shopping-cart"></i></span>
                                        <select
                                            class="form-control ${not empty fieldErrors.poId ? 'is-invalid' : ''}"
                                            name="poId" id="poSelect" required>
                                            <option value="">-- Choose Purchase Order --</option>
                                            <c:forEach var="po" items="${purchaseOrders}">
                                                <option value="${po.poId}" ${oldPoId==po.poId ? 'selected'
                                                                 : '' }>
                                                            ${po.poNumber} (#${po.poId})
                                                        </option>
                                                </c:forEach>
                                            </select>
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
                                                   value="${oldGrnNumber != null ? oldGrnNumber : ''}" readonly>
                                        </div>
                                    </div>
                                    <div class="col-md-4">
                                        <label class="form-label fw-bold">Supplier</label>
                                        <div class="input-group">
                                            <span class="input-group-text bg-light"><i
                                                    class="fas fa-truck"></i></span>
                                            <select class="form-control" name="supplierId" id="supplierSelect"
                                                    disabled>
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
                                <h5 class="card-title mb-0"><i class="fas fa-boxes me-2"></i>SKU Details</h5>
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
                                                <th style="min-width: 250px;">Product</th>
                                                <th style="width: 120px;">Price</th>
                                                <th style="width: 120px;">Quantity</th>
                                                <th style="width: 100px;">Good</th>
                                                <th style="width: 100px;">Damaged</th>
                                                <th style="width: 100px;">Missing</th>
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
    </style>

    <script id="variantsData" type="application/json">
        [
        <c:forEach var="v" items="${variants}" varStatus="status">
            {"id": "${v.variantId}", "sku": "${v.variantSku}", "name": "${v.productName}"}${!status.last ? ',' : ''}
        </c:forEach>
        ]
    </script>

    <script id="oldLinesData" type="application/json">
        [
        <c:forEach var="l" items="${oldLines}" varStatus="status">
            {"poLineId": "${l.poLineId}", "variantId": "${l.variantId}", "unitPrice": "${l.unitPrice}", "qtyExpected": "${l.qtyExpected}", "qtyGood": "${l.qtyGood}", "qtyDamaged": "${l.qtyDamaged}", "qtyMissing": "${l.qtyMissing}", "note": "${l.note != null ? l.note : ''}"}${!status.last ? ',' : ''}
        </c:forEach>
        ]
    </script>

    <script>
let idx = 0;
const variantsData = document.getElementById('variantsData').textContent;
const variants = JSON.parse(variantsData);

function addLine(data = null) {
    const tbody = document.querySelector("#linesTable tbody");
    const tr = document.createElement("tr");
    tr.className = "line-row";

    let productName = "Unknown Product";
    if (data && data.variantId) {
        const v = variants.find(v => v.id == data.variantId);
        if (v)
            productName = v.sku + " - " + v.name;
    }

    tr.innerHTML = `
<td style="min-width: 250px;">
<input type="hidden" name="lines[\${idx}].poLineId" value="\${data ? data.poLineId : ''}">
<input type="hidden" name="lines[\${idx}].variantId" value="\${data ? data.variantId : ''}">
<span class="small fw-semibold">\${productName}</span>
</td>
<td style="width: 120px;">
<input type="number" class="form-control form-control-sm text-center bg-light" value="\${data ? data.unitPrice : 0}" readonly>
</td>
<td style="width: 120px;">
<input type="number" class="form-control form-control-sm text-center bg-light qty-expected" name="lines[\${idx}].qtyExpected" value="\${data ? Math.floor(data.qtyExpected) : 0}" readonly>
</td>
<td style="width: 100px;">
<input type="number" min="0" step="1" class="form-control form-control-sm text-center border-success-subtle qty-good" 
   name="lines[\${idx}].qtyGood" value="\${data && data.fromOld ? Math.floor(data.qtyGood) : 0}" 
   oninput="this.value = Math.abs(Math.floor(this.value))"
   onfocus="if(this.value=='0') this.value='';" onblur="if(this.value=='') this.value='0';">
</td>
<td style="width: 100px;">
<input type="number" min="0" step="1" class="form-control form-control-sm text-center border-danger-subtle qty-damaged" 
   name="lines[\${idx}].qtyDamaged" value="\${data && data.fromOld ? Math.floor(data.qtyDamaged) : 0}" 
   oninput="this.value = Math.abs(Math.floor(this.value))"
   onfocus="if(this.value=='0') this.value='';" onblur="if(this.value=='') this.value='0';">
</td>
<td style="width: 100px;">
<input type="number" min="0" step="1" class="form-control form-control-sm text-center border-warning-subtle qty-missing" 
   name="lines[\${idx}].qtyMissing" value="\${data && data.fromOld ? Math.floor(data.qtyMissing) : 0}" 
   oninput="this.value = Math.abs(Math.floor(this.value))"
   onfocus="if(this.value=='0') this.value='';" onblur="if(this.value=='') this.value='0';">
</td>
<td>
<input type="text" class="form-control form-control-sm" name="lines[\${idx}].note" placeholder="Remark" value="\${data ? data.note : ''}">
</td>
`;
    tbody.appendChild(tr);

    const trError = document.createElement("tr");
    trError.innerHTML = `
<td colspan="7" class="border-0 py-0 ps-3">
<div class="error-message mb-2" id="error_\${idx}"></div>
</td>
`;
    tbody.appendChild(trError);
    idx++;
}

// Initialize with lines
document.addEventListener("DOMContentLoaded", function () {
    const oldLinesData = document.getElementById('oldLinesData').textContent;
    const oldLines = JSON.parse(oldLinesData);

    if (oldLines && oldLines.length > 0) {
        oldLines.forEach(line => {
            line.fromOld = true;
            addLine(line);
        });
    }

    // Handle PO Selection Change
    document.getElementById('poSelect').addEventListener('change', async function () {
        const poId = this.value;
        if (!poId) {
            document.querySelector("#linesTable tbody").innerHTML = '';
            document.getElementById('supplierSelect').value = '';
            document.getElementById('grnNumberHidden').value = '';
            document.getElementById('grnNumberDisplay').value = ''; // Clear display field
            return;
        }

        try {
            const resp = await fetch(`${pageContext.request.contextPath}/goods-receipt?action=getPoDetails&poId=\${poId}`);
            if (!resp.ok)
                throw new Error("Failed to fetch PO details");
            const data = await resp.json();

            // Update Supplier
            if (data.supplierId) {
                // temporarily enable to set value then disable again
                document.getElementById('supplierSelect').disabled = false;
                document.getElementById('supplierSelect').value = data.supplierId;
                document.getElementById('supplierSelect').disabled = true;
            }

            if (data.grnNumber) {
                document.getElementById('grnNumberHidden').value = data.grnNumber;
                document.getElementById('grnNumberDisplay').value = data.grnNumber;
            }

            // Clear and Populate Lines
            const tbody = document.querySelector("#linesTable tbody");
            tbody.innerHTML = '';
            idx = 0;

            data.lines.forEach(l => {
                addLine({
                    poLineId: l.poLineId,
                    variantId: l.variantId,
                    unitPrice: l.unitPrice,
                    qtyExpected: l.orderedQty,
                    qtyGood: 0,
                    qtyDamaged: 0,
                    qtyMissing: 0,
                    note: '',
                    fromOld: false
                });
            });
        } catch (err) {
            console.error(err);
            alert("Có lỗi khi tải thông tin PO: " + err.message);
        }
    });

    // Form validation
    document.getElementById('grnForm').addEventListener('submit', function (e) {
        e.preventDefault();
        const form = this;
        const rows = document.querySelectorAll('.line-row');

        let hasError = false;

        if (rows.length === 0) {
            Swal.fire({
                icon: 'warning',
                title: 'No Items',
                text: 'Please select a Purchase Order to display items.'
            });
            return;
        }

        rows.forEach((row, index) => {
            const qExp = parseFloat(row.querySelector(`.qty-expected`).value) || 0;
            const qGoodField = row.querySelector(`.qty-good`);
            const qDamagedField = row.querySelector(`.qty-damaged`);
            const qMissingField = row.querySelector(`.qty-missing`);
            const errorDiv = document.getElementById(`error_\${index}`);

            // Clear previous errors
            errorDiv.style.display = 'none';
            errorDiv.textContent = '';
            [qGoodField, qDamagedField, qMissingField].forEach(f => f.classList.remove('is-invalid-field'));

            const qGood = parseFloat(qGoodField.value) || 0;
            const qDamaged = parseFloat(qDamagedField.value) || 0;
            const qMissing = parseFloat(qMissingField.value) || 0;

            const totalReceived = qGood + qDamaged + qMissing;

            if (qGood === 0 && qDamaged === 0 && qMissing === 0) {
                errorDiv.textContent = 'At least one quantity must be greater than 0';
                errorDiv.style.display = 'block';
                [qGoodField, qDamagedField, qMissingField].forEach(f => f.classList.add('is-invalid-field'));
                hasError = true;
            } else if (qGood + qDamaged > qExp) {
                errorDiv.textContent = `Good and Damaged quantities exceeds ordered quantity (\${qExp})`;
                errorDiv.style.display = 'block';
                [qGoodField, qDamagedField].forEach(f => f.classList.add('is-invalid-field'));
                hasError = true;
            } else if (qMissing > qExp) {
                errorDiv.textContent = `Missing quantity cannot exceed ordered quantity (\${qExp})`;
                errorDiv.style.display = 'block';
                qMissingField.classList.add('is-invalid-field');
                hasError = true;
            } else if (totalReceived > qExp) {
                errorDiv.textContent = `Total quantity (including Missing) exceeds ordered quantity (\${qExp})`;
                errorDiv.style.display = 'block';
                [qGoodField, qDamagedField, qMissingField].forEach(f => f.classList.add('is-invalid-field'));
                hasError = true;
            } else if (totalReceived < qExp) {
                errorDiv.textContent = `Quantity incomplete. Please record missing amount to match (\${qExp})`;
                errorDiv.style.display = 'block';
                [qGoodField, qDamagedField, qMissingField].forEach(f => f.classList.add('is-invalid-field'));
                hasError = true;
            }
        });

        if (hasError) {
            return;
        }

        // Perfect match
        document.getElementById('supplierSelect').disabled = false;
        form.submit();
    });
});
    </script>