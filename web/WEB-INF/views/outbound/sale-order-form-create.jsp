<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib tagdir="/WEB-INF/tags/" prefix="t" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<t:layout title="Create Sale Order">

    <div class="py-4">
        <form action="${pageContext.request.contextPath}/sales-orders" method="post" id="soForm">
            <input type="hidden" name="action" value="create"/>

            <!-- Header -->
            <div class="card shadow-sm mb-3">
                <div class="card-header fw-semibold">Header</div>
                <div class="card-body row g-3">

                    <div class="col-md-4">
                        <label class="form-label">SO Number</label>
                        <input class="form-control ${not empty fieldErrors.soNumber ? 'is-invalid' : ''}"
                               name="soNumber"
                               value="${oldSoNumber}">
                        <c:if test="${not empty fieldErrors.soNumber}">
                            <div class="invalid-feedback d-block">${fieldErrors.soNumber}</div>
                        </c:if>
                    </div>

                    <div class="col-md-4">
                        <label class="form-label">Customer</label>
                        <select class="form-control ${not empty fieldErrors.customerId ? 'is-invalid' : ''}"
                                name="customerId">
                            <option value="">-- Select customer --</option>
                            <c:forEach var="cst" items="${customers}">
                                <option value="${cst.customerId}" ${cst.customerId == oldCustomerId ? 'selected' : ''}>
                                    ${cst.code} - ${cst.name}
                                </option>
                            </c:forEach>
                        </select>
                        <c:if test="${not empty fieldErrors.customerId}">
                            <div class="invalid-feedback d-block">${fieldErrors.customerId}</div>
                        </c:if>
                    </div>

                    <div class="col-md-4">
                        <label class="form-label">Ship To Address</label>
                        <input class="form-control ${not empty fieldErrors.shipToAddress ? 'is-invalid' : ''}"
                               name="shipToAddress"
                               value="${oldShipToAddress}">
                        <c:if test="${not empty fieldErrors.shipToAddress}">
                            <div class="invalid-feedback d-block">${fieldErrors.shipToAddress}</div>
                        </c:if>
                    </div>

                    <div class="col-md-4">
                        <label class="form-label">Requested Ship Date</label>
                        <input type="date"
                               class="form-control ${not empty fieldErrors.requestedShipDate ? 'is-invalid' : ''}"
                               name="requestedShipDate"
                               value="${oldRequestedShipDate}">
                        <c:if test="${not empty fieldErrors.requestedShipDate}">
                            <div class="invalid-feedback d-block">${fieldErrors.requestedShipDate}</div>
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

    <!-- Template: product options -->
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
        unitPrice: "${l.unitPrice}"
    }<c:if test="${!st.last}">,</c:if>
    </c:forEach>
    ];
</script>

<script>
    let idx = 0;

    function addLine() {
        const tbody = document.querySelector("#linesTable tbody");
        const tr = document.createElement("tr");
        const productOptionsHtml = document.getElementById("productOptionsTpl").innerHTML;

        tr.innerHTML =
                '<td>' +
                '  <select class="form-control product-select" name="lines[' + idx + '].productId">' +
                productOptionsHtml +
                '  </select>' +
                '</td>' +
                '<td>' +
                '  <select class="form-control variant-select" name="lines[' + idx + '].variantId" disabled>' +
                '    <option value="">-- Select product first --</option>' +
                '  </select>' +
                '</td>' +
                '<td><input class="form-control qty-input" type="number" step="1"  name="lines[' + idx + '].qty"></td>' +
                '<td><input class="form-control unit-input" type="number" step="1" name="lines[' + idx + '].unitPrice"></td>' +
                '<td class="text-center"><button type="button" class="btn btn-sm btn-danger btn-remove">X</button></td>';

        tbody.appendChild(tr);
        idx++;
    }

    // Product change -> load variants via /sales-orders?action=variants
    document.addEventListener("change", async function (e) {
        if (!e.target.classList.contains("product-select"))
            return;
        const productId = e.target.value;
        const tr = e.target.closest("tr");
        const variantSelect = tr.querySelector(".variant-select");

        variantSelect.disabled = true;
        variantSelect.innerHTML = '<option value="">-- Select product first --</option>';
        if (!productId)
            return;

        variantSelect.innerHTML = '<option value="">Loading...</option>';
        try {
            const url = '${pageContext.request.contextPath}/sales-orders?action=variants&productId=' + encodeURIComponent(productId);
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

    document.getElementById("soForm").addEventListener("submit", function () {
        document.querySelectorAll(".variant-select").forEach(s => s.disabled = false);
    });

    document.addEventListener("click", function (e) {
        if (e.target && e.target.classList.contains("btn-remove")) {
            e.target.closest("tr").remove();
        }
    });

    async function loadVariantsForRow(productSelect, variantSelect, productId, variantId) {
        const url = '${pageContext.request.contextPath}/sales-orders?action=variants&productId=' + encodeURIComponent(productId);
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

        if (line.qty != null)
            qtyEl.value = line.qty;
        if (line.unitPrice != null)
            unitEl.value = line.unitPrice;

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

    (function initLines() {
        const olds = window.oldLines || [];
        if (olds.length > 0) {
            olds.forEach(addLineWithOldData);
        } else {
            addLine();
        }
    })();

</script>

