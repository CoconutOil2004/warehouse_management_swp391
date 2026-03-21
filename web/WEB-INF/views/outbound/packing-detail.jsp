<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:layout title="Packing Session Detail: #${sessionDTO.packingSessionId}">
    <jsp:attribute name="actions">
        <div class="d-flex">
            <t:link url="${pageContext.request.contextPath}/packing?action=list" color="secondary" variant="outline" icon="bi-arrow-left">
                Back to List
            </t:link>
            <c:if test="${sessionDTO.status != 'DONE'}">
                <t:link url="${pageContext.request.contextPath}/goods-delivery-note?action=detail&id=${sessionDTO.gdnId}" color="primary" variant="solid" icon="bi-info-circle" cssClass="ml-2">
                    GDN Detail
                </t:link>
            </c:if>
        </div>
    </jsp:attribute>

    <jsp:body>
        <!-- Session Summary Cards -->
        <div class="row mb-4">
            <div class="col-xl-3 col-md-6 mb-4">
                <div class="card border-left-primary shadow h-100 py-2 border-0">
                    <div class="card-body">
                        <div class="row no-gutters align-items-center">
                            <div class="col mr-2">
                                <div class="text-xs font-weight-bold text-primary text-uppercase mb-1">GDN Number</div>
                                <div class="h5 mb-0 font-weight-bold text-gray-800">${sessionDTO.gdnNumber}</div>
                                <div class="mt-1 small text-muted">SO: ${sessionDTO.soNumber}</div>
                            </div>
                            <div class="col-auto">
                                <i class="fas fa-file-invoice fa-2x text-gray-300"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-xl-3 col-md-6 mb-4">
                <div class="card border-left-info shadow h-100 py-2 border-0">
                    <div class="card-body">
                        <div class="row no-gutters align-items-center">
                            <div class="col mr-2">
                                <div class="text-xs font-weight-bold text-info text-uppercase mb-1">Status</div>
                                <div class="h5 mb-0 font-weight-bold text-gray-800">
                                    <span class="badge badge-pill ${sessionDTO.status == 'DONE' ? 'badge-success' : (sessionDTO.status == 'IN_PROGRESS' ? 'badge-info' : 'badge-warning')} px-3">
                                        <i class="fas ${sessionDTO.status == 'DONE' ? 'fa-check-circle' : (sessionDTO.status == 'IN_PROGRESS' ? 'fa-tasks' : 'fa-clock')} mr-1"></i>
                                        ${sessionDTO.status}
                                    </span>
                                </div>
                            </div>
                            <div class="col-auto">
                                <i class="fas fa-info-circle fa-2x text-gray-300"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-xl-3 col-md-6 mb-4">
                <div class="card border-left-success shadow h-100 py-2 border-0">
                    <div class="card-body">
                        <div class="row no-gutters align-items-center">
                            <div class="col mr-2">
                                <div class="text-xs font-weight-bold text-success text-uppercase mb-1">Created By</div>
                                <div class="h5 mb-0 font-weight-bold text-gray-800">${sessionDTO.createdByName}</div>
                                <div class="mt-1 small text-muted">${sessionDTO.createdAtDisplay}</div>
                            </div>
                            <div class="col-auto">
                                <i class="fas fa-user-edit fa-2x text-gray-300"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-xl-3 col-md-6 mb-4">
                <div class="card border-left-warning shadow h-100 py-2 border-0">
                    <div class="card-body">
                        <div class="row no-gutters align-items-center">
                            <div class="col mr-2">
                                <div class="text-xs font-weight-bold text-warning text-uppercase mb-1">Completed At</div>
                                <div class="h5 mb-0 font-weight-bold text-gray-800">${sessionDTO.completedAtDisplay != '' ? sessionDTO.completedAtDisplay : '-'}</div>
                            </div>
                            <div class="col-auto">
                                <i class="fas fa-calendar-check fa-2x text-gray-300"></i>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="row">
            <!-- Line Progress -->
            <div class="col-lg-8">
                <div class="card shadow mb-4 border-0">
                    <div class="card-header py-3 bg-white d-flex align-items-center">
                        <div class="icon-circle bg-light mr-3 d-flex align-items-center justify-content-center" style="width: 40px; height: 40px; border-radius: 10px;">
                            <i class="fas fa-th-list text-primary"></i>
                        </div>
                        <h6 class="m-0 font-weight-bold text-gray-800">Line Configurations & Progress</h6>
                    </div>
                    <div class="card-body p-0">
                        <div class="table-responsive">
                            <table class="table table-hover align-middle mb-0">
                                <thead class="bg-light">
                                    <tr>
                                        <th class="pl-4">Product / SKU</th>
                                        <th class="text-center">Items/Pack</th>
                                        <th class="text-center">Total Packs</th>
                                        <th style="width: 30%;">Packing Progress</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="line" items="${lines}">
                                        <c:set var="linePackedPacks" value="0"/>
                                        <c:forEach var="t" items="${tasks}">
                                            <c:if test="${t.packingLineConfigId == line.packingLineConfigId}">
                                                <c:set var="linePackedPacks" value="${linePackedPacks + t.packedPacks}"/>
                                            </c:if>
                                        </c:forEach>
                                        <c:set var="linePercent" value="${line.numPacks > 0 ? (linePackedPacks * 100 / line.numPacks) : 0}"/>
                                        
                                        <tr>
                                            <td class="pl-4 py-3">
                                                <div class="font-weight-bold text-primary">${line.productName}</div>
                                                <div class="small text-muted">SKU: ${line.variantSku} | ${line.color} / ${line.size}</div>
                                            </td>
                                            <td class="text-center">${line.itemsPerPack}</td>
                                            <td class="text-center font-weight-bold text-gray-800">${line.numPacks}</td>
                                            <td class="pr-4">
                                                <div class="d-flex align-items-center">
                                                    <div class="progress flex-grow-1 shadow-sm mr-2" style="height: 10px; border-radius: 5px;">
                                                        <div class="progress-bar ${linePercent >= 100 ? 'bg-success' : 'bg-info'}"
                                                             role="progressbar" style="width: ${linePercent}%"
                                                             aria-valuenow="${linePercent}" aria-valuemin="0" aria-valuemax="100">
                                                        </div>
                                                    </div>
                                                    <span class="small font-weight-bold text-gray-600">${linePackedPacks}/${line.numPacks}</span>
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

            <!-- Task Assignment -->
            <div class="col-lg-4">
                <div class="card shadow mb-4 border-0">
                    <div class="card-header py-3 bg-white d-flex align-items-center">
                        <div class="icon-circle bg-light mr-3 d-flex align-items-center justify-content-center" style="width: 40px; height: 40px; border-radius: 10px;">
                            <i class="fas fa-users text-primary"></i>
                        </div>
                        <h6 class="m-0 font-weight-bold text-gray-800">Assigned Staff Tasks</h6>
                    </div>
                    <div class="card-body">
                        <c:forEach var="task" items="${tasks}">
                            <div class="mb-4 pb-3 border-bottom last-border-0">
                                <div class="d-flex justify-content-between align-items-center mb-2">
                                    <div class="d-flex align-items-center">
                                        <div class="avatar-sm mr-2">
                                            <div class="bg-primary text-white rounded-circle d-flex align-items-center justify-content-center" style="width: 30px; height: 30px;">
                                                <i class="fas fa-user fa-sm"></i>
                                            </div>
                                        </div>
                                        <div>
                                            <div class="font-weight-bold text-gray-800">${task.assignedToName}</div>
                                            <div class="text-xs text-muted">SKU: ${task.variantSku}</div>
                                        </div>
                                    </div>
                                    <span class="badge badge-pill ${task.status == 'DONE' ? 'badge-success' : (task.status == 'IN_PROGRESS' ? 'badge-info' : 'badge-light')}">
                                        ${task.status}
                                    </span>
                                </div>
                                <div class="d-flex align-items-center mt-2">
                                    <div class="progress flex-grow-1 mr-2" style="height: 6px;">
                                        <c:set var="taskPerc" value="${task.assignedPacks > 0 ? (task.packedPacks * 100 / task.assignedPacks) : 0}"/>
                                        <div class="progress-bar ${taskPerc >= 100 ? 'bg-success' : 'bg-info'}" style="width: ${taskPerc}%"></div>
                                    </div>
                                    <span class="small font-weight-bold">${task.packedPacks}/${task.assignedPacks}</span>
                                </div>
                                <c:if test="${not empty task.updatedAtDisplay}">
                                    <div class="text-right mt-1">
                                        <span class="text-xs text-muted italic"><i class="far fa-clock mr-1"></i>Updated: ${task.updatedAtDisplay}</span>
                                    </div>
                                </c:if>
                            </div>
                        </c:forEach>
                    </div>
                </div>
            </div>
        </div>
    </jsp:body>
</t:layout>

<style>
    .last-border-0:last-child {
        border-bottom: 0 !important;
        margin-bottom: 0 !important;
        padding-bottom: 0 !important;
    }
    .avatar-sm { width: 30px; height: 30px; }
    .gx-3 { margin-right: -0.75rem; margin-left: -0.75rem; }
    .gx-3 > div { padding-right: 0.75rem; padding-left: 0.75rem; }
</style>
