<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib tagdir="/WEB-INF/tags/" prefix="t" %>
<%@taglib uri="jakarta.tags.core" prefix="c" %>
<%@taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<t:layout title="Packing">
    <div class="container-fluid">
        <c:choose>
            <c:when test="${isReadyView}">
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <h4 class="mb-0"><i class="fas fa-box me-2"></i>Packing Queue</h4>
                    <div class="d-flex gap-2">
                        <a href="${pageContext.request.contextPath}/packing?action=list" class="btn btn-outline-secondary">
                            <i class="fas fa-list me-1"></i> All Packings
                        </a>
                        <a href="${pageContext.request.contextPath}/goods-delivery-note?action=list" class="btn btn-outline-secondary">
                            <i class="fas fa-file-alt me-1"></i> GDN List
                        </a>
                    </div>
                </div>
                <div class="alert alert-info mb-4">
                    <i class="fas fa-info-circle me-2"></i>
                    <strong>GDNs ready for packing:</strong> These are GDNs where pick tasks are completed and status is PACKING or CONFIRMED.
                    Click "Start" or "Packing Station" to begin packing.
                </div>
            </c:when>
            <c:otherwise>
                <div class="d-flex justify-content-between align-items-center mb-4">
                    <h4 class="mb-0"><i class="fas fa-box me-2"></i>Packing Management</h4>
                    <div class="d-flex gap-2">
                        <a href="${pageContext.request.contextPath}/packing?action=ready" class="btn btn-warning">
                            <i class="fas fa-clipboard-list me-1"></i> Packing Queue
                        </a>
                        <a href="${pageContext.request.contextPath}/goods-delivery-note?action=list" class="btn btn-outline-secondary">
                            <i class="fas fa-file-alt me-1"></i> GDN List
                        </a>
                    </div>
                </div>
                <div class="card mb-4 shadow-sm">
                    <div class="card-body">
                        <form hx-get="${pageContext.request.contextPath}/packing" hx-target="#wrapper" hx-select="#wrapper" hx-swap="outerHTML" hx-push-url="true" method="get" class="row g-3">
                            <input type="hidden" name="action" value="list"/>
                            <div class="col-md-3">
                                <label class="form-label fw-bold">Status</label>
                                <select class="form-select" name="status">
                                    <option value="">-- All --</option>
                                    <option value="PENDING" ${param.status == 'PENDING' ? 'selected' : ''}>PENDING</option>
                                    <option value="IN_PROGRESS" ${param.status == 'IN_PROGRESS' ? 'selected' : ''}>IN PROGRESS</option>
                                    <option value="DONE" ${param.status == 'DONE' ? 'selected' : ''}>DONE</option>
                                </select>
                            </div>
                            <div class="col-md-2 d-flex align-items-end">
                                <button type="submit" class="btn btn-primary w-100"><i class="fas fa-search me-1"></i> Filter</button>
                            </div>
                        </form>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>

        <div class="table-responsive shadow-sm rounded">
            <table class="table table-bordered table-hover align-middle mb-0">
                <thead class="table-dark">
                    <tr class="text-center">
                        <th>Pack ID</th>
                        <th>GDN Number</th>
                        <th>Package Type</th>
                        <th>Weight</th>
                        <th>Status</th>
                        <th>Packed by / at</th>
                        <th>Label</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="p" items="${packings}">
                        <tr>
                            <td class="text-center">${p.packId != null ? p.packId : '-'}</td>
                            <td>
                                <a href="${pageContext.request.contextPath}/goods-delivery-note?action=detail&id=${p.gdnId}" class="text-decoration-none">
                                    ${p.gdnNumber}
                                </a>
                            </td>
                            <td class="text-center">
                                <c:if test="${not empty p.packageType}">
                                    <span class="badge bg-secondary">
                                        <i class="fas ${p.packageType == 'BOX' ? 'fa-box-open' : 
                                                          p.packageType == 'ENVELOPE' ? 'fa-envelope-open-text' : 
                                                          p.packageType == 'BAG' ? 'fa-shopping-bag' : 
                                                          p.packageType == 'PALLET' ? 'fa-pallet' : 'fa-box'} me-1"></i>
                                        ${p.packageType}
                                    </span>
                                </c:if>
                                <c:if test="${empty p.packageType}">
                                    <span class="text-muted">-</span>
                                </c:if>
                            </td>
                            <td class="text-center">
                                <c:if test="${not empty p.weight}">
                                    ${p.weight} ${p.weightUnit}
                                </c:if>
                                <c:if test="${empty p.weight}">
                                    <span class="text-muted">-</span>
                                </c:if>
                            </td>
                            <td class="text-center">
                                <span class="badge ${
                                    p.status == 'DONE' ? 'bg-success' :
                                    p.status == 'IN_PROGRESS' ? 'bg-info' :
                                    'bg-warning text-dark'}">
                                    ${p.status}
                                </span>
                            </td>
                            <td class="text-center">
                                ${p.packedByName != null ? p.packedByName : '-'}
                                <c:if test="${not empty p.packedAt}">
                                    / <c:out value="${p.packedAt}"/>
                                </c:if>
                            </td>
                            <td>${p.packageLabel != null ? p.packageLabel : '-'}</td>
                            <td class="text-center">
                                <div class="btn-group btn-group-sm">
                                    <c:choose>
                                        <c:when test="${not empty p.packId}">
                                            <a href="${pageContext.request.contextPath}/packing?action=station&gdnId=${p.gdnId}" 
                                               class="btn btn-primary" title="Packing Station">
                                                <i class="fas fa-boxes"></i>
                                            </a>
                                            <a href="${pageContext.request.contextPath}/packing?action=form&gdnId=${p.gdnId}" 
                                               class="btn btn-secondary" title="Edit">
                                                <i class="fas fa-edit"></i>
                                            </a>
                                        </c:when>
                                        <c:otherwise>
                                            <a href="${pageContext.request.contextPath}/packing?action=form&gdnId=${p.gdnId}" 
                                               class="btn btn-warning" title="Start Packing">
                                                <i class="fas fa-box-open"></i> Start
                                            </a>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty packings}">
                        <tr><td colspan="8" class="text-center py-4 text-muted">
                            <c:choose>
                                <c:when test="${isReadyView}">
                                    <i class="fas fa-inbox me-2"></i>No GDNs ready for packing. Complete pick tasks first.
                                </c:when>
                                <c:otherwise>
                                    <i class="fas fa-inbox me-2"></i>No packing records found.
                                </c:otherwise>
                            </c:choose>
                        </td></tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>
</t:layout>
