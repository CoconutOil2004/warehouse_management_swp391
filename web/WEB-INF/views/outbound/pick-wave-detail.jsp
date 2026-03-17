<%@taglib tagdir="/WEB-INF/tags/" prefix="t" %>
<%@taglib uri="jakarta.tags.core" prefix="c" %>
<%@taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<%@taglib uri="jakarta.tags.functions" prefix="fn" %>

<t:layout title="Wave Detail: #${wave.waveId}">
    <jsp:attribute name="actions">
        <div class="d-flex gap-2">
            <t:link url="${pageContext.request.contextPath}/pick-wave?action=list"
                    color="dark" variant="split" icon="arrow-left">
                Back to List
            </t:link>
            <t:link url="${pageContext.request.contextPath}/pick-task?action=assign&waveId=${wave.waveId}"
                    color="primary" variant="split" icon="person-check">
                Assign Tasks
            </t:link>
        </div>
    </jsp:attribute>

    <jsp:body>
        <!-- Error/Success Messages -->
        <c:if test="${not empty error}">
            <div class="alert alert-danger alert-dismissible fade show">
                <i class="fas fa-exclamation-triangle me-2"></i>
                ${error}
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
            <!-- Header Info Card -->
            <div class="col-lg-12">
                <div class="card shadow-sm border-0 mb-4">
                    <div class="card-header bg-primary text-white py-3">
                        <h5 class="card-title mb-0"><i class="bi bi-info-circle me-2"></i>General Information</h5>
                    </div>
                    <div class="card-body">
                        <div class="row row-cols-1 row-cols-md-2 row-cols-lg-4 g-3">
                            <div class="col">
                                <p class="text-muted small mb-1 text-uppercase fw-bold">Wave Code</p>
                                <p class="mb-0 fw-semibold">${wave.waveCode != null ? wave.waveCode : '#' + wave.waveId}</p>
                            </div>
                            <div class="col">
                                <p class="text-muted small mb-1 text-uppercase fw-bold">Wave ID</p>
                                <p class="mb-0 fw-semibold">#${wave.waveId}</p>
                            </div>
                            <div class="col">
                                <p class="text-muted small mb-1 text-uppercase fw-bold">GDN Count</p>
                                <p class="mb-0 fw-semibold">${wave.gdnCount != null ? wave.gdnCount : (wave.gdns != null ? fn:length(wave.gdns) : 1)}</p>
                            </div>
                            <div class="col">
                                <p class="text-muted small mb-1 text-uppercase fw-bold">Status</p>
                                <p class="mb-0">
                                    <span class="badge ${wave.status == 'CREATED' ? 'bg-secondary' : (wave.status == 'IN_PROGRESS' ? 'bg-warning text-dark' : 'bg-success')} fw-semibold">
                                        ${wave.status}
                                    </span>
                                </p>
                            </div>
                            <div class="col">
                                <p class="text-muted small mb-1 text-uppercase fw-bold">Created At</p>
                                <p class="mb-0 fw-semibold">${wave.createdAtDisplay != null ? wave.createdAtDisplay : wave.createdAt}</p>
                            </div>
                            <div class="col">
                                <p class="text-muted small mb-1 text-uppercase fw-bold">Created By</p>
                                <p class="mb-0 fw-semibold">${wave.createdByName != null ? wave.createdByName : '-'}</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- GDNs in Wave -->
            <c:if test="${not empty wave.gdns}">
                <div class="col-lg-12">
                    <div class="card shadow-sm border-0 mb-4">
                        <div class="card-header bg-success text-white py-3">
                            <h5 class="card-title mb-0"><i class="bi bi-box-seam me-2"></i>Goods Delivery Notes in Wave</h5>
                        </div>
                        <div class="card-body p-0">
                            <div class="table-responsive">
                                <table class="table table-hover align-middle mb-0">
                                    <thead class="table-light">
                                        <tr>
                                            <th>GDN Number</th>
                                            <th>Sales Order</th>
                                            <th>Customer</th>
                                            <th>Status</th>
                                            <th>Created At</th>
                                            <th>Action</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="gdn" items="${wave.gdns}">
                                            <tr>
                                                <td class="fw-semibold text-primary">
                                                    <a href="${pageContext.request.contextPath}/goods-delivery-note?action=detail&id=${gdn.gdnId}" class="text-decoration-none">
                                                        ${gdn.gdnNumber}
                                                    </a>
                                                </td>
                                                <td>${gdn.soNumber}</td>
                                                <td>${gdn.customerName}</td>
                                                <td>
                                                    <span class="badge ${gdn.status == 'PENDING' ? 'bg-secondary' : (gdn.status == 'ONGOING' ? 'bg-warning text-dark' : 'bg-success')}">
                                                        ${gdn.status}
                                                    </span>
                                                </td>
                                                <td>${gdn.createdAtDisplay}</td>
                                                <td>
                                                    <div class="btn-group btn-group-sm" role="group">
                                                        <a href="${pageContext.request.contextPath}/goods-delivery-note?action=detail&id=${gdn.gdnId}"
                                                           class="btn btn-outline-primary">
                                                            <i class="bi bi-eye"></i> View
                                                        </a>
                                                        <c:if test="${wave.status == 'CREATED'}">
                                                            <button type="button"
                                                                    class="btn btn-outline-danger"
                                                                    data-bs-toggle="modal"
                                                                    data-bs-target="#removeGdnModal${gdn.gdnId}">
                                                                <i class="bi bi-trash"></i> Remove
                                                            </button>

                                                            <!-- Remove GDN Modal -->
                                                            <div class="modal fade" id="removeGdnModal${gdn.gdnId}" tabindex="-1" aria-hidden="true">
                                                                <div class="modal-dialog modal-dialog-centered">
                                                                    <div class="modal-content">
                                                                        <div class="modal-header bg-danger text-white">
                                                                            <h5 class="modal-title">
                                                                                <i class="bi bi-exclamation-triangle me-2"></i>Confirm Remove GDN
                                                                            </h5>
                                                                            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                                                                        </div>
                                                                        <div class="modal-body">
                                                                            <p>Are you sure you want to remove GDN <strong>${gdn.gdnNumber}</strong> from this wave?</p>
                                                                            <p class="text-muted small mb-0">
                                                                                <i class="bi bi-info-circle me-1"></i>
                                                                                This action cannot be undone. If this is the last GDN, the wave will be deleted.
                                                                            </p>
                                                                        </div>
                                                                        <div class="modal-footer">
                                                                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                                                                            <form action="${pageContext.request.contextPath}/pick-wave" method="get" class="d-inline">
                                                                                <input type="hidden" name="action" value="remove-gdn"/>
                                                                                <input type="hidden" name="waveId" value="${wave.waveId}"/>
                                                                                <input type="hidden" name="gdnId" value="${gdn.gdnId}"/>
                                                                                <button type="submit" class="btn btn-danger">
                                                                                    <i class="bi bi-trash me-1"></i>Remove GDN
                                                                                </button>
                                                                            </form>
                                                                        </div>
                                                                    </div>
                                                                </div>
                                                            </div>
                                                        </c:if>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            </c:if>

            <!-- Tasks Table -->
            <div class="col-lg-12">
                <c:set var="columns" value='${["Task ID", "GDN", "SO", "Status", "Assigned to"]}' />
                <t:table columns="${columns}">
                    <jsp:attribute name="head">
                        <div class="p-2 fw-bold text-uppercase small text-muted">
                            <i class="bi bi-list-task me-2"></i>Pick Tasks
                        </div>
                    </jsp:attribute>
                    <jsp:body>
                        <c:forEach var="t" items="${tasks}">
                            <tr>
                                <td>${t.pickTaskId}</td>
                                <td>
                                    <a href="${pageContext.request.contextPath}/goods-delivery-note?action=detail&id=${t.gdnId}" class="text-decoration-none">
                                        ${t.gdnNumber}
                                    </a>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty t.soId}">
                                            <a href="${pageContext.request.contextPath}/sales-orders?action=detail&id=${t.soId}" class="text-decoration-none">
                                                ${t.soNumber}
                                            </a>
                                        </c:when>
                                        <c:otherwise>${t.soNumber != null ? t.soNumber : "-"}</c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <span class="badge bg-info">${t.status}</span>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty t.assignedTo}">
                                            <a href="${pageContext.request.contextPath}/admin/user/detail?id=${t.assignedTo}" class="text-decoration-none">
                                                ${t.assignedToName}
                                            </a>
                                        </c:when>
                                        <c:otherwise>-</c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty tasks}">
                            <tr>
                                <td colspan="5" class="text-center py-4 text-muted">
                                    No tasks found for this wave.
                                </td>
                            </tr>
                        </c:if>
                    </jsp:body>
                </t:table>
            </div>
        </div>
    </jsp:body>
</t:layout>
