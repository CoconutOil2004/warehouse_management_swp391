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
                                <p class="text-muted small mb-1 text-uppercase fw-bold">Wave ID</p>
                                <p class="mb-0 fw-semibold">#${wave.waveId}</p>
                            </div>
                            <div class="col">
                                <p class="text-muted small mb-1 text-uppercase fw-bold">GDN Number</p>
                                <p class="mb-0 fw-semibold">
                                    <a href="${pageContext.request.contextPath}/goods-delivery-note?action=detail&id=${wave.gdnId}" class="text-decoration-none">
                                        ${wave.gdnNumber}
                                    </a>
                                </p>
                            </div>
                            <div class="col">
                                <p class="text-muted small mb-1 text-uppercase fw-bold">Status</p>
                                <p class="mb-0">
                                    <span class="badge ${wave.status == 'CREATED' ? 'bg-secondary' : 'bg-info'} fw-semibold">
                                        ${wave.status}
                                    </span>
                                </p>
                            </div>
                            <div class="col">
                                <p class="text-muted small mb-1 text-uppercase fw-bold">Created At</p>
                                <p class="mb-0 fw-semibold">${wave.createdAtDisplay != null ? wave.createdAtDisplay : wave.createdAt}</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

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
