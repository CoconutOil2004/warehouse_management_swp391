<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib tagdir="/WEB-INF/tags/" prefix="t" %>
<%@taglib uri="jakarta.tags.core" prefix="c" %>
<%@taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<%@taglib uri="jakarta.tags.functions" prefix="fn" %>

<t:layout title="My Packing Tasks">
    <jsp:attribute name="actions">
        <a href="${pageContext.request.contextPath}/packing?action=list" class="btn btn-outline-primary btn-sm shadow-sm rounded-pill px-3">
            <i class="fas fa-list mr-1"></i> Packing Sessions
        </a>
    </jsp:attribute>

    <jsp:body>
        <div class="row align-items-center mb-4">
            <div class="col">
                <h5 class="font-weight-bold text-gray-800 mb-1">Assigned Workload</h5>
                <p class="small text-muted mb-0">Track and update your individual packing tasks here. Mark items as packed as you go.</p>
            </div>
            <div class="col-auto">
                <div class="card shadow-sm border-0 rounded-lg bg-gray-100 px-3 py-2">
                    <span class="small font-weight-bold text-primary">
                        <i class="fas fa-layer-group mr-1"></i> ${fn:length(tasks)} Tasks Total
                    </span>
                </div>
            </div>
        </div>

        <c:if test="${not empty param.message}">
            <div class="alert alert-success alert-dismissible fade show shadow-sm border-0 mb-4" role="alert">
                <i class="fas fa-check-circle mr-2"></i> ${param.message}
                <button type="button" class="close" data-dismiss="alert" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
        </c:if>

        <div class="row">
            <c:forEach var="t" items="${tasks}">
                <div class="col-xl-4 col-md-6 mb-4">
                    <div class="card h-100 shadow border-0 overflow-hidden task-card ${t.status == 'DONE' ? 'opacity-75' : ''}" style="border-radius: 15px;">
                        <div class="card-header ${t.status == 'DONE' ? 'bg-success' : 'bg-primary'} py-3 px-4 d-flex justify-content-between align-items-center border-0">
                            <div>
                                <div class="text-xs text-white-50 font-weight-bold text-uppercase">GDN Reference</div>
                                <div class="font-weight-bold text-white h6 mb-0">${t.gdnNumber}</div>
                            </div>
                            <span class="badge badge-pill badge-light shadow-sm py-2 px-3 status-badge">
                                <c:choose>
                                    <c:when test="${t.status == 'DONE'}"><i class="fas fa-check mr-1 text-success"></i></c:when>
                                    <c:when test="${t.status == 'IN_PROGRESS'}"><i class="fas fa-spinner fa-spin mr-1 text-info"></i></c:when>
                                    <c:otherwise><i class="far fa-clock mr-1 text-warning"></i></c:otherwise>
                                </c:choose>
                                ${t.status}
                            </span>
                        </div>
                        <div class="card-body p-4">
                            <div class="mb-3">
                                <h6 class="font-weight-bold text-gray-900 mb-1">${t.variantSku}</h6>
                                <p class="small text-muted mb-0 line-clamp-2" style="height: 2.4em; overflow: hidden;">${t.productName}</p>
                            </div>
                            
                            <div class="bg-gray-100 rounded-lg p-3 mb-4">
                                <div class="row align-items-center">
                                    <div class="col-6 border-right">
                                        <div class="text-xs text-muted text-uppercase font-weight-bold mb-1">Target</div>
                                        <div class="h5 font-weight-bold text-gray-800 mb-0">${t.assignedPacks} <span class="small font-weight-normal text-muted">packs</span></div>
                                    </div>
                                    <div class="col-6">
                                        <div class="text-xs text-muted text-uppercase font-weight-bold mb-1">Packed</div>
                                        <div class="h5 font-weight-bold ${t.packedPacks >= t.assignedPacks ? 'text-success' : 'text-primary'} mb-0">${t.packedPacks}</div>
                                    </div>
                                </div>
                            </div>

                            <c:set var="progress" value="${t.assignedPacks > 0 ? (t.packedPacks * 100 / t.assignedPacks) : 0}"/>
                            <div class="d-flex justify-content-between align-items-center mb-1">
                                <span class="text-xs font-weight-bold text-muted">Progress</span>
                                <span class="text-xs font-weight-bold text-gray-800">${progress}%</span>
                            </div>
                            <div class="progress mb-4 rounded-pill" style="height: 8px;">
                                <div class="progress-bar ${progress >= 100 ? 'bg-success shadow-sm' : 'bg-primary shadow-sm'}" 
                                     role="progressbar" style="width: ${progress}%" 
                                     aria-valuenow="${progress}" aria-valuemin="0" aria-valuemax="100"></div>
                            </div>

                            <c:choose>
                                <c:when test="${t.status != 'DONE'}">
                                    <form action="${pageContext.request.contextPath}/packing" method="post" class="mt-auto">
                                        <input type="hidden" name="action" value="updateTask"/>
                                        <input type="hidden" name="taskId" value="${t.packingTaskId}"/>
                                        
                                        <div class="input-group input-group-lg shadow-sm rounded-lg overflow-hidden border">
                                            <input type="number" name="packedPacks" class="form-control border-0 text-center font-weight-bold" 
                                                   value="${t.packedPacks}" min="0" max="${t.assignedPacks}" required/>
                                            <div class="input-group-append">
                                                <button type="submit" class="btn btn-primary px-4 font-weight-bold" style="border-radius: 0;">
                                                    Update
                                                </button>
                                            </div>
                                        </div>
                                        <div class="text-center mt-2">
                                            <span class="text-xs text-muted">Items per pack: <strong>${t.itemsPerPack}</strong></span>
                                        </div>
                                    </form>
                                </c:when>
                                <c:otherwise>
                                    <div class="bg-light text-success text-center py-3 rounded-lg border border-success-50">
                                        <i class="fas fa-check-circle mr-2"></i>
                                        <span class="small font-weight-bold">Completed on ${t.updatedAtDisplay}</span>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>

        <c:if test="${empty tasks}">
            <div class="text-center py-5 my-5">
                <div class="bg-gray-100 d-inline-block rounded-circle p-5 mb-4 shadow-sm">
                    <i class="fas fa-clipboard-check fa-4x text-gray-300"></i>
                </div>
                <h4 class="font-weight-bold text-gray-800">You're All Caught Up!</h4>
                <p class="text-muted mx-auto" style="max-width: 400px;">No active packing tasks assigned to you. Go ahead and grab a coffee or check the packing status list.</p>
                <a href="${pageContext.request.contextPath}/packing?action=list" class="btn btn-primary shadow-sm rounded-pill px-5 py-2 font-weight-bold mt-2">
                    View General List
                </a>
            </div>
        </c:if>
    </jsp:body>
</t:layout>

<style>
    .rounded-lg { border-radius: 12px!important; }
    .task-card { transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1); }
    .task-card:hover { transform: translateY(-5px); box-shadow: 0 1rem 3rem rgba(0,0,0,0.175)!important; }
    .status-badge { font-size: 0.7rem; font-weight: 800; text-transform: uppercase; letter-spacing: 0.5px; }
    .line-clamp-2 { display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
    .border-success-50 { border-color: rgba(28, 200, 138, 0.3); }
</style>

