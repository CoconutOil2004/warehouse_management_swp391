<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib tagdir="/WEB-INF/tags/" prefix="t" %>
<%@taglib uri="jakarta.tags.core" prefix="c" %>
<t:layout title="Create Goods Delivery Note">
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
    <div class="container-fluid py-4">
        <div class="mb-3">
            <a href="${pageContext.request.contextPath}/goods-delivery-note?action=list"
                class="text-decoration-none text-muted">
                <i class="fas fa-arrow-left me-1"></i> Back to List
            </a>
        </div>

        <form action="${pageContext.request.contextPath}/goods-delivery-note" method="post" id="gdnForm">
            <input type="hidden" name="action" value="create" />

            <div class="row">
                <div class="col-lg-12">
                    <div class="card shadow-sm border-0 mb-4">
                        <div class="card-header bg-primary text-white py-3">
                            <h5 class="card-title mb-0"><i class="fas fa-file-invoice me-2"></i>Header Information</h5>
                        </div>
                        <div class="card-body">
                            <p class="text-muted small mb-2">Select one or more sales orders (SO without GDN). Each SO can only have one GDN.</p>
                            <div class="table-responsive border rounded">
                                <table class="table table-hover mb-0">
                                    <thead class="table-light">
                                        <tr class="align-middle">
                                            <th style="width: 52px;" class="text-center align-middle py-3">
                                                <div class="d-flex align-items-center justify-content-center">
                                                    <input type="checkbox" id="selectAllSo" class="form-check-input m-0" title="Select all" />
                                                </div>
                                            </th>
                                            <th class="align-middle">SO Number</th>
                                            <th class="align-middle">Customer</th>
                                            <th class="align-middle">Requested Ship Date</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="so" items="${salesOrders}">
                                            <tr class="align-middle">
                                                <td class="text-center align-middle py-2">
                                                    <div class="d-flex align-items-center justify-content-center">
                                                        <input type="checkbox" name="soNumbers" value="${so.soNumber}" class="form-check-input m-0 so-checkbox" />
                                                    </div>
                                                </td>
                                                <td class="fw-semibold align-middle">${so.soNumber}</td>
                                                <td class="align-middle">${so.customerName}</td>
                                                <td class="align-middle">${so.requestedShipDate != null ? so.requestedShipDate : '—'}</td>
                                            </tr>
                                        </c:forEach>
                                        <c:if test="${empty salesOrders}">
                                            <tr>
                                                <td colspan="4" class="text-center text-muted py-4">No sales orders (CREATED) available without GDN.</td>
                                            </tr>
                                        </c:if>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-lg-12">
                    <div class="card shadow-sm border-0">
                        <div class="card-header bg-dark text-white py-3">
                            <h5 class="card-title mb-0"><i class="fas fa-boxes me-2"></i>Items to Pick</h5>
                        </div>
                        <div class="card-body p-0">
                            <div class="table-responsive">
                                <table class="table table-hover align-middle mb-0" id="itemsTable">
                                    <thead class="table-light">
                                        <tr class="text-center">
                                            <th id="thSoNumber" class="d-none">SO Number</th>
                                            <th>Variant SKU</th>
                                            <th>Product Name</th>
                                            <th>Color</th>
                                            <th>Size</th>
                                            <th>Quantity Required</th>
                                            <th>Available</th>
                                        </tr>
                                    </thead>
                                    <tbody id="itemsTableBody">
                                        <tr>
                                            <td colspan="7" class="text-center text-muted py-4">
                                                Please select Sales Order(s) to view items
                                            </td>
                                        </tr>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col-12 mt-4">
                    <div class="d-flex justify-content-end gap-2">
                        <a href="${pageContext.request.contextPath}/goods-delivery-note?action=list"
                            class="btn btn-secondary">Cancel</a>
                        <button type="submit" class="btn btn-primary" id="btnCreateGdn">
                            <i class="fas fa-save me-1"></i> Create GDN
                        </button>
                    </div>
                </div>
            </div>
        </form>
    </div>

    <script>
        (function() {
            var baseUrl = '${pageContext.request.contextPath}/goods-delivery-note';
            var soCheckboxes = document.querySelectorAll('.so-checkbox');
            var selectAll = document.getElementById('selectAllSo');
            var itemsBody = document.getElementById('itemsTableBody');
            var thSoNumber = document.getElementById('thSoNumber');

            function getCheckedSoNumbers() {
                var checked = document.querySelectorAll('.so-checkbox:checked');
                return Array.prototype.map.call(checked, function(c) { return c.value; });
            }

            function escapeHtml(s) {
                if (s == null) return 'N/A';
                var div = document.createElement('div');
                div.textContent = s;
                return div.innerHTML;
            }

            function renderRows(rows, showSoColumn) {
                if (rows.length === 0) {
                    itemsBody.innerHTML = '<tr><td colspan="7" class="text-center text-muted py-4">No items</td></tr>';
                    return;
                }
                var html = '';
                rows.forEach(function(row) {
                    html += '<tr class="text-center">';
                    if (showSoColumn) {
                        html += '<td class="fw-semibold">' + escapeHtml(row.soNumber) + '</td>';
                    }
                    html += '<td>' + escapeHtml(row.variantSku) + '</td><td>' + escapeHtml(row.productName) + '</td>';
                    html += '<td>' + escapeHtml(row.color) + '</td><td>' + escapeHtml(row.size) + '</td>';
                    html += '<td><strong>' + (row.qtyOrdered != null ? row.qtyOrdered : 0) + '</strong></td>';
                    html += '<td>' + (row.qtyAvailable != null ? row.qtyAvailable : 0) + '</td></tr>';
                });
                itemsBody.innerHTML = html;
            }

            function loadSoItems() {
                var soNumbers = getCheckedSoNumbers();
                if (soNumbers.length === 0) {
                    thSoNumber.classList.add('d-none');
                    itemsBody.innerHTML = '<tr><td colspan="7" class="text-center text-muted py-4">Please select Sales Order(s) to view items.</td></tr>';
                    return;
                }

                var showSoColumn = soNumbers.length > 1;
                if (showSoColumn) {
                    thSoNumber.classList.remove('d-none');
                } else {
                    thSoNumber.classList.add('d-none');
                }

                var promises = soNumbers.map(function(soNumber) {
                    return fetch(baseUrl + '?action=getSoDetails&soNumber=' + encodeURIComponent(soNumber))
                        .then(function(r) { return r.json(); })
                        .then(function(data) {
                            var soNum = data.soNumber || soNumber;
                            return (data.lines || []).map(function(line) {
                                return {
                                    soNumber: soNum,
                                    variantSku: line.variantSku,
                                    productName: line.productName,
                                    color: line.color,
                                    size: line.size,
                                    qtyOrdered: line.qtyOrdered,
                                    qtyAvailable: line.qtyAvailable
                                };
                            });
                        })
                        .catch(function() { return []; });
                });

                itemsBody.innerHTML = '<tr><td colspan="7" class="text-center text-muted py-4"><i class="fas fa-spinner fa-spin me-1"></i> Loading...</td></tr>';

                Promise.all(promises).then(function(results) {
                    var allRows = [];
                    results.forEach(function(rows) { allRows = allRows.concat(rows); });
                    renderRows(allRows, showSoColumn);
                }).catch(function() {
                    itemsBody.innerHTML = '<tr><td colspan="7" class="text-center text-danger py-4">Error loading items.</td></tr>';
                });
            }

            if (selectAll) {
                selectAll.addEventListener('change', function() {
                    soCheckboxes.forEach(function(cb) { cb.checked = selectAll.checked; });
                    loadSoItems();
                });
            }
            soCheckboxes.forEach(function(cb) {
                cb.addEventListener('change', loadSoItems);
            });

            document.getElementById('gdnForm').addEventListener('submit', function(e) {
                if (getCheckedSoNumbers().length === 0) {
                    e.preventDefault();
                    alert('Please select at least one sales order.');
                }
            });
        })();
    </script>
</t:layout>
