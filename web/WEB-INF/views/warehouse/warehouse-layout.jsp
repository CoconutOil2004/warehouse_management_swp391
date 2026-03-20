<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib tagdir="/WEB-INF/tags/" prefix="t"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:set var="zoneHasSlots" value="${zoneHasSlots}" />
<c:if test="${empty zoneHasSlots}">
    <c:set var="zoneHasSlots" value="${null}" />
</c:if>

<t:layout title="Warehouse Layout">
    <style>
    .zone-card {
        margin-bottom: 20px;
        border: 1px solid #dee2e6;
        border-radius: 8px;
        padding: 15px;
    }
    .slot-grid {
        display: grid;
        gap: 5px;
        margin-top: 10px;
    }
    .slot-item {
        border: 1px solid #ccc;
        padding: 8px;
        text-align: center;
        background-color: #f8f9fa;
        border-radius: 4px;
        font-size: 12px;
    }
    .slot-item.blocked {
        background-color: #dc3545;
        color: white;
    }
    .slot-item.active {
        background-color: #28a745;
        color: white;
    }
    /* Custom dropdown styling */
    select#zoneType.form-select {
        background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 16 16'%3e%3cpath fill='none' stroke='%23343a40' stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M2 5l6 6 6-6'/%3e%3c/svg%3e") !important;
        background-repeat: no-repeat !important;
        background-position: right 0.75rem center !important;
        background-size: 16px 12px !important;
        padding-right: 2.5rem !important;
        transition: all 0.15s ease-in-out;
        appearance: none;
        -webkit-appearance: none;
        -moz-appearance: none;
    }
    select#zoneType.form-select:hover {
        border-color: #86b7fe;
        box-shadow: 0 0 0 0.25rem rgba(13, 110, 253, 0.25);
    }
    select#zoneType.form-select:focus {
        border-color: #86b7fe;
        outline: 0;
        box-shadow: 0 0 0 0.25rem rgba(13, 110, 253, 0.25);
    }
</style>

    <c:if test="${not empty error}">
    <div class="alert alert-danger alert-dismissible fade show" role="alert">
        <c:out value="${error}" />
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
    </c:if>

    <c:if test="${selectedWarehouseId != null}">

    <!-- Form tạo Zone -->
    <div class="card mb-4">
        <div class="card-header">
            <h5 class="mb-0">Create Zone</h5>
        </div>
        <div class="card-body">
            <form method="post" action="${pageContext.request.contextPath}/warehouse-layout">
                <input type="hidden" name="action" value="create-zone">
                <input type="hidden" name="warehouseId" value="${selectedWarehouseId}">
                
                <div class="row">
                    <div class="col-md-3">
                        <label for="code" class="form-label">Zone Code</label>
                        <input type="text" class="form-control" id="code" name="code" required>
                    </div>
                    <div class="col-md-4">
                        <label for="name" class="form-label">Zone Name</label>
                        <input type="text" class="form-control" id="name" name="name" required>
                    </div>
                    <div class="col-md-3">
                        <label for="zoneType" class="form-label">Zone Type</label>
                        <select class="form-select" id="zoneType" name="zoneType" required style="cursor: pointer; height: 38px;">
                            <option value="">-- Select Type --</option>
                            <option value="STORAGE">Storage Zone (Main)</option>
                            <option value="EXCESS">Excess Zone (Overstock)</option>
                            <option value="DAMAGE">Damage Zone (Defects)</option>
                        </select>
                    </div>
                    <div class="col-md-2">
                        <label class="form-label">&nbsp;</label>
                        <button type="submit" class="btn btn-primary w-100">Create Zone</button>
                    </div>
                </div>
            </form>
        </div>
    </div>

    <!-- Danh sách Zones -->
    <c:if test="${not empty zones}">
    <div class="card mb-4">
        <div class="card-header">
            <h5 class="mb-0">Zone List</h5>
        </div>
        <div class="card-body">
            <c:forEach items="${zones}" var="zone">
                <div class="zone-card">
                    <div class="d-flex justify-content-between align-items-center mb-3">
                        <div>
                            <h6 class="mb-1">
                                <span class="badge bg-info"><c:out value="${zone.code}" /></span>
                                <c:out value="${zone.name}" />
                            </h6>
                            <small class="text-muted">Type: 
                                <c:choose>
                                    <c:when test="${zone.zoneType == 'STORAGE'}">Storage Zone (Main)</c:when>
                                    <c:when test="${zone.zoneType == 'EXCESS'}">Excess Zone (Overstock)</c:when>
                                    <c:when test="${zone.zoneType == 'DAMAGE'}">Damage Zone (Defects)</c:when>
                                    <c:otherwise><c:out value="${zone.zoneType}" /></c:otherwise>
                                </c:choose>
                                <span>Status: 
                                        <span class="badge bg-${zone.status == 'ACTIVE' ? 'success' : 'secondary'}">
                                                <c:out value="${zone.status}" /></span>
