<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="dto.SupplierDTO"%>
<%@page import="dto.ProductDTO"%>
<%@page import="java.util.*" %>
<%@taglib tagdir="/WEB-INF/tags/" prefix="t" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<t:layout title="Create Purchase Order">

    <div class="py-4">
        <form action="${pageContext.request.contextPath}/purchase-orders" method="post" id="poForm">
            <input type="hidden" name="action" value="create"/>

            <!-- Header -->
            <div class="card shadow-sm mb-3">
                <div class="card-header fw-semibold">Header</div>
                <div class="card-body row g-3">

                    <div class="col-md-4">
                        <label class="form-label">PO Number</label>
                        <input class="form-control ${not empty fieldErrors.poNumber ? 'is-invalid' : ''}"
                               name="poNumber"
                               value="${oldPoNumber}">
                        <c:if test="${not empty fieldErrors.poNumber}">
                            <div class="invalid-feedback d-block">${fieldErrors.poNumber}</div>
                        </c:if>
                    </div>

                    <div class="col-md-4">
                        <label class="form-label">Supplier</label>
                        <select class="form-control ${not empty fieldErrors.supplierId ? 'is-invalid' : ''}"
                                name="supplierId">
                            <option value="">-- Select supplier --</option>

                            <c:forEach var="s" items="${suppliers}">
                                <option value="${s.supplierId}" ${s.supplierId == oldSupplierId ? 'selected' : ''}>
                                    ${s.code} - ${s.name}
                                </option>
                            </c:forEach>
                        </select>

                        <c:if test="${not empty fieldErrors.supplierId}">
                            <div class="invalid-feedback d-block">${fieldErrors.supplierId}</div>
                        </c:if>
                    </div>

                    <div class="col-md-4">
                        <label class="form-label">Expected Delivery Date</label>
                        <input type="date" class="form-control"
                               name="expectedDeliveryDate"
                               value="${oldExpected}">
                        <c:if test="${not empty fieldErrors.expectedDeliveryDate}">
                            <div class="text-danger small">${fieldErrors.expectedDeliveryDate}</div>
                        </c:if>
                    </div>

                    <div class="col-12">
                        <label class="form-label">Note</label>
                        <textarea class="form-control" name="note" rows="2">${oldNote}</textarea>
                        <c:if test="${not empty fieldErrors.note}">
                            <div class="text-danger small">${fieldErrors.note}</div>
                        </c:if>
                    </div>

                </div>
            </div>

            <!-- Lines -->
            <div class="card shadow-sm">
                <div class="card-header d-flex justify-content-between align-items-center">
                    <span class="fw-semibold">Lines</span>
                    <button type="button" class="btn btn-sm btn-primary" onclick="addLine()">+ Add line</button>
                </div>

                <c:if test="${not empty fieldErrors.lines}">
                    <div class="px-3 pt-3">
                        <div class="alert alert-danger py-2 mb-0">
                            ${fieldErrors.lines}
                        </div>
                    </div>
                </c:if>

                <div class="card-body p-0">
                    <div class="table-responsive">
                        <table class="table table-bordered mb-0" id="linesTable">
                            <thead class="table-dark text-center">
                                <tr>
                                    <th style="min-width:240px;">Product</th>
                                    <th style="min-width:240px;">Variant</th>
                                    <th style="width:140px;">Quantity</th>
                                    <th style="width:160px;">Unit Price</th>
                                    <th style="width:140px;">Currency</th>
                                    <th style="width:80px;">#</th>
                                </tr>
                            </thead>
                            <tbody></tbody>
                        </table>
                    </div>
                </div>

                <div class="card-footer d-flex justify-content-end gap-2">
                    <button class="btn btn-success" type="submit">Save</button>
                </div>
            </div>
        </form>
    </div>

    <!-- Template: product options (server render once, JS reuse) -->
    <template id="productOptionsTpl">
        <option value="">-- Select product --</option>
        <c:forEach var="p" items="${products}">
            <option value="${p.productId}">${p.sku} - ${p.name}</option>
        </c:forEach>
    </template>

</t:layout>
<script>
    window.oldLines = [
    <c:forEach var="l" items="${oldLines}" varStatus="st">
    {
    productId: "${l.productId}",
            variantId: "${l.variantId}",
            qty: "${l.qty}",
            unitPrice: "${l.unitPrice}",
            currency: "${l.currency}"
    }<c:if test="${!st.last}">,</c:if>
    </c:forEach>
    ];
