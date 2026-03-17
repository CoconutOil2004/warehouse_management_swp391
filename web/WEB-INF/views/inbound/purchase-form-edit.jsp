<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib tagdir="/WEB-INF/tags/" prefix="t"%>

<t:layout title="Edit Purchase Order">

    <c:if test="${empty po}">
        <div class="alert alert-danger m-4">PO not found</div>
    </c:if>

    <c:if test="${not empty po}">
        <div class="py-4">
            <form action="${pageContext.request.contextPath}/purchase-orders" method="post" id="poForm">
                <input type="hidden" name="action" value="update"/>
                <input type="hidden" name="poId" value="${po.poId}"/>

                <!-- Header -->
                <div class="card shadow-sm mb-3">
                    <div class="card-header fw-semibold">Header</div>
                    <div class="card-body row g-3">

                        <div class="col-md-4">
                            <label class="form-label">PO Number</label>
                            <input class="form-control ${not empty fieldErrors.poNumber ? 'is-invalid' : ''}"
                                   name="poNumber"
                                   value="${po.poNumber}"
                                   ${po.status != 'CREATED' ? 'readonly' : ''}>
                            <c:if test="${not empty fieldErrors.poNumber}">
                                <div class="invalid-feedback d-block">${fieldErrors.poNumber}</div>
                            </c:if>
                        </div>

                        <div class="col-md-4">
                            <label class="form-label">Supplier</label>
                            <select class="form-control ${not empty fieldErrors.supplierId ? 'is-invalid' : ''}"
                                    name="supplierId"
                                    ${po.status != 'CREATED' ? 'disabled' : ''}>
                                <option value="">-- Select supplier --</option>
                                <c:forEach var="s" items="${suppliers}">
                                    <option value="${s.supplierId}" ${s.supplierId == po.supplierId ? 'selected' : ''}>
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
                                   value="${po.expectedDeliveryDate}"
                                   ${po.status != 'CREATED' ? 'readonly' : ''}>
                            <c:if test="${not empty fieldErrors.expectedDeliveryDate}">
                                <div class="text-danger small">${fieldErrors.expectedDeliveryDate}</div>
                            </c:if>
                        </div>

                        <div class="col-12">
                            <label class="form-label">Note</label>
                            <textarea class="form-control" name="note" rows="2"
                                      ${po.status != 'CREATED' ? 'readonly' : ''}>${po.note}</textarea>
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
                        <button class="btn btn-success" type="submit"
                                ${po.status != 'CREATED' ? 'disabled' : ''}>Update PO</button>
                        <a href="${pageContext.request.contextPath}/purchase-orders" class="btn btn-secondary">Back</a>
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

        <script>
            window.existingLines = [];

            <c:choose>

                <c:when test="${not empty oldLines}">
                    <c:forEach var="l" items="${oldLines}">
            window.existingLines.push({
                poLineId: "<c:out value='${l["poLineId"]}'/>",
                productId: "<c:out value='${l["productId"]}'/>",
                variantId: "<c:out value='${l["variantId"]}'/>",
                qty: "<c:out value='${l["qty"]}'/>",
                unitPrice: "<c:out value='${l["unitPrice"]}'/>",
                currency: "<c:out value='${l["currency"]}'/>"
            });
                    </c:forEach>
                </c:when>

                <c:otherwise>
                    <c:forEach var="l" items="${lines}">
                        <fmt:formatNumber var="qtyStr" value="${l.orderedQty}" groupingUsed="false" minFractionDigits="0" maxFractionDigits="0"/>
                        <fmt:formatNumber var="unitPriceStr" value="${l.unitPrice}" groupingUsed="false" minFractionDigits="0" maxFractionDigits="0"/>
            window.existingLines.push({
                poLineId: "${l.poLineId}",
                productId: "${l.productId}",
                variantId: "${l.variantId}",
                qty: "<c:out value='${qtyStr}'/>",
                unitPrice: "<c:out value='${unitPriceStr}'/>",
                currency: "VND"
            });
                    </c:forEach>
                </c:otherwise>

            </c:choose>
        </script>


        <script>
            let idx = 0;
            function addLine() {
                const tbody = document.querySelector("#linesTable tbody");
                const tr = document.createElement("tr");
                const productOptionsHtml = document.getElementById("productOptionsTpl").innerHTML;
                tr.innerHTML =
                        '<td>' +
                        '  <input type="hidden" class="po-line-id" name="lines[' + idx + '].poLineId" value="">' +
                        '  <select class="form-control product-select" name="lines[' + idx + '].productId">' +
                        productOptionsHtml +
                        '  </select>' +
                        '</td>' +
                        '<td>' +
                        '  <select class="form-control variant-select" name="lines[' + idx + '].variantId" disabled>' +
                        '    <option value="">-- Select product first --</option>' +
                        '  </select>' +
                        '</td>' +
                        '<td><input class="form-control qty-input" type="number" step="1" min="1" name="lines[' + idx + '].qty"></td>' +
                        '<td><input class="form-control unit-input" type="number" step="1" min="0" name="lines[' + idx + '].unitPrice"></td>' +
                        '<td><input class="form-control currency-input" name="lines[' + idx + '].currency" value="VND" readonly></td>' +
                        '<td class="text-center"><button type="button" class="btn btn-sm btn-danger btn-remove">X</button></td>';
                tbody.appendChild(tr);
                idx++;
                return tr; // return for further manipulation
            }
            // Product change -> load variants
            document.addEventListener("change", async function (e) {
                if (!e.target.classList.contains("product-select"))
                    return;
                const productId = e.target.value;
                const tr = e.target.closest("tr");
                const variantSelect = tr.querySelector(".variant-select");
                // reset variant
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
            // IMPORTANT: submit -> enable all variant-selects
            document.getElementById("poForm").addEventListener("submit", function () {
                document.querySelectorAll(".variant-select").forEach(s => s.disabled = false);
            });
            // remove row
            document.addEventListener("click", function (e) {
                if (e.target && e.target.classList.contains("btn-remove")) {
                    e.target.closest("tr").remove();
                }
            });
            async function loadVariantsForRow(productSelect, variantSelect, productId, variantId) {
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
                if (variantId) {
                    variantSelect.value = variantId;
                }
            }
            function addLineWithData(line) {
                // Add plain line first
                const tr = addLine();
                const productSel = tr.querySelector(".product-select");
                const variantSel = tr.querySelector(".variant-select");
                const qtyEl = tr.querySelector(".qty-input");
                const unitEl = tr.querySelector(".unit-input");
                const currencyEl = tr.querySelector(".currency-input");
                const poLineIdEl = tr.querySelector(".po-line-id");
                // set values
                if (line.poLineId)
                    poLineIdEl.value = line.poLineId;
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
            (function initLines() {
                const existing = window.existingLines || [];
                if (existing.length > 0) {
                    existing.forEach(addLineWithData);
                } else {
                    addLine();
                }
            })();
        </script>
    </c:if>
</t:layout>