</span>
                            </small>
                        </div>
                        <div class="d-flex align-items-center" style="gap: 12px;">
                            <a href="${pageContext.request.contextPath}/warehouse-layout?action=view-zone&zoneId=${zone.zoneId}" 
                               class="btn btn-sm btn-outline-primary" style="min-width: 110px; position: relative; z-index: 10; pointer-events: auto;">
                                View Layout
                            </a>
                            <c:set var="hasSlots" value="${zoneHasSlots != null && zoneHasSlots[zone.zoneId]}" />
                            <c:set var="buttonClass" value="${hasSlots ? 'btn-outline-warning' : 'btn-outline-secondary'}" />
                            <c:set var="buttonText" value="${hasSlots ? 'Edit Layout' : 'Create Slots'}" />
                            <button type="button" class="btn btn-sm ${buttonClass}" 
                                    style="min-width: 140px; position: relative; z-index: 10; pointer-events: auto;"
                                    data-bs-toggle="modal" 
                                    data-bs-target="#slotModal${zone.zoneId}"
                                    onclick="event.stopPropagation();">
                                ${buttonText}
                            </button>
                        </div>
                    </div>
                </div>

                <!-- Modal tạo Slots cho Zone -->
                <div class="modal fade" id="slotModal${zone.zoneId}" tabindex="-1" aria-labelledby="slotModalLabel${zone.zoneId}" aria-hidden="true">
                    <div class="modal-dialog modal-lg">
                        <div class="modal-content">
                            <div class="modal-header">
                                <h5 class="modal-title" id="slotModalLabel${zone.zoneId}">
                                    <c:out value="${hasSlots ? 'Edit Layout' : 'Create Slots'}" /> for Zone: <c:out value="${zone.code}" /> - <c:out value="${zone.name}" />
                                </h5>
                                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                            </div>
                            <div class="modal-body">
                                <form method="post" action="${pageContext.request.contextPath}/warehouse-layout" id="slotForm${zone.zoneId}">
                                    <input type="hidden" name="action" value="create-slots">
                                    <input type="hidden" name="zoneId" value="${zone.zoneId}">
                                    <c:if test="${hasSlots}">
                                    <div class="alert alert-warning">
                                        <strong>Note:</strong> This zone already has slots. The system will only add new slots that do not exist.
                                        Existing slots will not be changed.
                                    </div>
                                    </c:if>
                                    
                                    <div class="row mb-3">
                                        <div class="col-md-4">
                                            <label for="rows${zone.zoneId}" class="form-label">Number of Rows</label>
                                            <input type="number" class="form-control" id="rows${zone.zoneId}" name="rows" min="1" max="100" value="5" required>
                                        </div>
                                        <div class="col-md-4">
                                            <label for="cols${zone.zoneId}" class="form-label">Number of Columns</label>
                                            <input type="number" class="form-control" id="cols${zone.zoneId}" name="cols" min="1" max="100" value="5" required>
                                        </div>
                                        <div class="col-md-4">
                                            <label for="codePrefix${zone.zoneId}" class="form-label">Slot Code Prefix</label>
                                            <input type="text" class="form-control" id="codePrefix${zone.zoneId}" name="codePrefix"
                                                   value="<c:out value="${zone.code}" />" readonly>
                                            <div class="form-text">Prefix is fixed to the zone code to avoid incorrect slot codes.</div>
                                        </div>
                                    </div>
                                    
                                    <div class="alert alert-info">
                                        <strong>Note:</strong> The system will create grid slots with format: 
                                        <code>[Prefix]-R[Row]-C[Column]</code>
                                        <br>Example: ZONE-A-R1-C1, ZONE-A-R1-C2, ...
                                        <c:if test="${hasSlots}">
                                        <br><strong>Existing slots will be skipped.</strong>
                                        </c:if>
                                    </div>
                                </form>
                            </div>
                            <div class="modal-footer">
                                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                                <button type="submit" class="btn btn-primary" form="slotForm${zone.zoneId}">
                                    <c:out value="${hasSlots ? 'Add Slots' : 'Create Slots'}" />
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>
    </div>
    </c:if>
    <c:if test="${empty zones}">
    <div class="alert alert-info">
        No zones found. Please create a zone first.
    </div>
    </c:if>

    </c:if>

    <script>
        // Use Bootstrap's built-in data API for opening/closing modals.
        // No custom modal instance management here to avoid stuck backdrops.
    </script>
</t:layout>
