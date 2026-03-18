<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib tagdir="/WEB-INF/tags/" prefix="t" %>
<%@taglib uri="jakarta.tags.core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<t:layout title="Assign Pick Lines - Wave #${wave.waveId}">
    <jsp:attribute name="actions">
        <div class="d-flex gap-2">
            <t:link url="${pageContext.request.contextPath}/pick-wave?action=detail&id=${wave.waveId}"
                    color="dark" variant="split" icon="arrow-left">
                Back to Wave
            </t:link>
        </div>
    </jsp:attribute>

    <jsp:body>
        <c:if test="${not empty error}">
            <div class="alert alert-danger alert-dismissible fade show">
                <i class="fas fa-exclamation-triangle me-2"></i> ${error}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:if>
        <c:if test="${not empty message}">
            <div class="alert alert-success alert-dismissible fade show">
                <i class="fas fa-check-circle me-2"></i> ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:if>

        <div class="row g-4">
            <!-- Wave Info -->
            <div class="col-lg-12">
                <div class="card shadow-sm border-0">
                    <div class="card-header bg-primary text-white py-3">
                        <h5 class="card-title mb-0">Wave #${wave.waveId} - ${wave.waveCode}</h5>
                    </div>
                    <div class="card-body">
                        <div class="row">
                            <div class="col-md-3">
                                <p class="text-muted small mb-1">Status</p>
                                <p class="mb-0 fw-semibold">
                                    <c:choose>
                                        <c:when test="${wave.status == 'CREATED'}">
                                            <span class="badge bg-secondary">Chờ phát hành</span>
                                        </c:when>
                                        <c:when test="${wave.status == 'RELEASED'}">
                                            <span class="badge bg-info">Đã phát hành</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-secondary">${wave.status}</span>
                                        </c:otherwise>
                                    </c:choose>
                                </p>
                            </div>
                            <div class="col-md-3">
                                <p class="text-muted small mb-1">GDN Count</p>
                                <p class="mb-0 fw-semibold">${wave.gdnCount != null ? wave.gdnCount : fn:length(wave.gdns)}</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Staff Workload -->
            <div class="col-lg-4">
                <div class="card shadow-sm border-0">
                    <div class="card-header bg-info text-white py-3">
                        <h5 class="card-title mb-0"><i class="bi bi-people me-2"></i>Staff Workload</h5>
                    </div>
                    <div class="card-body p-0">
                        <div class="list-group list-group-flush">
                            <c:forEach var="staff" items="${staffWorkload}">
                                <div class="list-group-item d-flex justify-content-between align-items-center">
                                    <span>${staff.fullName}</span>
                                    <span class="badge bg-primary rounded-pill">${staff.activeLines} lines</span>
                                </div>
                            </c:forEach>
                            <c:if test="${empty staffWorkload}">
                                <div class="list-group-item text-muted">No staff available</div>
                            </c:if>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Unassigned Lines -->
            <div class="col-lg-8">
                <div class="card shadow-sm border-0">
                    <div class="card-header bg-warning text-dark py-3 d-flex justify-content-between align-items-center">
                        <h5 class="card-title mb-0"><i class="bi bi-list-check me-2"></i>Unassigned Lines</h5>
                        <c:if test="${not empty unassignedLines}">
                            <form method="post" action="${pageContext.request.contextPath}/pick-task">
                                <input type="hidden" name="action" value="auto-assign-lines"/>
                                <input type="hidden" name="waveId" value="${wave.waveId}"/>
                                <button type="submit" class="btn btn-sm btn-outline-dark">
                                    <i class="bi bi-magic"></i> Auto Assign
                                </button>
                            </form>
                        </c:if>
                    </div>
                    <div class="card-body p-0">
                        <c:if test="${empty unassignedLines}">
                            <div class="text-center py-4 text-muted">
                                <i class="bi bi-check-circle fs-1"></i>
                                <p class="mb-0 mt-2">All lines are assigned!</p>
                            </div>
                        </c:if>
                        <c:if test="${not empty unassignedLines}">
                            <form method="post" action="${pageContext.request.contextPath}/pick-task">
                                <input type="hidden" name="action" value="assign-lines"/>
                                <input type="hidden" name="waveId" value="${wave.waveId}"/>
                                
                                <div class="table-responsive">
                                    <table class="table table-hover align-middle mb-0">
                                        <thead class="table-light">
                                            <tr>
                                                <th width="40">
                                                    <input type="checkbox" id="selectAll" onchange="toggleAll(this)"/>
                                                </th>
                                                <th>Line ID</th>
                                                <th>Slot</th>
                                                <th>Zone</th>
                                                <th>Product</th>
                                                <th>Qty Required</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="line" items="${unassignedLines}">
                                                <tr>
                                                    <td>
                                                        <input type="checkbox" name="lineIds" value="${line.pickTaskLineId}" class="line-checkbox"/>
                                                    </td>
                                                    <td>#${line.pickTaskLineId}</td>
                                                    <td>${line.slotCode}</td>
                                                    <td>${line.zoneCode}</td>
                                                    <td>
                                                        <div>${line.variantSku}</div>
                                                        <small class="text-muted">${line.productName}</small>
                                                    </td>
                                                    <td>${line.qtyToPick}</td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                                
                                <div class="card-footer bg-white border-top-0">
                                    <div class="row align-items-end">
                                        <div class="col-md-6">
                                            <label class="form-label">Assign to:</label>
                                            <select name="assignedTo" class="form-select" required>
                                                <option value="">-- Select Staff --</option>
                                                <c:forEach var="staff" items="${warehouseStaff}">
                                                    <option value="${staff.userId}">${staff.fullName}</option>
                                                </c:forEach>
                                            </select>
                                        </div>
                                        <div class="col-md-6">
                                            <button type="submit" class="btn btn-primary w-100">
                                                <i class="bi bi-person-check me-1"></i> Assign Selected
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            </form>
                        </c:if>
                    </div>
                </div>
            </div>
        </div>

        <script>
            function toggleAll(source) {
                const checkboxes = document.querySelectorAll('.line-checkbox');
                for (let checkbox of checkboxes) {
                    checkbox.checked = source.checked;
                }
            }
        </script>
    </jsp:body>
</t:layout>
