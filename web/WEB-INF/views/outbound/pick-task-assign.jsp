<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib tagdir="/WEB-INF/tags/" prefix="t" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<t:layout title="Assign Pick Tasks">
    <div>
        <!-- Header -->
        <div class="d-flex justify-content-between align-items-center mb-4">
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb mb-0">
                    <li class="breadcrumb-item">
                        <a href="${pageContext.request.contextPath}/pick-wave?action=list">Pick Wave</a>
                    </li>
                    <li class="breadcrumb-item">
                        <c:forEach var="gdn" items="${wave.gdns}" varStatus="status">
                            <a href="${pageContext.request.contextPath}/goods-delivery-note?action=detail&id=${gdn.gdnId}">${gdn.gdnNumber}</a><c:if test="${!status.last}">, </c:if>
                        </c:forEach>
                    </li>
                    <li class="breadcrumb-item active">Assign tasks</li>
                </ol>
            </nav>
            <c:if test="${not empty wave.gdns}">
            <a href="${pageContext.request.contextPath}/pick-wave?action=detail&id=${wave.waveId}"
               class="btn btn-outline-secondary btn-sm">
                <i class="fas fa-arrow-left me-1"></i> Back
            </a>
            </c:if>
        </div>

        <!-- Alert Messages -->
        <c:if test="${not empty param.error}">
            <div class="alert alert-danger alert-dismissible fade show">
                <i class="fas fa-exclamation-triangle me-2"></i>
                <c:choose>
                    <c:when test="${param.error == 'no_tasks'}">Please select staff for at least one task.</c:when>
                    <c:otherwise>Error occurred during assignment.</c:otherwise>
                </c:choose>
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:if>
        <c:if test="${not empty message}">
            <div class="alert alert-success alert-dismissible fade show">
                <i class="fas fa-check-circle me-2"></i> ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:if>

        <!-- Wave Info Card -->
        <div class="card shadow-sm mb-4 border-0">
            <div class="card-body py-3">
                <div class="row align-items-center">
                    <div class="col-md-8">
                        <h6 class="mb-0 fw-bold text-primary">
                            <i class="fas fa-wave-square me-2"></i>Wave #${wave.waveId}
                            <c:if test="${not empty wave.waveCode}"><small class="text-muted">(${wave.waveCode})</small></c:if>
                        </h6>
                        <p class="text-muted mb-0 small mt-1">
                            <c:forEach var="gdn" items="${wave.gdns}" varStatus="status">${gdn.gdnNumber}<c:if test="${!status.last}">, </c:if></c:forEach>
                            | Status: <span class="badge bg-info">${wave.status}</span> | ${tasks.size()} tasks
                        </p>
                    </div>
                    <div class="col-md-4 text-end">
                        <form action="${pageContext.request.contextPath}/pick-task" method="post" class="d-inline">
                            <input type="hidden" name="action" value="auto-assign"/>
                            <input type="hidden" name="waveId" value="${wave.waveId}"/>
                            <button type="submit" class="btn btn-outline-primary btn-sm"
                                onclick="return confirm('Auto-assign will distribute all unassigned tasks based on workload?')">
                                <i class="fas fa-magic me-1"></i> Auto-Assign
                            </button>
                        </form>
                    </div>
                </div>
            </div>
        </div>

        <!-- Main Assign Form -->
        <form id="assignAllForm" action="${pageContext.request.contextPath}/pick-task" method="post">
            <input type="hidden" name="action" value="assign-lines-batch"/>
            <input type="hidden" name="waveId" value="${wave.waveId}"/>

            <div class="card shadow-sm border-0">
                <div class="card-header bg-white border-0 py-3">
                    <div class="d-flex justify-content-between align-items-center">
                        <h6 class="mb-0 fw-bold">
                            <i class="fas fa-users-cog me-2 text-primary"></i>Assign Pick Lines to Staff
                        </h6>
                        <button type="submit" class="btn btn-primary btn-sm">
                            <i class="fas fa-paper-plane me-2"></i>Submit All Assignments
                        </button>
                    </div>
                </div>
                <div class="card-body p-0">
                    <div class="table-responsive">
                        <table class="table table-hover align-middle mb-0">
                            <thead class="bg-light">
                                <tr>
                                    <th class="ps-4">Line ID</th>
                                    <th>GDN</th>
                                    <th>Product</th>
                                    <th>Slot</th>
                                    <th>Zone</th>
                                    <th>Qty</th>
                                    <th>Status</th>
                                    <th class="pe-4">
                                        <i class="fas fa-user-tie me-1"></i>Assign to
                                    </th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="t" items="${tasks}">
                                    <c:forEach var="line" items="${t.lines}" varStatus="lineStatus">
                                        <tr class="${lineStatus.index % 2 == 0 ? '' : 'bg-light-subtle'}">
                                            <td class="ps-4 fw-bold text-primary">#${line.pickTaskLineId}</td>
                                            <td>
                                                <div class="fw-semibold">${t.gdnNumber}</div>
                                                <small class="text-muted">Task #${t.pickTaskId}</small>
                                            </td>
                                            <td>
                                                <div>${line.productName}</div>
                                                <small class="text-muted">${line.variantSku}</small>
                                            </td>
                                            <td>${line.slotCode}</td>
                                            <td>${line.zoneCode}</td>
                                            <td>${line.qtyToPick}</td>
                                            <td>
                                                <span class="badge
                                                    <c:choose>
                                                        <c:when test="${line.pickStatus == 'PENDING'}">bg-secondary</c:when>
                                                        <c:when test="${line.pickStatus == 'PICKED'}">bg-info</c:when>
                                                        <c:when test="${line.pickStatus == 'DONE'}">bg-success</c:when>
                                                        <c:otherwise>bg-warning</c:otherwise>
                                                    </c:choose>">
                                                    ${line.pickStatus}
                                                </span>
                                            </td>
                                            <td class="pe-4">
                                                <div class="d-flex align-items-center">
                                                    <select name="assignedTo_${line.pickTaskLineId}"
                                                            class="form-select form-select-sm staff-select"
                                                            data-line-id="${line.pickTaskLineId}"
                                                            ${line.pickStatus == 'DONE' || line.pickStatus == 'COMPLETED' ? 'disabled' : ''}>
                                                        <option value="">-- Select staff --</option>
                                                        <c:forEach var="u" items="${warehouseStaff}">
                                                            <option value="${u.userId}" ${line.assignedTo != null && line.assignedTo == u.userId ? 'selected' : ''}>
                                                                ${u.fullName}
                                                            </option>
                                                        </c:forEach>
                                                    </select>
                                                    <input type="hidden" name="lineIds" value="${line.pickTaskLineId}"/>
                                                </div>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:forEach>
                                <c:if test="${empty tasks}">
                                    <tr>
                                        <td colspan="8" class="text-center py-5 text-muted">
                                            <i class="fas fa-inbox fa-3x mb-3 d-block opacity-25"></i>
                                            <p>No tasks found in this wave.</p>
                                        </td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>
                <div class="card-footer bg-white border-0 py-3">
                    <div class="d-flex justify-content-between align-items-center">
                        <div class="text-muted small">
                            <i class="fas fa-info-circle me-1"></i>
                            Select staff for each task, then click "Submit All Assignments"
                        </div>
                        <button type="submit" class="btn btn-primary btn-sm">
                            <i class="fas fa-check-circle me-2"></i>Submit All Assignments
                        </button>
                    </div>
                </div>
            </div>
        </form>
    </div>

    <script>
        // Form validation
        document.getElementById('assignAllForm').addEventListener('submit', function(e) {
            const selects = document.querySelectorAll('.staff-select');
            let hasSelection = false;

            selects.forEach(select => {
                if (select.value) {
                    hasSelection = true;
                }
            });

            if (!hasSelection) {
                e.preventDefault();
                alert('Please select staff for at least one task.');
            }
        });
    </script>
</t:layout>