</script>
<script>
    let idx = 0;

    function addLine() {
        //lấy phần tbody để append thêm dòng
        const tbody = document.querySelector("#linesTable tbody");
        //tạo thêm dòng mới 
        const tr = document.createElement("tr");
        //lấy template product
        const productOptionsHtml = document.getElementById("productOptionsTpl").innerHTML;
        //gán html cho row
        tr.innerHTML =
                '<td>' +
                '  <select class="form-control product-select" name="lines[' + idx + '].productId">' +
                productOptionsHtml +
                '  </select>' +
                '</td>' +
                '<td>' +
                //disabled chỉ bật khi chọn product
                '  <select class="form-control variant-select" name="lines[' + idx + '].variantId" disabled>' +
                '    <option value="">-- Select product first --</option>' +
                '  </select>' +
                '</td>' +
                '<td><input class="form-control qty-input" type="number" step="1"  name="lines[' + idx + '].qty"></td>' +
                '<td><input class="form-control unit-input" type="number" step="1"  \n\
 name="lines[' + idx + '].unitPrice"></td>' +
                '<td><input class="form-control" name="lines[' + idx + '].currency" value="VND" readonly></td>' +
                '<td class="text-center"><button type="button" class="btn btn-sm btn-danger btn-remove">X</button></td>';
        //apend vào tbody
        tbody.appendChild(tr);
        idx++;
    }

    // Product change -> load variants
    document.addEventListener("change", async function (e) {
        if (!e.target.classList.contains("product-select"))
            return;
        const productId = e.target.value;
        const tr = e.target.closest("tr");
        const variantSelect = tr.querySelector(".variant-select");
        // reset variant mỗi lần đổi product
        variantSelect.disabled = true;
        variantSelect.innerHTML = '<option value="">-- Select product first --</option>';
        if (!productId)
            return;
        variantSelect.innerHTML = '<option value="">Loading...</option>';
        try {
            const url = '${pageContext.request.contextPath}/purchase-orders?action=variants&productId=' + encodeURIComponent(productId);
            const res = await fetch(url, {headers: {"Accept": "application/json"}});
            if (!res.ok)
                throw new Error("HTTP " + res.status);
            const data = await res.json();
            if (!data || data.length === 0) {
                variantSelect.innerHTML = '<option value="">(No variants)</option>';
                variantSelect.disabled = true;
                return;
            }
            variantSelect.innerHTML = '<option value="">-- Select variant --</option>';
            data.forEach(v => {
                const opt = document.createElement("option");
                opt.value = v.variantId;
                opt.textContent =
                        (v.variantSku || "") +
                        (v.color ? (" | " + v.color) : "") +
                        (v.size ? (" - " + v.size) : "");
                variantSelect.appendChild(opt);
            });
            variantSelect.disabled = false;
        } catch (err) {
            console.error(err);
            variantSelect.innerHTML = '<option value="">(Load failed)</option>';
            variantSelect.disabled = true;
        }
    });
    // IMPORTANT: submit -> enable tất cả variant-select để backend nhận được value (disabled sẽ không submit)
    document.getElementById("poForm").addEventListener("submit", function () {
        document.querySelectorAll(".variant-select").forEach(s => s.disabled = false);
    });
    // remove row
    document.addEventListener("click", function (e) {
        if (e.target && e.target.classList.contains("btn-remove")) {
            e.target.closest("tr").remove();
        }
    });
    // default 1 line
    async function loadVariantsForRow(productSelect, variantSelect, productId, variantId) {
        // gọi lại đúng endpoint variants
        const url = '${pageContext.request.contextPath}/purchase-orders?action=variants&productId=' + encodeURIComponent(productId);
        const res = await fetch(url, {headers: {"Accept": "application/json"}});
        if (!res.ok)
            throw new Error("HTTP " + res.status);

        const data = await res.json();
        variantSelect.innerHTML = '<option value="">-- Select variant --</option>';

        if (!data || data.length === 0) {
            variantSelect.innerHTML = '<option value="">(No variants)</option>';
            variantSelect.disabled = true;
            return;
        }
        data.forEach(v => {
            const opt = document.createElement("option");
            opt.value = v.variantId;
            opt.textContent =
                    (v.variantSku || "") +
                    (v.color ? (" | " + v.color) : "") +
                    (v.size ? (" - " + v.size) : "");
            variantSelect.appendChild(opt);
        });

        variantSelect.disabled = false;
        if (variantId)
            variantSelect.value = variantId;
    }

    function addLineWithOldData(line) {
        addLine();

        const tbody = document.querySelector("#linesTable tbody");
        const tr = tbody.lastElementChild;

        const productSel = tr.querySelector(".product-select");
        const variantSel = tr.querySelector(".variant-select");
        const qtyEl = tr.querySelector(".qty-input");
        const unitEl = tr.querySelector(".unit-input");
        const currencyEl = tr.querySelector('input[name$=".currency"]');

        // set old values
        if (line.qty != null)
            qtyEl.value = line.qty;
        if (line.unitPrice != null)
            unitEl.value = line.unitPrice;
        if (line.currency)
            currencyEl.value = line.currency;

        // set product + load variants + set variant
        if (line.productId) {
            productSel.value = line.productId;
            variantSel.disabled = true;
            variantSel.innerHTML = '<option value="">Loading...</option>';

            loadVariantsForRow(productSel, variantSel, line.productId, line.variantId)
                    .catch(err => {
                        console.error(err);
                        variantSel.innerHTML = '<option value="">(Load failed)</option>';
                        variantSel.disabled = true;
                    });
        }
    }

// INIT
    (function initLines() {
        const olds = window.oldLines || [];
        if (olds.length > 0) {
            olds.forEach(addLineWithOldData);
        } else {
            addLine();
        }
    })();

</script>

