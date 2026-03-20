<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib tagdir="/WEB-INF/tags/" prefix="t" %>
<%@taglib uri="jakarta.tags.core" prefix="c" %>
<t:layout title="Assign Packing">
    <div class="container-fluid py-4">
        <div class="mb-3">
            <a href="${pageContext.request.contextPath}/packing?action=form&gdnId=${packing.gdnId}"
               class="text-decoration-none text-muted">
                <i class="fas fa-arrow-left me-1"></i> Back
            </a>
        </div>

        <div class="card shadow-sm border-0">
            <div class="card-header bg-primary text-white py-3">
                <h5 class="mb-0">
                    Assign Packing for GDN <strong>${packing.gdnNumber}</strong>
                </h5>
            </div>
            <div class="card-body">
                <c:if test="${not empty param.error}">
                    <div class="alert alert-danger">
                        Invalid assignment request.
                    </div>
                </c:if>

                <form action="${pageContext.request.contextPath}/packing" method="post" class="row g-3">
                    <input type="hidden" name="action" value="assign"/>
                    <input type="hidden" name="packId" value="${packing.packId}"/>
                    <input type="hidden" name="gdnId" value="${packing.gdnId}"/>

                    <div class="col-md-6">
                        <label class="form-label fw-bold">Assign to employee</label>
                        <select name="assignedTo" class="form-select" required>
                            <option value="">-- Select staff --</option>
                            <c:forEach var="u" items="${warehouseStaff}">
                                <option value="${u.userId}" ${packing.packedBy != null && packing.packedBy == u.userId ? 'selected' : ''}>
                                    ${u.fullName} (${u.username})
                                </option>
                            </c:forEach>
                        </select>
                        <div class="form-text text-muted">
                            Warehouse staff can only pack GDNs assigned to them.
                        </div>
                    </div>

                    <div class="col-12">
                        <button type="submit" class="btn btn-success">
                            <i class="fas fa-user-check me-1"></i> Assign
                        </button>
                        <a class="btn btn-outline-secondary"
                           href="${pageContext.request.contextPath}/packing?action=form&gdnId=${packing.gdnId}">
                            Cancel
                        </a>
                    </div>
                </form>
            </div>
        </div>
    </div>
</t:layout>

