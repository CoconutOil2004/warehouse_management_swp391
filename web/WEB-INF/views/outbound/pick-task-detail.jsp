<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib tagdir="/WEB-INF/tags/" prefix="t" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<t:layout title="Task Detail: #${task.pickTaskId}">
    <jsp:attribute name="actions">
        <div class="d-flex gap-2">
            <t:link url="${pageContext.request.contextPath}/pick-task?action=myTasks" 
                    color="dark" variant="split" icon="arrow-left">
                Go back
            </t:link>
            <c:if test="${task.status == 'ASSIGNED'}">
                <form action="${pageContext.request.contextPath}/pick-task" method="post" class="m-0">
                    <input type="hidden" name="action" value="start"/>
                    <input type="hidden" name="id" value="${task.pickTaskId}"/>
                    <button type="submit" class="btn btn-success d-flex gap-2 align-items-center">
                        <i class="bi bi-play-fill"></i>
                        <span>Start Task</span>
                    </button>
                </form>
            </c:if>
        </div>
    </jsp:attribute>

    <jsp:body>
        <div class="row g-4">
            <!-- Task Info Card -->
            <div class="col-lg-12">
                <div class="card shadow-sm border-0 mb-4">
                    <div class="card-header bg-primary text-white py-3">
                        <h5 class="card-title mb-0"><i class="bi bi-info-circle me-2"></i>Task Information</h5>
                    </div>
                    <div class="card-body">
                        <div class="row row-cols-1 row-cols-md-2 row-cols-lg-4 g-3">
                            <div class="col">
                                <p class="text-muted small mb-1 text-uppercase fw-bold">Task ID</p>
                                <p class="mb-0 fw-semibold">#${task.pickTaskId}</p>
                            </div>
                            <div class="col">
                                <p class="text-muted small mb-1 text-uppercase fw-bold">GDN / SO</p>
                                <p class="mb-0 fw-semibold">
                                    <a href="${pageContext.request.contextPath}/goods-delivery-note?action=detail&id=${task.gdnId}" class="text-decoration-none">
                                        ${task.gdnNumber}
                                    </a>
                                    <span class="text-muted mx-1">/</span>
                                    <c:choose>
                                        <c:when test="${not empty task.soId}">
                                            <a href="${pageContext.request.contextPath}/sales-orders?action=detail&id=${task.soId}" class="text-decoration-none">
                                                ${task.soNumber}
                                            </a>
                                        </c:when>
                                        <c:otherwise>${task.soNumber != null ? task.soNumber : "-"}</c:otherwise>
                                    </c:choose>
                                </p>
                            </div>
                            <div class="col">
                                <p class="text-muted small mb-1 text-uppercase fw-bold">Status</p>
                                <p class="mb-0">
                                    <span class="badge rounded-pill ${task.status == 'ASSIGNED' ? 'bg-info' : (task.status == 'IN_PROGRESS' ? 'bg-warning text-dark' : 'bg-success')} fw-semibold">
                                        ${task.status}
                                    </span>
                                </p>
                            </div>
                            <div class="col">
                                <p class="text-muted small mb-1 text-uppercase fw-bold">Assigned At</p>
                                <p class="mb-0 fw-semibold text-muted small"><c:out value="${task.assignedAt}" /></p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Pick Lines Table -->
            <div class="col-lg-12">
                <form action="${pageContext.request.contextPath}/pick-task" method="post" id="completeForm">
                    <input type="hidden" name="action" value="complete"/>
                    <input type="hidden" name="pickTaskId" value="${task.pickTaskId}"/>
                    
                    <c:set var="columns" value='${["Slot / Zone", "Variant / Product", "Qty to Pick", "Qty Picked", "Status"]}' />
                    <t:table columns="${columns}">
                        <jsp:attribute name="head">
                            <div class="p-2 fw-bold text-uppercase small text-muted">
                                <i class="bi bi-list-check me-2"></i>Pick Lines
                            </div>
                        </jsp:attribute>
                        <jsp:body>
                            <c:forEach var="line" items="${task.lines}">
                                <tr>
                                    <td class="text-center">${line.slotCode} <span class="text-muted mx-1">/</span> ${line.zoneCode}</td>
                                    <td>
                                        <span class="fw-bold text-primary">${line.variantSku}</span> 
                                        <span class="text-muted mx-1">–</span> 
                                        ${line.productName}
                                        <c:if test="${not empty line.color or not empty line.size}">
                                            <br/><small class="text-muted">Size: ${line.size}, Color: ${line.color}</small>
                                        </c:if>
                                    </td>
                                    <td class="text-center fw-bold">
                                        <fmt:formatNumber value="${line.qtyToPick}" maxFractionDigits="0"/>
                                    </td>
                                    <td class="text-center" style="width: 120px;">
                                        <input type="hidden" name="lineIds" value="${line.pickTaskLineId}"/>
                                        <input type="number" name="qtyPicked" 
                                               class="form-control form-control-sm text-center fw-bold border-primary shadow-sm" 
                                               min="0" step="1" 
                                               value="${line.qtyPicked != null ? line.qtyPicked.stripTrailingZeros().toPlainString() : '0'}"
                                               ${task.status != 'IN_PROGRESS' ? 'disabled' : ''}/>
                                    </td>
                                    <td class="text-center">
                                        <span class="badge ${line.pickStatus == 'PENDING' ? 'bg-light text-dark border' : 'bg-success text-white small'}">
                                            ${line.pickStatus}
                                        </span>
                                    </td>
                                </tr>
                            </c:forEach>
                        </jsp:body>
                    </t:table>

                    <c:if test="${task.status == 'IN_PROGRESS'}">
                        <div class="mt-4 text-end">
                            <button type="submit" class="btn btn-primary btn-lg px-5 shadow">
                                <i class="bi bi-check-circle-fill me-2"></i>Complete Task
                            </button>
                        </div>
                    </c:if>
                </form>
            </div>
        </div>
    </jsp:body>
</t:layout>
