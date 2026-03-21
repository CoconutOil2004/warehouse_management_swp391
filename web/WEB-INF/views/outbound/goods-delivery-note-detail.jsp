<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib tagdir="/WEB-INF/tags/" prefix="t" %>
<%@taglib uri="jakarta.tags.core" prefix="c" %>
<%@taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<%@taglib uri="jakarta.tags.functions" prefix="fn" %>
<t:layout title="Goods Delivery Note Details">
    <div class="container-fluid py-4">
        <c:if test="${not empty param.error || not empty error}">
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                <c:choose>
                    <c:when test="${not empty param.error}">${fn:replace(fn:replace(param.error, '+', ' '), '%3A', ':')}</c:when>
                    <c:otherwise>${error}</c:otherwise>
                </c:choose>
                <button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            </div>
        </c:if>
        <!-- Breadcrumb / Back Link -->
        <div class="d-flex justify-content-between align-items-center mb-4">
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb mb-0">
                    <li class="breadcrumb-item"><a
                            href="${pageContext.request.contextPath}/goods-delivery-note?action=list"
                            class="text-decoration-none text-muted">Goods Delivery Note</a></li>
                    <li class="breadcrumb-item active" aria-current="page">${gdn.gdnNumber}</li>
                </ol>
            </nav>
            <span class="badge ${
                    gdn.status == 'CREATED' ? 'bg-secondary' :
                    (gdn.status == 'PICKING' ? 'bg-warning text-dark' :
                    (gdn.status == 'PACKING' ? 'bg-info text-dark' :
                    (gdn.status == 'SHIPPING' ? 'bg-primary' :
                    (gdn.status == 'DONE' ? 'bg-success' :
                    (gdn.status == 'CANCELLED' ? 'bg-danger' : 'bg-secondary')))))} px-3 py-2 rounded-pill shadow-sm">
                <i class="fas ${
                        gdn.status == 'CREATED' ? 'fa-file' :
                        (gdn.status == 'PICKING' ? 'fa-dolly' :
                        (gdn.status == 'PACKING' ? 'fa-box' :
                        (gdn.status == 'SHIPPING' ? 'fa-truck' :
                        (gdn.status == 'DONE' ? 'fa-check-circle' :
                        (gdn.status == 'CANCELLED' ? 'fa-times-circle' : 'fa-file')))))} me-1"></i>
                ${gdn.status}
            </span>
        </div>

        <div class="row g-4">
            <!-- Header Info Card -->
            <div class="col-lg-12">
                <div class="card shadow-sm border-0">
                    <div class="card-header bg-primary text-white py-3">
                        <h5 class="card-title mb-0"><i class="fas fa-info-circle me-2"></i>General Information</h5>
                    </div>
                    <div class="card-body">
                        <div class="row row-cols-1 row-cols-md-2 row-cols-lg-4 g-3">
                            <div class="col">
                                <p class="text-muted small mb-1 text-uppercase fw-bold">GDN Number</p>
                                <p class="mb-0 fw-semibold">${gdn.gdnNumber}</p>
                            </div>
                            <div class="col">
                                <p class="text-muted small mb-1 text-uppercase fw-bold">Sales Order</p>
                                <p class="mb-0 fw-semibold">${gdn.soNumber}</p>
                            </div>
                            <div class="col">
                                <p class="text-muted small mb-1 text-uppercase fw-bold">Customer</p>
                                <p class="mb-0 fw-semibold">${gdn.customerName}</p>
                            </div>
                            <div class="col">
                                <p class="text-muted small mb-1 text-uppercase fw-bold">Created At</p>
                                <p class="mb-0 fw-semibold">${gdn.createdAtDisplay}</p>
                            </div>
                            <div class="col">
                                <p class="text-muted small mb-1 text-uppercase fw-bold">Status</p>
                                <p class="mb-0">
                                    <span class="badge ${
                                            gdn.status == 'CREATED' ? 'bg-secondary' :
                                            (gdn.status == 'PICKING' ? 'bg-warning' :
                                            (gdn.status == 'PACKING' ? 'bg-info' :
                                            (gdn.status == 'SHIPPING' ? 'bg-primary' :
                                            (gdn.status == 'DONE' ? 'bg-success' :
                                            (gdn.status == 'CANCELLED' ? 'bg-danger' : 'bg-secondary')))))} fw-semibold">
                                        ${gdn.status}
                                    </span>
                                </p>
                            </div>  
                            <div class="col">
                                <p class="text-muted small mb-1 text-uppercase fw-bold">Created By</p>
                                <p class="mb-0 fw-semibold">${gdn.creatorName}</p>
                            </div>
                            <c:if test="${not empty gdn.confirmedAt}">
                                <div class="col">
                                    <p class="text-muted small mb-1 text-uppercase fw-bold">Confirmed At</p>
                                    <p class="mb-0 fw-semibold">${gdn.confirmedAtDisplay}</p>
                                </div>
                            </c:if>
                            <div class="col-lg-12">
                                <hr class="my-2 opacity-10">
                                <p class="text-muted small mb-1 text-uppercase fw-bold">Ship To Address</p>
                                <p class="mb-0 text-secondary">${gdn.customerAddress != null && gdn.customerAddress != '' ? gdn.customerAddress : 'No address provided.'}</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Items to Pick Card -->
            <div class="col-lg-12">
                <div class="card shadow-sm border-0 mb-4">
                    <div class="card-header bg-dark text-white py-3">
                        <h5 class="card-title mb-0"><i class="fas fa-list me-2"></i>Items to Pick</h5>
                    </div>
                    <div class="card-body p-0">
                        <div class="table-responsive">
                            <table class="table table-hover align-middle mb-0">
                                <thead class="table-light text-secondary text-uppercase small">
                                    <tr class="text-center">
                                        <th class="text-start">Variant SKU</th>
                                        <th class="text-start">Product Name</th>
                                        <th>Color</th>
                                        <th>Size</th>
                                        <th>Qty Required</th>
                                        <th>Qty Picked</th>
                                        <th>Qty Packed</th>
                                        <th>Available</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="line" items="${gdn.lines != null ? gdn.lines : []}">
                                        <tr class="text-center">
                                            <td class="text-start fw-bold">${line.variantSku}</td>
                                            <td class="text-start">${line.productName}</td>
                                            <td>${line.color != null ? line.color : 'N/A'}</td>
                                            <td>${line.size != null ? line.size : 'N/A'}</td>
                                            <td><strong><fmt:formatNumber value="${line.qtyRequired != null ? line.qtyRequired : 0}" maxFractionDigits="0" /></strong></td>
                                            <td><span class="badge bg-info"><fmt:formatNumber value="${line.qtyPicked != null ? line.qtyPicked : 0}" maxFractionDigits="0" /></span></td>
                                            <td><span class="badge bg-warning text-dark"><fmt:formatNumber value="${line.qtyPacked != null ? line.qtyPacked : 0}" maxFractionDigits="0" /></span></td>
                                            <td><span class="badge bg-success"><fmt:formatNumber value="${line.qtyAvailable != null ? line.qtyAvailable : 0}" maxFractionDigits="0" /></span></td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Pick Tasks -> Pack Tasks -> Shipments -->
        <div class="row g-4 mt-1">
            <div class="col-lg-4">
                <div class="card shadow-sm border-0 mb-3">
                    <div class="card-header bg-secondary text-white py-3">
                        <h5 class="card-title mb-0 d-flex align-items-center">
                            <i class="fas fa-tasks me-2"></i>Pick Tasks
                        </h5>
                    </div>
                    <div class="card-body p-0">
                        <div class="table-responsive">
                            <table class="table table-hover align-middle mb-0">
                                <thead class="table-light text-secondary text-uppercase small">
                                    <tr class="text-center">
                                        <th class="text-start">Task ID</th>
                                        <th class="text-start">Wave</th>
                                        <th class="text-start">Assigned To</th>
                                        <th>Status</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:if test="${empty pickTasks}">
                                        <tr>
                                            <td colspan="4" class="text-center text-muted py-3">
                                                No pick tasks created for this GDN yet.
                                            </td>
                                        </tr>
                                    </c:if>
                                    <c:forEach var="task" items="${pickTasks}">
                                        <tr class="text-center">
                                            <td class="text-start">#${task.pickTaskId}</td>
                                            <td class="text-start">
                                                <c:choose>
                                                    <c:when test="${task.waveId != null}">Wave #${task.waveId}</c:when>
                                                    <c:otherwise>-</c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td class="text-start">
                                                <c:out value="${task.assignedToName != null ? task.assignedToName : 'Unassigned'}" />
                                            </td>
                                            <td>
                                                <span class="badge
                                                    ${task.status == 'COMPLETED' ? 'bg-success' :
                                                      task.status == 'IN_PROGRESS' ? 'bg-info text-dark' :
                                                      task.status == 'ASSIGNED' ? 'bg-primary' :
                                                      'bg-secondary'}">
                                                    ${task.status}
                                                </span>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-lg-4">
                <div class="card shadow-sm border-0 mb-3 h-100">
                    <div class="card-header bg-secondary text-white py-3">
                        <h5 class="card-title mb-0 d-flex align-items-center">
                            <i class="fas fa-box me-2"></i>Packing Tasks
                        </h5>
                    </div>
                    <div class="card-body p-0">
                        <div class="table-responsive">
                            <table class="table table-hover align-middle mb-0">
                                <thead class="table-light text-secondary text-uppercase small">
                                    <tr class="text-center">
                                        <th class="text-start">Task</th>
                                        <th class="text-start">Assignee</th>
                                        <th>Progress</th>
                                        <th>Status</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:if test="${empty packTasks}">
                                        <tr>
                                            <td colspan="4" class="text-center text-muted py-4">
                                                <i class="fas fa-info-circle me-1"></i> No packing tasks for this GDN.
                                            </td>
                                        </tr>
                                    </c:if>
                                    <c:forEach var="p" items="${packTasks}">
                                        <tr class="text-center">
                                            <td class="text-start small fw-bold">#${p.pickingTaskId != null ? p.pickingTaskId : p.packingTaskId}</td>
                                            <td class="text-start small">
                                                <c:out value="${p.assignedToName != null ? p.assignedToName : 'Unassigned'}" />
                                            </td>
                                            <td>
                                                <span class="badge rounded-pill bg-light text-dark border">
                                                    ${p.packedPacks}/${p.assignedPacks}
                                                </span>
                                            </td>
                                            <td>
                                                <span class="badge rounded-pill ${p.status == 'DONE' ? 'bg-success' : (p.status == 'IN_PROGRESS' ? 'bg-info text-dark' : 'bg-secondary')}">
                                                    ${p.status}
                                                </span>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-lg-4">
                <div class="card shadow-sm border-0 mb-3">
                    <div class="card-header bg-secondary text-white py-3">
                        <h5 class="card-title mb-0 d-flex align-items-center">
                            <i class="fas fa-truck me-2"></i>Shipments
                        </h5>
                    </div>
                    <div class="card-body p-0">
                        <div class="table-responsive">
                            <table class="table table-hover align-middle mb-0">
                                <thead class="table-light text-secondary text-uppercase small">
                                    <tr class="text-center">
                                        <th class="text-start">Shipment</th>
                                        <th class="text-start">Carrier</th>
                                        <th>Status</th>
                                        <th>Tracking</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:if test="${empty shipments}">
                                        <tr>
                                            <td colspan="4" class="text-center text-muted py-3">
                                                No shipments created for this GDN yet.
                                            </td>
                                        </tr>
                                    </c:if>
                                    <c:forEach var="s" items="${shipments}">
                                        <tr class="text-center">
                                            <td class="text-start">
                                                <a href="${pageContext.request.contextPath}/shipment?action=detail&id=${s.shipmentId}" class="text-decoration-none">
                                                    ${s.shipmentNumber}
                                                </a>
                                            </td>
                                            <td class="text-start">
                                                <c:out value="${s.carrierName != null ? s.carrierName : '-'}" />
                                            </td>
                                            <td>
                                                <span class="badge ${
                                                    s.status == 'DELIVERED' ? 'bg-success' :
                                                    (s.status == 'CANCELLED' ? 'bg-danger' :
                                                    (s.status == 'IN_TRANSIT' || s.status == 'PICKED_UP' ? 'bg-info text-dark' :
                                                    'bg-secondary'))}">
                                                    ${s.status}
                                                </span>
                                            </td>
                                            <td class="text-center">
                                                <c:out value="${s.trackingCode != null ? s.trackingCode : '-'}" />
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Action Buttons -->
        <div class="d-flex justify-content-between align-items-center mt-3 p-3 bg-white rounded shadow-sm border">
            <div>
                <a href="${pageContext.request.contextPath}/goods-delivery-note?action=list"
                    class="btn btn-outline-secondary me-2">
                    <i class="fas fa-arrow-left me-1"></i> Back to List
                </a>
            </div>

            <%-- CREATED: Edit (popup) + Create wave & start picking --%>
            <c:if test="${gdn.status == 'CREATED'}">
                <div class="d-flex gap-2 flex-wrap align-items-center">
                    <button type="button" class="btn btn-warning shadow-sm text-dark d-flex align-items-center"
                        data-bs-toggle="modal" data-bs-target="#editGdnModal${gdn.gdnId}"
                        style="min-width: 100px; height: 38px;">
                        <i class="fas fa-edit me-2"></i>Edit
                    </button>
                    <form action="${pageContext.request.contextPath}/pick-wave" method="post" class="d-flex align-items-center mb-0">
                        <input type="hidden" name="action" value="create"/>
                        <input type="hidden" name="gdnId" value="${gdn.gdnId}"/>
                        <button type="submit" class="btn btn-primary shadow-sm d-flex align-items-center justify-content-center"
                            style="min-width: 180px; height: 38px;">
                            <i class="fas fa-box-open me-2"></i>Create wave & Start picking
                        </button>
                    </form>
                </div>
            </c:if>

            <c:if test="${gdn.status == 'PICKING'}">
                <div class="d-flex gap-2 flex-wrap align-items-center">
                    <c:if test="${wave != null}">
                        <a href="${pageContext.request.contextPath}/pick-task?action=assign&waveId=${wave.waveId}"
                           class="btn btn-primary shadow-sm">
                            <i class="fas fa-user-check me-2"></i>Assign tasks
                        </a>
                    </c:if>
                    <c:if test="${wave == null}">
                        <a href="${pageContext.request.contextPath}/pick-task?action=assign&gdnId=${gdn.gdnId}"
                           class="btn btn-primary shadow-sm d-flex align-items-center justify-content-center"
                           style="min-width: 165px; height: 38px; padding: 0 1rem;">
                            <i class="fas fa-user-check me-2"></i>Assign Pick Task
                        </a>
                    </c:if>
                </div>
            </c:if>

            <c:if test="${gdn.status == 'PACKING'}">
                <div class="d-flex gap-2 flex-wrap align-items-center">
                    <a href="${pageContext.request.contextPath}/packing?action=create&gdnId=${gdn.gdnId}"
                       class="btn btn-warning shadow-sm d-flex align-items-center justify-content-center px-4">
                        <i class="fas fa-box-open me-2"></i>Configure Packing
                    </a>
                    <a href="${pageContext.request.contextPath}/packing?action=list"
                       class="btn btn-outline-primary shadow-sm">
                        <i class="fas fa-list me-1"></i> Packing Sessions
                    </a>
                </div>
            </c:if>

            <c:if test="${gdn.status == 'SHIPPING' && empty shipments}">
                <div class="d-flex gap-2">
                    <a href="${pageContext.request.contextPath}/shipment?action=create&gdnId=${gdn.gdnId}&soNumber=${gdn.soNumber}"
                        class="btn btn-success shadow-sm">
                        <i class="fas fa-truck me-2"></i>Create Shipment
                    </a>
                </div>
            </c:if>
        </div>

        <!-- Edit GDN Modal (system modal: t:alert) -->
        <c:if test="${gdn.status == 'CREATED'}">
            <t:alert id="editGdnModal${gdn.gdnId}">
                <jsp:attribute name="title">Edit GDN</jsp:attribute>
                <jsp:attribute name="desciption">
                    <form id="editGdnForm${gdn.gdnId}" action="${pageContext.request.contextPath}/goods-delivery-note" method="post" class="m-0">
                        <input type="hidden" name="action" value="update" />
                        <input type="hidden" name="gdnId" value="${gdn.gdnId}" />

                        <div class="row g-3">
                            <div class="col-12">
                                <div class="small text-muted">GDN</div>
                                <div class="fw-semibold">${gdn.gdnNumber}</div>
                            </div>
                            <div class="col-12">
                                <div class="small text-muted">Sales Order</div>
                                <div class="fw-semibold">${gdn.soNumber}</div>
                            </div>
                            <div class="col-12">
                                <div class="small text-muted">Customer</div>
                                <div class="fw-semibold">${gdn.customerName}</div>
                            </div>
                            <div class="col-12">
                                <label class="form-label fw-bold mb-1">Status</label>
                                <select name="status" class="form-select" required>
                                    <option value="CREATED" ${gdn.status == 'CREATED' ? 'selected' : ''}>CREATED</option>
                                    <option value="CANCELLED">CANCELLED</option>
                                </select>
                                <div class="form-text">
                                    Qty Picked / Qty Packed is updated from pick tasks and cannot be edited here.
                                </div>
                            </div>
                        </div>
                    </form>
                </jsp:attribute>
                <jsp:attribute name="action">
                    <button type="button" class="btn btn-primary"
                        onclick="document.getElementById('editGdnForm${gdn.gdnId}').submit()">
                        Save
                    </button>
                </jsp:attribute>
            </t:alert>
        </c:if>
    </div>
</t:layout>
