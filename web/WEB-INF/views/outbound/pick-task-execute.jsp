<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib tagdir="/WEB-INF/tags/" prefix="t" %>
<%@taglib uri="jakarta.tags.core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<t:layout title="Pick Line #${line.pickTaskLineId}">
    <jsp:attribute name="actions">
        <div class="d-flex gap-2">
            <t:link url="${pageContext.request.contextPath}/pick-task?action=myLines"
                    color="dark" variant="split" icon="arrow-left">
                Back to My Lines
            </t:link>
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

        <div class="row justify-content-center">
            <div class="col-lg-6 col-md-8">
                <div class="card shadow-lg border-0">
                    <div class="card-header bg-primary text-white py-4 text-center">
                        <h4 class="mb-0"><i class="bi bi-qr-code me-2"></i>PICK LINE #${line.pickTaskLineId}</h4>
                    </div>
                    <div class="card-body p-4">
                        <!-- Product Info -->
                        <div class="text-center mb-4">
                            <div class="display-5 fw-bold text-primary mb-2">${line.variantSku}</div>
                            <p class="lead mb-0">${line.productName}</p>
                            <c:if test="${not empty line.color || not empty line.size}">
                                <p class="text-muted mb-0">
                                    <c:if test="${not empty line.color}">${line.color}</c:if>
                                    <c:if test="${not empty line.color && not empty line.size}"> / </c:if>
                                    <c:if test="${not empty line.size}">${line.size}</c:if>
                                </p>
                            </c:if>
                        </div>

                        <!-- Location -->
                        <div class="bg-light rounded p-3 mb-4 text-center">
                            <small class="text-muted d-block">LOCATION</small>
                            <div class="fs-3 fw-bold text-dark">${line.slotCode}</div>
                            <span class="badge bg-info">${line.zoneCode}</span>
                        </div>

                        <!-- Quantity -->
                        <div class="bg-light rounded p-3 mb-4 text-center">
                            <small class="text-muted d-block">QUANTITY TO PICK</small>
                            <div class="display-4 fw-bold text-warning">${line.qtyToPick}</div>
                        </div>

                        <!-- Already Picked -->
                        <c:if test="${line.qtyPicked > 0}">
                            <div class="alert alert-info mb-4 text-center">
                                <i class="bi bi-check-circle me-2"></i>
                                Already picked: <strong>${line.qtyPicked}</strong>
                            </div>
                        </c:if>

                        <!-- Pick Form -->
                        <form method="post" action="${pageContext.request.contextPath}/pick-task">
                            <input type="hidden" name="action" value="save-pick"/>
                            <input type="hidden" name="lineId" value="${line.pickTaskLineId}"/>

                            <div class="mb-3">
                                <label class="form-label fw-bold">Enter Quantity Picked:</label>
                                <input type="number" class="form-control form-control-lg text-center" 
                                       name="qtyPicked" id="qtyPicked"
                                       value="${line.qtyToPick}" min="0" max="${line.qtyToPick}" 
                                       step="1" required autofocus/>
                            </div>

                            <div class="d-grid gap-2">
                                <button type="submit" class="btn btn-success btn-lg">
                                    <i class="bi bi-check-circle-fill me-2"></i> CONFIRM PICK
                                </button>
                            </div>
                        </form>
                    </div>
                </div>

                <!-- Other Lines -->
                <c:if test="${not empty myLines && myLines.size() > 1}">
                    <div class="card shadow-sm border-0 mt-4">
                        <div class="card-header bg-light py-3">
                            <h6 class="mb-0"><i class="bi bi-list me-2"></i>Other Lines</h6>
                        </div>
                        <div class="list-group list-group-flush">
                            <c:forEach var="otherLine" items="${myLines}">
                                <c:if test="${otherLine.pickTaskLineId != line.pickTaskLineId}">
                                    <a href="${pageContext.request.contextPath}/pick-task?action=pick&id=${otherLine.pickTaskLineId}" 
                                       class="list-group-item list-group-item-action d-flex justify-content-between align-items-center">
                                        <div>
                                            <span class="badge bg-secondary me-2">#${otherLine.pickTaskLineId}</span>
                                            ${otherLine.variantSku}
                                            <small class="text-muted d-block">${otherLine.slotCode}</small>
                                        </div>
                                        <div class="text-end">
                                            <span class="badge ${otherLine.pickStatus == 'DONE' ? 'bg-success' : (otherLine.pickStatus == 'PICKED' ? 'bg-info' : 'bg-warning text-dark')}">
                                                ${otherLine.qtyPicked}/${otherLine.qtyToPick}
                                            </span>
                                        </div>
                                    </a>
                                </c:if>
                            </c:forEach>
                        </div>
                    </div>
                </c:if>
            </div>
        </div>

        <script>
            document.getElementById('qtyPicked').focus();
            document.getElementById('qtyPicked').select();
        </script>
    </jsp:body>
</t:layout>
