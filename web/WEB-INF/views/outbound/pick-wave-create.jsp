<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib tagdir="/WEB-INF/tags/" prefix="t" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<t:layout title="Create Pick Wave">
    <div>
        <div class="d-flex justify-content-between align-items-center mb-4">
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb mb-0">
                    <li class="breadcrumb-item">
                        <a href="${pageContext.request.contextPath}/pick-wave?action=list">Pick Wave</a>
                    </li>
                    <li class="breadcrumb-item active" aria-current="page">Create Pick Wave</li>
                </ol>
            </nav>
            <a href="${pageContext.request.contextPath}/pick-wave?action=list"
               class="btn btn-outline-secondary">
                <i class="fas fa-arrow-left me-1"></i> Back to Pick Wave
            </a>
        </div>

        <!-- Error message -->
        <c:if test="${not empty error}">
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                <i class="fas fa-exclamation-triangle me-2"></i>
                ${error}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:if>

        <!-- Filter GDN candidates -->
        <div class="card mb-4 shadow-sm">
            <div class="card-body">
                <h5 class="card-title mb-3">
                    <i class="fas fa-filter me-2"></i>Filter GDNs
                </h5>
                <p class="text-muted small mb-3">
                    Select zones to filter GDNs that have inventory in those areas. Leave empty to see all GDNs in CREATED status.
                </p>
                <form action="${pageContext.request.contextPath}/pick-wave" method="get" class="row g-3">
                    <input type="hidden" name="action" value="create"/>

                    <!-- Zone multi-select -->
                    <div class="col-md-4">
                        <label class="form-label fw-bold">
                            <i class="fas fa-warehouse me-1"></i>Zones
                        </label>
                        <select class="form-select" name="zoneIds" multiple size="5">
                            <option value="">-- All Zones --</option>
                            <c:forEach var="z" items="${zones}">
                                <option value="${z.zoneId}"
                                    ${fn:contains(selectedZoneIds, z.zoneId) ? 'selected' : ''}>
                                    ${z.code} - ${z.name}
                                </option>
                            </c:forEach>
                        </select>
                        <small class="text-muted">Hold Ctrl to select multiple zones</small>
                    </div>

                    <div class="col-md-3">
                        <label class="form-label fw-bold">GDN Number</label>
                        <input type="text"
                               class="form-control"
                               name="gdnNumber"
                               value="${gdnNumber}"
                               placeholder="Search GDN number...">
                    </div>
                    <div class="col-md-3">
                        <label class="form-label fw-bold">Sales Order Number</label>
                        <input type="text"
                               class="form-control"
                               name="soNumber"
                               value="${soNumber}"
                               placeholder="Search SO number...">
                    </div>
                    <div class="col-md-2">
                        <label class="form-label fw-bold">Status</label>
                        <select class="form-select" name="status">
                            <option value="CREATED" ${status == 'CREATED' || empty status ? 'selected' : ''}>CREATED</option>
                            <option value="PICKING" ${status == 'PICKING' ? 'selected' : ''}>PICKING</option>
                            <option value="PACKING" ${status == 'PACKING' ? 'selected' : ''}>PACKING</option>
                            <option value="" ${status == '' ? 'selected' : ''}>-- All --</option>
                        </select>
                    </div>
                    <div class="col-md-12 d-flex align-items-end">
                        <button type="submit" class="btn btn-primary me-2">
                            <i class="fas fa-search"></i> Filter
                        </button>
                        <button type="button" class="btn btn-outline-secondary" onclick="resetFilter()">
                            <i class="fas fa-redo"></i> Reset
                        </button>
                    </div>
                </form>
            </div>
        </div>

        <!-- Preview Panel -->
        <c:if test="${not empty gdns}">
            <div class="card mb-4 shadow-sm border-info">
                <div class="card-body">
                    <div class="row align-items-center">
                        <div class="col-md-8">
                            <h6 class="card-title mb-2">
                                <i class="fas fa-info-circle me-2"></i>Preview Selection
                            </h6>
                            <p class="mb-0 text-muted">
                                Chọn các GDN bạn muốn thêm vào Pick Wave. Hệ thống sẽ tự động tạo tasks theo zones.
                            </p>
                        </div>
                        <div class="col-md-4 text-end">
                            <div class="d-flex justify-content-end gap-3">
                                <div>
                                    <span class="d-block text-muted small">Selected GDNs</span>
                                    <span class="fs-4 fw-bold text-primary" id="selectedCount">0</span>
                                </div>
                                <div>
                                    <span class="d-block text-muted small">Est. Lines</span>
                                    <span class="fs-4 fw-bold text-success" id="estLines">0</span>
                                </div>
                                <div>
                                    <span class="d-block text-muted small">Est. Zones</span>
                                    <span class="fs-4 fw-bold text-info" id="estZones">0</span>
                                </div>
                            </div>
                        </div>
                    </div>
                    <!-- Warning for > 20 GDNs -->
                    <div class="alert alert-warning mt-3 mb-0" id="warningBox" style="display: none;">
                        <i class="fas fa-exclamation-triangle me-2"></i>
                        <strong>Warning:</strong> Chọn nhiều hơn 20 GDNs có thể làm chậm hệ thống.
                    </div>
                </div>
            </div>
        </c:if>

        <!-- GDN selection & create wave -->
        <form action="${pageContext.request.contextPath}/pick-wave" method="post" id="createWaveForm">
            <input type="hidden" name="action" value="create"/>
            <div class="card shadow-sm">
                <div class="card-header d-flex justify-content-between align-items-center">
                    <h5 class="mb-0">
                        <i class="fas fa-boxes me-2"></i>GDN candidates
                    </h5>
                    <button type="submit"
                            class="btn btn-primary"
                            id="createWaveBtn"
                            disabled>
                        <i class="fas fa-box-open me-1"></i>
                        Create Pick Wave &amp; Assign Tasks
                    </button>
                </div>
                <div class="card-body p-0">
                    <div class="table-responsive">
                        <table class="table table-hover align-middle mb-0">
                            <thead class="table-light">
                                <tr>
                                    <th style="width: 50px;" class="text-center align-middle">
                                        <div class="form-check">
                                            <input type="checkbox" id="selectAll" class="form-check-input" >
                                        </div>
                                    </th>
                                    <th class="align-middle">GDN Number</th>
                                    <th class="align-middle">Sales Order</th>
                                    <th class="align-middle">Customer</th>
                                    <th class="align-middle text-center">Status</th>
                                    <th class="align-middle">Created By</th>
                                    <th class="align-middle">Created At</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="g" items="${gdns}">
                                    <tr class="gdn-row"
                                        data-gdn-id="${g.gdnId}"
                                        data-est-lines="5"
                                        data-est-zones="2">
                                        <td class="text-center align-middle">
                                            <div class="form-check">
                                                <input type="checkbox"
                                                       name="gdnId"
                                                       value="${g.gdnId}"
                                                       class="form-check-input gdn-checkbox">
                                            </div>
                                        </td>
                                        <td class="fw-semibold text-primary">
                                            ${g.gdnNumber}
                                        </td>
                                        <td>${g.soNumber}</td>
                                        <td>${g.customerName}</td>
                                        <td class="text-center">
                                            <span class="badge ${
                                                g.status == 'CREATED' ? 'bg-secondary' :
                                                (g.status == 'PICKING' ? 'bg-warning text-dark' :
                                                (g.status == 'PACKING' ? 'bg-info text-dark' :
                                                (g.status == 'CANCELLED' ? 'bg-danger' :
                                                (g.status == 'DONE' || g.status == 'CONFIRMED' ? 'bg-success' : 'bg-secondary'))))}">
                                                ${g.status}
                                            </span>
                                        </td>
                                        <td>${g.creatorName}</td>
                                        <td class="text-center">
                                            ${g.createdAtDisplay}
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty gdns}">
                                    <tr>
                                        <td colspan="7" class="text-center py-4 text-muted">
                                            <i class="fas fa-inbox fa-2x mb-2 d-block"></i>
                                            No eligible GDN found with current filters.
                                        </td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>
                <c:if test="${not empty gdns}">
                    <div class="card-footer text-muted small">
                        <i class="fas fa-info-circle me-1"></i>
                        Showing ${fn:length(gdns)} GDNs. Select at least 1 GDN to create a Pick Wave.
                    </div>
                </c:if>
            </div>
        </form>
    </div>

    <script>
        // Preview panel update
        document.addEventListener('DOMContentLoaded', function() {
            const checkboxes = document.querySelectorAll('.gdn-checkbox');
            const selectAll = document.getElementById('selectAll');
            const selectedCountEl = document.getElementById('selectedCount');
            const estLinesEl = document.getElementById('estLines');
            const estZonesEl = document.getElementById('estZones');
            const warningBox = document.getElementById('warningBox');
            const createWaveBtn = document.getElementById('createWaveBtn');

            function updatePreview() {
                const checked = document.querySelectorAll('.gdn-checkbox:checked');
                const count = checked.length;

                // Update count
                selectedCountEl.textContent = count;

                // Calculate estimated lines and zones (simplified - in real app, fetch from server)
                let totalLines = 0;
                let zones = new Set();
                checked.forEach(cb => {
                    const row = cb.closest('tr');
                    totalLines += parseInt(row.dataset.estLines || 5);
                    // In real app, get actual zones from data attribute
                    zones.add('Zone A');
                    zones.add('Zone B');
                });

                estLinesEl.textContent = totalLines;
                estZonesEl.textContent = zones.size;

                // Show/hide warning
                if (count > 20) {
                    warningBox.style.display = 'block';
                } else {
                    warningBox.style.display = 'none';
                }

                // Enable/disable button
                createWaveBtn.disabled = count === 0;
            }

            // Select all
            selectAll.addEventListener('change', function() {
                checkboxes.forEach(cb => {
                    cb.checked = this.checked;
                });
                updatePreview();
            });

            // Individual checkbox change
            checkboxes.forEach(cb => {
                cb.addEventListener('change', updatePreview);
            });

            // Initial update
            updatePreview();
        });

        function resetFilter() {
            window.location.href = '${pageContext.request.contextPath}/pick-wave?action=create';
        }
    </script>
</t:layout>
