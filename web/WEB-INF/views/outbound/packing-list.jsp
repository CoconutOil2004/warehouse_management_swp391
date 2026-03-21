<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib tagdir="/WEB-INF/tags/" prefix="t" %>
<%@taglib uri="jakarta.tags.core" prefix="c" %>
<%@taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<%@taglib uri="jakarta.tags.functions" prefix="fn" %>

<t:layout title="Packing Management">
    <jsp:attribute name="actions">
        <div class="d-flex gap-2">
            <c:if test="${fn:contains(sessionScope.USER.roleNames, 'ADMIN') || fn:contains(sessionScope.USER.roleNames, 'WAREHOUSE_MANAGER')}">
                <a href="${pageContext.request.contextPath}/packing?action=create&step=1" class="btn btn-primary btn-icon-split shadow-sm">
                    <span class="icon d-flex align-items-center">
                        <i class="bi bi-plus-lg"></i>
                    </span>
                    <span class="text">Create Packing</span>
                </a>
            </c:if>
            <c:if test="${fn:contains(sessionScope.USER.roleNames, 'WAREHOUSE_STAFF')}">
                <a href="${pageContext.request.contextPath}/packing?action=myTasks" class="btn btn-info btn-icon-split shadow-sm ml-2">
                    <span class="icon d-flex align-items-center">
                        <i class="fas fa-tasks"></i>
                    </span>
                    <span class="text">My Tasks</span>
                </a>
            </c:if>
        </div>
    </jsp:attribute>

    <jsp:body>
        <div class="row">
            <!-- PENDING Card -->
            <div class="col-xl-4 col-md-6 mb-4">
                <div class="card border-left-warning shadow h-100 py-2">
                    <div class="card-body">
                        <div class="row no-gutters align-items-center">
                            <div class="col mr-2">
                                <div class="text-xs font-weight-bold text-warning text-uppercase mb-1">Pending Sessions</div>
                                <div class="h5 mb-0 font-weight-bold text-gray-800">
                                    <c:set var="pendingCount" value="0"/>
                                    <c:forEach var="s" items="${sessions}">
                                        <c:if test="${s.status == 'PENDING'}"><c:set var="pendingCount" value="${pendingCount + 1}"/></c:if>
                                    </c:forEach>
                                    ${pendingCount}
                                </div>
                            </div>
                            <div class="col-auto">
                                <i class="fas fa-clock fa-2x text-gray-300"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- IN PROGRESS Card -->
            <div class="col-xl-4 col-md-6 mb-4">
                <div class="card border-left-info shadow h-100 py-2">
                    <div class="card-body">
                        <div class="row no-gutters align-items-center">
                            <div class="col mr-2">
                                <div class="text-xs font-weight-bold text-info text-uppercase mb-1">In Progress</div>
                                <div class="h5 mb-0 font-weight-bold text-gray-800">
                                    <c:set var="progressCount" value="0"/>
                                    <c:forEach var="s" items="${sessions}">
                                        <c:if test="${s.status == 'IN_PROGRESS'}"><c:set var="progressCount" value="${progressCount + 1}"/></c:if>
                                    </c:forEach>
                                    ${progressCount}
                                </div>
                            </div>
                            <div class="col-auto">
                                <i class="fas fa-tasks fa-2x text-gray-300"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- DONE Card -->
            <div class="col-xl-4 col-md-6 mb-4">
                <div class="card border-left-success shadow h-100 py-2">
                    <div class="card-body">
                        <div class="row no-gutters align-items-center">
                            <div class="col mr-2">
                                <div class="text-xs font-weight-bold text-success text-uppercase mb-1">Completed (Total)</div>
                                <div class="h5 mb-0 font-weight-bold text-gray-800">
                                    <c:set var="doneCount" value="0"/>
                                    <c:forEach var="s" items="${sessions}">
                                        <c:if test="${s.status == 'DONE'}"><c:set var="doneCount" value="${doneCount + 1}"/></c:if>
                                    </c:forEach>
                                    ${doneCount}
                                </div>
                            </div>
                            <div class="col-auto">
                                <i class="fas fa-check-circle fa-2x text-gray-300"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <c:if test="${not empty param.message}">
            <div class="alert alert-success alert-dismissible fade show shadow-sm border-0 mb-4" role="alert">
                <span class="icon mr-2 text-success"><i class="fas fa-check-circle"></i></span>
                ${param.message}
                <button type="button" class="close" data-dismiss="alert" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
        </c:if>

        <c:if test="${not empty param.error}">
            <div class="alert alert-danger alert-dismissible fade show shadow-sm border-0 mb-4" role="alert">
                <span class="icon mr-2 text-danger"><i class="fas fa-exclamation-triangle"></i></span>
                <strong>Error:</strong> ${param.error}
                <button type="button" class="close" data-dismiss="alert" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
        </c:if>

        <div class="card shadow border-0 mb-4">
            <div class="card-header py-3 bg-white d-flex align-items-center">
                <div class="icon-circle bg-light mr-3 d-flex align-items-center justify-content-center" style="width: 40px; height: 40px; border-radius: 10px;">
                    <i class="fas fa-filter text-primary"></i>
                </div>
                <h6 class="m-0 font-weight-bold text-gray-800">Filter Packing Sessions</h6>
            </div>
            <div class="card-body bg-gray-50 py-4">
                <form method="get" class="d-flex flex-column flex-sm-row align-items-end gap-3 m-0">
                    <input type="hidden" name="action" value="list"/>
                    <div class="w-100">
                        <label class="small font-weight-bold text-uppercase text-muted mb-2 d-block">Search by Status</label>
                        <div class="input-group">
                            <div class="input-group-prepend">
                                <span class="input-group-text bg-white border-right-0 text-muted small px-3">
                                    <i class="fas fa-tasks"></i>
                                </span>
                            </div>
                            <select class="form-control border-left-0 bg-white font-weight-bold text-gray-800" name="status">
                                <option value="">-- All Sessions --</option>
                                <option value="PENDING" ${status == 'PENDING' ? 'selected' : ''}>PENDING</option>
                                <option value="IN_PROGRESS" ${status == 'IN_PROGRESS' ? 'selected' : ''}>IN PROGRESS</option>
                                <option value="DONE" ${status == 'DONE' ? 'selected' : ''}>DONE</option>
                            </select>
                        </div>
                    </div>
                    
                    <button type="submit" class="btn btn-primary w-100 w-sm-auto text-nowrap">
                        <i class="bi bi-search mr-1"></i> 
                        <span>Filter</span>
                    </button>
                    
                    <a href="${pageContext.request.contextPath}/packing?action=list" class="btn btn-secondary w-100 w-sm-auto text-nowrap">
                        <i class="bi bi-arrow-clockwise mr-1"></i> 
                        <span>Reset</span>
                    </a>
                    
                </form>
            </div>
        </div>

        <div class="card shadow mb-4">
            <div class="card-header py-3 bg-white">
                <h6 class="m-0 font-weight-bold text-primary">Session List</h6>
            </div>
            <div class="card-body p-0">
                <div class="table-responsive">
                    <table class="table table-hover align-middle mb-0" style="min-width: 800px;">
                        <thead class="bg-light text-muted small text-uppercase font-weight-bold">
                            <tr>
                                <th class="px-4">Session ID</th>
                                <th>GDN Detail</th>
                                <th>Client / SO</th>
                                <th>Creator</th>
                                <th>Timeline</th>
                                <th class="text-center">Status</th>
                                <th class="text-center">Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="s" items="${sessions}">
                                <tr>
                                    <td class="px-4 font-weight-bold text-gray-800">#${s.packingSessionId}</td>
                                    <td>
                                        <div class="fw-bold fs-5 mb-0">
                                            <a href="${pageContext.request.contextPath}/goods-delivery-note?action=detail&id=${s.gdnId}" class="text-primary font-weight-bold text-decoration-none">
                                                ${s.gdnNumber}
                                            </a>
                                        </div>
                                    </td>
                                    <td>
                                        <div class="text-gray-800 font-weight-bold">${s.customerName}</div>
                                        <div class="small text-muted">SO: ${s.soNumber}</div>
                                    </td>
                                    <td>
                                        <div class="d-flex align-items-center">
                                            <div class="avatar-sm bg-light rounded-circle text-center d-flex align-items-center justify-content-center mr-2" style="width: 30px; height: 30px;">
                                                <i class="fas fa-user small"></i>
                                            </div>
                                            <span class="small">${s.createdByName}</span>
                                        </div>
                                    </td>
                                    <td>
                                        <div class="small"><i class="far fa-calendar-alt mr-1"></i> <strong>Start:</strong> ${s.createdAtDisplay}</div>
                                        <div class="small text-muted"><i class="far fa-calendar-check mr-1"></i> <strong>End:</strong> ${s.completedAtDisplay != '' ? s.completedAtDisplay : '-'}</div>
                                    </td>
                                    <td class="text-center">
                                        <span class="badge badge-pill ${
                                            s.status == 'DONE' ? 'badge-success' :
                                            s.status == 'IN_PROGRESS' ? 'badge-info' :
                                            'badge-warning'} py-2 px-3">
                                            <c:choose>
                                                <c:when test="${s.status == 'DONE'}"><i class="fas fa-check-circle mr-1"></i></c:when>
                                                <c:when test="${s.status == 'IN_PROGRESS'}"><i class="fas fa-tasks mr-1"></i></c:when>
                                                <c:otherwise><i class="far fa-clock mr-1"></i></c:otherwise>
                                            </c:choose>
                                            ${s.status}
                                        </span>
                                    </td>
                                    <td class="text-center">
                                        <a href="${pageContext.request.contextPath}/packing?action=detail&id=${s.packingSessionId}" class="btn btn-sm btn-icon-split btn-outline-primary shadow-sm" title="View details">
                                            <span class="icon"><i class="fas fa-search"></i></span>
                                            <span class="text small">Detail</span>
                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty sessions}">
                                <tr>
                                    <td colspan="7" class="text-center py-5">
                                        <img src="https://illustrations.popsy.co/amber/no-data.svg" alt="No data" style="width: 150px; opacity: 0.6;">
                                        <p class="mt-3 text-muted">No packing sessions found in this status.</p>
                                    </td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
            <div class="card-footer bg-white text-muted small py-3">
                <i class="fas fa-info-circle mr-1"></i> Total found: <strong>${fn:length(sessions)}</strong> sessions
            </div>
        </div>
    </jsp:body>
</t:layout>

<style>
    /* Premium Look Adjustments */
    .border-left-primary { border-left: .25rem solid #4e73df!important; }
    .border-left-success { border-left: .25rem solid #1cc88a!important; }
    .border-left-info { border-left: .25rem solid #36b9cc!important; }
    .border-left-warning { border-left: .25rem solid #f6c23e!important; }
    
    .table thead th {
        background-color: #f8f9fc;
        border-bottom: 2px solid #e3e6f0;
    }
    
    .table-hover tbody tr:hover {
        background-color: #fcfcfd;
    }
    
    .badge-pill {
        border-radius: 50rem;
    }
    
    .btn-icon-split .text {
        font-weight: 600;
    }
    
    .avatar-sm {
        color: #4e73df;
    }
</style>

