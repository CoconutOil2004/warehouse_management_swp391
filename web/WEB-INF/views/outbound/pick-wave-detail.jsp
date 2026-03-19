<%@taglib tagdir="/WEB-INF/tags/" prefix="t" %>
<%@taglib uri="jakarta.tags.core" prefix="c" %>
<%@taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<%@taglib uri="jakarta.tags.functions" prefix="fn" %>

<t:layout title="Wave Detail: #${wave.waveId}">
    <jsp:attribute name="actions">
        <div class="d-flex gap-2">
            <div>
              <t:link url="${pageContext.request.contextPath}/pick-wave?action=list" color="dark" variant="split" icon="arrow-left">
                Back
              </t:link>
            </div>
            <c:if test="${wave.status == 'CREATED'}">
                <c:if test="${unassignedCount == 0}">
                    <form method="post" action="${pageContext.request.contextPath}/pick-wave">
                        <input type="hidden" name="action" value="release"/>
                        <input type="hidden" name="id" value="${wave.waveId}"/>
                        <button type="submit" class="btn btn-success"
                            onclick="return confirm('Release wave? Workers can start picking.')">
                            <i class="bi bi-play-fill"></i> Release
                        </button>
                    </form>
                </c:if>
                <c:if test="${unassignedCount > 0}">
                    <div class="btn btn-secondary" title="Assign all tasks before releasing">
                        <i class="bi bi-hourglass-split"></i> Release (${unassignedCount} lines pending)
                    </div>
                </c:if>
            </c:if>
            <c:if test="${wave.status == 'CREATED' || wave.status == 'RELEASED'}">
                <form method="post" action="${pageContext.request.contextPath}/pick-wave">
                    <input type="hidden" name="action" value="cancel"/>
                    <input type="hidden" name="id" value="${wave.waveId}"/>
                    <button type="submit" class="btn btn-danger"
                        onclick="return confirm('Cancel wave?')">
                        <i class="bi bi-x-lg"></i> Cancel
                    </button>
                </form>
            </c:if>
            <div>
              <t:link url="${pageContext.request.contextPath}/pick-task?action=assign&waveId=${wave.waveId}"
                      color="primary" variant="split" icon="person-check">
                  Assign
              </t:link>
            </div>
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
                                <p class="mb-0 fw-semibold">${wave.waveCode != null ? wave.waveCode : '#'.concat(wave.waveId)}</p>
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
                                  ${wave.status}
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
                                                    <c:set var="gdnStatusBadge" value="bg-secondary" />
                                                    <c:choose>
                                                        <c:when test="${gdn.status == 'CREATED'}"><c:set var="gdnStatusBadge" value="bg-secondary" /></c:when>
                                                        <c:when test="${gdn.status == 'PICKING'}"><c:set var="gdnStatusBadge" value="bg-warning text-dark" /></c:when>
                                                        <c:when test="${gdn.status == 'PACKING'}"><c:set var="gdnStatusBadge" value="bg-info text-dark" /></c:when>
                                                        <c:when test="${gdn.status == 'SHIPPING'}"><c:set var="gdnStatusBadge" value="bg-primary" /></c:when>
                                                        <c:when test="${gdn.status == 'DONE'}"><c:set var="gdnStatusBadge" value="bg-success" /></c:when>
                                                        <c:when test="${gdn.status == 'CANCELLED'}"><c:set var="gdnStatusBadge" value="bg-danger" /></c:when>
                                                    </c:choose>
                                                    <span class="badge ${gdnStatusBadge}">
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
                <div class="card shadow-sm border-0 mb-4">
                    <div class="card-header bg-primary text-white py-3">
                        <h5 class="card-title mb-0"><i class="bi bi-list-task me-2"></i>Pick Tasks</h5>
                    </div>
                    <div class="card-body p-0">
                        <div class="table-responsive">
                            <table class="table table-hover align-middle mb-0">
                                <thead class="table-light">
                                    <tr>
                                        <th>Task ID</th>
                                        <th>Zone</th>
                                        <th>Lines</th>
                                        <th>Status</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="t" items="${tasks}">
                                        <tr>
                                            <td class="fw-semibold">#${t.pickTaskId}</td>
                                            <td>
                                                <c:if test="${not empty t.lines}">
                                                    ${t.lines[0].zoneCode}
                                                </c:if>
                                            </td>
                                            <td>
                                                <span class="badge bg-secondary">${t.totalLines != null ? t.totalLines : (t.lines != null ? fn:length(t.lines) : 0)}</span>
                                            </td>
                                            <td>
                                              <c:if test="${not empty t.status}">
                                                  ${t.status}
                                              </c:if>
                                            </td>
                                            <td>
                                                <button class="btn btn-sm btn-outline-primary" type="button"
                                                    data-bs-toggle="collapse" data-bs-target="#taskLines${t.pickTaskId}">
                                                    <i class="bi bi-chevron-down"></i> View Lines
                                                </button>
                                            </td>
                                        </tr>
                                        <!-- Lines sub-table -->
                                        <tr class="collapse" id="taskLines${t.pickTaskId}">
                                            <td colspan="5" class="p-0">
                                                <div class="bg-light p-3">
                                                    <table class="table table-sm mb-0">
                                                        <thead>
                                                            <tr>
                                                                <th>Line ID</th>
                                                                <th>Slot</th>
                                                                <th>Product</th>
                                                                <th>Qty Required</th>
                                                                <th>Qty Picked</th>
                                                                <th>Status</th>
                                                                <th>Assigned To</th>
                                                            </tr>
                                                        </thead>
                                                        <tbody>
                                                            <c:forEach var="line" items="${t.lines}">
                                                                <tr>
                                                                    <td>#${line.pickTaskLineId}</td>
                                                                    <td>${line.slotCode}</td>
                                                                    <td>${line.variantSku} - ${line.productName}</td>
                                                                    <td>${line.qtyToPick}</td>
                                                                    <td>${line.qtyPicked}</td>
                                                                    <td>
                                                                      ${line.pickStatus}
                                                                    </td>
                                                                    <td>${line.assignedToName != null ? line.assignedToName : '-'}</td>
                                                                </tr>
                                                            </c:forEach>
                                                        </tbody>
                                                    </table>
                                                </div>
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
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </jsp:body>
</t:layout>
