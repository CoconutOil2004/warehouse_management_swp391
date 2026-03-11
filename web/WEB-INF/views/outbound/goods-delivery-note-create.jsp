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
                    <div class="card shadow-sm border-0 mb-4 overflow-hidden">
                        <div class="card-header bg-primary text-white py-3 px-4">
                            <h5 class="card-title mb-0 d-flex align-items-center">
                                <i class="fas fa-file-invoice me-2 opacity-90"></i>Header Information
                            </h5>
                        </div>
                        <div class="card-body p-4">
                            <div>
                                <label class="form-label text-uppercase small fw-bold text-muted mb-2">Sales Order</label>
                                <select name="soNumber" id="soNumberSelect" class="form-control form-control-lg border-2" required>
                                    <option value="">— Select a sales order (no GDN yet) —</option>
                                    <c:forEach var="so" items="${salesOrders}">
                                        <option value="${so.soNumber}">${so.soNumber} — ${so.customerName} (${so.requestedShipDate != null ? so.requestedShipDate : '—'})</option>
                                    </c:forEach>
                                </select>
                                <p class="form-text text-muted small mt-2 mb-0">One GDN is created per selected SO. Only CREATED orders without an existing GDN are listed.</p>
                                <c:if test="${empty salesOrders}">
                                    <p class="text-warning small mt-2 mb-0"><i class="fas fa-info-circle me-1"></i>No sales orders available.</p>
                                </c:if>
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
                                            <td colspan="6" class="text-center text-muted py-4">
                                                Please select a Sales Order to view items
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
            var soSelect = document.getElementById('soNumberSelect');
            var itemsBody = document.getElementById('itemsTableBody');

            function escapeHtml(s) {
                if (s == null) return 'N/A';
                var div = document.createElement('div');
                div.textContent = s;
                return div.innerHTML;
            }

            function renderRows(rows) {
                if (rows.length === 0) {
                    itemsBody.innerHTML = '<tr><td colspan="6" class="text-center text-muted py-4">No items</td></tr>';
                    return;
                }
                var html = '';
                rows.forEach(function(row) {
                    html += '<tr class="text-center">';
                    html += '<td>' + escapeHtml(row.variantSku) + '</td><td>' + escapeHtml(row.productName) + '</td>';
                    html += '<td>' + escapeHtml(row.color) + '</td><td>' + escapeHtml(row.size) + '</td>';
                    html += '<td><strong>' + (row.qtyOrdered != null ? row.qtyOrdered : 0) + '</strong></td>';
                    html += '<td>' + (row.qtyAvailable != null ? row.qtyAvailable : 0) + '</td></tr>';
                });
                itemsBody.innerHTML = html;
            }

            function loadSoItems() {
                var soNumber = soSelect ? soSelect.value : '';
                if (!soNumber || soNumber.trim() === '') {
                    itemsBody.innerHTML = '<tr><td colspan="6" class="text-center text-muted py-4">Please select a Sales Order to view items.</td></tr>';
                    return;
                }
                itemsBody.innerHTML = '<tr><td colspan="6" class="text-center text-muted py-4"><i class="fas fa-spinner fa-spin me-1"></i> Loading...</td></tr>';
                fetch(baseUrl + '?action=getSoDetails&soNumber=' + encodeURIComponent(soNumber))
                    .then(function(r) { return r.json(); })
                    .then(function(data) {
                        var rows = (data.lines || []).map(function(line) {
                            return {
                                variantSku: line.variantSku,
                                productName: line.productName,
                                color: line.color,
                                size: line.size,
                                qtyOrdered: line.qtyOrdered,
                                qtyAvailable: line.qtyAvailable
                            };
                        });
                        renderRows(rows);
                    })
                    .catch(function() {
                        itemsBody.innerHTML = '<tr><td colspan="6" class="text-center text-danger py-4">Error loading items.</td></tr>';
                    });
            }

            if (soSelect) {
                soSelect.addEventListener('change', loadSoItems);
            }

            document.getElementById('gdnForm').addEventListener('submit', function(e) {
                var soNumber = soSelect ? soSelect.value : '';
                if (!soNumber || soNumber.trim() === '') {
                    e.preventDefault();
                    alert('Please select a sales order.');
                }
            });
        })();
    </script>
</t:layout>
