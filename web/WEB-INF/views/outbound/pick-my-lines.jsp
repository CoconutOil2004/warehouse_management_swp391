<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib tagdir="/WEB-INF/tags/" prefix="t" %>
<%@taglib uri="jakarta.tags.core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<t:layout title="My Pick Lines">
    <jsp:attribute name="actions">
        <div class="d-flex gap-2">
            <span class="navbar-text text-dark fw-semibold">
                <i class="bi bi-person-circle me-1"></i> ${user.fullName}
            </span>
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

        <div class="row g-3">
            <div class="col-12">
                <h4 class="mb-3"><i class="bi bi-box-seam me-2"></i>My Pick Lines</h4>
            </div>

            <c:if test="${empty lines}">
                <div class="col-12">
                    <div class="card shadow-sm border-0">
                        <div class="card-body text-center py-5">
                            <i class="bi bi-check-circle fs-1 text-success"></i>
                            <h5 class="mt-3">No assigned lines</h5>
                            <p class="text-muted mb-0">You have no pick tasks assigned yet.</p>
                        </div>
                    </div>
                </div>
            </c:if>

            <c:forEach var="line" items="${lines}">
                <div class="col-12">
                    <div class="card shadow-sm border-0 ${line.pickStatus == 'DONE' ? 'border-success' : ''}">
                        <div class="card-body">
                            <div class="d-flex justify-content-between align-items-start mb-3">
                                <div>
                                    <h5 class="mb-1">
                                        <span class="badge bg-secondary me-2">#${line.pickTaskLineId}</span>
                                        ${line.variantSku}
                                    </h5>
                                    <p class="mb-0 text-muted">${line.productName}</p>
                                </div>
                                <div class="text-end">
                                    <c:choose>
                                        <c:when test="${line.pickStatus == 'PENDING'}">
                                            <span class="badge bg-warning text-dark">Chờ nhặt</span>
                                        </c:when>
                                        <c:when test="${line.pickStatus == 'PICKED'}">
                                            <span class="badge bg-info">Đang nhặt</span>
                                        </c:when>
                                        <c:when test="${line.pickStatus == 'DONE'}">
                                            <span class="badge bg-success">Hoàn thành</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-secondary">${line.pickStatus}</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>

                            <div class="row g-2 mb-3">
                                <div class="col-6">
                                    <div class="bg-light rounded p-2">
                                        <small class="text-muted d-block">Location</small>
                                        <strong>${line.slotCode}</strong>
                                        <span class="text-muted">(${line.zoneCode})</span>
                                    </div>
                                </div>
                                <div class="col-6">
                                    <div class="bg-light rounded p-2">
                                        <small class="text-muted d-block">Quantity</small>
                                        <strong>${line.qtyPicked} / ${line.qtyToPick}</strong>
                                    </div>
                                </div>
                            </div>

                            <c:if test="${line.pickStatus == 'PENDING' || line.pickStatus == 'PICKED'}">
                                <form method="post" action="${pageContext.request.contextPath}/pick-task">
                                    <input type="hidden" name="action" value="start-line"/>
                                    <input type="hidden" name="lineId" value="${line.pickTaskLineId}"/>
                                    <button type="submit" class="btn btn-primary w-100">
                                        <i class="bi bi-play-fill me-1"></i> Start Picking
                                    </button>
                                </form>
                            </c:if>

                            <c:if test="${line.pickStatus == 'PICKED'}">
                                <form method="post" action="${pageContext.request.contextPath}/pick-task" class="mt-2">
                                    <input type="hidden" name="action" value="save-pick"/>
                                    <input type="hidden" name="lineId" value="${line.pickTaskLineId}"/>
                                    <div class="input-group">
                                        <span class="input-group-text">Qty Picked</span>
                                        <input type="number" class="form-control" name="qtyPicked" 
                                               value="${line.qtyToPick}" min="0" max="${line.qtyToPick}" step="1"/>
                                        <button type="submit" class="btn btn-success">
                                            <i class="bi bi-check-lg"></i> Complete
                                        </button>
                                    </div>
                                </form>
                            </c:if>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>
    </jsp:body>
</t:layout>
