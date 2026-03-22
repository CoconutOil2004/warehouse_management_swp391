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
                <div class="d-flex align-items-center">
                    <i class="fas fa-check-circle mr-3 fa-lg"></i>
                    <div>
                        <h6 class="alert-heading font-weight-bold mb-0">Success!</h6>
                        <p class="small mb-0">${param.message}</p>
                    </div>
                </div>
                <button type="button" class="close" data-dismiss="alert" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
        </c:if>

        <c:if test="${not empty param.error}">
            <div class="alert alert-danger alert-dismissible fade show shadow-sm border-0 mb-4" role="alert">
                <div class="d-flex align-items-center">
                    <i class="fas fa-exclamation-triangle mr-3 fa-lg"></i>
                    <div>
                        <h6 class="alert-heading font-weight-bold mb-0">Update Failed</h6>
                        <p class="small mb-0">${param.error}</p>
                    </div>
                </div>
                <button type="button" class="close" data-dismiss="alert" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
        </c:if>

        <div class="row">
            <c:forEach var="t" items="${tasks}">
                <div class="col-xl-4 col-md-6 mb-4">
                    <div class="card h-100 shadow-sm border-0 overflow-hidden task-card" style="border-radius: 20px;">
                        <!-- Header: GDN Info -->
                        <div class="card-header bg-gradient-primary text-white py-3 px-4 d-flex justify-content-between align-items-center border-0">
                            <div>
                                <div class="text-xs text-white-50 font-weight-bold text-uppercase" style="letter-spacing: 1px;">GDN Reference</div>
                                <div class="font-weight-bold h5 mb-0">${t.gdnNumber}</div>
                            </div>
                            <span class="badge badge-pill badge-light shadow-sm py-2 px-3 status-badge" style="color: #4e73df;">
                                <c:choose>
                                    <c:when test="${t.status == 'DONE'}"><i class="fas fa-check-circle mr-1"></i></c:when>
                                    <c:when test="${t.status == 'IN_PROGRESS'}"><i class="fas fa-spinner fa-spin mr-1"></i></c:when>
                                    <c:otherwise><i class="far fa-clock mr-1"></i></c:otherwise>
                                </c:choose>
                                ${t.status}
                            </span>
                        </div>
                        
                        <div class="card-body p-4 d-flex flex-column">
                            <!-- Product Info -->
                            <div class="mb-4">
                                <h5 class="font-weight-bold text-gray-900 mb-1" style="letter-spacing: -0.5px;">${t.variantSku}</h5>
                                <p class="text-muted mb-0 small text-uppercase font-weight-bold" style="letter-spacing: 0.5px;">${t.productName}</p>
                            </div>
                            
                            <!-- Stats: Target vs Packed -->
                            <div class="bg-light rounded-xl p-4 mb-4 border-0">
                                <div class="row align-items-center">
                                    <div class="col-6 border-right py-1">
                                        <div class="text-xs text-muted text-uppercase font-weight-bold mb-2">Target</div>
                                        <div class="h3 font-weight-bold text-gray-900 mb-0">
                                            ${t.assignedPacks} <span class="h6 font-weight-normal text-muted">packs</span>
                                        </div>
                                    </div>
                                    <div class="col-6 py-1 pl-4">
                                        <div class="text-xs text-muted text-uppercase font-weight-bold mb-2">Packed</div>
                                        <div class="h3 font-weight-bold ${t.packedPacks >= t.assignedPacks ? 'text-success' : 'text-primary'} mb-0">
                                            ${t.packedPacks}
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- Progress Section -->
                            <c:set var="progress" value="${t.assignedPacks > 0 ? (t.packedPacks * 100 / t.assignedPacks) : 0}"/>
                            <div class="d-flex justify-content-between align-items-center mb-2">
                                <span class="text-xs font-weight-bold text-muted text-uppercase">Progress</span>
                                <span class="text-xs font-weight-bold text-gray-900">${progress}%</span>
                            </div>
                            <div class="progress mb-4 rounded-pill" style="height: 10px; background-color: #eaecf4;">
                                <div class="progress-bar ${progress >= 100 ? 'bg-success' : 'bg-primary'} shadow-none" 
                                     role="progressbar" style="width: ${progress}%" 
                                     aria-valuenow="${progress}" aria-valuemin="0" aria-valuemax="100"></div>
                            </div>

                            <!-- Packaging Breakdown -->
                            <c:set var="qtyInt" value="${t.qtyPicked.intValue()}"/>
                            <c:set var="ipp" value="${t.itemsPerPack}"/>
                            <c:set var="fullPacksLine" value="${(qtyInt / ipp).intValue()}"/>
                            <c:set var="partialQtyLine" value="${qtyInt % ipp}"/>
                            <c:set var="totalPacksLine" value="${partialQtyLine > 0 ? fullPacksLine + 1 : fullPacksLine}"/>

                            <div class="p-3 mb-4 rounded-lg bg-gray-50 border border-gray-100">
                                <div class="d-flex justify-content-between mb-2">
                                    <span class="small text-muted font-weight-bold">
                                        <i class="fas fa-box text-success mr-1"></i> Full Packs
                                    </span>
                                    <span class="small font-weight-bold text-gray-900">${fullPacksLine}</span>
                                </div>
                                <c:if test="${partialQtyLine > 0}">
                                    <div class="d-flex justify-content-between mb-2">
                                        <span class="small text-muted font-weight-bold">
                                            <i class="fas fa-box-open text-warning mr-1"></i> Partial Packs
                                        </span>
                                        <span class="small font-weight-bold text-gray-900">1 <small class="text-muted">(${partialQtyLine} items)</small></span>
                                    </div>
                                </c:if>
                                <div class="d-flex justify-content-between mt-2 pt-2 border-top">
                                    <span class="small text-muted font-weight-bold">Items per pack</span>
                                    <span class="small font-weight-bold text-primary">${ipp} items</span>
                                </div>
                            </div>

                            <!-- Interaction Part -->
                            <c:choose>
                                <c:when test="${t.status != 'DONE'}">
                                    <form action="${pageContext.request.contextPath}/packing" method="post" class="mt-auto">
                                        <input type="hidden" name="action" value="updateTask"/>
                                        <input type="hidden" name="taskId" value="${t.packingTaskId}"/>
                                        
                                        <label class="text-xs font-weight-bold text-muted text-uppercase mb-2">Packs just completed</label>
                                        <div class="input-group input-group-lg shadow-sm rounded-lg overflow-hidden border-0" style="background: #f8f9fc; padding: 5px;">
                                            <input type="number" name="newlyPacked" class="form-control border-0 bg-transparent text-center font-weight-bold h4 mb-0" 
                                                   value="${t.assignedPacks - t.packedPacks}" min="1" max="${t.assignedPacks - t.packedPacks}" required style="box-shadow: none;"/>
                                            <div class="input-group-append">
                                                <button type="submit" class="btn btn-primary px-5 font-weight-bold shadow-sm rounded-lg" style="border-radius: 12px!important;">
                                                    Update
                                                </button>
                                            </div>
                                        </div>
                                    </form>
                                </c:when>

                                <c:otherwise>
                                    <div class="mt-auto bg-gray-100 text-success text-center py-3 rounded-lg border-0 shadow-none">
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
    .rounded-xl { border-radius: 18px!important; }
    .task-card { transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1); }
    .task-card:hover { transform: translateY(-7px); box-shadow: 0 1.5rem 4rem rgba(0,0,0,0.12)!important; }
    .status-badge { font-size: 0.7rem; font-weight: 800; text-transform: uppercase; letter-spacing: 0.5px; }
    .bg-gradient-primary { background: linear-gradient(135deg, #4e73df 0%, #224abe 100%); }
    .bg-gray-50 { background-color: #f8f9fc; }
    .bg-light { background-color: #f8f9fc!important; }
    .line-clamp-2 { display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
    .border-success-50 { border-color: rgba(28, 200, 138, 0.3); }
</style>


